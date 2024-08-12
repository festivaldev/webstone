package tf.festival.webstone.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tf.festival.webstone.block.WebstoneRemoteBlock;

import java.util.UUID;

public class WebstoneRemoteBlockEntity extends BlockEntity {
    private UUID blockId = UUID.randomUUID();

    public WebstoneRemoteBlockEntity(BlockPos pos, BlockState state) {
        super(WebstoneBlockEntities.WEBSTONE_REMOTE_BLOCK_ENTITY.get(), pos, state);
    }

    public UUID getBlockId() {
        return blockId;
    }

    public boolean isPowered() {
        return getBlockState().getValue(WebstoneRemoteBlock.POWERED);
    }

    public void setPowered(boolean powered) {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(WebstoneRemoteBlock.POWERED, powered), 3);
        }
    }

    public void setPower(int power) {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(WebstoneRemoteBlock.POWER, power), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("BlockID")) {
            blockId = UUID.fromString(tag.getString("BlockID"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("BlockID", blockId.toString());
    }
}
