package com.simibubi.create.foundation.config.ui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class BaseConfigScreen extends ConfigScreen {

	private static final DelegatedStencilElement.ElementRenderer DISABLED_RENDERER = (ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.i(Theme.Key.BUTTON_DISABLE, true), Theme.i(Theme.Key.BUTTON_DISABLE, false) | 0x40_000000);

	public static BaseConfigScreen forCreate(Screen parent) {
		return new BaseConfigScreen(parent)
				.withTitles("Client Settings", "World Generation Settings", "Gameplay Settings")
				.withSpecs(AllConfigs.CLIENT.specification, AllConfigs.COMMON.specification, AllConfigs.SERVER.specification);
	}

	BoxWidget clientConfigWidget;
	BoxWidget commonConfigWidget;
	BoxWidget serverConfigWidget;

	ForgeConfigSpec clientSpec;
	ForgeConfigSpec commonSpec;
	ForgeConfigSpec serverSpec;
	String clientTile = "CLIENT CONFIG";
	String commonTile = "COMMON CONFIG";
	String serverTile = "SERVER CONFIG";
	String modID = Create.ID;

	/**
	 * If you are a Create Addon dev and want to make use of the same GUI
	 * for your mod's config, use this Constructor to create a entry point
	 *
	 * @param parent the previously opened screen
	 * @param modID  the modID of your addon/mod
	 */
	public BaseConfigScreen(Screen parent, @Nonnull String modID) {
		this(parent);
		this.modID = modID;
	}

	private BaseConfigScreen(Screen parent) {
		super(parent);
	}

	/**
	 * If you have static references to your Configs or ConfigSpecs (like Create does in {@link AllConfigs}),
	 * please use {@link #withSpecs(ForgeConfigSpec, ForgeConfigSpec, ForgeConfigSpec)} instead
	 */
	public BaseConfigScreen searchForSpecsInModContainer() {
		try {
			clientSpec = ConfigHelper.findConfigSpecFor(ModConfig.Type.CLIENT, this.modID);
		} catch (Exception e) {
			Create.LOGGER.warn("Unable to find ClientConfigSpec for mod: " + this.modID);
		}

		try {
			commonSpec = ConfigHelper.findConfigSpecFor(ModConfig.Type.COMMON, this.modID);
		} catch (Exception e) {
			Create.LOGGER.warn("Unable to find CommonConfigSpec for mod: " + this.modID, e);
		}

		try {
			serverSpec = ConfigHelper.findConfigSpecFor(ModConfig.Type.SERVER, this.modID);
		} catch (Exception e) {
			Create.LOGGER.warn("Unable to find ServerConfigSpec for mod: " + this.modID, e);
		}

		return this;
	}

	public BaseConfigScreen withSpecs(@Nullable ForgeConfigSpec client, @Nullable ForgeConfigSpec common, @Nullable ForgeConfigSpec server) {
		clientSpec = client;
		commonSpec = common;
		serverSpec = server;
		return this;
	}

	public BaseConfigScreen withTitles(@Nullable String client, @Nullable String common, @Nullable String server) {
		if (client != null)
			clientTile = client;

		if (common != null)
			commonTile = common;

		if (server != null)
			serverTile = server;

		return this;
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		TextStencilElement clientText = new TextStencilElement(client.fontRenderer, new StringTextComponent(clientTile).formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(clientConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 - 30, 200, 16).showingElement(clientText));

		if (clientSpec != null) {
			clientConfigWidget.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.CLIENT, clientSpec)));
			clientText.withElementRenderer(BoxWidget.gradientFactory.apply(clientConfigWidget));
		} else {
			clientConfigWidget.active = false;
			clientConfigWidget.updateColorsFromState();
			clientText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement commonText = new TextStencilElement(client.fontRenderer, new StringTextComponent(commonTile).formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(commonConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15, 200, 16).showingElement(commonText));

		if (commonSpec != null) {
			commonConfigWidget.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.COMMON, commonSpec)));
			commonText.withElementRenderer(BoxWidget.gradientFactory.apply(commonConfigWidget));
		} else {
			commonConfigWidget.active = false;
			commonConfigWidget.updateColorsFromState();
			commonText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement serverText = new TextStencilElement(client.fontRenderer, new StringTextComponent(serverTile).formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(serverConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 + 30, 200, 16).showingElement(serverText));

		if (serverSpec != null && Minecraft.getInstance().world != null) {
			serverConfigWidget.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.SERVER, serverSpec)));
			serverText.withElementRenderer(BoxWidget.gradientFactory.apply(serverConfigWidget));
		} else {
			serverConfigWidget.active = false;
			serverConfigWidget.updateColorsFromState();
			serverText.withElementRenderer(DISABLED_RENDERER);
		}

		ConfigScreen.modID = this.modID;
	}
}
