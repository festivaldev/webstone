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

import java.util.ArrayList;
import java.util.UUID;

public class WebstoneWorldData extends SavedData {
    private final ArrayList<WebstoneBlockGroup> blockGroups = new ArrayList<>();
    private final ArrayList<WebstoneBlock> blocks = new ArrayList<>();

    public WebstoneWorldData() {
    }

    public static WebstoneWorldData load(CompoundTag nbt) {
        WebstoneWorldData data = new WebstoneWorldData();

        ListTag groupList = nbt.getList("BlockGroups", Tag.TAG_COMPOUND);

        for (Tag tag : groupList) {
            CompoundTag entryTag = (CompoundTag) tag;

            UUID groupId = UUID.fromString(entryTag.getString("GroupID"));
            String name = entryTag.getString("Name");

            ArrayList<UUID> blockIds = new ArrayList<>();
            ListTag blocks = entryTag.getList("BlockIDs", Tag.TAG_STRING);

            for (Tag blockIdTag : blocks) {
                blockIds.add(UUID.fromString(blockIdTag.getAsString()));
            }

            WebstoneBlockGroup blockGroup = new WebstoneBlockGroup(groupId, name, blockIds);

            data.blockGroups.add(blockGroup);
        }

        ListTag blockTagList = nbt.getList("RegisteredBlocks", Tag.TAG_COMPOUND);

        for (Tag blockTag : blockTagList) {
            CompoundTag entryTag = (CompoundTag) blockTag;
            UUID blockId = UUID.fromString(entryTag.getString("BlockID"));

            UUID groupId = null;
            try {
                groupId = UUID.fromString(entryTag.getString("GroupID"));
            } catch (Exception e) {
            }

            String name = entryTag.getString("Name");
            boolean powered = entryTag.getBoolean("Powered");
            int power = entryTag.getInt("Power");

            WebstoneBlock block = new WebstoneBlock(blockId, name, powered, power);

            if (groupId != null && data.getBlockGroupById(groupId) != null) {
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

            data.blocks.add(block);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        ListTag blockList = new ListTag();
        for (WebstoneBlock block : blocks) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("BlockID", block.getBlockId().toString());

            if (block.getGroupId() != null) {
                entryTag.putString("GroupID", block.getGroupId().toString());
            }

            entryTag.putString("Name", block.getName());
            entryTag.putBoolean("Powered", block.isPowered());
            entryTag.putInt("Power", block.getPower());

            blockList.add(entryTag);
        }

        nbt.put("RegisteredBlocks", blockList);

        ListTag groupList = new ListTag();
        for (WebstoneBlockGroup group : blockGroups) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("Name", group.getName());
            entryTag.putString("GroupID", group.getGroupId().toString());

            ListTag blocks = new ListTag();
            for (UUID blockId : group.getBlockIds()) {
                blocks.add(StringTag.valueOf(blockId.toString()));
            }
            entryTag.put("BlockIDs", blocks);

            groupList.add(entryTag);
        }

        nbt.put("BlockGroups", groupList);

        return nbt;
    }

    public static WebstoneWorldData getInstance(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(WebstoneWorldData::load, WebstoneWorldData::new, Webstone.MOD_ID);
    }

    public ArrayList<WebstoneBlock> getBlocks() {
        return blocks;
    }

    public WebstoneBlock getBlockById(UUID blockId) {
        return blocks.stream().filter(block -> blockId.equals(block.getBlockId())).findFirst().orElse(null);
    }

    public ArrayList<WebstoneBlockGroup> getBlockGroups() {
        return blockGroups;
    }

    public WebstoneBlockGroup getBlockGroupById(UUID groupId) {
        return blockGroups.stream().filter(group -> groupId.equals(group.getGroupId())).findFirst().orElse(null);
    }
}