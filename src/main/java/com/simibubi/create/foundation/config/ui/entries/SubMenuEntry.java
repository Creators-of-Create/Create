package com.simibubi.create.foundation.config.ui.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigButton;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.common.ForgeConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected ConfigButton button;

	public SubMenuEntry(Screen parent, String label, ForgeConfigSpec spec, UnmodifiableConfig config) {
		super(label);
		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "Click to open").centered(true, true);
		button = ConfigButton.createFromStencilElement(0, 0, text)
				.withCallback(() -> ScreenOpener.transitionTo(new SubMenuConfigScreen(parent, spec, config)));

		listeners.add(button);
	}

	@Override
	public void tick() {
		super.tick();
		button.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.x = x + getLabelWidth(width);
		button.y = y;
		button.withBounds(width - getLabelWidth(width), height);
		button.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult);
	}

	/*@Override
	public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
		return button.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
	}*/
}
