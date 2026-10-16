package si.terona;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import si.terona.bridge.BridgeProtocol;
import si.terona.bridge.RouteConfig;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/** Backend commands request a switch for their executing player, never a console command. */
public final class FlowGateBridge extends JavaPlugin implements Listener {
    private volatile Map<String, String> slashServers = Map.of();
    private final Set<Command> registeredCommands = new HashSet<>();
    private final Map<UUID, Long> lastRequest = new ConcurrentHashMap<>();
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;
    private static final long COOLDOWN_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

    @Override public void onEnable() {
        saveDefaultConfig();
        try {
            // Public on Paper; legacy Bukkit implementations expose only a field.
            try { commandMap = (CommandMap) getServer().getClass().getMethod("getCommandMap").invoke(getServer()); }
            catch (NoSuchMethodException legacy) { commandMap = (CommandMap) findField(getServer().getClass(), "commandMap").get(getServer()); }
            knownCommands = knownCommands(commandMap);
            slashServers = readRoutes();
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getServer().getPluginManager().registerEvents(this, this);
            registerCommands();
        } catch (Exception failure) {
            getLogger().log(Level.SEVERE, "Cannot start FlowGate-Bridge; check route configuration and server command registration support", failure);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private Map<String, String> readRoutes() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(new java.io.File(getDataFolder(), "config.yml"));
        return RouteConfig.parse(config);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException absent) { /* search superclass */ }
        }
        throw new NoSuchFieldException(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> knownCommands(CommandMap map) throws Exception {
        try { return (Map<String, Command>) map.getClass().getMethod("getKnownCommands").invoke(map); }
        catch (NoSuchMethodException legacy) { return (Map<String, Command>) findField(map.getClass(), "knownCommands").get(map); }
    }

    private void registerCommands() {
        for (String name : slashServers.keySet()) {
            if (commandMap.getCommand(name) != null) {
                getLogger().warning("Skipping route /" + name + ": command already belongs to another plugin or the server");
                continue;
            }
            Command route = new ProxyCommand(name);
            commandMap.register("flowgatebridge", route);
            registeredCommands.add(route);
        }
        syncCommands();
        getLogger().info("Registered " + registeredCommands.size() + " connection commands.");
    }

    private void unregisterCommands() {
        if (knownCommands != null) for (String name : new ArrayList<>(knownCommands.keySet())) {
            if (registeredCommands.contains(knownCommands.get(name))) knownCommands.remove(name);
        }
        if (commandMap != null) for (Command command : registeredCommands) command.unregister(commandMap);
        registeredCommands.clear();
    }

    private void syncCommands() {
        try { getServer().getClass().getMethod("syncCommands").invoke(getServer()); }
        catch (NoSuchMethodException legacy) { /* Older servers update commands themselves. */ }
        catch (ReflectiveOperationException error) { getLogger().log(Level.WARNING, "Commands registered, but client command-tree synchronization failed", error); }
    }

    @Override public void onDisable() {
        unregisterCommands();
        lastRequest.clear();
        slashServers = Map.of();
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) { lastRequest.remove(event.getPlayer().getUniqueId()); }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) return false;
        if (!sender.hasPermission("flowgatebridge.admin")) { sender.sendMessage("§cNo permission."); return true; }
        Map<String, String> replacement;
        try { replacement = readRoutes(); }
        catch (Exception failure) {
            getLogger().warning("Reload rejected; previous routes retained: " + failure.getClass().getSimpleName());
            sender.sendMessage("§cReload failed. Check config.yml; previous routes are still active.");
            return true;
        }
        unregisterCommands();
        slashServers = replacement;
        registerCommands();
        sender.sendMessage("§aFlowGate-Bridge reloaded!");
        return true;
    }

    @Override public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
        return sender.hasPermission("flowgatebridge.admin") && args.length == 1
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT)) ? List.of("reload") : List.of();
    }

    private final class ProxyCommand extends BukkitCommand {
        private ProxyCommand(String name) { super(name); setPermission("flowgatebridge.use"); }

        @Override public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (!testPermission(sender) || !(sender instanceof Player player) || !player.isOnline()) return true;
            String target = slashServers.get(getName().toLowerCase(Locale.ROOT));
            if (target == null || !isEnabled()) return true;
            long now = System.nanoTime();
            java.util.concurrent.atomic.AtomicBoolean accepted = new java.util.concurrent.atomic.AtomicBoolean();
            lastRequest.compute(player.getUniqueId(), (id, previous) -> {
                if (previous != null && now - previous < COOLDOWN_NANOS) return previous;
                accepted.set(true); return now;
            });
            if (!accepted.get()) return true;
            try { player.sendPluginMessage(FlowGateBridge.this, "BungeeCord", BridgeProtocol.connect(target)); }
            catch (IllegalArgumentException | IllegalStateException failure) {
                getLogger().log(Level.WARNING, "Unable to send connection request; verify proxy channel support", failure);
                player.sendMessage("§cCould not send the connection request.");
            }
            return true;
        }
        @Override public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) { return List.of(); }
    }
}
