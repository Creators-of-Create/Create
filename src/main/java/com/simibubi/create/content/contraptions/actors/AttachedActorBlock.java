package com.simibubi.create.content.contraptions.actors;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.placement.PoleHelper;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AttachedActorBlock extends HorizontalDirectionalBlock implements IWrenchable, ProperWaterloggedBlock {
	private final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	protected AttachedActorBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		return AllShapes.HARVESTER_BASE.get(direction);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing;
		if (context.getClickedFace().getAxis().isVertical())
			facing = context.getHorizontalDirection().getOpposite();
		else
			facing = context.getClickedFace();
		return withWater(defaultBlockState().setValue(FACING, facing), context);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (!player.isShiftKeyDown() && player.mayBuild()) {
			if (placementHelper.matchesItem(stack)) {
				placementHelper.getOffset(player, level, state, pos, hitResult)
					.placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
				return ItemInteractionResult.SUCCESS;
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private class PlacementHelper extends PoleHelper<Direction> {

		public PlacementHelper() {
			super(state -> state.getBlock() == AttachedActorBlock.this,
				state -> state.getValue(FACING).getClockWise().getAxis(), FACING);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return stack -> stack.getItem() instanceof BlockItem bi
								&& bi.getBlock() == AttachedActorBlock.this;
		}

	}
}
