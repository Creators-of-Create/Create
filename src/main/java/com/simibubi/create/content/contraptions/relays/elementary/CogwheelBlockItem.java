package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

import static com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock.AXIS;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	private final int placementHelperId;

	public CogwheelBlockItem(CogWheelBlock block, Properties builder) {
		super(block, builder);
		large = block.isLarge;

		placementHelperId = PlacementHelpers.register(large ? new LargeCogHelper() : new SmallCogHelper());
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		World world = context.getWorld();
		BlockPos pos = context.getPos().offset(context.getFace().getOpposite());
		BlockState state = world.getBlockState(pos);

		if (!helper.getStatePredicate().test(state))
			return super.tryPlace(context);

		PlacementOffset offset = helper.getOffset(world, state, pos, new BlockRayTraceResult(context.getHitVec(), context.getFace(), pos, true));

		if (!offset.isSuccessful())
			return super.tryPlace(context);

		BlockPos newPos = new BlockPos(offset.getPos());

		if (!world.getBlockState(newPos).getMaterial().isReplaceable())
			return ActionResultType.PASS;

		if (world.isRemote)
			return ActionResultType.SUCCESS;

		world.setBlockState(newPos, offset.getTransform().apply(this.getBlock().getDefaultState()));
		if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
			context.getItem().shrink(1);
		}

		return ActionResultType.SUCCESS;

		/*if (!(placedOnState.getBlock() instanceof CogWheelBlock))
			return super.tryPlace(context);
		if (face.getAxis() == placedOnState.get(CogWheelBlock.AXIS))
			return super.tryPlace(context);

		boolean placedOnLarge = CogWheelBlock.isLargeCog(placedOnState);
		if (placedOnLarge || large) {

			boolean largeOnLarge = placedOnLarge && large;
			Axis offsetAxis = Axis.X;
			for (Axis axis : Axis.values()) {
				if (placedOnState.get(CogWheelBlock.AXIS) == axis)
					continue;
				if (axis == face.getAxis())
					continue;
				offsetAxis = axis;
			}

			if (largeOnLarge)
				offsetAxis = placedOnState.get(CogWheelBlock.AXIS);

			Vec3d hitVec = context.getHitVec().subtract(VecHelper.getCenterOf(placedOnPos));
			hitVec = hitVec
					.mul(new Vec3d(Direction.getFacingFromAxis(AxisDirection.POSITIVE, offsetAxis).getDirectionVec()));

			BlockPos correctPos =
				context.getPos().add(Math.signum(hitVec.x), Math.signum(hitVec.y), Math.signum(hitVec.z));

			if (context.getWorld().getBlockState(correctPos).getMaterial().isReplaceable())
				context = BlockItemUseContext.func_221536_a(context, correctPos, largeOnLarge ? face
						: Direction.getFacingFromAxis(AxisDirection.POSITIVE, placedOnState.get(CogWheelBlock.AXIS)));
			else
				return ActionResultType.FAIL;
		}

		return super.tryPlace(context);*/
	}

	@Override
	// Trigger cogwheel criterion
	protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();

		if (!world.isRemote && player != null) {
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
							BlockPos connectedPos = context.getPos().offset(d1, offset1).offset(d2, offset2);
							BlockState blockState = world.getBlockState(connectedPos);
							if (!(blockState.getBlock() instanceof CogWheelBlock))
								continue;
							if (blockState.get(CogWheelBlock.AXIS) != axis)
								continue;
							if (AllBlocks.LARGE_COGWHEEL.has(blockState) == large)
								continue;
							AllTriggers.triggerFor(AllTriggers.SHIFTING_GEARS, player);
						}
					}
				}
			}
		}
		return super.placeBlock(context, state);
	}

	@MethodsReturnNonnullByDefault
	private static class SmallCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.COGWHEEL::isIn;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (!((CogWheelBlock) state.getBlock()).isLarge) {
				List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS));

				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir);

					if (hasLargeCogwheelNeighbor(world, newPos, state.get(AXIS)))
						continue;

					if (!world.getBlockState(newPos).getMaterial().isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));

				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockRayTraceResult ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, state.get(AXIS)), ((CogWheelBlock) state.getBlock()).isLarge ? 1.5D : 0.75D);
		}
	}

	@MethodsReturnNonnullByDefault
	private static class LargeCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.LARGE_COGWHEEL::isIn;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (((CogWheelBlock) state.getBlock()).isLarge) {
				Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getHitVec(), state.get(AXIS)).get(0);
				List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS));
				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir).offset(side);
					if (!world.getBlockState(newPos).getMaterial().isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, dir.getAxis()));
				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}
	}

	@MethodsReturnNonnullByDefault
	private abstract static class DiagonalCogHelper implements IPlacementHelper {

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof CogWheelBlock;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			//diagonal gears of different size
			Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS)).get(0);
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getHitVec(), state.get(AXIS), d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.offset(dir).offset(closest);
				if (!world.getBlockState(newPos).getMaterial().isReplaceable())
					continue;

				if (AllBlocks.COGWHEEL.has(state) && hasSmallCogwheelNeighbor(world, newPos, state.get(AXIS)))
					continue;

				return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));
			}

			return PlacementOffset.fail();
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockRayTraceResult ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, state.get(AXIS)), 1D);
		}

		protected boolean hitOnShaft(BlockState state, BlockRayTraceResult ray) {
			return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS)).getBoundingBox().grow(0.001).contains(ray.getHitVec().subtract(ray.getHitVec().align(Iterate.axisSet)));
		}

		protected boolean hasLargeCogwheelNeighbor(World world, BlockPos pos, Direction.Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.LARGE_COGWHEEL.has(world.getBlockState(pos.offset(dir))))
					return true;
			}

			return false;
		}

		protected boolean hasSmallCogwheelNeighbor(World world, BlockPos pos, Direction.Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.COGWHEEL.has(world.getBlockState(pos.offset(dir))))
					return true;
			}

			return false;
		}
	}
}
