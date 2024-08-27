package com.simibubi.create.content.decoration.copycat;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PlacementOffset;

import com.simibubi.create.foundation.utility.worldWrappers.OcclusionTestLevel;

import net.minecraft.MethodsReturnNonnullByDefault;
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
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CopycatPanelBlock extends WaterloggedCopycatBlock {

	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public CopycatPanelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
	}

	@Override
	public boolean isAcceptedRegardless(BlockState material) {
		return CopycatSpecialCases.isBarsMaterial(material) || CopycatSpecialCases.isTrapdoorMaterial(material);
	}

	@Override
	public BlockState prepareMaterial(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer,
		InteractionHand pHand, BlockHitResult pHit, BlockState material) {
		if (!CopycatSpecialCases.isTrapdoorMaterial(material))
			return super.prepareMaterial(pLevel, pPos, pState, pPlayer, pHand, pHit, material);

		Direction panelFacing = pState.getValue(FACING);
		if (panelFacing == Direction.DOWN)
			material = material.setValue(TrapDoorBlock.HALF, Half.TOP);
		if (panelFacing.getAxis() == Axis.Y)
			return material.setValue(TrapDoorBlock.FACING, pPlayer.getDirection())
				.setValue(TrapDoorBlock.OPEN, false);

		boolean clickedNearTop = pHit.getLocation().y - .5 > pPos.getY();
		return material.setValue(TrapDoorBlock.OPEN, true)
			.setValue(TrapDoorBlock.HALF, clickedNearTop ? Half.TOP : Half.BOTTOM)
			.setValue(TrapDoorBlock.FACING, panelFacing);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		if (!player.isShiftKeyDown() && player.mayBuild()) {
			ItemStack heldItem = player.getItemInHand(hand);
			IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
			if (placementHelper.matchesItem(heldItem)) {
				placementHelper.getOffset(player, world, state, pos, ray)
					.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
				return InteractionResult.SUCCESS;
			}
		}

		return super.use(state, world, pos, player, hand, ray);
	}

	@Override
	public boolean isUnblockableConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		return true;
	}

	@Override
	public boolean isIgnoredConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		Direction facing = state.getValue(FACING);
		BlockState toState = reader.getBlockState(toPos);

		if (!toState.is(this))
			return facing != face.getOpposite();

		BlockPos diff = fromPos.subtract(toPos);
		int coord = facing.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ());
		return facing == toState.getValue(FACING)
			.getOpposite()
			&& !(coord != 0 && coord == facing.getAxisDirection()
				.getStep());
	}

	@Override
	public boolean canFaceBeOccluded(BlockState state, Direction face) {
		return state.getValue(FACING)
			.getOpposite() == face;
	}

	@Override
	public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
		return canFaceBeOccluded(state, face.getOpposite());
	}

	@Override
	public BlockState getConnectiveMaterial(BlockAndTintGetter reader, BlockState otherState, Direction face,
		BlockPos fromPos, BlockPos toPos) {
		BlockState panelState = reader.getBlockState(toPos);
		Direction facing = panelState.getValue(FACING);

		if (!otherState.is(this))
			return facing == face.getOpposite() ? getMaterial(reader, toPos) : null;

		if (isOccluded(panelState, otherState, facing))
			return getMaterial(reader, toPos);

		BlockPos diff = fromPos.subtract(toPos);
		int coord = facing.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ());

		if (otherState.setValue(WATERLOGGED, false) == panelState.setValue(WATERLOGGED, false) && coord == 0)
			return getMaterial(reader, toPos);

		return null;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		return stateForPlacement.setValue(FACING, pContext.getNearestLookingDirection()
			.getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.CASING_3PX.get(pState.getValue(FACING));
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

	@Override
	public boolean supportsExternalFaceHiding(BlockState state) {
		return true;
	}

	@Override
	public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState,
		Direction dir) {
		BlockPos otherPos = pos.relative(dir);
		BlockState material = getMaterial(level, pos);
		BlockState otherMaterial = getMaterial(level, otherPos);

		if (state.is(this) == neighborState.is(this)) {
			if (CopycatSpecialCases.isBarsMaterial(material)
				&& CopycatSpecialCases.isBarsMaterial(otherMaterial))
				return state.getValue(FACING) == neighborState.getValue(FACING);
			if (material.skipRendering(otherMaterial, dir.getOpposite()))
				return isOccluded(state, neighborState, dir.getOpposite());

			OcclusionTestLevel occlusionTestLevel = new OcclusionTestLevel();
			occlusionTestLevel.setBlock(pos, material);
			occlusionTestLevel.setBlock(otherPos, otherMaterial);
			if (material.isSolidRender(occlusionTestLevel, pos) && otherMaterial.isSolidRender(occlusionTestLevel, otherPos))
				if(!Block.shouldRenderFace(otherMaterial, occlusionTestLevel, pos, dir.getOpposite(), otherPos)) {
					occlusionTestLevel.clear();
					return isOccluded(state, neighborState, dir.getOpposite());
				}

			occlusionTestLevel.clear();
		}

		return state.getValue(FACING) == dir.getOpposite()
			&& getMaterial(level, pos).skipRendering(neighborState, dir.getOpposite());
	}

	public static boolean isOccluded(BlockState state, BlockState other, Direction pDirection) {
		state = state.setValue(WATERLOGGED, false);
		other = other.setValue(WATERLOGGED, false);
		Direction facing = state.getValue(FACING);
		if (facing.getOpposite() == other.getValue(FACING) && pDirection == facing)
			return true;
		if (other.getValue(FACING) != facing)
			return false;
		return pDirection.getAxis() != facing.getAxis();
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.COPYCAT_PANEL::isIn;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.COPYCAT_PANEL::has;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
				state.getValue(FACING)
					.getAxis(),
				dir -> world.getBlockState(pos.relative(dir))
					.getMaterial()
					.isReplaceable());

			if (directions.isEmpty())
				return PlacementOffset.fail();
			else {
				return PlacementOffset.success(pos.relative(directions.get(0)),
					s -> s.setValue(FACING, state.getValue(FACING)));
			}
		}
	}

}
