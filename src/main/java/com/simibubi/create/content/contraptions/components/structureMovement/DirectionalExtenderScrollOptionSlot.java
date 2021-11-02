package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.BiPredicate;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

	public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
		super(allowedDirections);
	}

	@Override
	protected Vec3 getLocalOffset(BlockState state) {
		return super.getLocalOffset(state)
				.add(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getNormal()).scale(-2 / 16f));
	}

	@Override
	protected void rotate(BlockState state, PoseStack ms) {
		if (!getSide().getAxis().isHorizontal())
			MatrixTransformStack.of(ms).rotateY(AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING)) - 90);
		super.rotate(state, ms);
	}
}
