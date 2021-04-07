package com.simibubi.create.foundation.config.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.CombinedStencilElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.StencilWidget;

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

		StencilElement text = new TextStencilElement.Centered(client.fontRenderer, new StringTextComponent("CLIENT CONFIG").formatted(TextFormatting.BOLD), 200).at(0, 11, 0);
		widgets.add(clientConfigWidget = ConfigButton.createFromTextElement(
				width/2 - 100,
				height/2 - 15 - 50,
				200,
				30,
				text
		));

		StencilElement text2 = new TextStencilElement.Centered(client.fontRenderer, new StringTextComponent("COMMON CONFIG").formatted(TextFormatting.BOLD), 200).at(0, 11, 0);
		widgets.add(commonConfigWidget = ConfigButton.createFromTextElement(
				width/2 - 100,
				height/2 - 15,
				200,
				30,
				text2
		));
		commonConfigWidget.active = false;
		commonConfigWidget.updateColorsFromState();

		StencilElement text3 = new TextStencilElement.Centered(client.fontRenderer, new StringTextComponent("SERVER CONFIG").formatted(TextFormatting.BOLD), 200).at(0, 11, 0);
		widgets.add(serverConfigWidget = ConfigButton.createFromTextElement(
				width/2 - 100,
				height/2 - 15 + 50,
				200,
				30,
				text3
		));
		serverConfigWidget.active = false;
		serverConfigWidget.updateColorsFromState();
	}

	@Override
	public void tick() {
		super.tick();

		widgets.stream()
				.filter(w -> w instanceof ConfigButton)
				.forEach(w -> ((ConfigButton) w).tick());
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);

	}
}
