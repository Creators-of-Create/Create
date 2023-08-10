package com.simibubi.create.content.kinetics.simpleRelays;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.createmod.catnip.utility.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	private final int placementHelperId;
	private final int integratedCogHelperId;

	public CogwheelBlockItem(CogWheelBlock block, Properties builder) {
		super(block, builder);
		large = block.isLarge;

		placementHelperId = PlacementHelpers.register(large ? new LargeCogHelper() : new SmallCogHelper());
		integratedCogHelperId =
			PlacementHelpers.register(large ? new IntegratedLargeCogHelper() : new IntegratedSmallCogHelper());
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		Player player = context.getPlayer();
		BlockHitResult ray = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, true);
		if (helper.matchesState(state) && player != null && !player.isShiftKeyDown()) {
			return helper.getOffset(player, world, state, pos, ray)
				.placeInWorld(world, this, player, context.getHand(), ray);
		}

		if (integratedCogHelperId != -1) {
			helper = PlacementHelpers.get(integratedCogHelperId);

			if (helper.matchesState(state) && player != null && !player.isShiftKeyDown()) {
				return helper.getOffset(player, world, state, pos, ray)
					.placeInWorld(world, this, player, context.getHand(), ray);
			}
		}

		return super.onItemUseFirst(stack, context);
	}

	@MethodsReturnNonnullByDefault
	private static class SmallCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isSmallCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (!ICogWheel.isLargeCog(state)) {
				Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
				List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);

				for (Direction dir : directions) {
					BlockPos newPos = pos.relative(dir);

					if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, axis))
						continue;

					if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.setValue(AXIS, axis));

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
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (ICogWheel.isLargeCog(state)) {
				Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
				Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), axis)
					.get(0);
				List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);
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
			return s -> ICogWheel.isSmallCog(s) || ICogWheel.isLargeCog(s);
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			// diagonal gears of different size
			Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
			Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis)
				.get(0);
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis,
				d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.relative(dir)
					.relative(closest);
				if (!world.getBlockState(newPos)
					.getMaterial()
					.isReplaceable())
					continue;

				if (!CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), world, newPos, axis))
					continue;

				return PlacementOffset.success(newPos, s -> s.setValue(AXIS, axis));
			}

			return PlacementOffset.fail();
		}

		protected boolean hitOnShaft(BlockState state, BlockHitResult ray) {
			return AllShapes.SIX_VOXEL_POLE.get(((IRotate) state.getBlock()).getRotationAxis(state))
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
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			Direction face = ray.getDirection();
			Axis newAxis;

			if (state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
					.getAxis();
			else if (state.hasProperty(DirectionalKineticBlock.FACING))
				newAxis = state.getValue(DirectionalKineticBlock.FACING)
					.getAxis();
			else if (state.hasProperty(RotatedPillarKineticBlock.AXIS))
				newAxis = state.getValue(RotatedPillarKineticBlock.AXIS);
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
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			Direction face = ray.getDirection();
			Axis newAxis;

			if (state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
					.getAxis();
			else if (state.hasProperty(DirectionalKineticBlock.FACING))
				newAxis = state.getValue(DirectionalKineticBlock.FACING)
					.getAxis();
			else if (state.hasProperty(RotatedPillarKineticBlock.AXIS))
				newAxis = state.getValue(RotatedPillarKineticBlock.AXIS);
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
