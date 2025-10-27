package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem.Ammo;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.IItemDecorator;

public class PotatoCannonItemRenderer extends CustomRenderedItemModelRenderer {
	public static final IItemDecorator DECORATOR = (guiGraphics, font, stack, xOffset, yOffset) -> {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}

		Ammo ammo = PotatoCannonItem.getAmmo(player, stack);
		if (ammo == null || AllItems.POTATO_CANNON.is(ammo.stack())) {
			return false;
		}

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(xOffset, yOffset + 8, 100);
		poseStack.scale(.5f, .5f, .5f);
		guiGraphics.renderItem(ammo.stack(), 0, 0);
		poseStack.popPose();
		return false;
	};

	protected static final PartialModel COG = PartialModel.of(Create.asResource("item/potato_cannon/cog"));

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
						  ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		renderer.render(model.getOriginalModel(), light);
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		float angle = AnimationTickHolder.getRenderTime() * -2.5f;

		if (player != null) {
			boolean inMainHand = player.getMainHandItem() == stack;
			boolean inOffHand = player.getOffhandItem() == stack;

			if (inMainHand || inOffHand) {
				boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
				float speed = CreateClient.POTATO_CANNON_RENDER_HANDLER.getAnimation(inMainHand ^ leftHanded,
					AnimationTickHolder.getPartialTicks());
				angle += 360 * Mth.clamp(speed * 5, 0, 1);
			}
		}

		angle %= 360;
		float offset = .5f / 16;

		ms.pushPose();
		ms.translate(0, offset, 0);
		ms.mulPose(Axis.ZP.rotationDegrees(angle));
		ms.translate(0, -offset, 0);
		renderer.render(COG.get(), light);
		ms.popPose();
	}
}
