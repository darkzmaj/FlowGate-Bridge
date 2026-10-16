package si.terona.bridge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/** The existing BungeeCord Connect wire format. No arbitrary command dispatch. */
public final class BridgeProtocol {
    private static final Pattern TARGET = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]{0,127}");
    private BridgeProtocol() {}

    public static boolean isValidTarget(String target) {
        return target != null && TARGET.matcher(target).matches();
    }

    public static byte[] connect(String target) {
        if (!isValidTarget(target)) throw new IllegalArgumentException("Invalid proxy server name");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(140);
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeUTF("Connect");
                output.writeUTF(target);
            }
            return bytes.toByteArray();
        } catch (IOException impossible) {
            throw new IllegalStateException("Cannot encode in-memory Connect message", impossible);
        }
    }
}
