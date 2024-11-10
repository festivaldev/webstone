package tf.festival.webstone;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.minecraftforge.fml.loading.FMLPaths;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import tf.festival.webstone.data.WebstoneBlock;
import tf.festival.webstone.data.WebstoneBlockGroup;

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
import java.util.*;

public class WebstoneSocketServer extends WebSocketServer {
    enum AuthenticationState {
        NONE, AUTHENTICATED, SUBSCRIBED
    }

    private final HashMap<UUID, WebSocket> socketClients = new HashMap<>();
    private final HashMap<UUID, Timer> socketClientTimers = new HashMap<>();
    private final HashMap<UUID, ArrayList<UUID>> registryClients = new HashMap<>();

    public UUID getSocketId(WebSocket ws) {
        for (Map.Entry<UUID, WebSocket> entry : socketClients.entrySet()) {
            if (entry.getValue() == ws) {
                return entry.getKey();
            }
        }

        return null;
    }

    private UUID getRegistryIdForSocket(WebSocket ws) {
        UUID socketId = getSocketId(ws);

        if (socketId == null) return null;

        for (Map.Entry<UUID, ArrayList<UUID>> entry : registryClients.entrySet()) {
            if (entry.getValue().contains(socketId)) {
                return entry.getKey();
            }
        }

        return null;
    }

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
    public void onStart() {
        Webstone.LOGGER.info(String.format("Webstone WebSocket server started on port %d. (Secure: %b)", this.getPort(), WebstoneConfig.SECURE_WEBSOCKET.get()));
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake handshake) {
        Webstone.LOGGER.info(String.format("New connection from %s", ws.getRemoteSocketAddress().getAddress().getHostAddress()));
        ws.setAttachment(AuthenticationState.NONE);

        UUID socketId = UUID.randomUUID();
        socketClients.put(socketId, ws);

        WelcomeMessage welcomeMessage = new WelcomeMessage(socketId);

        Timer disconnectTimer = new Timer();
        disconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ws.close();
            }
        }, Date.from(welcomeMessage.expireTime));

        socketClientTimers.put(socketId, disconnectTimer);

        ws.send(SocketMessage.serialize(SocketMessageType.WELCOME, welcomeMessage));
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        Webstone.LOGGER.info(String.format("Connection closed from %s", ws.getRemoteSocketAddress().getAddress().getHostAddress()));

        for (ArrayList<UUID> clients : registryClients.values()) {
            clients.remove(getSocketId(ws));
        }

        socketClientTimers.remove(getSocketId(ws));
        socketClients.remove(getSocketId(ws));
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        // Webstone.LOGGER.info(message);

        try {
            SocketMessage<?> messageObj = SocketMessage.deserialize(message);

            if (!isAuthenticated(ws)
                && messageObj.type == SocketMessageType.AUTH_REQ
                && handleAuthentication(ws, (AuthenticationRequestMessage) messageObj.payload)) {
                if ((Object) socketClientTimers.get(getSocketId(ws)) instanceof Timer timer) {
                    timer.purge();
                    timer.cancel();

                    socketClientTimers.remove(getSocketId(ws));
                    // Webstone.LOGGER.info("disconnect timer cancelled");
                }

                ws.send(SocketMessage.serialize(SocketMessageType.BLOCK_LISTS, new BlockListMessage()));
            } else if (!isSubscribed(ws)
                && messageObj.type == SocketMessageType.SUBSCRIBE) {
                handleSubscription(ws, (SubscriptionMessage) messageObj.payload);
            } else {
                handleMessage(ws, messageObj);
            }
        } catch (Exception ex) {
            Webstone.LOGGER.error(ex.getMessage(), (Object[]) ex.getStackTrace());

            ws.send(SocketMessage.serialize(SocketMessageType.SERVER_ERROR, new ServerErrorMessage(ex)));
            ws.close();
        }
    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        Webstone.LOGGER.error(ex.getMessage(), (Object[]) ex.getStackTrace());

        if (ws != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
            Webstone.LOGGER.error("Additionally, WebSocket was empty when this error occurred.");
        }
    }

    public void broadcastBlockGroupList(UUID registryId) {
        if (!registryClients.containsKey(registryId)) return;

        WebstoneRegistry registry = WebstoneRegistry.getRegistry(registryId);
        if (registry == null) return;

        for (UUID socketId : registryClients.get(registryId)) {
            if (!socketClients.containsKey(socketId)) continue;

            socketClients.get(socketId).send(SocketMessage.serialize(SocketMessageType.BLOCK_GROUPS, new BlockGroupsMessage(registry.getBlockGroups())));
        }
    }

    public void broadcastBlockList(UUID registryId) {
        if (!registryClients.containsKey(registryId)) return;

        WebstoneRegistry registry = WebstoneRegistry.getRegistry(registryId);
        if (registry == null) return;

        for (UUID socketId : registryClients.get(registryId)) {
            if (!socketClients.containsKey(socketId)) continue;

            socketClients.get(socketId).send(SocketMessage.serialize(SocketMessageType.BLOCKS, new BlocksMessage(registry.getBlocks())));
        }
    }

    public void broadcastBlockUpdated(WebstoneBlock block) {
        WebstoneRegistry registry = WebstoneRegistry.getRegistry(block.getRegistryId());

        if (registry == null || !registryClients.containsKey(registry.getRegistryId())) return;

        for (UUID socketId : registryClients.get(registry.getRegistryId())) {
            if (!socketClients.containsKey(socketId)) continue;

            socketClients.get(socketId).send(SocketMessage.serialize(SocketMessageType.BLOCK_UPDATE, new BlockEventMessage(block)));
        }
    }

    public void broadcastBlockGroupUpdated(WebstoneBlockGroup blockGroup) {
        WebstoneRegistry registry = WebstoneRegistry.getRegistryForBlockGroup(blockGroup.getGroupId());
        if (registry == null || !registryClients.containsKey(registry.getRegistryId())) return;

        for (UUID socketId : registryClients.get(registry.getRegistryId())) {
            if (!socketClients.containsKey(socketId)) continue;

            socketClients.get(socketId).send(SocketMessage.serialize(SocketMessageType.BLOCK_GROUP_UPDATE, new BlockGroupEventMessage(blockGroup)));
        }
    }

    // region Authentication
    private boolean isAuthenticated(WebSocket ws) {
        return ws.<AuthenticationState>getAttachment().ordinal() >= AuthenticationState.AUTHENTICATED.ordinal();
    }

    private boolean isSubscribed(WebSocket ws) {
        return ws.<AuthenticationState>getAttachment().ordinal() >= AuthenticationState.SUBSCRIBED.ordinal();
    }

    private boolean handleAuthentication(WebSocket ws, AuthenticationRequestMessage message) {
        if (WebstoneConfig.PASSPHRASE.get().isEmpty()) {
            ws.setAttachment(AuthenticationState.AUTHENTICATED);
            ws.send(SocketMessage.serialize(SocketMessageType.AUTH_RES, new AuthenticationResponseMessage(true, "Authentication successful")));

            return true;
        }

        if (message != null && BCrypt.verifyer().verify(message.passphrase.toCharArray(), WebstoneConfig.PASSPHRASE.get()).verified) {
            ws.setAttachment(AuthenticationState.AUTHENTICATED);
            ws.send(SocketMessage.serialize(SocketMessageType.AUTH_RES, new AuthenticationResponseMessage(true, "Authentication successful")));

            return true;
        } else {
            ws.send(SocketMessage.serialize(SocketMessageType.AUTH_RES, new AuthenticationResponseMessage(false, "Invalid server passphrase")));
            ws.close();
        }

        return false;
    }

    private void handleSubscription(WebSocket ws, SubscriptionMessage message) {
        WebstoneRegistry registry = WebstoneRegistry.getRegistry(message.registryId);

        if (registry == null || !registry.comparePassphrase(message.passphrase)) {
            ws.send(SocketMessage.serialize(SocketMessageType.SUBSCRIBE, new SubscriptionResponseMessage(false, "Invalid passphrase", null)));
            return;
        }

        registryClients.computeIfAbsent(registry.getRegistryId(), k -> new ArrayList<>());

        registryClients.get(registry.getRegistryId()).add(getSocketId(ws));
        ws.setAttachment(AuthenticationState.SUBSCRIBED);
        ws.send(SocketMessage.serialize(SocketMessageType.SUBSCRIBE, new SubscriptionResponseMessage(true, "Authentication successful", registry.getRegistryId())));

        ws.send(SocketMessage.serialize(SocketMessageType.BLOCKS, new BlocksMessage(registry.getBlocks())));
        ws.send(SocketMessage.serialize(SocketMessageType.BLOCK_GROUPS, new BlockGroupsMessage(registry.getBlockGroups())));

        ws.setAttachment(AuthenticationState.AUTHENTICATED);
    }
    // endregion

    // region Web Socket Handlers
    private void handleMessage(WebSocket ws, SocketMessage<?> messageObj) {
        switch (messageObj.type) {
            case UNSUBSCRIBE -> {
                UnsubscriptionMessage message = (UnsubscriptionMessage) messageObj.payload;
                WebstoneRegistry registry = WebstoneRegistry.getRegistry(message.registryId);

                if (registry == null) {
                    return;
                }

                ArrayList<UUID> registryClientList = registryClients.get(registry.getRegistryId());
                if (registryClientList != null && registryClientList.contains(getSocketId(ws))) {
                    registryClientList.remove(getSocketId(ws));

                    ws.send(SocketMessage.serialize(SocketMessageType.UNSUBSCRIBE, new UnsubscriptionMessage(registry.getRegistryId())));
                }
            }
            case BLOCK_STATE, BLOCK_POWER, RENAME_BLOCK, UNREGISTER_BLOCK, CHANGE_BLOCK_GROUP -> {
                BlockEventMessage message = (BlockEventMessage) messageObj.payload;
                WebstoneRegistry registry = WebstoneRegistry.getRegistryForBlock(message.blockId);

                if (registry != null) {
                    WebstoneBlock block = registry.getBlockById(message.blockId);

                    if (block != null) {
                        Webstone.SERVER.execute(() -> {
                            boolean result = switch (messageObj.type) {
                                case BLOCK_STATE -> block.setPowered(message.powered);
                                case BLOCK_POWER -> block.setPower(message.power);
                                case RENAME_BLOCK -> block.setName(message.name);
                                case UNREGISTER_BLOCK -> {
                                    if (block.getGroupId() != null) {
                                        if ((Object) registry.getBlockGroupById(block.getGroupId()) instanceof WebstoneBlockGroup blockGroup) {
                                            blockGroup.removeBlock(block);
                                            broadcastBlockGroupUpdated(blockGroup);
                                        }
                                    }

                                    registry.removeBlock(block);

                                    Webstone.WORLD_DATA.setDirty();
                                    broadcastBlockList(registry.getRegistryId());

                                    yield false;
                                }
                                case CHANGE_BLOCK_GROUP -> {
                                    WebstoneBlockGroup blockGroup = null;
                                    UUID groupId = null;

                                    try {
                                        groupId = UUID.fromString(message.groupId);
                                    } catch (Exception e) {
                                        // groupId is empty = no group specified
                                    }

                                    if (groupId == block.getGroupId()) {
                                        yield false;
                                    }

                                    if (groupId != null) {
                                        blockGroup = registry.getBlockGroupById(groupId);
                                    }

                                    if (block.getGroupId() != null && (Object) registry.getBlockGroupById(block.getGroupId()) instanceof WebstoneBlockGroup _blockGroup) {
                                        _blockGroup.removeBlock(block);

                                        Webstone.WORLD_DATA.setDirty();
                                        broadcastBlockGroupUpdated(_blockGroup);
                                    }

                                    if (blockGroup != null) {
                                        if (blockGroup.addBlock(block)) {
                                            Webstone.WORLD_DATA.setDirty();
                                            broadcastBlockGroupUpdated(blockGroup);
                                        }
                                    }

                                    yield Webstone.WORLD_DATA.isDirty();
                                }
                                default -> false;
                            };

                            if (result) {
                                Webstone.WORLD_DATA.setDirty();
                                broadcastBlockUpdated(block);
                            }
                        });
                    }
                }
            }
            case CREATE_GROUP, RENAME_GROUP, DELETE_GROUP -> {
                BlockGroupEventMessage message = (BlockGroupEventMessage) messageObj.payload;
                WebstoneRegistry registry = null;

                if (message.groupId != null) {
                    registry = WebstoneRegistry.getRegistryForBlockGroup(message.groupId);
                }

                WebstoneBlockGroup blockGroup = null;

                if (registry != null) {
                    blockGroup = registry.getBlockGroupById(message.groupId);
                }

                switch (messageObj.type) {
                    case CREATE_GROUP -> {
                        registry = WebstoneRegistry.getRegistry(getRegistryIdForSocket(ws));

                        if (registry != null) {
                            registry.getBlockGroups().add(new WebstoneBlockGroup(message.name));

                            Webstone.WORLD_DATA.setDirty();
                            broadcastBlockGroupList(registry.getRegistryId());
                        }
                    }
                    case RENAME_GROUP -> {
                        if (blockGroup != null) {
                            blockGroup.setName(message.name);

                            Webstone.WORLD_DATA.setDirty();
                            broadcastBlockGroupUpdated(blockGroup);
                        }
                    }

                    case DELETE_GROUP -> {
                        if (blockGroup != null) {
                            for (UUID blockId : new ArrayList<>(blockGroup.getBlockIds())) {
                                blockGroup.removeBlock(registry.getBlockById(blockId));
                            }

                            registry.getBlockGroups().remove(blockGroup);

                            Webstone.WORLD_DATA.setDirty();

                            broadcastBlockGroupList(registry.getRegistryId());
                            broadcastBlockList(registry.getRegistryId());
                        }
                    }
                    default -> {}
                }
            }
            case CHANGE_BLOCK_INDEX -> {
                ChangeIndexMessage message = (ChangeIndexMessage) messageObj.payload;
                WebstoneRegistry registry = WebstoneRegistry.getRegistryForBlock(message.id);

                if (registry != null) {
                    WebstoneBlock block = registry.getBlockById(message.id);

                    if (block != null) {
                        if (block.getGroupId() != null && (Object) registry.getBlockGroupById(block.getGroupId()) instanceof WebstoneBlockGroup blockGroup) {
                            if (blockGroup.moveBlock(block, message.newIndex)) {
                                Webstone.WORLD_DATA.setDirty();

                                broadcastBlockGroupUpdated(blockGroup);
                            }
                        }
                    }
                }
            }
            case CHANGE_GROUP_INDEX -> {
                ChangeIndexMessage message = (ChangeIndexMessage) messageObj.payload;
                WebstoneRegistry registry = WebstoneRegistry.getRegistryForBlockGroup(message.id);

                if (registry != null) {
                    WebstoneBlockGroup blockGroup = registry.getBlockGroupById(message.id);

                    if (blockGroup != null) {
                        registry.getBlockGroups().remove(blockGroup);
                        registry.getBlockGroups().add(message.newIndex, blockGroup);

                        Webstone.WORLD_DATA.setDirty();
                        broadcastBlockGroupList(registry.getRegistryId());
                    }
                }
            }
            default -> {}
        }
    }
    // endregion

    // region SSL Stuff
    private static SSLContext getContext() {
        SSLContext context;
        String password = WebstoneConfig.CERTIFICATE_KEY_PASS.get();
        try {
            context = SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM(getBytes(new File(Paths.get(FMLPaths.GAMEDIR.get().toString(), "data", WebstoneConfig.CERTIFICATE_FILENAME.get()).toString())), "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
            byte[] keyBytes = parseDERFromPEM(getBytes(new File(Paths.get(FMLPaths.GAMEDIR.get().toString(), "data", WebstoneConfig.CERTIFICATE_KEY_FILENAME.get()).toString())), "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

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

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws
        InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private static byte[] getBytes(File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
        } catch (IOException ex) {
            Webstone.LOGGER.error(ex.getMessage(), (Object[]) ex.getStackTrace());
        }
        return bytesArray;
    }
    // endregion
}