package com.simibubi.create.content.curiosities.weapons;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class PotatoCannonItemRenderer extends CustomRenderedItemModelRenderer<PotatoCannonModel> {

	@Override
	protected void render(ItemStack stack, PotatoCannonModel model, PartialItemModelRenderer renderer,
		TransformType transformType, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		renderer.render(model.getOriginalModel(), light);
		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean mainHand = player.getMainHandItem() == stack;
		boolean offHand = player.getOffhandItem() == stack;
		boolean leftHanded = player.getMainArm() == HandSide.LEFT;

		float offset = .5f / 16;
		float worldTime = AnimationTickHolder.getRenderTime() / 10;
		float angle = worldTime * -25;
		float speed = CreateClient.POTATO_CANNON_RENDER_HANDLER.getAnimation(mainHand ^ leftHanded,
			AnimationTickHolder.getPartialTicks());

		if (mainHand || offHand)
			angle += 360 * MathHelper.clamp(speed * 5, 0, 1);
		angle %= 360;

		ms.pushPose();
		ms.translate(0, offset, 0);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle));
		ms.translate(0, -offset, 0);
		renderer.render(model.getPartial("cog"), light);
		ms.popPose();

		if (transformType == TransformType.GUI) {
			PotatoCannonItem.getAmmoforPreview(stack)
				.ifPresent(ammo -> {
					MatrixStack localMs = new MatrixStack();
					localMs.translate(-1 / 4f, -1 / 4f, 1);
					localMs.scale(.5f, .5f, .5f);
					MatrixTransformStack.of(localMs)
						.rotateY(-34);
					itemRenderer.renderStatic(ammo, TransformType.GUI, light, OverlayTexture.NO_OVERLAY, localMs, buffer);
				});
		}

	}

	@Override
	public PotatoCannonModel createModel(IBakedModel originalModel) {
		return new PotatoCannonModel(originalModel);
	}

}
