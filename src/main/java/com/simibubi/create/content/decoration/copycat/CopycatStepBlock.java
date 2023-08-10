package com.simibubi.create.content.decoration.copycat;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.placement.PoleHelper;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VoxelShaper;
import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CopycatStepBlock extends WaterloggedCopycatBlock {

	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public CopycatStepBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(HALF, Half.BOTTOM)
			.setValue(FACING, Direction.SOUTH));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		if (!player.isShiftKeyDown() && player.mayBuild()) {
			ItemStack heldItem = player.getItemInHand(hand);
			IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
			if (helper.matchesItem(heldItem))
				return helper.getOffset(player, world, state, pos, ray)
					.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
		}

		return super.use(state, world, pos, player, hand, ray);
	}

	@Override
	public BlockState getConnectiveMaterial(BlockAndTintGetter reader, BlockState otherState, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		if (!otherState.is(this))
			return null;

		BlockState stepState = reader.getBlockState(toPos);
		Direction facing = stepState.getValue(FACING);
		BlockPos diff = fromPos.subtract(toPos);

		if (diff.getY() != 0) {
			if (isOccluded(stepState, otherState, diff.getY() > 0 ? Direction.UP : Direction.DOWN))
				return getMaterial(reader, toPos);
			return null;
		}

		if (isOccluded(stepState, otherState, facing))
			return getMaterial(reader, toPos);

		int coord = facing.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ());

		if (otherState.setValue(WATERLOGGED, false) == stepState.setValue(WATERLOGGED, false) && coord == 0)
			return getMaterial(reader, toPos);

		return null;
	}

	@Override
	public boolean isUnblockableConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		return true;
	}

	@Override
	public boolean isIgnoredConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		BlockState toState = reader.getBlockState(toPos);

		if (!toState.is(this)) {
			if (!canFaceBeOccluded(state, face.getOpposite()))
				return true;
			for (Direction d : Iterate.directions)
				if (fromPos.relative(d)
					.equals(toPos) && !canFaceBeOccluded(state, d))
					return true;
			return false;
		}

		Direction facing = state.getValue(FACING);
		BlockPos diff = fromPos.subtract(toPos);
		int coord = facing.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ());

		Half half = state.getValue(HALF);
		if (half != toState.getValue(HALF))
			return diff.getY() == 0;

		return facing == toState.getValue(FACING)
			.getOpposite()
			&& !(coord != 0 && coord != facing.getAxisDirection()
				.getStep());
	}

	@Override
	public boolean canFaceBeOccluded(BlockState state, Direction face) {
		if (face.getAxis() == Axis.Y)
			return (state.getValue(HALF) == Half.TOP) == (face == Direction.UP);
		return state.getValue(FACING) == face;
	}

	@Override
	public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
		return canFaceBeOccluded(state, face.getOpposite());
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement =
			super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection());
		Direction direction = pContext.getClickedFace();
		if (direction == Direction.UP)
			return stateForPlacement;
		if (direction == Direction.DOWN || (pContext.getClickLocation().y - pContext.getClickedPos()
			.getY() > 0.5D))
			return stateForPlacement.setValue(HALF, Half.TOP);
		return stateForPlacement;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(HALF, FACING));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		VoxelShaper voxelShaper = pState.getValue(HALF) == Half.BOTTOM ? AllShapes.STEP_BOTTOM : AllShapes.STEP_TOP;
		return voxelShaper.get(pState.getValue(FACING));
	}

	@Override
	public boolean supportsExternalFaceHiding(BlockState state) {
		return true;
	}

	@Override
	public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState,
		Direction dir) {
		if (state.is(this) == neighborState.is(this)
			&& getMaterial(level, pos).skipRendering(getMaterial(level, pos.relative(dir)), dir.getOpposite()))
			return isOccluded(state, neighborState, dir);
		return false;
	}

	public static boolean isOccluded(BlockState state, BlockState other, Direction pDirection) {
		state = state.setValue(WATERLOGGED, false);
		other = other.setValue(WATERLOGGED, false);

		Half half = state.getValue(HALF);
		boolean vertical = pDirection.getAxis() == Axis.Y;
		if (half != other.getValue(HALF))
			return vertical && (pDirection == Direction.UP) == (half == Half.TOP);
		if (vertical)
			return false;

		Direction facing = state.getValue(FACING);
		if (facing.getOpposite() == other.getValue(FACING) && pDirection == facing)
			return true;
		if (other.getValue(FACING) != facing)
			return false;
		return pDirection.getAxis() != facing.getAxis();
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRot) {
		return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	private static class PlacementHelper extends PoleHelper<Direction> {

		public PlacementHelper() {
			super(AllBlocks.COPYCAT_STEP::has, state -> state.getValue(FACING)
				.getClockWise()
				.getAxis(), FACING);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.COPYCAT_STEP::isIn;
		}

	}

}
