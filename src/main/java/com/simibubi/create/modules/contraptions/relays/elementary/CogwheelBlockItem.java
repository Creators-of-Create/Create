package com.simibubi.create.modules.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Debug;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	public CogwheelBlockItem(Block block, Properties builder, boolean isLarge) {
		super(block, builder);
		large = isLarge;
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

		boolean placedOnLarge = AllBlocks.LARGE_COGWHEEL.typeOf(placedOnState);
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

			BlockPos correctPos = context.getPos().add(Math.signum(hitVec.x), Math.signum(hitVec.y),
					Math.signum(hitVec.z));

			if (context.getWorld().getBlockState(correctPos).getMaterial().isReplaceable())
				context = BlockItemUseContext.func_221536_a(context, correctPos, largeOnLarge ? face
						: Direction.getFacingFromAxis(AxisDirection.POSITIVE, placedOnState.get(CogWheelBlock.AXIS)));
			else
				return ActionResultType.FAIL;
		}

		return super.tryPlace(context);
	}

}
