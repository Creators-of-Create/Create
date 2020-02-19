package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.function.BiPredicate;

import com.simibubi.create.foundation.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

	public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
		super(allowedDirections);
	}

	@Override
	protected Vec3d getLocation(BlockState state) {
		return super.getLocation(state)
				.add(new Vec3d(state.get(BlockStateProperties.FACING).getDirectionVec()).scale(-2 / 16f));
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		Vec3d orientation = super.getOrientation(state);
		if (direction.getAxis().isHorizontal())
			return orientation;
		return orientation.add(0, AngleHelper.horizontalAngle(state.get(BlockStateProperties.FACING)) - 90, 0);
	}
}