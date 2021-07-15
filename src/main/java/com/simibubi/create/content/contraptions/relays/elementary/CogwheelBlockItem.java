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

import net.minecraft.item.Item.Properties;

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
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		PlayerEntity player = context.getPlayer();
		BlockRayTraceResult ray = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), pos, true);
		if (helper.matchesState(state) && player != null && !player.isShiftKeyDown()) {
			return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
		}

		if (integratedCogHelperId != -1) {
			helper = PlacementHelpers.get(integratedCogHelperId);

			if (helper.matchesState(state) && player != null && !player.isShiftKeyDown()) {
				return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
			}
		}

		return super.onItemUseFirst(stack, context);
	}

	@Override
	// Trigger cogwheel criterion
	protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
		triggerShiftingGearsAdvancement(context.getLevel(), context.getClickedPos(), state, context.getPlayer());
		return super.placeBlock(context, state);
	}

	protected void triggerShiftingGearsAdvancement(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (world.isClientSide || player == null)
			return;

		Axis axis = state.getValue(CogWheelBlock.AXIS);
		for (Axis perpendicular1 : Iterate.axes) {
			if (perpendicular1 == axis)
				continue;
			Direction d1 = Direction.get(AxisDirection.POSITIVE, perpendicular1);
			for (Axis perpendicular2 : Iterate.axes) {
				if (perpendicular1 == perpendicular2)
					continue;
				if (axis == perpendicular2)
					continue;
				Direction d2 = Direction.get(AxisDirection.POSITIVE, perpendicular2);
				for (int offset1 : Iterate.positiveAndNegative) {
					for (int offset2 : Iterate.positiveAndNegative) {
						BlockPos connectedPos = pos.relative(d1, offset1)
								.relative(d2, offset2);
						BlockState blockState = world.getBlockState(connectedPos);
						if (!(blockState.getBlock() instanceof CogWheelBlock))
							continue;
						if (blockState.getValue(CogWheelBlock.AXIS) != axis)
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
						IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), state.getValue(AXIS));

				for (Direction dir : directions) {
					BlockPos newPos = pos.relative(dir);

					if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, state.getValue(AXIS)))
						continue;

					if (!world.getBlockState(newPos)
							.getMaterial()
							.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.setValue(AXIS, state.getValue(AXIS)));

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
				Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), state.getValue(AXIS))
						.get(0);
				List<Direction> directions =
						IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), state.getValue(AXIS));
				for (Direction dir : directions) {
					BlockPos newPos = pos.relative(dir)
							.relative(side);

					if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, dir.getAxis()))
						continue;

					if (!world.getBlockState(newPos)
							.getMaterial()
							.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.setValue(AXIS, dir.getAxis()));
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
			Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), state.getValue(AXIS))
					.get(0);
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
					state.getValue(AXIS), d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.relative(dir)
						.relative(closest);
				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), world, newPos, state.getValue(AXIS)))
					continue;

				return PlacementOffset.success(newPos, s -> s.setValue(AXIS, state.getValue(AXIS)));
			}

			return PlacementOffset.fail();
		}

		protected boolean hitOnShaft(BlockState state, BlockRayTraceResult ray) {
			return AllShapes.SIX_VOXEL_POLE.get(state.getValue(AXIS))
					.bounds()
					.inflate(0.001)
					.contains(ray.getLocation()
							.subtract(ray.getLocation()
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
			Direction face = ray.getDirection();
			Axis newAxis;

			if (state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
						.getAxis();
			else if (state.hasProperty(DirectionalKineticBlock.FACING))
				newAxis = state.getValue(DirectionalKineticBlock.FACING)
						.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions =
					IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), face.getAxis(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.relative(face)
						.relative(d);

				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success(newPos, s -> s.setValue(CogWheelBlock.AXIS, newAxis));
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
			Direction face = ray.getDirection();
			Axis newAxis;

			if (state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
						.getAxis();
			else if (state.hasProperty(DirectionalKineticBlock.FACING))
				newAxis = state.getValue(DirectionalKineticBlock.FACING)
						.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.relative(d);

				if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success()
						.at(newPos)
						.withTransform(s -> s.setValue(CogWheelBlock.AXIS, newAxis));
			}

			return PlacementOffset.fail();
		}

	}
}
