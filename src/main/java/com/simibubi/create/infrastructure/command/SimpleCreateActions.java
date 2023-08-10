package com.simibubi.create.infrastructure.command;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.goggles.GoggleConfigScreen;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfig;

public class SimpleCreateActions {

	public static void rainbowDebug(String value) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || "".equals(value))
			return;

		if (value.equals("info")) {
			Component text = Components.literal("Rainbow Debug Utility is currently: ")
					.append(boolToText(AllConfigs.client().rainbowDebug.get()));
			player.displayClientMessage(text, false);
			return;
		}

		AllConfigs.client().rainbowDebug.set(Boolean.parseBoolean(value));
		Component text = boolToText(AllConfigs.client().rainbowDebug.get())
				.append(Components.literal(" Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));
		player.displayClientMessage(text, false);
	}

	public static void overlayReset(String value) {
		AllConfigs.client().overlayOffsetX.set(0);
		AllConfigs.client().overlayOffsetY.set(0);
	}

	public static void overlayScreen(String value) {
		ScreenOpener.open(new GoggleConfigScreen());
	}

	public static void experimentalLighting(String value) {
		ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	public static void fabulousWarning(String value) {
		AllConfigs.client().ignoreFabulousWarning.set(true);
		Minecraft.getInstance().gui.handleChat(ChatType.CHAT,
				Components.literal("Disabled Fabulous graphics warning"),
				Minecraft.getInstance().player.getUUID());
	}

	public static void zoomMultiplier(String value) {
		try {
			float v = Float.parseFloat(value);
			if (v <= 0)
				return;

			CameraDistanceModifier.zoomOut(v);
		} catch (NumberFormatException ignored) {
			Create.LOGGER.debug("Received non-float value {} in zoom packet, ignoring", value);
		}
	}

	public static void camAngleTarget(String value, boolean yaw) {
		try {
			float v = Float.parseFloat(value);

			if (yaw) {
				CameraAngleAnimationService.setYawTarget(v);
			} else {
				CameraAngleAnimationService.setPitchTarget(v);
			}

		} catch (NumberFormatException ignored) {
			Create.LOGGER.debug("Received non-float value {} in camAngle packet, ignoring", value);
		}
	}

	public static void camAngleFunction(String value) {
		CameraAngleAnimationService.Mode mode = CameraAngleAnimationService.Mode.LINEAR;
		String modeString = value;
		float speed = -1;
		String[] split = value.split(":");
		if (split.length > 1) {
			modeString = split[0];
			try {
				speed = Float.parseFloat(split[1]);
			} catch (NumberFormatException ignored) {}
		}
		try {
			mode = CameraAngleAnimationService.Mode.valueOf(modeString);
		} catch (IllegalArgumentException ignored) {}

		CameraAngleAnimationService.setAnimationMode(mode);
		CameraAngleAnimationService.setAnimationSpeed(speed);
	}

	private static MutableComponent boolToText(boolean b) {
		return b ? Components.literal("enabled").withStyle(ChatFormatting.DARK_GREEN)
				: Components.literal("disabled").withStyle(ChatFormatting.RED);
	}

}
