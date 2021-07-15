package com.simibubi.create.content.curiosities.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ExtendoGripItemRenderer extends CustomRenderedItemModelRenderer<ExtendoGripModel> {

	private static final Vector3d rotationOffset = new Vector3d(0, 1 / 2f, 1 / 2f);
	private static final Vector3d cogRotationOffset = new Vector3d(0, 1 / 16f, 0);

	@Override
	protected void render(ItemStack stack, ExtendoGripModel model, PartialItemModelRenderer renderer, TransformType transformType,
		MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		MatrixStacker stacker = MatrixStacker.of(ms);
		float animation = 0.25f;
		boolean leftHand = transformType == TransformType.FIRST_PERSON_LEFT_HAND;
		boolean rightHand = transformType == TransformType.FIRST_PERSON_RIGHT_HAND;
		if (leftHand || rightHand)
			animation = MathHelper.lerp(AnimationTickHolder.getPartialTicks(),
										ExtendoGripRenderHandler.lastMainHandAnimation,
										ExtendoGripRenderHandler.mainHandAnimation);

		animation = animation * animation * animation;
		float extensionAngle = MathHelper.lerp(animation, 24f, 156f);
		float halfAngle = extensionAngle / 2;
		float oppositeAngle = 180 - extensionAngle;

		// grip
		renderer.renderSolid(model.getOriginalModel(), light);

		// bits
		ms.pushPose();
		ms.translate(0, 1 / 16f, -7 / 16f);
		ms.scale(1, 1, 1 + animation);
		ms.pushPose();
		stacker.rotateX(-halfAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("thin_short"), light);
		stacker.translateBack(rotationOffset);

		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateX(-oppositeAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("wide_long"), light);
		stacker.translateBack(rotationOffset);

		ms.translate(0, 11 / 16f, 0);
		stacker.rotateX(oppositeAngle)
			.translate(rotationOffset);
		ms.translate(0, 0.5f / 16f, 0);
		renderer.renderSolid(model.getPartial("thin_short"), light);
		stacker.translateBack(rotationOffset);

		ms.popPose();
		ms.pushPose();

		stacker.rotateX(-180 + halfAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("wide_short"), light);
		stacker.translateBack(rotationOffset);

		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateX(oppositeAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("thin_long"), light);
		stacker.translateBack(rotationOffset);

		ms.translate(0, 11 / 16f, 0);
		stacker.rotateX(-oppositeAngle)
			.translate(rotationOffset);
		ms.translate(0, 0.5f / 16f, 0);
		renderer.renderSolid(model.getPartial("wide_short"), light);
		stacker.translateBack(rotationOffset);

		// hand
		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateX(180 - halfAngle)
			.rotateY(180);
		ms.translate(0, 0, -4 / 16f);
		ms.scale(1, 1, 1 / (1 + animation));
		renderer.renderSolid((leftHand || rightHand) ? ExtendoGripRenderHandler.pose.get()
			: AllBlockPartials.DEPLOYER_HAND_POINTING.get(), light);
		ms.popPose();

		ms.popPose();

		// cog
		ms.pushPose();
		float angle = AnimationTickHolder.getRenderTime() * -2;
		if (leftHand || rightHand)
			angle += 360 * animation;
		angle %= 360;
		stacker.translate(cogRotationOffset)
			.rotateZ(angle)
			.translateBack(cogRotationOffset);
		renderer.renderSolid(model.getPartial("cog"), light);
		ms.popPose();
	}

}
