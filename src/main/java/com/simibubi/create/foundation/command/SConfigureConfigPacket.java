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
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.OptifineHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.NetworkEvent;

public class SConfigureConfigPacket extends SimplePacketBase {

	private final String option;
	private final String value;

	public SConfigureConfigPacket(String option, String value) {
		this.option = option;
		this.value = value;
	}

	public SConfigureConfigPacket(PacketBuffer buffer) {
		this.option = buffer.readString(32767);
		this.value = buffer.readString(32767);
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeString(option);
		buffer.writeString(value);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
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
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;

		ConfigHelper.ConfigPath configPath;
		try {
			configPath = ConfigHelper.ConfigPath.parse(option);
		} catch (IllegalArgumentException e) {
			player.sendStatusMessage(new StringTextComponent(e.getMessage()), false);
			return;
		}

		if (configPath.getType() != ModConfig.Type.CLIENT) {
			Create.LOGGER.warn("Received type-mismatched config packet on client");
			return;
		}

		try {
			ConfigHelper.setConfigValue(configPath, value);
			player.sendStatusMessage(new StringTextComponent("Great Success!"), false);
		} catch (ConfigHelper.InvalidValueException e) {
			player.sendStatusMessage(new StringTextComponent("Config could not be set the the specified value!"), false);
		} catch (Exception e) {
			player.sendStatusMessage(new StringTextComponent("Something went wrong while trying to set config value. Check the client logs for more information"), false);
			Create.LOGGER.warn("Exception during client-side config value set:", e);
		}

	}

	public enum Actions {
		configScreen(() -> Actions::configScreen),
		rainbowDebug(() -> Actions::rainbowDebug),
		overlayScreen(() -> Actions::overlayScreen),
		fixLighting(() -> Actions::experimentalLighting),
		overlayReset(() -> Actions::overlayReset),
		experimentalRendering(() -> Actions::experimentalRendering),
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

		@OnlyIn(Dist.CLIENT)
		private static void configScreen(String value) {
			if (value.equals("")) {
				ScreenOpener.open(BaseConfigScreen.forCreate(null));
				return;
			}

			ClientPlayerEntity player = Minecraft.getInstance().player;
			ConfigHelper.ConfigPath configPath;
			try {
				 configPath = ConfigHelper.ConfigPath.parse(value);
			} catch (IllegalArgumentException e) {
				player.sendStatusMessage(new StringTextComponent(e.getMessage()), false);
				return;
			}

			try {
				ScreenOpener.open(SubMenuConfigScreen.find(configPath));
			} catch (Exception e) {
				player.sendStatusMessage(new StringTextComponent("Unable to find the specified config"), false);
			}
		}

		@OnlyIn(Dist.CLIENT)
		private static void rainbowDebug(String value) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player == null || "".equals(value))
				return;

			if (value.equals("info")) {
				ITextComponent text = new StringTextComponent("Rainbow Debug Utility is currently: ")
					.append(boolToText(AllConfigs.CLIENT.rainbowDebug.get()));
				player.sendStatusMessage(text, false);
				return;
			}

			AllConfigs.CLIENT.rainbowDebug.set(Boolean.parseBoolean(value));
			ITextComponent text = boolToText(AllConfigs.CLIENT.rainbowDebug.get())
				.append(new StringTextComponent(" Rainbow Debug Utility").formatted(TextFormatting.WHITE));
			player.sendStatusMessage(text, false);
		}

		@OnlyIn(Dist.CLIENT)
		private static void experimentalRendering(String value) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player == null || "".equals(value))
				return;

			if (value.equals("info")) {
				ITextComponent text = new StringTextComponent("Experimental Rendering is currently: ")
					.append(boolToText(AllConfigs.CLIENT.experimentalRendering.get()));
				player.sendStatusMessage(text, false);
				return;
			}

			boolean parsedBoolean = Boolean.parseBoolean(value);
			boolean cannotUseER = OptifineHandler.usingShaders() && parsedBoolean;

			AllConfigs.CLIENT.experimentalRendering.set(parsedBoolean);

			ITextComponent text = boolToText(AllConfigs.CLIENT.experimentalRendering.get())
				.append(new StringTextComponent(" Experimental Rendering").formatted(TextFormatting.WHITE));
			ITextComponent error = new StringTextComponent("Experimental Rendering does not support Optifine Shaders")
				.formatted(TextFormatting.RED);

			player.sendStatusMessage(cannotUseER ? error : text, false);
			FastRenderDispatcher.refresh();
		}

		@OnlyIn(Dist.CLIENT)
		private static void overlayReset(String value) {
			AllConfigs.CLIENT.overlayOffsetX.set(0);
			AllConfigs.CLIENT.overlayOffsetY.set(0);
		}

		@OnlyIn(Dist.CLIENT)
		private static void overlayScreen(String value) {
			ScreenOpener.open(new GoggleConfigScreen());
		}

		@OnlyIn(Dist.CLIENT)
		private static void experimentalLighting(String value) {
			ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
			Minecraft.getInstance().worldRenderer.loadRenderers();
		}

		@OnlyIn(Dist.CLIENT)
		private static void openPonder(String value) {
			if (value.equals("index")) {
				ScreenOpener.transitionTo(new PonderIndexScreen());
				return;
			}

			ResourceLocation id = new ResourceLocation(value);
			if (!PonderRegistry.all.containsKey(id)) {
				Create.LOGGER.error("Could not find ponder scenes for item: " + id);
				return;
			}

			ScreenOpener.transitionTo(PonderUI.of(id));

		}

		@OnlyIn(Dist.CLIENT)
		private static void fabulousWarning(String value) {
			AllConfigs.CLIENT.ignoreFabulousWarning.set(true);
			Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.CHAT,
				new StringTextComponent("Disabled Fabulous graphics warning"),
				Minecraft.getInstance().player.getUniqueID());
		}

		private static IFormattableTextComponent boolToText(boolean b) {
			return b ? new StringTextComponent("enabled").formatted(TextFormatting.DARK_GREEN)
				: new StringTextComponent("disabled").formatted(TextFormatting.RED);
		}
	}
}
