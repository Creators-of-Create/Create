package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	public CogwheelBlockItem(CogWheelBlock block, Properties builder) {
		super(block, builder);
		large = block.isLarge;
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		Direction face = context.getFace();
		BlockPos placedOnPos = context.getPos().offset(face.getOpposite());
		BlockState placedOnState = context.getWorld().getBlockState(placedOnPos);

		if (!(placedOnState.getBlock() instanceof CogWheelBlock))
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

			Vector3d hitVec = context.getHitVec().subtract(VecHelper.getCenterOf(placedOnPos));
			hitVec = hitVec
					.mul(Vector3d.of(Direction.getFacingFromAxis(AxisDirection.POSITIVE, offsetAxis).getDirectionVec()));

			BlockPos correctPos =
				context.getPos().add(Math.signum(hitVec.x), Math.signum(hitVec.y), Math.signum(hitVec.z));

			if (context.getWorld().getBlockState(correctPos).getMaterial().isReplaceable())
				context = BlockItemUseContext.func_221536_a(context, correctPos, largeOnLarge ? face
						: Direction.getFacingFromAxis(AxisDirection.POSITIVE, placedOnState.get(CogWheelBlock.AXIS)));
			else
				return ActionResultType.FAIL;
		}

		return super.tryPlace(context);
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

}
