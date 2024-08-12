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
import tf.festival.webstone.Webstone;
import tf.festival.webstone.blockentity.WebstoneBlockEntities;
import tf.festival.webstone.blockentity.WebstoneRemoteBlockEntity;

import java.util.UUID;

public class WebstoneRemoteBlock extends DirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public WebstoneRemoteBlock(Properties pProperties) {
        super(pProperties);

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(POWERED, Boolean.FALSE)
                .setValue(POWER, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED, POWER);
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, @NotNull BlockGetter pBlockAccess, @NotNull BlockPos pPos, @NotNull Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    public int getSignal(BlockState pBlockState, @NotNull BlockGetter pBlockAccess, @NotNull BlockPos pPos, @NotNull Direction pSide) {
        return pBlockState.getValue(POWERED) && pBlockState.getValue(FACING) == pSide ? pBlockState.getValue(POWER) : 0;
    }

    @Override
    public void tick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);

        this.updateNeighborsInFront(pLevel, pPos, pState);
    }

    protected void updateNeighborsInFront(Level pLevel, BlockPos pPos, BlockState pState) {
        Direction direction = pState.getValue(FACING);
        BlockPos position = pPos.relative(direction.getOpposite());
        pLevel.neighborChanged(position, this, pPos);
        pLevel.updateNeighborsAtExceptFromFacing(position, this, direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite().getOpposite());
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            if (pHand == InteractionHand.MAIN_HAND && pPlayer.getItemInHand(pHand).isEmpty()) {
                BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
                if (blockEntity instanceof WebstoneRemoteBlockEntity) {
                    UUID blockId = ((WebstoneRemoteBlockEntity) blockEntity).getBlockId();

                    if (!pPlayer.isCrouching()) {
                        if (Webstone.registerBlock(blockId, pState.getValue(POWERED), pState.getValue(POWER), (WebstoneRemoteBlockEntity) blockEntity)) {
                            pPlayer.displayClientMessage(Component.literal("Webstone block registered."), true);

                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        Webstone.setBlockState(blockId, !pState.getValue(POWERED), (WebstoneRemoteBlockEntity) blockEntity);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return WebstoneBlockEntities.WEBSTONE_REMOTE_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public void onRemove(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            if (!pLevel.isClientSide) {
                BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
                if (blockEntity instanceof WebstoneRemoteBlockEntity) {
                    UUID blockId = ((WebstoneRemoteBlockEntity) blockEntity).getBlockId();

                    Webstone.unregisterBlock(blockId);
                }
            }
        }
    }
}
