package si.terona.bridge;
import org.junit.jupiter.api.Test;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BridgeRegressionTest {
    @Test void preservesEstablishedConnectFormatAndTargetCase() throws Exception {
        byte[] message=BridgeProtocol.connect("Lobby-EU_1");
        try(DataInputStream input=new DataInputStream(new ByteArrayInputStream(message))) {
            assertEquals("Connect",input.readUTF());assertEquals("Lobby-EU_1",input.readUTF());assertEquals(0,input.available());
        }
    }
    @Test void rejectsControlCharactersOversizedNamesAndCommandPayloads() {
        for(String target:new String[]{"", "lobby\nstop", "lobby;op user", "../lobby", "x".repeat(129), "lobby\0"})
            assertThrows(IllegalArgumentException.class,()->BridgeProtocol.connect(target));
    }
    @Test void normalizesCommandNamesWithoutChangingTargetCase() throws Exception {
        Locale before=Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            YamlConfiguration config=new YamlConfiguration();config.loadFromString("slash-servers: {MINIGAME: Lobby-EU}\n");
            assertEquals(Map.of("minigame","Lobby-EU"),RouteConfig.parse(config));
        } finally {Locale.setDefault(before);}
    }
    @Test void rejectsMalformedAndCollidingRoutes() throws Exception {
        for(String yaml:List.of("slash-servers: []", "slash-servers: {bad:name: lobby}", "slash-servers: {lobby: 42}", "slash-servers: {hub: lobby, HUB: lobby2}")) {
            YamlConfiguration config=new YamlConfiguration();config.loadFromString(yaml);
            assertThrows(IllegalArgumentException.class,()->RouteConfig.parse(config));
        }
    }
    @Test void emptyRouteMapIntentionallyDisablesAllConnectionCommands() throws Exception {
        YamlConfiguration config=new YamlConfiguration();config.loadFromString("slash-servers: {}\n");
        assertTrue(RouteConfig.parse(config).isEmpty());
    }
}
