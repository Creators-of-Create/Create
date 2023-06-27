package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;

public class AnimatedDeployer extends AnimatedKinetics {

	@Override
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(shaft(Direction.Axis.Z))
			.rotateBlock(0, 0, getCurrentAngle())
			.scale(scale)
			.render(matrixStack);

		blockElement(AllBlocks.DEPLOYER.getDefaultState()
			.setValue(DeployerBlock.FACING, Direction.DOWN)
			.setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
			.scale(scale)
			.render(matrixStack);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

		matrixStack.pushPose();

		matrixStack.translate(0, offset * 17, 0);
		blockElement(AllPartialModels.DEPLOYER_POLE)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrixStack);
		blockElement(AllPartialModels.DEPLOYER_HAND_HOLDING)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
