package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class PulleyBlock extends HorizontalAxisKineticBlock implements ITE<PulleyTileEntity> {

    public PulleyBlock(Properties properties) {
        super(properties);
    }

    private static void onRopeBroken(Level world, BlockPos pulleyPos) {
		BlockEntity te = world.getBlockEntity(pulleyPos);
		if (te instanceof PulleyTileEntity) {
			PulleyTileEntity pulley = (PulleyTileEntity) te;
			pulley.initialOffset = 0;
			pulley.onLengthBroken();
		}
	}

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isClientSide) {
                BlockState below = worldIn.getBlockState(pos.below());
                if (below.getBlock() instanceof RopeBlockBase)
                    worldIn.destroyBlock(pos.below(), true);
            }
            if (state.hasBlockEntity())
                worldIn.removeBlockEntity(pos);
        }
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                  BlockHitResult hit) {
        if (!player.mayBuild())
            return InteractionResult.PASS;
        if (player.isShiftKeyDown())
            return InteractionResult.PASS;
        if (player.getItemInHand(handIn)
                .isEmpty()) {
            withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.PULLEY.get(state.getValue(HORIZONTAL_AXIS));
    }

    @Override
    public Class<PulleyTileEntity> getTileEntityClass() {
        return PulleyTileEntity.class;
    }

    @Override
    public BlockEntityType<? extends PulleyTileEntity> getTileEntityType() {
    	return AllTileEntities.ROPE_PULLEY.get();
    }

	private static class RopeBlockBase extends Block implements SimpleWaterloggedBlock, BlockPickInteractionAware {

        public RopeBlockBase(Properties properties) {
            super(properties);
            registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
        }

		@Override
    	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
    		return false;
    	}

        @Override
        public PushReaction getPistonPushReaction(BlockState state) {
            return PushReaction.BLOCK;
        }

		@Override
		public ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, @Nullable Player player, @Nullable HitResult result) {
			return AllBlocks.ROPE_PULLEY.asStack();
		}

        @Override
        public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            if (!isMoving && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !newState.hasProperty(BlockStateProperties.WATERLOGGED) || state.getValue(BlockStateProperties.WATERLOGGED) == newState.getValue(BlockStateProperties.WATERLOGGED))) {
                onRopeBroken(worldIn, pos.above());
                if (!worldIn.isClientSide) {
                    BlockState above = worldIn.getBlockState(pos.above());
                    BlockState below = worldIn.getBlockState(pos.below());
                    if (above.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.above(), true);
                    if (below.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.below(), true);
                }
            }
            if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
                worldIn.removeBlockEntity(pos);
            }
        }


        @Override
        public FluidState getFluidState(BlockState state) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
        }

        @Override
        protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.WATERLOGGED);
            super.createBlockStateDefinition(builder);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                              LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
            if (state.getValue(BlockStateProperties.WATERLOGGED)) {
                world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
            }
            return state;
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
            return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
        }

    }

    public static class MagnetBlock extends RopeBlockBase {

        public MagnetBlock(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
            return AllShapes.PULLEY_MAGNET;
        }

    }

    public static class RopeBlock extends RopeBlockBase {

        public RopeBlock(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
            return AllShapes.FOUR_VOXEL_POLE.get(Direction.UP);
        }
    }

}
