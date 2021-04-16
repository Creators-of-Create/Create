package com.simibubi.create.foundation.config.ui;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class BaseConfigScreen extends ConfigScreen {

	PonderButton clientConfigWidget;
	PonderButton commonConfigWidget;
	PonderButton serverConfigWidget;

	public BaseConfigScreen(Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		TextStencilElement text = new TextStencilElement(client.fontRenderer, new StringTextComponent("CLIENT CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		text.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2));
		widgets.add(clientConfigWidget = new PonderButton(width / 2 - 100, height / 2 - 15 - 50, (_$, _$$) -> {
			ScreenOpener.transitionTo(new SubMenuConfigScreen(this, AllConfigs.CLIENT.specification));
		}, 200, 30).showingUnscaled(text));
		clientConfigWidget.fade(1);

		TextStencilElement text2 = new TextStencilElement(client.fontRenderer, new StringTextComponent("COMMON CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		text2.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2));
		widgets.add(commonConfigWidget = new PonderButton(width / 2 - 100, height / 2 - 15, (_$, _$$) -> {
			ScreenOpener.transitionTo(new SubMenuConfigScreen(this, AllConfigs.COMMON.specification));
		}, 200, 30).showingUnscaled(text2));
		commonConfigWidget.fade(1);

		TextStencilElement text3 = new TextStencilElement(client.fontRenderer, new StringTextComponent("SERVER CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		text3.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2));
		widgets.add(serverConfigWidget = new PonderButton(width / 2 - 100, height / 2 - 15 + 50, (_$, _$$) -> {
		}, 200, 30).showingUnscaled(text3));
		serverConfigWidget.fade(1);

		if (Minecraft.getInstance().world != null) {
			serverConfigWidget.withCallback(() -> ScreenOpener.transitionTo(new ServerSubMenuConfigScreen(this, AllConfigs.SERVER.specification)));
		} else {
			serverConfigWidget.active = false;
			text3.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, ConfigButton.Palette.button_disable_1, ConfigButton.Palette.button_disable_2));
		}
	}
}
