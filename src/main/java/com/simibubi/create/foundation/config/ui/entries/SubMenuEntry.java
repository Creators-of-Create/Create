package com.simibubi.create.foundation.config.ui.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraftforge.common.ForgeConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected BoxWidget button;

	public SubMenuEntry(SubMenuConfigScreen parent, String label, ForgeConfigSpec spec, UnmodifiableConfig config) {
		super(label);

		button = new BoxWidget(0, 0, 35, 16)
				.showingElement(AllIcons.I_CONFIG_OPEN.asStencil().at(10, 0))
				.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(parent, label, parent.type, spec, config)));
		button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(button)));

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

		button.x = x + width - 108;
		button.y = y + 10;
		button.setHeight(height - 20);
		button.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}
}
