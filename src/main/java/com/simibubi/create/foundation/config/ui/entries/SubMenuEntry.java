package com.simibubi.create.foundation.config.ui.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigButton;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.ServerSubMenuConfigScreen;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.common.ForgeConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected PonderButton button;

	public SubMenuEntry(Screen parent, String label, ForgeConfigSpec spec, UnmodifiableConfig config) {
		super(label);
		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "Click to open").centered(true, true);
		text.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0 ,0, height/2, height, width, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2));
		button = new PonderButton(0, 0, () -> ScreenOpener.transitionTo(isForServer() ?
				new ServerSubMenuConfigScreen(parent, spec, config) :
				new SubMenuConfigScreen(parent, spec, config))
		).showingUnscaled(text);
		button.fade(1);

		listeners.add(button);
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.x = x + getLabelWidth(width);
		button.y = y + 10;
		button.setWidth(width - getLabelWidth(width) - 4);
		button.setHeight(height - 20);
		button.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult);
	}
}
