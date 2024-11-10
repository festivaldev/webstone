package tf.festival.webstone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import tf.festival.webstone.block.WebstoneRemoteBlock;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;
import tf.festival.webstone.data.WebstoneBlock;
import tf.festival.webstone.data.WebstoneBlockGroup;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class WebstoneWorldData extends SavedData {
    public static WebstoneWorldData getInstance(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(WebstoneWorldData::load, WebstoneWorldData::new, Webstone.MOD_ID);
    }

    public static WebstoneWorldData load(CompoundTag compoundTag) {
        WebstoneWorldData data = new WebstoneWorldData();

        ListTag registriesTag = compoundTag.getList("BlockRegistry", Tag.TAG_COMPOUND);

        for (Tag _registryTag : registriesTag) {
            CompoundTag registryTag = (CompoundTag) _registryTag;

            UUID registryId = UUID.fromString(registryTag.getString("RegistryID"));
            WebstoneRegistry registry = new WebstoneRegistry(registryId);

            String passphraseHash = registryTag.getString("Passphrase");
            if (!passphraseHash.isEmpty()) {
                registry.setPassphraseHash(passphraseHash);
            }

            ArrayList<WebstoneBlockGroup> blockGroups = new ArrayList<>();
            ListTag groupList = registryTag.getList("BlockGroups", Tag.TAG_COMPOUND);

            for (Tag _groupTag : groupList) {
                CompoundTag groupTag = (CompoundTag) _groupTag;

                UUID groupId = UUID.fromString(groupTag.getString("GroupID"));
                String name = groupTag.getString("Name");

                ArrayList<UUID> blockIds = new ArrayList<>();
                ListTag blocks = groupTag.getList("BlockIDs", Tag.TAG_STRING);

                for (Tag blockIdTag : blocks) {
                    blockIds.add(UUID.fromString(blockIdTag.getAsString()));
                }

                blockGroups.add(new WebstoneBlockGroup(groupId, name, blockIds));
            }

            registry.getBlockGroups().addAll(blockGroups);

            ArrayList<WebstoneBlock> blocks = new ArrayList<>();
            ListTag blockList = registryTag.getList("Blocks", Tag.TAG_COMPOUND);

            for (Tag _blockTag : blockList) {
                CompoundTag blockTag = (CompoundTag) _blockTag;
                UUID blockId = UUID.fromString(blockTag.getString("BlockID"));

                UUID groupId = null;
                try {
                    groupId = UUID.fromString(blockTag.getString("GroupID"));
                } catch (Exception e) {
                    // Block is not assigned to any group
                }

                String name = blockTag.getString("Name");
                boolean powered = blockTag.getBoolean("Powered");
                int power = blockTag.getInt("Power");

                WebstoneBlock block = new WebstoneBlock(blockId, name, powered, power);

                if (groupId != null && registry.getBlockGroupById(groupId) != null) {
                    block.setGroupId(groupId);
                }

                for (ServerLevel level : Webstone.SERVER.getAllLevels()) {
                    for (ChunkHolder holder : Webstone.getLoadedChunks(level)) {
                        if (holder.getFullChunk() == null) continue;

                        for (BlockEntity entity : holder.getFullChunk().getBlockEntities().values()) {
                            if (entity instanceof WebstoneRemoteBlockEntity blockEntity) {
                                if (blockEntity.getBlockId().equals(blockId)) {
                                    block.setBlock((WebstoneRemoteBlock) blockEntity.getBlockState().getBlock());
                                    block.setBlockEntity(blockEntity);
                                }
                            }
                        }
                    }
                }

                blocks.add(block);
            }

            registry.addBlocks(blocks);
            WebstoneRegistry.getAllRegistries().put(registryId, registry);
        }

        CompoundTag userContextTag = compoundTag.getCompound("UserContext");
        for (String userId : userContextTag.getAllKeys()) {
            WebstoneRegistry.setUserRegistryContext(UUID.fromString(userId), WebstoneRegistry.WebstoneRegistryContext.values()[userContextTag.getInt(userId)]);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag registriesTag = new ListTag();

        for (Map.Entry<UUID, WebstoneRegistry> registryEntry : WebstoneRegistry.getAllRegistries().entrySet()) {
            WebstoneRegistry registry = registryEntry.getValue();
            CompoundTag registryTag = new CompoundTag();

            registryTag.putString("RegistryID", registry.getRegistryId().toString());

            if (registry.getPassphrase() != null && !registry.getPassphrase().isEmpty()) {
                registryTag.putString("Passphrase", registry.getPassphrase());
            }

            ListTag blockListTag = new ListTag();
            for (WebstoneBlock block : registry.getBlocks()) {
                CompoundTag blockTag = new CompoundTag();

                blockTag.putString("BlockID", block.getBlockId().toString());

                if (block.getGroupId() != null) {
                    blockTag.putString("GroupID", block.getGroupId().toString());
                }

                blockTag.putString("Name", block.getName());
                blockTag.putBoolean("Powered", block.isPowered());
                blockTag.putInt("Power", block.getPower());

                blockListTag.add(blockTag);
            }

            registryTag.put("Blocks", blockListTag);

            ListTag groupListTag = new ListTag();
            for (WebstoneBlockGroup blockGroup : registry.getBlockGroups()) {
                CompoundTag blockGroupTag = new CompoundTag();

                blockGroupTag.putString("GroupID", blockGroup.getGroupId().toString());
                blockGroupTag.putString("Name", blockGroup.getName());

                ListTag blocks = new ListTag();
                for (UUID blockId : blockGroup.getBlockIds()) {
                    blocks.add(StringTag.valueOf(blockId.toString()));
                }
                blockGroupTag.put("BlockIDs", blocks);

                groupListTag.add(blockGroupTag);
            }

            registryTag.put("BlockGroups", groupListTag);

            registriesTag.add(registryTag);
        }

        compoundTag.put("BlockRegistry", registriesTag);

        CompoundTag userContextTag = new CompoundTag();
        for (Map.Entry<UUID, WebstoneRegistry.WebstoneRegistryContext> userContext : WebstoneRegistry.getAllUsersRegistryContext().entrySet()) {
            userContextTag.putInt(userContext.getKey().toString(), userContext.getValue().ordinal());
        }

        compoundTag.put("UserContext", userContextTag);

        return compoundTag;
    }
}