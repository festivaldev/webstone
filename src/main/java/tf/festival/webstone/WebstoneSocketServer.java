package tf.festival.webstone;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class WebstoneSocketServer extends WebSocketServer {
    private final Gson gson = new Gson();

    public WebstoneSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        Webstone.LOGGER.info(String.format("Webstone WebSocket server started on port %d", this.getPort()));
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake handshake) {
        Webstone.LOGGER.info(String.format("New connection from %s", ws.getRemoteSocketAddress().getAddress().getHostAddress()));
        ws.send(encodeJson("block_list", Webstone.getWorldData().getRegisteredBlocks()));
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        Webstone.LOGGER.info(String.format("Connection closed from %s", ws.getRemoteSocketAddress().getAddress().getHostAddress()));
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();
            JsonElement data = json.get("data");

            switch (type) {
                case "block_state": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());
                    boolean powered = block.get("powered").getAsBoolean();

                    Webstone.SERVER.execute(() -> {
                        Webstone.setBlockState(blockId, powered, null);
                    });

                    break;
                }
                case "block_power": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());
                    int power = block.get("power").getAsInt();

                    Webstone.SERVER.execute(() -> {
                        Webstone.setBlockPower(blockId, power);
                    });

                    break;
                }
                case "rename_block": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());
                    String name = block.get("name").getAsString().substring(0, 64).trim();

                    Webstone.renameBlock(blockId, name);
                    break;
                }
                case "unregister_block": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());

                    Webstone.unregisterBlock(blockId);
                    break;
                }
                default:
                    break;
            }
        } catch (JsonSyntaxException ex) {
        }
    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        ex.printStackTrace();
        if (ws != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    public void broadcastBlockList() {
        broadcast(encodeJson("block_list", Webstone.getWorldData().getRegisteredBlocks()));
    }

    public void broadcastUpdatedBlockState(UUID blockId, boolean powered) {
        JsonObject message = new JsonObject();
        message.addProperty("blockId", blockId.toString());
        message.addProperty("powered", powered);

        broadcast(encodeJson("block_state", message));
    }

    public void broadcastUpdatedBlockPower(UUID blockId, int power) {
        JsonObject message = new JsonObject();
        message.addProperty("blockId", blockId.toString());
        message.addProperty("power", power);

        broadcast(encodeJson("block_power", message));
    }

    private String encodeJson(String type, Object object) {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.add("data", gson.toJsonTree(object));

        return message.toString();
    }
}