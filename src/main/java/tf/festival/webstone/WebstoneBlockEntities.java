package tf.festival.webstone;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;

public class WebstoneBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Webstone.MOD_ID);

    public static final RegistryObject<BlockEntityType<WebstoneRemoteBlockEntity>> WEBSTONE_REMOTE_BLOCK_ENTITY = BLOCK_ENTITIES.register("webstone_remote_block", () -> BlockEntityType.Builder.of(WebstoneRemoteBlockEntity::new, WebstoneBlocks.WEBSTONE_REMOTE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}