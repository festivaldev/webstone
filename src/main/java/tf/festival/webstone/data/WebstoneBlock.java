package tf.festival.webstone.data;

import tf.festival.webstone.block.WebstoneRemoteBlock;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;

import java.util.UUID;

public class WebstoneBlock {
    private final UUID blockId;
    private String name;
    private boolean powered;
    private int power;

    private UUID registryId;
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

    public boolean setName(String name) {
        if (!name.equals(this.name)) {
            this.name = name;

            return true;
        }

        return false;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean setPowered(boolean powered) {
        if (powered != this.powered) {
            this.powered = powered;

            if (this.blockEntity != null)
                this.blockEntity.setPowered((powered));

            return true;
        }

        return false;
    }

    public int getPower() {
        return this.power;
    }

    public boolean setPower(int power) {
        if (Math.min(Math.max(power, 0), 15) != this.power) {
            this.power = Math.min(Math.max(power, 0), 15);

            if (this.blockEntity != null)
                this.blockEntity.setPower(this.power);

            return true;
        }

        return false;
    }

    public UUID getRegistryId() {
        if (this.registryId != null) return this.registryId;
        return null;
    }

    public void setRegistryId(UUID registryId) {
        this.registryId = registryId;
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