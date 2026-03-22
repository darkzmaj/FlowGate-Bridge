package si.terona;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class FlowGateBridge extends JavaPlugin {

    private final Map<String, String> slashServers = new HashMap<>();

    @Override
    public void onEnable() {
        slashServers.clear();
        saveDefaultConfig();
        loadSlashServers();
        registerOutgoingChannel();
        registerCommands();
    }

    private void loadSlashServers() {
        ConfigurationSection section = getConfig().getConfigurationSection("slash-servers");
        if (section == null) {
            getLogger().warning("No 'slash-servers' section found in config.yml");
            return;
        }

        for (String command : section.getKeys(false)) {
            String targetServer = section.getString(command);

            if (command.isEmpty() || command.contains(" ")) {
                getLogger().warning("Invalid command name: '" + command + "'");
                continue;
            }

            if (targetServer == null || targetServer.isEmpty()) {
                getLogger().warning("Target server for command '" + command + "' is empty!");
                continue;
            }
            
            slashServers.put(command.toLowerCase(), targetServer.toLowerCase());
        }
        getLogger().info("Loaded " + slashServers.size() + " slash commands.");
    }

    private void registerOutgoingChannel() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerCommands() {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(getServer());

            for (String cmdName : slashServers.keySet()) {
                Command existing = commandMap.getCommand(cmdName);
                if (existing != null) {
                    continue;
                }

                ProxyCommand proxyCommand = new ProxyCommand(cmdName);
                commandMap.register("flowgatebridge", proxyCommand);
            }
            
            // To ensure Paper recognizes new commands for tab completion and command map
            try {
                getServer().getClass().getMethod("syncCommands").invoke(getServer());
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Exception e) {
            getLogger().severe("Failed to register commands via reflection: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flowgatebridge.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            reloadConfig();
            slashServers.clear();
            loadSlashServers();
            registerCommands();

            sender.sendMessage("§aFlowGate-Bridge reloaded!");
            return true;
        }
        return false;
    }

    private class ProxyCommand extends BukkitCommand {

        protected ProxyCommand(@NotNull String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (!(sender instanceof Player player)) {
                return true;
            }

            String targetServer = slashServers.get(getName().toLowerCase());

            if (targetServer == null || targetServer.isEmpty()) {
                player.sendMessage("§cServer not configured.");
                return true;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(targetServer);

            player.sendPluginMessage(FlowGateBridge.this, "BungeeCord", out.toByteArray());
            return true;
        }
    }
}
