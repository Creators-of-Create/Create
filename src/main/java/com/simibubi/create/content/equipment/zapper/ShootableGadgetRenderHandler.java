package com.simibubi.create.content.equipment.zapper;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderHandEvent;

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
		return Mth.lerp(partialTicks, rightHand ? lastRightHandAnimation : lastLeftHandAnimation,
			rightHand ? rightHandAnimation : leftHandAnimation);
	}

	protected float animationDecay() {
		return 0.8f;
	}

	public void shoot(InteractionHand hand, Vec3 location) {
		LocalPlayer player = Minecraft.getInstance().player;
		boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
		if (rightHand) {
			rightHandAnimation = .2f;
			dontReequipRight = false;
		} else {
			leftHandAnimation = .2f;
			dontReequipLeft = false;
		}
		playSound(hand, location);
	}

	protected abstract void playSound(InteractionHand hand, Vec3 position);

	protected abstract boolean appliesTo(ItemStack stack);

	protected abstract void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt);

	protected abstract void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt);

	public void registerListeners(IEventBus bus) {
		bus.addListener(this::onRenderPlayerHand);
	}

	protected void onRenderPlayerHand(RenderHandEvent event) {
		ItemStack heldItem = event.getItemStack();
		if (!appliesTo(heldItem))
			return;
		// TODO 26.2: Rebuild custom first-person gadget rendering against the new player render-state API.
	}

	public void dontAnimateItem(InteractionHand hand) {
		LocalPlayer player = Minecraft.getInstance().player;
		boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
		dontReequipRight |= rightHand;
		dontReequipLeft |= !rightHand;
	}

}
