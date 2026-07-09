package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;

import org.joml.Matrix3x2fStack;

public class AnimatedDeployer extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 20;

		blockElement(shaft(Direction.Axis.Z))
			.rotateBlock(0, 0, getCurrentAngle())
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.DEPLOYER.getDefaultState()
			.setValue(DeployerBlock.FACING, Direction.DOWN)
			.setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
			.scale(scale)
			.render(graphics, 0, 0, 0);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

		matrixStack.pushMatrix();

		matrixStack.translate(0, offset * 17);
		blockElement(AllPartialModels.DEPLOYER_POLE)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		blockElement(AllPartialModels.DEPLOYER_HAND_HOLDING)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
