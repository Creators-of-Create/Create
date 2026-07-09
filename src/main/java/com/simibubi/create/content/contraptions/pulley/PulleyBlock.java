package com.simibubi.create.content.contraptions.pulley;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PulleyBlock extends HorizontalAxisKineticBlock implements IBE<PulleyBlockEntity> {

	public PulleyBlock(Properties properties) {
		super(properties);
	}

	private static void onRopeBroken(Level world, BlockPos pulleyPos) {
		BlockEntity be = world.getBlockEntity(pulleyPos);
		if (be instanceof PulleyBlockEntity pulley) {
			pulley.initialOffset = 0;
			pulley.onLengthBroken();
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		BlockState below = level.getBlockState(pos.below());
		if (below.getBlock() instanceof RopeBlockBase)
			level.destroyBlock(pos.below(), true);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.mayBuild())
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (player.isShiftKeyDown())
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (stack.isEmpty()) {
            withBlockEntityDo(level, pos, be -> be.assembleNextTick = true);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public Class<PulleyBlockEntity> getBlockEntityClass() {
		return PulleyBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PulleyBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ROPE_PULLEY.get();
	}

	private static class RopeBlockBase extends Block implements SimpleWaterloggedBlock {

		public RopeBlockBase(Properties properties) {
			super(properties);
			registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
		}

		@Override
		protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
			return false;
		}

		@Override
		public PushReaction getPistonPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		@Override
		protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
			return AllBlocks.ROPE_PULLEY.asStack();
		}

		@Override
		protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
			super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
			if (!movedByPiston) {
				onRopeBroken(level, pos.above());
				{
					BlockState above = level.getBlockState(pos.above());
					BlockState below = level.getBlockState(pos.below());
					if (above.getBlock() instanceof RopeBlockBase)
						level.destroyBlock(pos.above(), true);
					if (below.getBlock() instanceof RopeBlockBase)
						level.destroyBlock(pos.below(), true);
				}
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
		protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
										 Direction direction, BlockPos neighbourPos, BlockState neighbourState,
										 RandomSource random) {
			if (state.getValue(BlockStateProperties.WATERLOGGED))
				ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
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
