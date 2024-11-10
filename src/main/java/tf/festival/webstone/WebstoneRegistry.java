package tf.festival.webstone;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;
import tf.festival.webstone.data.WebstoneBlock;
import tf.festival.webstone.data.WebstoneBlockGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebstoneRegistry {
    public enum WebstoneRegistryContext {
        SERVER,
        PLAYER
    }

    // ZeroUUID = global server context
    private static final Map<UUID, WebstoneRegistry> perUserRegistry = new HashMap<>(Map.ofEntries(
        Map.entry(new UUID(0, 0), new WebstoneRegistry(new UUID(0, 0)))
    ));

    private static final Map<UUID, WebstoneRegistryContext> userRegistryContext = new HashMap<>();

    private final UUID registryId;
    private final ArrayList<WebstoneBlockGroup> blockGroups = new ArrayList<>();
    private final ArrayList<WebstoneBlock> blocks = new ArrayList<>();

    // Bcrypt hash of auto-generated or user-set passphrase, or null if server registry (uses config passphrase)
    private String passphraseHash;

    public WebstoneRegistry(UUID registryId) {
        this.registryId = registryId;
    }

    // region
    public static boolean registerBlock(UUID blockId, WebstoneRemoteBlockEntity blockEntity, Player player, boolean powered, int power) {
        if (!containsBlockInAnyRegistry(blockId)) {
            if (getUserRegistryContext(player.getUUID()) == WebstoneRegistryContext.PLAYER &&
                getRegistry(player.getUUID()) == null) {
                player.sendSystemMessage(Component.literal("Your personal block registry has not been set up."));
                player.sendSystemMessage(Component.literal("Use \"/webstone genpass\" or \"/webstone setpass\" to set it up."));
                player.sendSystemMessage(Component.literal("Use \"/webstone context server\" to register public blocks instead."));

                return false;
            }

            WebstoneBlock block = new WebstoneBlock(blockId, "Example", powered, power);
            block.setBlockEntity(blockEntity);

            switch (WebstoneRegistry.getUserRegistryContext(player.getUUID())) {
                case SERVER -> {
                    WebstoneRegistry.getServerRegistry().addBlock(block);
                    Webstone.SOCKET_SERVER.broadcastBlockList(new UUID(0, 0));
                }
                case PLAYER -> {
                    WebstoneRegistry.getRegistry(player.getUUID()).addBlock(block);
                    Webstone.SOCKET_SERVER.broadcastBlockList(WebstoneRegistry.getRegistry(player.getUUID()).registryId);
                }
                default -> {
                    return false;
                }
            }

            Webstone.WORLD_DATA.setDirty();

            return true;
        }

        player.displayClientMessage(Component.literal("Block is already registered."), true);

        return false;
    }

    public static void unregisterBlock(UUID blockId) {
        if (containsBlockInAnyRegistry(blockId)) {
            WebstoneRegistry registry = getRegistryForBlock(blockId);
            WebstoneBlock block = registry.getBlockById(blockId);

            if (block != null) {
                if (block.getGroupId() != null) {
                    if ((Object) registry.getBlockGroupById(block.getGroupId()) instanceof WebstoneBlockGroup blockGroup) {
                        blockGroup.removeBlock(block);
                        Webstone.SOCKET_SERVER.broadcastBlockGroupUpdated(blockGroup);
                    }
                }

                registry.removeBlock(block);

                Webstone.WORLD_DATA.setDirty();
                Webstone.SOCKET_SERVER.broadcastBlockList(registry.registryId);
            }
        }
    }

    public static boolean setBlockState(UUID blockId, boolean powered) {
        WebstoneRegistry registry = getRegistryForBlock(blockId);

        if (registry != null) {
            registry.getBlockById(blockId).setPowered(powered);
            return true;
        }

        return false;
    }

    public static boolean setBlockPower(UUID blockId, int power) {
        WebstoneRegistry registry = getRegistryForBlock(blockId);

        if (registry != null) {
            registry.getBlockById(blockId).setPower(power);
            return true;
        }

        return false;
    }
    // endregion

    // region
    public static void clear() {
        perUserRegistry.clear();
        userRegistryContext.clear();
    }

    public static Map<UUID, WebstoneRegistry> getAllRegistries() {
        return perUserRegistry;
    }

    public static WebstoneRegistry getServerRegistry() {
        return perUserRegistry.get(new UUID(0, 0));
    }

    public static WebstoneRegistry getOrCreateRegistry(UUID playerId) {
        if (!perUserRegistry.containsKey(playerId)) {
            perUserRegistry.put(playerId, new WebstoneRegistry(playerId));

            Webstone.WORLD_DATA.setDirty();
        }

        return perUserRegistry.get(playerId);
    }

    public static WebstoneRegistry getRegistry(UUID playerId) {
        if (playerId == null) return null;
        return perUserRegistry.get(playerId);
    }

    public static WebstoneRegistryContext getUserRegistryContext(UUID id) {
        if (!userRegistryContext.containsKey(id)) {
            return WebstoneRegistryContext.PLAYER;
        }

        return userRegistryContext.get(id);
    }

    public static void setUserRegistryContext(UUID id, WebstoneRegistryContext context) {
        userRegistryContext.put(id, context);
        Webstone.WORLD_DATA.setDirty();
    }

    public static Map<UUID, WebstoneRegistryContext> getAllUsersRegistryContext() {
        return userRegistryContext;
    }

    public static WebstoneRegistry getRegistryForBlock(UUID blockId) {
        for (WebstoneRegistry registry : perUserRegistry.values()) {
            if (registry.getBlockById(blockId) != null) return registry;
        }

        return null;
    }

    public static WebstoneRegistry getRegistryForBlockGroup(UUID blockGroupId) {
        for (WebstoneRegistry registry : perUserRegistry.values()) {
            if (registry.getBlockGroupById(blockGroupId) != null) return registry;
        }

        return null;
    }

    public static boolean containsBlockInAnyRegistry(UUID blockId) {
        return getRegistryForBlock(blockId) != null;
    }
    // endregion

    // region
    public UUID getRegistryId() {
        return registryId;
    }

    public String getName() {
        if (registryId.equals(new UUID(0, 0))) {
            return "Public Blocks";
        }

        if (Webstone.SERVER.getProfileCache().get(registryId).isPresent()) {
            return Webstone.SERVER.getProfileCache().get(registryId).get().getName();
        }

        return "Unknown Block List";
    }

    public ArrayList<WebstoneBlock> getBlocks() {
        return blocks;
    }

    public void addBlocks(ArrayList<WebstoneBlock> blocks) {
        for (WebstoneBlock block : blocks) {
            addBlock(block);
        }
    }

    public WebstoneBlock getBlockById(UUID blockId) {
        return blocks.stream().filter(block -> blockId.equals(block.getBlockId())).findFirst().orElse(null);
    }

    public void addBlock(WebstoneBlock block) {
        block.setRegistryId(registryId);
        blocks.add(block);
    }

    public void removeBlock(WebstoneBlock block) {
        block.setRegistryId(null);
        blocks.remove(block);
    }

    public ArrayList<WebstoneBlockGroup> getBlockGroups() {
        return blockGroups;
    }

    public WebstoneBlockGroup getBlockGroupById(UUID groupId) {
        return blockGroups.stream().filter(group -> groupId.equals(group.getGroupId())).findFirst().orElse(null);
    }

    public String getPassphrase() {
        return passphraseHash;
    }

    public void setPassphrase(@NotNull String passphrase) {
        passphraseHash = BCrypt.withDefaults().hashToString(12, passphrase.toCharArray());

        Webstone.WORLD_DATA.setDirty();
    }

    public void setPassphraseHash(@NotNull String passphraseHash) {
        this.passphraseHash = passphraseHash;
    }

    public boolean comparePassphrase(@NotNull String passphrase) {
        if (passphraseHash == null || passphraseHash.isEmpty()) return true;

        return BCrypt.verifyer().verify(passphrase.toCharArray(), passphraseHash).verified;
    }
    // endregion
}