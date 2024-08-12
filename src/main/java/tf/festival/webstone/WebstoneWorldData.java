package tf.festival.webstone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
    private ArrayList<WebstoneRegisteredBlock> registeredBlocks = new ArrayList<>();

    public WebstoneWorldData() {
    }

    public static WebstoneWorldData load(CompoundTag nbt) {
        WebstoneWorldData data = new WebstoneWorldData();

        ListTag list = nbt.getList("RegisteredBlocks", Tag.TAG_COMPOUND);

        for (Tag tag : list) {
            CompoundTag entryTag = (CompoundTag) tag;
            UUID blockId = UUID.fromString(entryTag.getString("BlockID"));
            String name = entryTag.getString("Name");
            boolean powered = entryTag.getBoolean("Powered");
            int power = entryTag.getInt("Power");

            WebstoneRegisteredBlock registeredBlock = new WebstoneRegisteredBlock(blockId, name, powered, power);

            for (ServerLevel level : Webstone.SERVER.getAllLevels()) {
                for (ChunkHolder holder : Webstone.getLoadedChunks(level)) {
                    if (holder.getFullChunk() == null) continue;

                    for (BlockEntity entity : holder.getFullChunk().getBlockEntities().values()) {
                        if (entity instanceof WebstoneRemoteBlockEntity) {
                            WebstoneRemoteBlockEntity blockEntity = (WebstoneRemoteBlockEntity) entity;

                            if (blockEntity.getBlockId().equals(blockId)) {
                                registeredBlock.setBlock((WebstoneRemoteBlock) blockEntity.getBlockState().getBlock());
                                registeredBlock.setBlockEntity(blockEntity);
                            }
                        }
                    }
                }
            }

            data.registeredBlocks.add(registeredBlock);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        ListTag list = new ListTag();
        for (WebstoneRegisteredBlock block : registeredBlocks) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("BlockID", block.getBlockId().toString());
            entryTag.putString("Name", block.getName());
            entryTag.putBoolean("Powered", block.isPowered());
            entryTag.putInt("Power", block.getPower());

            list.add(entryTag);
        }

        nbt.put("RegisteredBlocks", list);

        return nbt;
    }

    public static WebstoneWorldData getInstance(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(WebstoneWorldData::load, WebstoneWorldData::new, Webstone.MOD_ID);
    }

    public ArrayList<WebstoneRegisteredBlock> getRegisteredBlocks() {
        return registeredBlocks;
    }
}