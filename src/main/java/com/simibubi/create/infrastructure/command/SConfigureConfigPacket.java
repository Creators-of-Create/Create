package com.simibubi.create.infrastructure.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.goggles.GoggleConfigScreen;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.ui.PonderIndexScreen;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent.Context;

public class SConfigureConfigPacket extends SimplePacketBase {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final String option;
	private final String value;

	public SConfigureConfigPacket(String option, String value) {
		this.option = option;
		this.value = value;
	}

	public SConfigureConfigPacket(FriendlyByteBuf buffer) {
		this.option = buffer.readUtf(32767);
		this.value = buffer.readUtf(32767);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(option);
		buffer.writeUtf(value);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			if (option.startsWith("SET")) {
				trySetConfig(option.substring(3), value);
				return;
			}

			try {
				Actions.valueOf(option)
					.performAction(value);
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Received ConfigureConfigPacket with invalid Option: " + option);
			}
		}));
		return true;
	}

	private static void trySetConfig(String option, String value) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		ConfigHelper.ConfigPath configPath;
		try {
			configPath = ConfigHelper.ConfigPath.parse(option);
		} catch (IllegalArgumentException e) {
			player.displayClientMessage(Components.literal(e.getMessage()), false);
			return;
		}

		if (configPath.getType() != ModConfig.Type.CLIENT) {
			Create.LOGGER.warn("Received type-mismatched config packet on client");
			return;
		}

		try {
			ConfigHelper.setConfigValue(configPath, value);
			player.displayClientMessage(Components.literal("Great Success!"), false);
		} catch (ConfigHelper.InvalidValueException e) {
			player.displayClientMessage(Components.literal("Config could not be set the the specified value!"), false);
		} catch (Exception e) {
			player.displayClientMessage(Components.literal("Something went wrong while trying to set config value. Check the client logs for more information"), false);
			Create.LOGGER.warn("Exception during client-side config value set:", e);
		}

	}

	public enum Actions {
		configScreen(() -> Actions::configScreen),
		rainbowDebug(() -> Actions::rainbowDebug),
		overlayScreen(() -> Actions::overlayScreen),
		fixLighting(() -> Actions::experimentalLighting),
		overlayReset(() -> Actions::overlayReset),
		openPonder(() -> Actions::openPonder),
		fabulousWarning(() -> Actions::fabulousWarning),
		zoomMultiplier(() -> Actions::zoomMultiplier),
		camAngleYawTarget(() -> value -> camAngleTarget(value, true)),
		camAnglePitchTarget(() -> value -> camAngleTarget(value, false)),
		camAngleFunction(() -> Actions::camAngleFunction)

		;

		private final Supplier<Consumer<String>> consumer;

		Actions(Supplier<Consumer<String>> action) {
			this.consumer = action;
		}

		void performAction(String value) {
			consumer.get()
				.accept(value);
		}

		@OnlyIn(Dist.CLIENT)
		private static void configScreen(String value) {
			if (value.equals("")) {
				ScreenOpener.open(BaseConfigScreen.forCreate(null));
				return;
			}

			LocalPlayer player = Minecraft.getInstance().player;
			ConfigHelper.ConfigPath configPath;
			try {
				 configPath = ConfigHelper.ConfigPath.parse(value);
			} catch (IllegalArgumentException e) {
				player.displayClientMessage(Components.literal(e.getMessage()), false);
				return;
			}

			try {
				ScreenOpener.open(SubMenuConfigScreen.find(configPath));
			} catch (Exception e) {
				player.displayClientMessage(Components.literal("Unable to find the specified config"), false);
			}
		}

		@OnlyIn(Dist.CLIENT)
		private static void rainbowDebug(String value) {
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

		@OnlyIn(Dist.CLIENT)
		private static void overlayReset(String value) {
			AllConfigs.client().overlayOffsetX.set(0);
			AllConfigs.client().overlayOffsetY.set(0);
		}

		@OnlyIn(Dist.CLIENT)
		private static void overlayScreen(String value) {
			ScreenOpener.open(new GoggleConfigScreen());
		}

		@OnlyIn(Dist.CLIENT)
		private static void experimentalLighting(String value) {
			ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
			Minecraft.getInstance().levelRenderer.allChanged();
		}

		@OnlyIn(Dist.CLIENT)
		private static void openPonder(String value) {
			if (value.equals("index")) {
				ScreenOpener.transitionTo(new PonderIndexScreen());
				return;
			}

			ResourceLocation id = new ResourceLocation(value);
			if (!PonderRegistry.ALL.containsKey(id)) {
				Create.LOGGER.error("Could not find ponder scenes for item: " + id);
				return;
			}

			ScreenOpener.transitionTo(PonderUI.of(id));

		}

		@OnlyIn(Dist.CLIENT)
		private static void fabulousWarning(String value) {
			AllConfigs.client().ignoreFabulousWarning.set(true);
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				player.displayClientMessage(
					Components.literal("Disabled Fabulous graphics warning"), false);
			}
		}

		@OnlyIn(Dist.CLIENT)
		private static void zoomMultiplier(String value) {
			try {
				float v = Float.parseFloat(value);
				if (v <= 0)
					return;

				CameraDistanceModifier.zoomOut(v);
			} catch (NumberFormatException ignored) {
				Create.LOGGER.debug("Received non-float value {} in zoom packet, ignoring", value);
			}
		}

		@OnlyIn(Dist.CLIENT)
		private static void camAngleTarget(String value, boolean yaw) {
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

		@OnlyIn(Dist.CLIENT)
		private static void camAngleFunction(String value) {
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
}
