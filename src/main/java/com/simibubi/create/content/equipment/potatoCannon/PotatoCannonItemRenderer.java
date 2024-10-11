package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PotatoCannonItemRenderer extends CustomRenderedItemModelRenderer {

	protected static final PartialModel COG = PartialModel.of(Create.asResource("item/potato_cannon/cog"));

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
		ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		renderer.render(model.getOriginalModel(), light);
		LocalPlayer player = mc.player;
		boolean mainHand = player.getMainHandItem() == stack;
		boolean offHand = player.getOffhandItem() == stack;
		boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;

		float offset = .5f / 16;
		float worldTime = AnimationTickHolder.getRenderTime() / 10;
		float angle = worldTime * -25;
		float speed = CreateClient.POTATO_CANNON_RENDER_HANDLER.getAnimation(mainHand ^ leftHanded,
			AnimationTickHolder.getPartialTicks());

		if (mainHand || offHand)
			angle += 360 * Mth.clamp(speed * 5, 0, 1);
		angle %= 360;

		ms.pushPose();
		ms.translate(0, offset, 0);
		ms.mulPose(Axis.ZP.rotationDegrees(angle));
		ms.translate(0, -offset, 0);
		renderer.render(COG.get(), light);
		ms.popPose();

		if (transformType == ItemDisplayContext.GUI) {
			PotatoCannonItem.getAmmoforPreview(stack)
				.ifPresent(ammo -> {
					PoseStack localMs = new PoseStack();
					localMs.translate(-1 / 4f, -1 / 4f, 1);
					localMs.scale(.5f, .5f, .5f);
					TransformStack.of(localMs)
						.rotateYDegrees(-34);
					itemRenderer.renderStatic(ammo, ItemDisplayContext.GUI, light, OverlayTexture.NO_OVERLAY, localMs,
						buffer, mc.level, 0);
				});
		}

	}

}
