package com.simibubi.create.foundation.command;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.goggles.GoggleConfigScreen;
import com.simibubi.create.content.logistics.trains.CameraDistanceModifier;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfig;

public class SimpleCreateActions {

	public static void rainbowDebug(String value) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || "".equals(value))
			return;

		if (value.equals("info")) {
			Component text = new TextComponent("Rainbow Debug Utility is currently: ")
					.append(boolToText(AllConfigs.CLIENT.rainbowDebug.get()));
			player.displayClientMessage(text, false);
			return;
		}

		AllConfigs.CLIENT.rainbowDebug.set(Boolean.parseBoolean(value));
		Component text = boolToText(AllConfigs.CLIENT.rainbowDebug.get())
				.append(new TextComponent(" Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));
		player.displayClientMessage(text, false);
	}

	public static void overlayReset(String value) {
		AllConfigs.CLIENT.overlayOffsetX.set(0);
		AllConfigs.CLIENT.overlayOffsetY.set(0);
	}

	public static void overlayScreen(String value) {
		ScreenOpener.open(new GoggleConfigScreen());
	}

	public static void experimentalLighting(String value) {
		ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	public static void fabulousWarning(String value) {
		AllConfigs.CLIENT.ignoreFabulousWarning.set(true);
		Minecraft.getInstance().gui.handleChat(ChatType.CHAT,
				new TextComponent("Disabled Fabulous graphics warning"),
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
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN)
				: new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}

}
