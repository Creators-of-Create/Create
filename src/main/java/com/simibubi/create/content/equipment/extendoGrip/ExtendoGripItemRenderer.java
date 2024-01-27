package com.simibubi.create.content.equipment.extendoGrip;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ExtendoGripItemRenderer extends CustomRenderedItemModelRenderer {

	protected static final PartialModel COG = new PartialModel(Create.asResource("item/extendo_grip/cog"));
	protected static final PartialModel THIN_SHORT = new PartialModel(Create.asResource("item/extendo_grip/thin_short"));
	protected static final PartialModel WIDE_SHORT = new PartialModel(Create.asResource("item/extendo_grip/wide_short"));
	protected static final PartialModel THIN_LONG = new PartialModel(Create.asResource("item/extendo_grip/thin_long"));
	protected static final PartialModel WIDE_LONG = new PartialModel(Create.asResource("item/extendo_grip/wide_long"));

	private static final Vec3 ROTATION_OFFSET = new Vec3(0, 1 / 2f, 1 / 2f);
	private static final Vec3 COG_ROTATION_OFFSET = new Vec3(0, 1 / 16f, 0);

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		var stacker = TransformStack.of(ms);
		float animation = 0.25f;
		boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
		boolean rightHand = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
		if (leftHand || rightHand)
			animation = Mth.lerp(AnimationTickHolder.getPartialTicks(),
										ExtendoGripRenderHandler.lastMainHandAnimation,
										ExtendoGripRenderHandler.mainHandAnimation);

		animation = animation * animation * animation;
		float extensionAngle = Mth.lerp(animation, 24f, 156f);
		float halfAngle = extensionAngle / 2;
		float oppositeAngle = 180 - extensionAngle;

		// grip
		renderer.renderSolid(model.getOriginalModel(), light);

		// bits
		ms.pushPose();
		ms.translate(0, 1 / 16f, -7 / 16f);
		ms.scale(1, 1, 1 + animation);
		ms.pushPose();
		stacker.rotateXDegrees(-halfAngle)
			.translate(ROTATION_OFFSET);
		renderer.renderSolid(THIN_SHORT.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateXDegrees(-oppositeAngle)
			.translate(ROTATION_OFFSET);
		renderer.renderSolid(WIDE_LONG.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		ms.translate(0, 11 / 16f, 0);
		stacker.rotateXDegrees(oppositeAngle)
			.translate(ROTATION_OFFSET);
		ms.translate(0, 0.5f / 16f, 0);
		renderer.renderSolid(THIN_SHORT.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		ms.popPose();
		ms.pushPose();

		stacker.rotateXDegrees(-180 + halfAngle)
			.translate(ROTATION_OFFSET);
		renderer.renderSolid(WIDE_SHORT.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateXDegrees(oppositeAngle)
			.translate(ROTATION_OFFSET);
		renderer.renderSolid(THIN_LONG.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		ms.translate(0, 11 / 16f, 0);
		stacker.rotateXDegrees(-oppositeAngle)
			.translate(ROTATION_OFFSET);
		ms.translate(0, 0.5f / 16f, 0);
		renderer.renderSolid(WIDE_SHORT.get(), light);
		stacker.translateBack(ROTATION_OFFSET);

		// hand
		ms.translate(0, 5.5f / 16f, 0);
		stacker.rotateXDegrees(180 - halfAngle)
			.rotateYDegrees(180);
		ms.translate(0, 0, -4 / 16f);
		ms.scale(1, 1, 1 / (1 + animation));
		renderer.renderSolid((leftHand || rightHand) ? ExtendoGripRenderHandler.pose.get()
			: AllPartialModels.DEPLOYER_HAND_POINTING.get(), light);
		ms.popPose();

		ms.popPose();

		// cog
		ms.pushPose();
		float angle = AnimationTickHolder.getRenderTime() * -2;
		if (leftHand || rightHand)
			angle += 360 * animation;
		angle %= 360;
		stacker.translate(COG_ROTATION_OFFSET)
			.rotateZDegrees(angle)
			.translateBack(COG_ROTATION_OFFSET);
		renderer.renderSolid(COG.get(), light);
		ms.popPose();
	}

}
