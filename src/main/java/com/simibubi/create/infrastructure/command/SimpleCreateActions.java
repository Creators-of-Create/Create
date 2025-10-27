package com.simibubi.create.infrastructure.command;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.goggles.GoggleConfigScreen;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.NeoForgeConfig;

public class SimpleCreateActions {

	public static void rainbowDebug(String value) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || "".equals(value))
			return;

		if (value.equals("info")) {
			Component text = Component.literal("Rainbow Debug Utility is currently: ")
				.append(boolToText(KineticDebugger.rainbowDebug));
			player.displayClientMessage(text, false);
			return;
		}

		KineticDebugger.rainbowDebug = Boolean.parseBoolean(value);
		Component text = boolToText(KineticDebugger.rainbowDebug)
				.append(Component.literal(" Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));
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
		NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	public static void fabulousWarning(String value) {

		AllConfigs.client().ignoreFabulousWarning.set(true);
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.displayClientMessage(Component.literal("Disabled Fabulous graphics warning"), false);
		}
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
        if (b) {
            return Component.literal("enabled").withStyle(ChatFormatting.DARK_GREEN);
        } else {
            return Component.literal("disabled").withStyle(ChatFormatting.RED);
        }
	}

}
