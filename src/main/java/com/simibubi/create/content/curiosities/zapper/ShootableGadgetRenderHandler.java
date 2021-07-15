package com.simibubi.create.content.curiosities.zapper;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class ShootableGadgetRenderHandler {

	protected float leftHandAnimation;
	protected float rightHandAnimation;
	protected float lastLeftHandAnimation;
	protected float lastRightHandAnimation;
	protected boolean dontReequipLeft;
	protected boolean dontReequipRight;

	public void tick() {
		lastLeftHandAnimation = leftHandAnimation;
		lastRightHandAnimation = rightHandAnimation;
		leftHandAnimation *= animationDecay();
		rightHandAnimation *= animationDecay();
	}

	public float getAnimation(boolean rightHand, float partialTicks) {
		return MathHelper.lerp(partialTicks, rightHand ? lastRightHandAnimation : lastLeftHandAnimation,
			rightHand ? rightHandAnimation : leftHandAnimation);
	}

	protected float animationDecay() {
		return 0.8f;
	}

	public void shoot(Hand hand, Vector3d location) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == HandSide.LEFT;
		if (rightHand) {
			rightHandAnimation = .2f;
			dontReequipRight = false;
		} else {
			leftHandAnimation = .2f;
			dontReequipLeft = false;
		}
		playSound(hand, location);
	}

	protected abstract void playSound(Hand hand, Vector3d position);

	protected abstract boolean appliesTo(ItemStack stack);

	protected abstract void transformTool(MatrixStack ms, float flip, float equipProgress, float recoil, float pt);

	protected abstract void transformHand(MatrixStack ms, float flip, float equipProgress, float recoil, float pt);

	public void register(IEventBus bus) {
		bus.addListener(this::onRenderPlayerHand);
	}

	protected void onRenderPlayerHand(RenderHandEvent event) {
		ItemStack heldItem = event.getItemStack();
		if (!appliesTo(heldItem))
			return;

		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayerEntity player = mc.player;
		TextureManager textureManager = mc.getTextureManager();
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher()
			.getRenderer(player);
		FirstPersonRenderer firstPersonRenderer = mc.getItemInHandRenderer();

		MatrixStack ms = event.getMatrixStack();
		IRenderTypeBuffer buffer = event.getBuffers();
		int light = event.getLight();
		float pt = event.getPartialTicks();

		boolean rightHand = event.getHand() == Hand.MAIN_HAND ^ mc.player.getMainArm() == HandSide.LEFT;
		float recoil = rightHand ? MathHelper.lerp(pt, lastRightHandAnimation, rightHandAnimation)
			: MathHelper.lerp(pt, lastLeftHandAnimation, leftHandAnimation);
		float equipProgress = event.getEquipProgress();

		if (rightHand && (rightHandAnimation > .01f || dontReequipRight))
			equipProgress = 0;
		if (!rightHand && (leftHandAnimation > .01f || dontReequipLeft))
			equipProgress = 0;

		// Render arm
		ms.pushPose();
		textureManager.bind(player.getSkinTextureLocation());

		float flip = rightHand ? 1.0F : -1.0F;
		float f1 = MathHelper.sqrt(event.getSwingProgress());
		float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
		float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
		float f4 = -0.4F * MathHelper.sin(event.getSwingProgress() * (float) Math.PI);
		float f5 = MathHelper.sin(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI);
		float f6 = MathHelper.sin(f1 * (float) Math.PI);

		ms.translate(flip * (f2 + 0.64F - .1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F + .3f + recoil);
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * 75.0F));
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * f6 * 70.0F));
		ms.mulPose(Vector3f.ZP.rotationDegrees(flip * f5 * -20.0F));
		ms.translate(flip * -1.0F, 3.6F, 3.5F);
		ms.mulPose(Vector3f.ZP.rotationDegrees(flip * 120.0F));
		ms.mulPose(Vector3f.XP.rotationDegrees(200.0F));
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * -135.0F));
		ms.translate(flip * 5.6F, 0.0F, 0.0F);
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * 40.0F));
		transformHand(ms, flip, equipProgress, recoil, pt);
		if (rightHand)
			playerrenderer.renderRightHand(ms, buffer, light, player);
		else
			playerrenderer.renderLeftHand(ms, buffer, light, player);
		ms.popPose();

		// Render gadget
		ms.pushPose();
		ms.translate(flip * (f2 + 0.64F - .1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F - 0.1f + recoil);
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * f6 * 70.0F));
		ms.mulPose(Vector3f.ZP.rotationDegrees(flip * f5 * -20.0F));
		transformTool(ms, flip, equipProgress, recoil, pt);
		firstPersonRenderer.renderItem(mc.player, heldItem,
			rightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
				: ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
			!rightHand, ms, buffer, light);
		ms.popPose();

		event.setCanceled(true);
	}

	public void dontAnimateItem(Hand hand) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == HandSide.LEFT;
		dontReequipRight |= rightHand;
		dontReequipLeft |= !rightHand;
	}

}
