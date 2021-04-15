package com.simibubi.create.foundation.config.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.TextStencilElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class BaseConfigScreen extends ConfigScreen {

	ConfigButton clientConfigWidget;
	ConfigButton commonConfigWidget;
	ConfigButton serverConfigWidget;

	public BaseConfigScreen(Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		StencilElement text = new TextStencilElement(client.fontRenderer, new StringTextComponent("CLIENT CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(clientConfigWidget = ConfigButton.createFromStencilElement(
				width / 2 - 100,
				height / 2 - 15 - 50,
				text
				)
						.withBounds(200, 30)
						.withCallback(() -> ScreenOpener.transitionTo(new SubMenuConfigScreen(this, AllConfigs.CLIENT.specification)))
		);

		StencilElement text2 = new TextStencilElement(client.fontRenderer, new StringTextComponent("COMMON CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(commonConfigWidget = ConfigButton.createFromStencilElement(
				width / 2 - 100,
				height / 2 - 15,
				text2
				)
						.withBounds(200, 30)
						.withCallback(() -> ScreenOpener.transitionTo(new SubMenuConfigScreen(this, AllConfigs.COMMON.specification)))
		);

		StencilElement text3 = new TextStencilElement(client.fontRenderer, new StringTextComponent("SERVER CONFIG").formatted(TextFormatting.BOLD)).centered(true, true);
		widgets.add(serverConfigWidget = ConfigButton.createFromStencilElement(
				width / 2 - 100,
				height / 2 - 15 + 50,
				text3
				)
						.withBounds(200, 30)
		);

		if (Minecraft.getInstance().world != null) {
			serverConfigWidget.withCallback(() -> ScreenOpener.transitionTo(new ServerSubMenuConfigScreen(this, AllConfigs.SERVER.specification)));
		} else {
			serverConfigWidget.active = false;
			serverConfigWidget.updateColorsFromState();
		}
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);


		//<testStencil.at(200, 200, 0).render(ms);
	}
}
