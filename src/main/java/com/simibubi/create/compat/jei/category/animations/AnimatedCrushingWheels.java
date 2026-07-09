package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.joml.Matrix3x2fStack;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	private final BlockState wheel = AllBlocks.CRUSHING_WHEEL.getDefaultState()
			.setValue(BlockStateProperties.AXIS, Direction.Axis.X);

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 22;

		blockElement(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render(graphics, 0, 0, 0);

		blockElement(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
