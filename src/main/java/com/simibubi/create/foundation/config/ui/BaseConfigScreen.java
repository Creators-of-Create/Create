package com.simibubi.create.foundation.config.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.element.TextStencilElement;
import com.simibubi.create.foundation.gui.widget.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class BaseConfigScreen extends ConfigScreen {

	public static final DelegatedStencilElement.ElementRenderer DISABLED_RENDERER = (ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.p(Theme.Key.BUTTON_DISABLE));
	private static final Map<String, UnaryOperator<BaseConfigScreen>> DEFAULTS = new HashMap<>();

	static {
		DEFAULTS.put(Create.ID, (base) -> base
				.withTitles("Client Settings", "World Generation Settings", "Gameplay Settings")
				.withSpecs(AllConfigs.CLIENT.specification, AllConfigs.COMMON.specification, AllConfigs.SERVER.specification)
		);
	}

	/**
	 * If you are a Create Addon dev and want to change the config labels,
	 * add a default action here.
	 *
	 * Make sure you call either {@link #withSpecs(ForgeConfigSpec, ForgeConfigSpec, ForgeConfigSpec)}
	 * or {@link #searchForSpecsInModContainer()}
	 *
	 * @param modID     the modID of your addon/mod
	 */
	public static void setDefaultActionFor(String modID, UnaryOperator<BaseConfigScreen> transform) {
		if (modID.equals(Create.ID))
			return;

		DEFAULTS.put(modID, transform);
	}

	public static BaseConfigScreen forCreate(Screen parent) {
		return new BaseConfigScreen(parent);
	}

	BoxWidget clientConfigWidget;
	BoxWidget commonConfigWidget;
	BoxWidget serverConfigWidget;
	BoxWidget goBack;
	BoxWidget others;
	BoxWidget title;

	ForgeConfigSpec clientSpec;
	ForgeConfigSpec commonSpec;
	ForgeConfigSpec serverSpec;
	String clientTile = "Client Config";
	String commonTile = "Common Config";
	String serverTile = "Server Config";
	String modID;
	protected boolean returnOnClose;

	public BaseConfigScreen(Screen parent, @Nonnull String modID) {
		super(parent);
		this.modID = modID;

		if (DEFAULTS.containsKey(modID))
			DEFAULTS.get(modID).apply(this);
		else {
			this.searchForSpecsInModContainer();
		}
	}

	private BaseConfigScreen(Screen parent) {
		this(parent, Create.ID);
	}

	/**
	 * If you have static references to your Configs or ConfigSpecs (like Create does in {@link AllConfigs}),
	 * please use {@link #withSpecs(ForgeConfigSpec, ForgeConfigSpec, ForgeConfigSpec)} instead
	 */
	public BaseConfigScreen searchForSpecsInModContainer() {
		if (!ConfigHelper.hasAnyForgeConfig(this.modID)){
			return this;
		}

		try {
			clientSpec = ConfigHelper.findForgeConfigSpecFor(ModConfig.Type.CLIENT, this.modID);
		} catch (Exception e) {
			Create.LOGGER.debug("Unable to find ClientConfigSpec for mod: " + this.modID);
		}

		try {
			commonSpec = ConfigHelper.findForgeConfigSpecFor(ModConfig.Type.COMMON, this.modID);
		} catch (Exception e) {
			Create.LOGGER.debug("Unable to find CommonConfigSpec for mod: " + this.modID);
		}

		try {
			serverSpec = ConfigHelper.findForgeConfigSpecFor(ModConfig.Type.SERVER, this.modID);
		} catch (Exception e) {
			Create.LOGGER.debug("Unable to find ServerConfigSpec for mod: " + this.modID);
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
		super.init();
		returnOnClose = true;

		TextStencilElement clientText = new TextStencilElement(font, Components.literal(clientTile)).centered(true, true);
		addRenderableWidget(clientConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 - 30, 200, 16).showingElement(clientText));

		if (clientSpec != null) {
			clientConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.CLIENT, clientSpec)));
			clientText.withElementRenderer(BoxWidget.gradientFactory.apply(clientConfigWidget));
		} else {
			clientConfigWidget.active = false;
			clientConfigWidget.updateColorsFromState();
			clientText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement commonText = new TextStencilElement(font, Components.literal(commonTile)).centered(true, true);
		addRenderableWidget(commonConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15, 200, 16).showingElement(commonText));

		if (commonSpec != null) {
			commonConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.COMMON, commonSpec)));
			commonText.withElementRenderer(BoxWidget.gradientFactory.apply(commonConfigWidget));
		} else {
			commonConfigWidget.active = false;
			commonConfigWidget.updateColorsFromState();
			commonText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement serverText = new TextStencilElement(font, Components.literal(serverTile)).centered(true, true);
		addRenderableWidget(serverConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 + 30, 200, 16).showingElement(serverText));

		if (serverSpec == null) {
			serverConfigWidget.active = false;
			serverConfigWidget.updateColorsFromState();
			serverText.withElementRenderer(DISABLED_RENDERER);
		} else if (minecraft.level == null) {
			serverText.withElementRenderer(DISABLED_RENDERER);
			serverConfigWidget.getToolTip()
					.add(Components.literal("Stored individually per World"));
			serverConfigWidget.getToolTip()
					.addAll(TooltipHelper.cutTextComponent(
							Components.literal(
									"Gameplay settings can only be accessed from the in-game menu after joining a World or Server."),
							ChatFormatting.GRAY, ChatFormatting.GRAY));
		} else {
			serverConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.SERVER, serverSpec)));
			serverText.withElementRenderer(BoxWidget.gradientFactory.apply(serverConfigWidget));
		}

		TextStencilElement titleText = new TextStencilElement(font, modID.toUpperCase(Locale.ROOT))
				.centered(true, true)
				.withElementRenderer((ms, w, h, alpha) -> {
					UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w / 2, Theme.p(Theme.Key.CONFIG_TITLE_A));
					UIRenderHelper.angledGradient(ms, 0, w / 2, h / 2, h, w / 2, Theme.p(Theme.Key.CONFIG_TITLE_B));
				});
		int boxWidth = width + 10;
		int boxHeight = 39;
		int boxPadding = 4;
		title = new BoxWidget(-5, height / 2 - 110, boxWidth, boxHeight)
				//.withCustomBackground(new Color(0x20_000000, true))
				.withBorderColors(Theme.p(Theme.Key.BUTTON_IDLE))
				.withPadding(0, boxPadding)
				.rescaleElement(boxWidth / 2f, (boxHeight - 2 * boxPadding) / 2f)//double the text size by telling it the element is only half as big as the available space
				.showingElement(titleText.at(0, 7));
		title.active = false;

		addRenderableWidget(title);


		ConfigScreen.modID = this.modID;

		goBack = new BoxWidget(width / 2 - 134, height / 2, 20, 20).withPadding(2, 2)
				.withCallback(() -> linkTo(parent));
		goBack.showingElement(AllIcons.I_CONFIG_BACK.asStencil()
				.withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip()
				.add(Components.literal("Go Back"));
		addRenderableWidget(goBack);

		TextStencilElement othersText = new TextStencilElement(font, Components.literal("Access Configs of other Mods")).centered(true, true);
		others = new BoxWidget(width / 2 - 100, height / 2 - 15 + 90, 200, 16).showingElement(othersText);
		othersText.withElementRenderer(BoxWidget.gradientFactory.apply(others));
		others.withCallback(() -> linkTo(new ConfigModListScreen(this)));
		addRenderableWidget(others);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		drawCenteredString(ms, font, "Access Configs for Mod:", width / 2, height / 2 - 105, Theme.i(Theme.Key.TEXT_ACCENT_STRONG));
	}

	private void linkTo(Screen screen) {
		returnOnClose = false;
		ScreenOpener.open(screen);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			linkTo(parent);
		}
		return false;
	}

}
