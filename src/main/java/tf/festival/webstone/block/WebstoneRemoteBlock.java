package tf.festival.webstone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import tf.festival.webstone.WebstoneBlockEntities;
import tf.festival.webstone.WebstoneRegistry;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;

import java.util.UUID;

public class WebstoneRemoteBlock extends DirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public WebstoneRemoteBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any()
            .setValue(POWERED, Boolean.FALSE)
            .setValue(POWER, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER);
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    @Override
    public int getSignal(BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return blockState.getValue(POWERED) && blockState.getValue(FACING) == side ? blockState.getValue(POWER) : 0;
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);

        this.updateNeighborsInFront(level, pos, state);
    }

    protected void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        BlockPos position = pos.relative(direction.getOpposite());
        level.neighborChanged(position, this, pos);
        level.updateNeighborsAtExceptFromFacing(position, this, direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Float.MAX_VALUE;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return WebstoneBlockEntities.WEBSTONE_REMOTE_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (blockEntity instanceof WebstoneRemoteBlockEntity) {
                    UUID blockId = ((WebstoneRemoteBlockEntity) blockEntity).getBlockId();

                    if (!player.isCrouching()) {
                        if (WebstoneRegistry.registerBlock(blockId, (WebstoneRemoteBlockEntity) blockEntity, player, state.getValue(POWERED), state.getValue(POWER))) {
                            player.displayClientMessage(Component.literal("Webstone block registered."), true);

                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        if (WebstoneRegistry.setBlockState(blockId, !state.getValue(POWERED))) {
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof WebstoneRemoteBlockEntity) {
                    UUID blockId = ((WebstoneRemoteBlockEntity) blockEntity).getBlockId();

                    WebstoneRegistry.unregisterBlock(blockId);
                }
            }
        }
    }
}