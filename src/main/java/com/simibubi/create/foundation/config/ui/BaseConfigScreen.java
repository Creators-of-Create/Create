package com.simibubi.create.foundation.config.ui;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.config.ModConfig;

public class BaseConfigScreen extends ConfigScreen {

	BoxWidget clientConfigWidget;
	BoxWidget commonConfigWidget;
	BoxWidget serverConfigWidget;

	public BaseConfigScreen(Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		TextStencilElement text = new TextStencilElement(client.fontRenderer, new StringTextComponent("Client Settings").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(clientConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 - 30, 200, 16)
				.showingElement(text)
				.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.CLIENT, AllConfigs.CLIENT.specification)))
		);
		text.withElementRenderer(BoxWidget.gradientFactory.apply(clientConfigWidget));

		TextStencilElement text2 = new TextStencilElement(client.fontRenderer, new StringTextComponent("World Generation Settings").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(commonConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15, 200, 16)
				.showingElement(text2)
				.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.COMMON, AllConfigs.COMMON.specification)))
		);
		text2.withElementRenderer(BoxWidget.gradientFactory.apply(commonConfigWidget));

		TextStencilElement text3 = new TextStencilElement(client.fontRenderer, new StringTextComponent("Gameplay Settings").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(serverConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 + 30, 200, 16)
			.showingElement(text3)
		);

		if (Minecraft.getInstance().world != null) {
			serverConfigWidget.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(this, ModConfig.Type.SERVER, AllConfigs.SERVER.specification)));
			text3.withElementRenderer(BoxWidget.gradientFactory.apply(serverConfigWidget));
		} else {
			serverConfigWidget.active = false;
			serverConfigWidget.updateColorsFromState();
			text3.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.i(Theme.Key.BUTTON_DISABLE, true), Theme.i(Theme.Key.BUTTON_DISABLE, false) | 0x40_000000));
		}
	}
}
