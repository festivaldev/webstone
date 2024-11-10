package tf.festival.webstone;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

@Mod(Webstone.MOD_ID)
@Mod.EventBusSubscriber(modid = Webstone.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Webstone {
    public static final String MOD_ID = "webstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static MinecraftServer SERVER;
    public static WebstoneWorldData WORLD_DATA;
    public static WebstoneSocketServer SOCKET_SERVER;


    public Webstone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WebstoneConfig.SPEC, "webstone-config.toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        WebstoneBlocks.register(modEventBus);
        WebstoneBlockEntities.register(modEventBus);
        WebstoneItems.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::onBuildCreativeModeTabContents);
    }

    // region Events
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WebstoneCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        SERVER = event.getServer();
        WORLD_DATA = WebstoneWorldData.getInstance(SERVER.overworld());

        try {
            SOCKET_SERVER = new WebstoneSocketServer(WebstoneConfig.WEBSOCKET_PORT.get());
            SOCKET_SERVER.start();
        } catch (UnknownHostException ex) {
            Webstone.LOGGER.error(ex.getMessage(), (Object[]) ex.getStackTrace());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        try {
            SOCKET_SERVER.stop();
        } catch (InterruptedException ex) {
            Webstone.LOGGER.error(ex.getMessage(), (Object[]) ex.getStackTrace());
        }

        SERVER = null;
        WORLD_DATA = null;
        SOCKET_SERVER = null;
    }

    @SubscribeEvent
    public void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            for (RegistryObject<Block> block : WebstoneBlocks.BLOCKS.getEntries()) {
                event.accept(block);
            }
        }
    }
    // endregion

    // https://gist.github.com/Mimickal/43aa3a75a52c2b55ab9358f8c9acd1f9
    public static Iterable<ChunkHolder> getLoadedChunks(ServerLevel world) {
        // Ironically, Reflection is probably the most portable way to do this.
        try {
            ChunkMap chunkMap = world.getChunkSource().chunkMap;
            Method getChunks = chunkMap.getClass().getDeclaredMethod("getChunks");
            getChunks.setAccessible(true);

            // AFAIK there's no way to do this cast that Java thinks is safe.
            // ChunkMap.getChunks() only ever returns this type, so it's safe enough.
            @SuppressWarnings("unchecked") Iterable<ChunkHolder> chunkIterator = (Iterable<ChunkHolder>) getChunks.invoke(chunkMap);

            return chunkIterator;

            // Any of these exceptions being thrown means we messed something up above, so just explode.
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("ServerChunkCache.getChunks() isn't a method, apparently.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke ServerChunkCache.getChunks()", e);
        }
    }
}