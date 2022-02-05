package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public class ControlsHandler {

	public static Collection<Integer> currentlyPressed = new HashSet<>();

	public static int PACKET_RATE = 5;
	static int packetCooldown;

	static WeakReference<AbstractContraptionEntity> entityRef = new WeakReference<>(null);
	static BlockPos controlsPos;

	public static void controllerClicked(AbstractContraptionEntity entity, BlockPos controllerLocalPos) {
		AbstractContraptionEntity prevEntity = entityRef.get();
		if (prevEntity != null) {
			stopControlling();
			if (prevEntity == entity)
				return;
		}
		if (!entity.startControlling(controllerLocalPos))
			return;
		entityRef = new WeakReference<AbstractContraptionEntity>(entity);
		controlsPos = controllerLocalPos;
		Minecraft.getInstance().player.displayClientMessage(
			Lang.translate("contraption.controls.start_controlling", entity.getContraptionName()), true);
	}

	public static void stopControlling() {
		AbstractContraptionEntity abstractContraptionEntity = entityRef.get();
		if (abstractContraptionEntity != null)
			abstractContraptionEntity.stopControlling(controlsPos);
		ControlsUtil.getControls()
			.forEach(kb -> kb.setDown(ControlsUtil.isActuallyPressed(kb)));
		packetCooldown = 0;
		entityRef = new WeakReference<>(null);
		controlsPos = null;
//		if (!currentlyPressed.isEmpty())
//			AllPackets.channel.sendToServer(new LinkedControllerInputPacket(currentlyPressed, false));
		currentlyPressed.clear();
		Minecraft.getInstance().player.displayClientMessage(Lang.translate("contraption.controls.stop_controlling"),
			true);
	}

	public static void tick() {
		AbstractContraptionEntity entity = entityRef.get();
		if (entity == null)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player.isSpectator()) {
			stopControlling();
			return;
		}
		if (InputConstants.isKeyDown(mc.getWindow()
			.getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
			stopControlling();
			return;
		}
		if (!entity.toGlobalVector(VecHelper.getCenterOf(controlsPos), 1)
			.closerThan(player.position(), 10)) {
			stopControlling();
			return;
		}

		Vector<KeyMapping> controls = ControlsUtil.getControls();
		Collection<Integer> pressedKeys = new HashSet<>();
		for (int i = 0; i < controls.size(); i++) {
			if (ControlsUtil.isActuallyPressed(controls.get(i)))
				pressedKeys.add(i);
		}

		Collection<Integer> newKeys = new HashSet<>(pressedKeys);
		Collection<Integer> releasedKeys = currentlyPressed;
		newKeys.removeAll(releasedKeys);
		releasedKeys.removeAll(pressedKeys);

		// Released Keys
		if (!releasedKeys.isEmpty()) {
//				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
//				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .5f, true);
		}

		// Newly Pressed Keys
		if (!newKeys.isEmpty()) {
			if (newKeys.contains(Integer.valueOf(5))) {
				stopControlling();
				return;
			}

//				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(newKeys, true, lecternPos));
//				packetCooldown = PACKET_RATE;
//				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .75f, true);
		}

		// Keepalive Pressed Keys
		if (packetCooldown == 0) {
//				if (!pressedKeys.isEmpty()) {
//					AllPackets.channel.sendToServer(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
//					packetCooldown = PACKET_RATE;
//				}
		}

		// TODO do this server side
		if (!entity.control(controlsPos, pressedKeys, player)) {
			stopControlling();
			return;
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.setDown(false));
	}

}
