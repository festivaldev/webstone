package tf.festival.webstone;

import net.minecraftforge.common.ForgeConfigSpec;

public class WebstoneConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> PASSPHRASE;
    public static final ForgeConfigSpec.ConfigValue<Integer> WEBSOCKET_PORT;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SECURE_WEBSOCKET;
    public static final ForgeConfigSpec.ConfigValue<String> CERTIFICATE_FILENAME;
    public static final ForgeConfigSpec.ConfigValue<String> CERTIFICATE_KEY_FILENAME;
    public static final ForgeConfigSpec.ConfigValue<String> CERTIFICATE_KEY_PASS;

    static {
        BUILDER.push("Webstone Configuration");

        PASSPHRASE = BUILDER.comment("Passphrase to allow only authorized users.").define("Passphrase", "");
        WEBSOCKET_PORT = BUILDER.comment("Port used by the Webstone WebSocket Server. (Default: 4321)").define("WebSocketPort", 4321);

        SECURE_WEBSOCKET = BUILDER.comment("Specifies if the WebSocket should use a secure connection.").define("SecureWebSocket", false);
        CERTIFICATE_FILENAME = BUILDER.comment("Filename of the certificate public key inside \".minecraft/data\". (Default: cert.pem)").define("CertificateFilename", "cert.pem");
        CERTIFICATE_KEY_FILENAME = BUILDER.comment("Filename of the certificate private key inside \".minecraft/data\". (Default: key.pem)").define("CertificateKeyFilename", "key.pem");
        CERTIFICATE_KEY_PASS = BUILDER.comment("Passphrase used for the private key.").define("CertificateKeyPass", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
