package com.simibubi.create.foundation.render;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class PlayerSkyhookRenderer {

	private static final Set<UUID> hangingPlayers = new HashSet<>();

	public static void updatePlayerList(Collection<UUID> uuids) {
		hangingPlayers.clear();
		hangingPlayers.addAll(uuids);
	}

	public static void beforeSetupAnim(Player player, HumanoidModel<?> model) {
		if (hangingPlayers.contains(player.getUUID()))
			return;

		model.head.resetPose();
		model.hat.resetPose();
		model.body.resetPose();
		model.leftArm.resetPose();
		model.rightArm.resetPose();
		model.leftLeg.resetPose();
		model.rightLeg.resetPose();
	}

	public static void afterSetupAnim(Player player, HumanoidModel<?> model) {
		if (hangingPlayers.contains(player.getUUID()))
			setHangingPose(model);
	}

	private static void setHangingPose(HumanoidModel<?> model) {
		if (Minecraft.getInstance().isPaused())
			return;

		model.head.x = 0;
		model.hat.x = 0;
		model.body.resetPose();
		model.leftArm.resetPose();
		model.rightArm.resetPose();
		model.leftLeg.resetPose();
		model.rightLeg.resetPose();

		float time = AnimationTickHolder.getTicks(true) + AnimationTickHolder.getPartialTicks();
		float mainCycle = Mth.sin(((float) ((time + 10) * 0.3f / Math.PI)));
		float limbCycle = Mth.sin(((float) (time * 0.3f / Math.PI)));
		float bodySwing = AngleHelper.rad(15 + (mainCycle * 10));
		float limbSwing = AngleHelper.rad(limbCycle * 15);

		model.body.zRot = bodySwing;
		model.head.zRot = bodySwing;
		model.hat.zRot = bodySwing;

		model.rightArm.y -= 3;

		float offsetX = model.rightArm.x;
		float offsetY = model.rightArm.y;
//		model.rightArm.x = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing);
//		model.rightArm.y = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing);
		float armPivotX = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing) + 4.5f;
		float armPivotY = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing) + 2;
		model.rightArm.xRot = -AngleHelper.rad(150);
		model.rightArm.zRot = AngleHelper.rad(15);

		offsetX = model.leftArm.x;
		offsetY = model.leftArm.y;
		model.leftArm.x = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing);
		model.leftArm.y = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing);
		model.leftArm.zRot = -AngleHelper.rad(20) + 0.5f * bodySwing + limbSwing;

		model.rightLeg.y -= 0.2f;
		offsetX = model.rightLeg.x;
		offsetY = model.rightLeg.y;
		model.rightLeg.x = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing);
		model.rightLeg.y = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing);
		model.rightLeg.xRot = -AngleHelper.rad(25);
		model.rightLeg.zRot = AngleHelper.rad(10) + 0.5f * bodySwing + limbSwing;

		model.leftLeg.y -= 0.8f;
		offsetX = model.leftLeg.x;
		offsetY = model.leftLeg.y;
		model.leftLeg.x = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing);
		model.leftLeg.y = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing);
		model.leftLeg.xRot = AngleHelper.rad(10);
		model.leftLeg.zRot = -AngleHelper.rad(10) + 0.5f * bodySwing + limbSwing;

		model.hat.x -= armPivotX;
		model.head.x -= armPivotX;
		model.body.x -= armPivotX;
		model.leftArm.x -= armPivotX;
		model.leftLeg.x -= armPivotX;
		model.rightLeg.x -= armPivotX;

		model.hat.y -= armPivotY;
		model.head.y -= armPivotY;
		model.body.y -= armPivotY;
		model.leftArm.y -= armPivotY;
		model.leftLeg.y -= armPivotY;
		model.rightLeg.y -= armPivotY;
	}

}
