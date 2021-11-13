package com.simibubi.create.foundation.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.goggles.GoggleConfigScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.content.PonderIndexScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.common.ForgeConfig;
import com.tterrag.registrate.fabric.EnvExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class SConfigureConfigPacket extends SimplePacketBase {

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
	public void handle(Supplier<Context> ctx) {
		ctx.get()
			.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
				if (option.startsWith("SET")) {
					trySetConfig(option.substring(3), value);
					return;
				}

				try {
					Actions.valueOf(option)
						.performAction(value);
				} catch (IllegalArgumentException e) {
					LogManager.getLogger()
						.warn("Received ConfigureConfigPacket with invalid Option: " + option);
				}
			}));

		ctx.get()
			.setPacketHandled(true);
	}

	private static void trySetConfig(String option, String value) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		ConfigHelper.ConfigPath configPath;
		try {
			configPath = ConfigHelper.ConfigPath.parse(option);
		} catch (IllegalArgumentException e) {
			player.displayClientMessage(new TextComponent(e.getMessage()), false);
			return;
		}

		if (configPath.getType() != ModConfig.Type.CLIENT) {
			Create.LOGGER.warn("Received type-mismatched config packet on client");
			return;
		}

		try {
			ConfigHelper.setConfigValue(configPath, value);
			player.displayClientMessage(new TextComponent("Great Success!"), false);
		} catch (ConfigHelper.InvalidValueException e) {
			player.displayClientMessage(new TextComponent("Config could not be set the the specified value!"), false);
		} catch (Exception e) {
			player.displayClientMessage(new TextComponent("Something went wrong while trying to set config value. Check the client logs for more information"), false);
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
		fabulousWarning(() -> Actions::fabulousWarning)

		;

		private final Supplier<Consumer<String>> consumer;

		Actions(Supplier<Consumer<String>> action) {
			this.consumer = action;
		}

		void performAction(String value) {
			consumer.get()
				.accept(value);
		}

		@Environment(EnvType.CLIENT)
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
				player.displayClientMessage(new TextComponent(e.getMessage()), false);
				return;
			}

			try {
				ScreenOpener.open(SubMenuConfigScreen.find(configPath));
			} catch (Exception e) {
				player.displayClientMessage(new TextComponent("Unable to find the specified config"), false);
			}
		}

		@Environment(EnvType.CLIENT)
		private static void rainbowDebug(String value) {
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

		@Environment(EnvType.CLIENT)
		private static void overlayReset(String value) {
			AllConfigs.CLIENT.overlayOffsetX.set(0);
			AllConfigs.CLIENT.overlayOffsetY.set(0);
		}

		@Environment(EnvType.CLIENT)
		private static void overlayScreen(String value) {
			ScreenOpener.open(new GoggleConfigScreen());
		}

		@Environment(EnvType.CLIENT)
		private static void experimentalLighting(String value) {
			ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
			Minecraft.getInstance().levelRenderer.allChanged();
		}

		@Environment(EnvType.CLIENT)
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

		@Environment(EnvType.CLIENT)
		private static void fabulousWarning(String value) {
			AllConfigs.CLIENT.ignoreFabulousWarning.set(true);
			Minecraft.getInstance().gui.handleChat(ChatType.CHAT,
				new TextComponent("Disabled Fabulous graphics warning"),
				Minecraft.getInstance().player.getUUID());
		}

		private static MutableComponent boolToText(boolean b) {
			return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN)
				: new TextComponent("disabled").withStyle(ChatFormatting.RED);
		}
	}
}
