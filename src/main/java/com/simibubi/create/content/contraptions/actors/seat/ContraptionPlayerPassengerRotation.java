package com.simibubi.create.content.contraptions.actors.seat;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ContraptionPlayerPassengerRotation {

	static boolean active;
	static int prevId;
	static float prevYaw;
	static float prevPitch;

	public static void tick() {
		active = AllConfigs.client().rotateWhenSeated.get();
	}

	public static void frame() {
		Player player = Minecraft.getInstance().player;
		if (!active)
			return;
		if (player == null || !player.isPassenger()) {
			prevId = 0;
			return;
		}

		Entity vehicle = player.getVehicle();
		if (!(vehicle instanceof AbstractContraptionEntity contraptionEntity))
			return;

		ContraptionRotationState rotationState = contraptionEntity.getRotationState();

		float yaw = AngleHelper.wrapAngle180((contraptionEntity instanceof CarriageContraptionEntity cce)
			? cce.getViewYRot(AnimationTickHolder.getPartialTicks())
			: rotationState.yRotation);

		float pitch = (contraptionEntity instanceof CarriageContraptionEntity cce)
			? cce.getViewXRot(AnimationTickHolder.getPartialTicks())
			: 0;

		if (prevId != contraptionEntity.getId()) {
			prevId = contraptionEntity.getId();
			prevYaw = yaw;
			prevPitch = pitch;
		}

		float yawDiff = AngleHelper.getShortestAngleDiff(yaw, prevYaw);
		float pitchDiff = AngleHelper.getShortestAngleDiff(pitch, prevPitch);

		prevYaw = yaw;
		prevPitch = pitch;

		float yawRelativeToTrain = Mth.abs(AngleHelper.getShortestAngleDiff(player.getYRot(), -yaw - 90));
		if (yawRelativeToTrain > 120)
			pitchDiff *= -1;
		else if (yawRelativeToTrain > 60)
			pitchDiff *= 0;

		player.setYRot((float) (player.getYRot() + yawDiff));
		player.setXRot((float) (player.getXRot() + pitchDiff));
	}

}
