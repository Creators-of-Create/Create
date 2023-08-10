package com.simibubi.create.content.contraptions;

import java.util.function.BiPredicate;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;

import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

	public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
		super(allowedDirections);
	}

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		return super.getLocalOffset(state)
				.add(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getNormal()).scale(-2 / 16f));
	}

	@Override
	public void rotate(BlockState state, PoseStack ms) {
		if (!getSide().getAxis().isHorizontal())
			TransformStack.cast(ms)
					.rotateY(AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING)) + 180);
		super.rotate(state, ms);
	}
}
