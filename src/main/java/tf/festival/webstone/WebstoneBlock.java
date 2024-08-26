package tf.festival.webstone;

import tf.festival.webstone.block.WebstoneRemoteBlock;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;

import java.util.UUID;

public class WebstoneBlock {
    private UUID blockId;
    private String name;
    private boolean powered;
    private int power;

    private UUID groupId;

    private transient WebstoneRemoteBlock block;
    private transient WebstoneRemoteBlockEntity blockEntity;

    public WebstoneBlock(UUID blockId, String name, boolean powered, int power) {
        this.blockId = blockId;
        this.name = name;
        this.powered = powered;
        this.power = power;
    }

    public UUID getBlockId() {
        return this.blockId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;

        if (this.blockEntity != null)
            this.blockEntity.setPowered((powered));
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power) {
        this.power = Math.min(Math.max(power, 0), 15);

        if (this.blockEntity != null)
            this.blockEntity.setPower(this.power);
    }

    public UUID getGroupId() {
        if (this.groupId != null) return this.groupId;
        return null;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public WebstoneRemoteBlock getBlock() {
        return this.block;
    }

    public void setBlock(WebstoneRemoteBlock block) {
        this.block = block;
    }

    public WebstoneRemoteBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public void setBlockEntity(WebstoneRemoteBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
}