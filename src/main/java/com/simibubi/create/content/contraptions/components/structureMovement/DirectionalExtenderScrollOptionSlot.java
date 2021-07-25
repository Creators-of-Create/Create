package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.BiPredicate;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

	public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
		super(allowedDirections);
	}

	@Override
	protected Vector3d getLocalOffset(BlockState state) {
		return super.getLocalOffset(state)
				.add(Vector3d.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getNormal()).scale(-2 / 16f));
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		if (!getSide().getAxis().isHorizontal())
			MatrixTransformStack.of(ms).rotateY(AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING)) - 90);
		super.rotate(state, ms);
	}
}
