package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlocks;

import me.shedaniel.math.Rectangle;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	private final BlockState wheel = AllBlocks.CRUSHING_WHEEL.getDefaultState()
			.setValue(BlockStateProperties.AXIS, Axis.X);

	@Override
	public void render(PoseStack matrixStack, Rectangle rectangle, int mouseX, int mouseY, float delta) {
		matrixStack.pushPose();
		matrixStack.translate(mouseX, mouseY, 100);
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(-22.5f));
		int scale = 22;

		blockElement(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		blockElement(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render(matrixStack);

		matrixStack.popPose();
	}

	@Override
	public int getZ() {
		return 0;
	}

	@Override
	public void setZ(int i) {

	}

}
