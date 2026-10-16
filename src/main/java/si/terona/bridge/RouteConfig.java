package si.terona.bridge;

import org.bukkit.configuration.ConfigurationSection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class RouteConfig {
    private static final Pattern COMMAND = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_-]{0,63}");
    private RouteConfig() {}

    public static Map<String, String> parse(ConfigurationSection config) {
        ConfigurationSection section = config.getConfigurationSection("slash-servers");
        if (section == null) throw new IllegalArgumentException("'slash-servers' must be a mapping (use {} for no routes)");
        Map<String, String> routes = new LinkedHashMap<>();
        for (String command : section.getKeys(false)) {
            Object raw = section.get(command);
            if (!COMMAND.matcher(command).matches() || !(raw instanceof String target)
                    || !BridgeProtocol.isValidTarget(target)) {
                throw new IllegalArgumentException("Invalid route: use a simple command name and a server name of 1–128 letters, numbers, dots, underscores or hyphens");
            }
            if (routes.putIfAbsent(command.toLowerCase(Locale.ROOT), target) != null) {
                throw new IllegalArgumentException("Duplicate command name after case normalization");
            }
        }
        return Map.copyOf(routes);
    }
}
