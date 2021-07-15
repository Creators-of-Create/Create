package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class AnimatedDeployer extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		defaultBlockElement(shaft(Axis.Z))
			.rotateBlock(0, 0, getCurrentAngle())
			.scale(scale)
			.render(matrixStack);

		defaultBlockElement(AllBlocks.DEPLOYER.getDefaultState()
			.setValue(DeployerBlock.FACING, Direction.DOWN)
			.setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
			.scale(scale)
			.render(matrixStack);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

		matrixStack.pushPose();

		matrixStack.translate(0, offset * 17, 0);
		defaultBlockElement(AllBlockPartials.DEPLOYER_POLE)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrixStack);
		defaultBlockElement(AllBlockPartials.DEPLOYER_HAND_HOLDING)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();

		defaultBlockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
