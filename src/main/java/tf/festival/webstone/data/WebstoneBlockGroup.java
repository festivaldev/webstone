package tf.festival.webstone.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class WebstoneBlockGroup {
    // ZeroUUID = ungrouped blocks
    private UUID groupId = UUID.randomUUID();
    private String name;
    private final ArrayList<UUID> blockIds = new ArrayList<>();

    public WebstoneBlockGroup(String name) {
        this.name = name;
    }

    public WebstoneBlockGroup(UUID groupId, String name, Collection<UUID> blockIds) {
        this.groupId = groupId;
        this.name = name;

        if (blockIds != null) {
            this.blockIds.addAll(blockIds);
        }
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<UUID> getBlockIds() {
        return Collections.unmodifiableCollection(blockIds);
    }

    public boolean addBlock(WebstoneBlock block) {
        if (block == null) return false;

        if (!this.blockIds.contains(block.getBlockId()) && block.getGroupId() == null) {
            this.blockIds.add(block.getBlockId());
            block.setGroupId(this.getGroupId());

            return true;
        }

        return false;
    }

    public boolean addBlock(WebstoneBlock block, int index) {
        if (block == null) return false;

        if (!this.blockIds.contains(block.getBlockId()) && block.getGroupId() == null) {
            this.blockIds.add(index, block.getBlockId());
            block.setGroupId(this.getGroupId());

            return true;
        }

        return false;
    }

    public boolean moveBlock(WebstoneBlock block, int index) {
        if (block == null) return false;

        return this.removeBlock(block) && this.addBlock(block, index);
    }

    public boolean removeBlock(WebstoneBlock block) {
        if (block == null) return false;

        if (this.blockIds.contains(block.getBlockId()) && block.getGroupId().equals(this.groupId)) {
            this.blockIds.remove(block.getBlockId());
            block.setGroupId(null);

            return true;
        }

        return false;
    }
}