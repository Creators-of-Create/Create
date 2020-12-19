package com.simibubi.create.content.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class SpeedControllerBlock extends HorizontalAxisKineticBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public SpeedControllerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ROTATION_SPEED_CONTROLLER.create();
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState above = context.getWorld().getBlockState(context.getPos().up());
		if (CogWheelBlock.isLargeCog(above) && above.get(CogWheelBlock.AXIS).isHorizontal())
			return getDefaultState().with(HORIZONTAL_AXIS, above.get(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
		return super.getStateForPlacement(context);
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		ItemStack heldItem = player.getHeldItem(hand);
		if (helper.matchesItem(heldItem)) {
			PlacementOffset offset = helper.getOffset(world, state, pos, ray);

			if (!offset.isReplaceable(world))
				return ActionResultType.PASS;

			offset.placeInWorld(world, AllBlocks.LARGE_COGWHEEL.getDefaultState(), player, heldItem);

			return ActionResultType.SUCCESS;

		}

		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SPEED_CONTROLLER.get(state.get(HORIZONTAL_AXIS));
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.LARGE_COGWHEEL::isIn;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.ROTATION_SPEED_CONTROLLER::has;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			BlockPos newPos = pos.up();
			if (!world.getBlockState(newPos).getMaterial().isReplaceable())
				return PlacementOffset.fail();

			Axis newAxis = state.get(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

			if (CogwheelBlockItem.DiagonalCogHelper.hasLargeCogwheelNeighbor(world, newPos, newAxis) || CogwheelBlockItem.DiagonalCogHelper.hasSmallCogwheelNeighbor(world, newPos, newAxis))
				return PlacementOffset.fail();

			return PlacementOffset.success(newPos, s -> s.with(CogWheelBlock.AXIS, newAxis));
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockRayTraceResult ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, state.get(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X));
		}
	}
}
