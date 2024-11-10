package tf.festival.webstone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tf.festival.webstone.data.WebstoneBlock;
import tf.festival.webstone.data.WebstoneBlockGroup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

enum SocketMessageType {
    NONE,
    SERVER_ERROR,
    WELCOME,
    AUTH_REQ,
    AUTH_RES,
    SUBSCRIBE,
    UNSUBSCRIBE,
    BLOCK_LISTS,
    BLOCKS,
    BLOCK_GROUPS,
    BLOCK_UPDATE,
    BLOCK_GROUP_UPDATE,

    BLOCK_POWER,
    BLOCK_STATE,
    RENAME_BLOCK,
    UNREGISTER_BLOCK,
    CHANGE_BLOCK_GROUP,

    CREATE_GROUP,
    RENAME_GROUP,
    DELETE_GROUP,
    CHANGE_BLOCK_INDEX,

    CHANGE_GROUP_INDEX,
}

class SocketMessage<T> {
    SocketMessageType type;
    T payload;

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantAdapter())
        .create();

    public static <T> String serialize(SocketMessageType type, T payload) {
        return gson.toJson(new SocketMessage<>(type, payload));
    }

    public static SocketMessage<?> deserialize(String message) {
        SocketMessage<?> _message = gson.fromJson(message, TypeToken.get(SocketMessage.class));


        if (_message.type == null) {
            throw new IllegalArgumentException("Unknown payload type");
        }

        Class<?> payloadClass = switch (_message.type) {
            case WELCOME -> WelcomeMessage.class;
            case AUTH_REQ -> AuthenticationRequestMessage.class;
            case SUBSCRIBE -> SubscriptionMessage.class;
            case UNSUBSCRIBE -> UnsubscriptionMessage.class;
            case BLOCK_STATE, BLOCK_POWER, RENAME_BLOCK, UNREGISTER_BLOCK, CHANGE_BLOCK_GROUP ->
                BlockEventMessage.class;
            case CREATE_GROUP, RENAME_GROUP, DELETE_GROUP -> BlockGroupEventMessage.class;
            case CHANGE_BLOCK_INDEX, CHANGE_GROUP_INDEX -> ChangeIndexMessage.class;
            default -> throw new IllegalArgumentException("Unknown payload type");
        };

        return gson.fromJson(message, TypeToken.getParameterized(SocketMessage.class, payloadClass).getType());
    }

    public SocketMessage(SocketMessageType type, T payload) {
        this.type = type;
        this.payload = payload;
    }
}

class ServerErrorMessage {
    String message;
    StackTraceElement[] stackTrace;

    public ServerErrorMessage(Exception ex) {
        this.message = ex.getMessage();
        this.stackTrace = ex.getStackTrace();
    }
}

class WelcomeMessage {
    UUID socketId;
    Instant expireTime = Instant.now().plus(15, ChronoUnit.SECONDS);

    public WelcomeMessage(UUID socketId) {
        this.socketId = socketId;
    }
}

class AuthenticationRequestMessage {
    String passphrase;
}

class AuthenticationResponseMessage {
    boolean authorized;
    String message;

    public AuthenticationResponseMessage(boolean authorized, String message) {
        this.authorized = authorized;
        this.message = message;
    }
}

class SubscriptionMessage {
    UUID registryId;
    String passphrase;
}

class SubscriptionResponseMessage {
    boolean subscribed;
    String message;
    UUID registryId;

    public SubscriptionResponseMessage(boolean subscribed, String message, UUID registryId) {
        this.subscribed = subscribed;
        this.message = message;
        this.registryId = registryId;
    }
}

class UnsubscriptionMessage {
    UUID registryId;

    public UnsubscriptionMessage(UUID registryId) {
        this.registryId = registryId;
    }
}

class BlockListMessage {
    final HashMap<UUID, String> blockLists = new HashMap<>();

    public BlockListMessage() {
        for (WebstoneRegistry registry : WebstoneRegistry.getAllRegistries().values()) {
            blockLists.put(registry.getRegistryId(), registry.getName());
        }
    }
}

class BlocksMessage {
    ArrayList<WebstoneBlock> blocks;

    public BlocksMessage(ArrayList<WebstoneBlock> blocks) {
        this.blocks = blocks;
    }
}

class BlockGroupsMessage {
    ArrayList<WebstoneBlockGroup> blockGroups;

    public BlockGroupsMessage(ArrayList<WebstoneBlockGroup> blockGroups) {
        this.blockGroups = blockGroups;
    }
}

class BlockEventMessage {
    UUID blockId;
    String name;
    boolean powered;
    int power;
    String groupId;

    public BlockEventMessage(WebstoneBlock block) {
        this.blockId = block.getBlockId();
        this.name = block.getName();
        this.powered = block.isPowered();
        this.power = block.getPower();

        if (block.getGroupId() != null) {
            this.groupId = block.getGroupId().toString();
        }
    }
}

class BlockGroupEventMessage {
    UUID groupId;
    String name;
    ArrayList<UUID> blockIds;

    public BlockGroupEventMessage(WebstoneBlockGroup blockGroup) {
        this.groupId = blockGroup.getGroupId();
        this.name = blockGroup.getName();
        this.blockIds = new ArrayList<>(blockGroup.getBlockIds());
    }
}

class ChangeIndexMessage {
    UUID id;
    int newIndex;
}
