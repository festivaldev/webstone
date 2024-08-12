package tf.festival.webstone;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import tf.festival.webstone.block.WebstoneBlocks;
import tf.festival.webstone.blockentity.WebstoneBlockEntities;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;
import tf.festival.webstone.item.WebstoneItems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.UUID;

@Mod(Webstone.MOD_ID)
public class Webstone {
    public static final String MOD_ID = "webstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static MinecraftServer SERVER;
    public static WebstoneSocketServer SOCKET_SERVER;

    private static WebstoneWorldData worldData;

    public Webstone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WebstoneConfig.SPEC, "webstone-config.toml");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        WebstoneBlocks.register(modEventBus);
        WebstoneBlockEntities.register(modEventBus);
        WebstoneItems.register(modEventBus);

//        WebstoneCreativeModeTab.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    public static WebstoneWorldData getWorldData() {
        return worldData;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(WebstoneBlocks.WEBSTONE_REMOTE_BLOCK);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        SERVER = event.getServer();

        worldData = WebstoneWorldData.getInstance(SERVER.overworld());

        try {
            SOCKET_SERVER = new WebstoneSocketServer(WebstoneConfig.WEBSOCKET_PORT.get());
            SOCKET_SERVER.start();
        } catch (UnknownHostException ex) {
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        try {
            SOCKET_SERVER.stop();
        } catch (InterruptedException ex) {
        }

        SERVER = null;
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEventListener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
//            WebstoneCommand.register(event.getDispatcher());
        }
    }

    public static boolean registerBlock(UUID blockId, boolean powered, int power, WebstoneRemoteBlockEntity blockEntity) {
        if (worldData.getRegisteredBlocks().stream().filter(block -> blockId.equals(block.getBlockId())).findFirst().orElse(null) == null) {
            WebstoneRegisteredBlock registeredBlock = new WebstoneRegisteredBlock(blockId, "Example", powered, power);
            registeredBlock.setBlockEntity(blockEntity);

            worldData.getRegisteredBlocks().add(registeredBlock);

            worldData.setDirty();
            SOCKET_SERVER.broadcastBlockList();

            return true;
        }

        return false;
    }

    public static void unregisterBlock(UUID blockId) {
        WebstoneRegisteredBlock registeredBlock = worldData.getRegisteredBlocks().stream().filter(block -> blockId.equals(block.getBlockId())).findFirst().orElse(null);
        if (registeredBlock != null) {
            worldData.getRegisteredBlocks().remove(registeredBlock);

            worldData.setDirty();
            SOCKET_SERVER.broadcastBlockList();
        }
    }

    public static void renameBlock(UUID blockId, String name) {
        WebstoneRegisteredBlock registeredBlock = worldData.getRegisteredBlocks().stream().filter(block -> blockId.equals(block.getBlockId())).findFirst().orElse(null);
        if (registeredBlock != null) {
            registeredBlock.setName(name);

            worldData.setDirty();
            SOCKET_SERVER.broadcastBlockList();
        }
    }

    public static void setBlockState(UUID blockId, boolean powered, WebstoneRemoteBlockEntity blockEntity) {
        if (SERVER != null) {
            WebstoneRegisteredBlock registeredBlock = worldData.getRegisteredBlocks().stream().filter(block -> block.getBlockId().equals(blockId)).findFirst().orElse(null);

            if (registeredBlock != null) {
                registeredBlock.setPowered(powered);

                worldData.setDirty();
                SOCKET_SERVER.broadcastUpdatedBlockState(blockId, powered);
            } else if (blockEntity != null) {
                blockEntity.setPowered(powered);
            }
        }
    }

    public static void setBlockPower(UUID blockId, int power) {
        if (SERVER != null) {
            WebstoneRegisteredBlock registeredBlock = worldData.getRegisteredBlocks().stream().filter(block -> block.getBlockId().equals(blockId)).findFirst().orElse(null);

            if (registeredBlock != null) {
                registeredBlock.setPower(power);

                worldData.setDirty();
                SOCKET_SERVER.broadcastUpdatedBlockPower(blockId, power);
            }
        }
    }

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