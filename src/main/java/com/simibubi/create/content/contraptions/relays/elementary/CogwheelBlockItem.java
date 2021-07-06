package com.simibubi.create.content.contraptions.relays.elementary;

import static com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock.AXIS;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	private final int placementHelperId;
	private final int integratedCogHelperId;

	public CogwheelBlockItem(CogWheelBlock block, Properties builder) {
		super(block, builder);
		large = block.isLarge;

		placementHelperId = PlacementHelpers.register(large ? new LargeCogHelper() : new SmallCogHelper());
		integratedCogHelperId = PlacementHelpers.register(large ? new IntegratedLargeCogHelper() : new IntegratedSmallCogHelper());
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState state = world.getBlockState(pos);

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		PlayerEntity player = context.getPlayer();
		BlockRayTraceResult ray = new BlockRayTraceResult(context.getHitVec(), context.getFace(), pos, true);
		if (helper.matchesState(state) && player != null && !player.isSneaking()) {
			return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
		}

		if (integratedCogHelperId != -1) {
			helper = PlacementHelpers.get(integratedCogHelperId);

			if (helper.matchesState(state) && player != null && !player.isSneaking()) {
				return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
			}
		}

		return super.onItemUseFirst(stack, context);
	}

	@Override
	// Trigger cogwheel criterion
	protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
		triggerShiftingGearsAdvancement(context.getWorld(), context.getPos(), state, context.getPlayer());
		return super.placeBlock(context, state);
	}

	protected void triggerShiftingGearsAdvancement(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (world.isRemote || player == null)
			return;

		Axis axis = state.get(CogWheelBlock.AXIS);
		for (Axis perpendicular1 : Iterate.axes) {
			if (perpendicular1 == axis)
				continue;
			Direction d1 = Direction.getFacingFromAxis(AxisDirection.POSITIVE, perpendicular1);
			for (Axis perpendicular2 : Iterate.axes) {
				if (perpendicular1 == perpendicular2)
					continue;
				if (axis == perpendicular2)
					continue;
				Direction d2 = Direction.getFacingFromAxis(AxisDirection.POSITIVE, perpendicular2);
				for (int offset1 : Iterate.positiveAndNegative) {
					for (int offset2 : Iterate.positiveAndNegative) {
						BlockPos connectedPos = pos.offset(d1, offset1)
								.offset(d2, offset2);
						BlockState blockState = world.getBlockState(connectedPos);
						if (!(blockState.getBlock() instanceof CogWheelBlock))
							continue;
						if (blockState.get(CogWheelBlock.AXIS) != axis)
							continue;
						if (ICogWheel.isLargeCog(blockState) == large)
							continue;
						AllTriggers.triggerFor(AllTriggers.SHIFTING_GEARS, player);
					}
				}
			}
		}
	}

	@MethodsReturnNonnullByDefault
	private static class SmallCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isSmallCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (!((CogWheelBlock) state.getBlock()).isLarge) {
				List<Direction> directions =
						IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS));

				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir);

					if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, state.get(AXIS)))
						continue;

					if (!world.getBlockState(newPos)
							.getMaterial()
							.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));

				}

				return PlacementOffset.fail();
			}

			return super.getOffset(player, world, state, pos, ray);
		}
	}

	@MethodsReturnNonnullByDefault
	private static class LargeCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (((CogWheelBlock) state.getBlock()).isLarge) {
				Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getHitVec(), state.get(AXIS))
						.get(0);
				List<Direction> directions =
						IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS));
				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir)
							.offset(side);

					if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, dir.getAxis()))
						continue;

					if (!world.getBlockState(newPos)
							.getMaterial()
							.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, dir.getAxis()));
				}

				return PlacementOffset.fail();
			}

			return super.getOffset(player, world, state, pos, ray);
		}
	}

	@MethodsReturnNonnullByDefault
	public abstract static class DiagonalCogHelper implements IPlacementHelper {

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof CogWheelBlock;
		}

		@Override
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			// diagonal gears of different size
			Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS))
					.get(0);
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(),
					state.get(AXIS), d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.offset(dir)
						.offset(closest);
				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), world, newPos, state.get(AXIS)))
					continue;

				return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));
			}

			return PlacementOffset.fail();
		}

		protected boolean hitOnShaft(BlockState state, BlockRayTraceResult ray) {
			return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS))
					.getBoundingBox()
					.grow(0.001)
					.contains(ray.getHitVec()
							.subtract(ray.getHitVec()
									.align(Iterate.axisSet)));
		}
	}

	@MethodsReturnNonnullByDefault
	public static class IntegratedLargeCogHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> !ICogWheel.isDedicatedCogWheel(s.getBlock()) && ICogWheel.isSmallCog(s);
		}

		@Override
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			Direction face = ray.getFace();
			Axis newAxis;

			if (state.contains(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.get(HorizontalKineticBlock.HORIZONTAL_FACING)
						.getAxis();
			else if (state.contains(DirectionalKineticBlock.FACING))
				newAxis = state.get(DirectionalKineticBlock.FACING)
						.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions =
					IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), face.getAxis(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.offset(face)
						.offset(d);

				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success(newPos, s -> s.with(CogWheelBlock.AXIS, newAxis));
			}

			return PlacementOffset.fail();
		}

	}

	@MethodsReturnNonnullByDefault
	public static class IntegratedSmallCogHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isSmallCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> !ICogWheel.isDedicatedCogWheel(s.getBlock()) && ICogWheel.isSmallCog(s);
		}

		@Override
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			Direction face = ray.getFace();
			Axis newAxis;

			if (state.contains(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.get(HorizontalKineticBlock.HORIZONTAL_FACING)
						.getAxis();
			else if (state.contains(DirectionalKineticBlock.FACING))
				newAxis = state.get(DirectionalKineticBlock.FACING)
						.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.offset(d);

				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success()
						.at(newPos)
						.withTransform(s -> s.with(CogWheelBlock.AXIS, newAxis));
			}

			return PlacementOffset.fail();
		}

	}
}
