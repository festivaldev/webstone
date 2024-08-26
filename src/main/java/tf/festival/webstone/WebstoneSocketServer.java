package tf.festival.webstone;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.codec.binary.Hex;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

public class WebstoneSocketServer extends WebSocketServer {
    private final Gson gson = new Gson();

    public WebstoneSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));

        if (WebstoneConfig.SECURE_WEBSOCKET.get()) {
            SSLContext context = getContext();
            if (context != null) {
                this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
            }
        }
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

        if (!WebstoneConfig.PASSPHRASE.get().isEmpty()) {
            try {
                if (!request.hasFieldValue("Sec-WebSocket-Protocol")
                    || !BCrypt.verifyer().verify(
                    new String(Hex.decodeHex(request.getFieldValue("Sec-WebSocket-Protocol").toCharArray()), StandardCharsets.UTF_8).toCharArray(),
                    WebstoneConfig.PASSPHRASE.get()
                ).verified) {
                    throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
                }
            } catch (Exception e) {
                throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
            }
        }

        return builder;
    }

    @Override
    public void onStart() {
        Webstone.LOGGER.info(String.format("Webstone WebSocket server started on port %d. (Secure: %b)", this.getPort(), WebstoneConfig.SECURE_WEBSOCKET.get()));
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake handshake) {
        Webstone.LOGGER.info(String.format("New connection from %s", ws.getRemoteSocketAddress().getAddress().getHostAddress()));
        ws.send(encodeJson("block_list", Webstone.getWorldData().getBlocks()));
        ws.send(encodeJson("block_groups", Webstone.getWorldData().getBlockGroups()));
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
                    String name = block.get("name").getAsString().substring(0, Math.min(block.get("name").getAsString().length(), 64)).trim();

                    Webstone.renameBlock(blockId, name);
                    break;
                }
                case "unregister_block": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());

                    Webstone.unregisterBlock(blockId);
                    break;
                }
                case "change_group": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());
                    UUID groupId = null;

                    try {
                        groupId = UUID.fromString(block.get("groupId").getAsString());
                    } catch (Exception e) {
                    }

                    Webstone.changeBlockGroup(blockId, groupId);
                    break;
                }
                case "move_block": {
                    JsonObject block = data.getAsJsonObject();

                    UUID blockId = UUID.fromString(block.get("blockId").getAsString());
                    int newIndex = block.get("newIndex").getAsInt();

                    Webstone.changeBlockGroupIndex(blockId, newIndex);
                    break;
                }
                case "create_group": {
                    JsonObject group = data.getAsJsonObject();

                    String name = group.get("name").getAsString();

                    Webstone.createGroup(name);
                    break;
                }
                case "rename_group": {
                    JsonObject group = data.getAsJsonObject();

                    UUID groupId = UUID.fromString(group.get("groupId").getAsString());
                    String name = group.get("name").getAsString();

                    Webstone.renameGroup(groupId, name);
                    break;
                }
                case "delete_group": {
                    JsonObject group = data.getAsJsonObject();

                    UUID groupId = UUID.fromString(group.get("groupId").getAsString());

                    Webstone.deleteGroup(groupId);
                    break;
                }
                case "move_group": {
                    JsonObject group = data.getAsJsonObject();

                    UUID groupId = UUID.fromString(group.get("groupId").getAsString());
                    int newIndex = group.get("newIndex").getAsInt();

                    Webstone.changeGroupIndex(groupId, newIndex);
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

    public void broadcastBlockGroupList() {
        broadcast(encodeJson("block_groups", Webstone.getWorldData().getBlockGroups()));
    }

    public void broadcastBlockList() {
        broadcast(encodeJson("block_list", Webstone.getWorldData().getBlocks()));
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

    // region SSL Stuff
    private static SSLContext getContext() {
        SSLContext context;
        String password = WebstoneConfig.CERTIFICATE_KEY_PASS.get();
        try {
            context = SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM(getBytes(new File(Paths.get(FMLPaths.GAMEDIR.get().toString(), "data", WebstoneConfig.CERTIFICATE_FILENAME.get()).toString())),
                "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
            byte[] keyBytes = parseDERFromPEM(
                getBytes(new File(Paths.get(FMLPaths.GAMEDIR.get().toString(), "data", WebstoneConfig.CERTIFICATE_KEY_FILENAME.get()).toString())),
                "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

            X509Certificate cert = generateCertificateFromDER(certBytes);
            RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, password.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();

            context.init(km, null, null);
        } catch (Exception e) {
            context = null;
        }
        return context;
    }

    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        String[] tokens = data.split(beginDelimiter);
        tokens = tokens[1].split(endDelimiter);
        return DatatypeConverter.parseBase64Binary(tokens[0]);
    }

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
        throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes)
        throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private static byte[] getBytes(File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }
    // endregion
}