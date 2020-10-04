package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-22.5f));
		int scale = 22;
		
		BlockState wheel = AllBlocks.CRUSHING_WHEEL.get()
				.getDefaultState()
				.with(BlockStateProperties.AXIS, Axis.X);

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render(matrixStack);
	}

}
