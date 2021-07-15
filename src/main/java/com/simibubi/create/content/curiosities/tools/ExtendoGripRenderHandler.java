package com.simibubi.create.content.curiosities.tools;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ExtendoGripRenderHandler {

	public static float mainHandAnimation;
	public static float lastMainHandAnimation;
	public static PartialModel pose = AllBlockPartials.DEPLOYER_HAND_PUNCHING;

	public static void tick() {
		lastMainHandAnimation = mainHandAnimation;
		mainHandAnimation *= MathHelper.clamp(mainHandAnimation, 0.8f, 0.99f);

		pose = AllBlockPartials.DEPLOYER_HAND_PUNCHING;
		if (!AllItems.EXTENDO_GRIP.isIn(getRenderedOffHandStack()))
			return;
		ItemStack main = getRenderedMainHandStack();
		if (main.isEmpty())
			return;
		if (!(main.getItem() instanceof BlockItem))
			return;
		if (!Minecraft.getInstance()
			.getItemRenderer()
			.getModel(main, null, null)
			.isGui3d())
			return;
		pose = AllBlockPartials.DEPLOYER_HAND_HOLDING;
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		ItemStack heldItem = event.getItemStack();
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		boolean rightHand = event.getHand() == Hand.MAIN_HAND ^ player.getMainArm() == HandSide.LEFT;

		ItemStack offhandItem = getRenderedOffHandStack();
		boolean notInOffhand = !AllItems.EXTENDO_GRIP.isIn(offhandItem);
		if (notInOffhand && !AllItems.EXTENDO_GRIP.isIn(heldItem))
			return;

		MatrixStack ms = event.getMatrixStack();
		MatrixStacker msr = MatrixStacker.of(ms);
		AbstractClientPlayerEntity abstractclientplayerentity = mc.player;
		mc.getTextureManager()
			.bind(abstractclientplayerentity.getSkinTextureLocation());

		float flip = rightHand ? 1.0F : -1.0F;
		float swingProgress = event.getSwingProgress();
		boolean blockItem = heldItem.getItem() instanceof BlockItem;
		float equipProgress = blockItem ? 0 : event.getEquipProgress() / 4;

		ms.pushPose();
		if (event.getHand() == Hand.MAIN_HAND) {

			if (1 - swingProgress > mainHandAnimation && swingProgress > 0)
				mainHandAnimation = 0.95f;
			float animation = MathHelper.lerp(AnimationTickHolder.getPartialTicks(),
											  ExtendoGripRenderHandler.lastMainHandAnimation,
											  ExtendoGripRenderHandler.mainHandAnimation);
			animation = animation * animation * animation;

			ms.translate(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

			ms.pushPose();
			msr.rotateY(flip * 75.0F);
			ms.translate(flip * -1.0F, 3.6F, 3.5F);
			msr.rotateZ(flip * 120)
				.rotateX(200)
				.rotateY(flip * -135.0F);
			ms.translate(flip * 5.6F, 0.0F, 0.0F);
			msr.rotateY(flip * 40.0F);
			ms.translate(flip * 0.05f, -0.3f, -0.3f);

			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher()
				.getRenderer(player);
			if (rightHand)
				playerrenderer.renderRightHand(event.getMatrixStack(), event.getBuffers(), event.getLight(), player);
			else
				playerrenderer.renderLeftHand(event.getMatrixStack(), event.getBuffers(), event.getLight(), player);
			ms.popPose();

			// Render gun
			ms.pushPose();
			ms.translate(flip * -0.1f, 0, -0.3f);
			FirstPersonRenderer firstPersonRenderer = mc.getItemInHandRenderer();
			TransformType transform =
				rightHand ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
			firstPersonRenderer.renderItem(mc.player, notInOffhand ? heldItem : offhandItem, transform, !rightHand,
				event.getMatrixStack(), event.getBuffers(), event.getLight());

			if (!notInOffhand) {
				ForgeHooksClient.handleCameraTransforms(ms, mc.getItemRenderer()
					.getModel(offhandItem, null, null), transform, !rightHand);
				ms.translate(flip * -.05f, .15f, -1.2f);
				ms.translate(0, 0, -animation * 2.25f);
				if (blockItem && mc.getItemRenderer()
					.getModel(heldItem, null, null)
					.isGui3d()) {
					msr.rotateY(flip * 45);
					ms.translate(flip * 0.15f, -0.15f, -.05f);
					ms.scale(1.25f, 1.25f, 1.25f);
				}

				firstPersonRenderer.renderItem(mc.player, heldItem, transform, !rightHand, event.getMatrixStack(),
					event.getBuffers(), event.getLight());
			}

			ms.popPose();
		}
		ms.popPose();
		event.setCanceled(true);
	}

	private static ItemStack getRenderedMainHandStack() {
		return Minecraft.getInstance().getItemInHandRenderer().mainHandItem;
	}

	private static ItemStack getRenderedOffHandStack() {
		return Minecraft.getInstance().getItemInHandRenderer().offHandItem;
	}

}
