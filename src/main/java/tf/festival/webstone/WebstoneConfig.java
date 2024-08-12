package tf.festival.webstone;

import net.minecraftforge.common.ForgeConfigSpec;

public class WebstoneConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WEBSOCKET_PORT;

    static {
        BUILDER.push("Webstone Configuration");
        WEBSOCKET_PORT = BUILDER.comment("Port used by the Webstone WebSocket Server. (Default: 4321)").define("WebSocketPort", 4321);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
