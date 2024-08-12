package tf.festival.webstone;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tf.festival.webstone.block.WebstoneBlocks;

public class WebstoneCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Webstone.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WEBSTONE_TAB = CREATIVE_MODE_TABS.register("webstone_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(WebstoneBlocks.WEBSTONE_REMOTE_BLOCK.get()))
            .title(Component.translatable("creativetab.webstone_tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(WebstoneBlocks.WEBSTONE_REMOTE_BLOCK.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
