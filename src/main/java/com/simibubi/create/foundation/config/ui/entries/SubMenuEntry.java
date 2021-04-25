package com.simibubi.create.foundation.config.ui.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected BoxWidget button;

	public SubMenuEntry(SubMenuConfigScreen parent, String label, ForgeConfigSpec spec, UnmodifiableConfig config) {
		super(label);
		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "Click to open").centered(true, true);

		button = new BoxWidget()
				.showingElement(text)
				.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(parent, label, parent.type, spec, config)));
		text.withElementRenderer(BoxWidget.gradientFactory.apply(button));

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
