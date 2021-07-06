package com.simibubi.create.foundation.config.ui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.RenderElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraftforge.common.ForgeConfigSpec;

public class BooleanEntry extends ValueEntry<Boolean> {

	RenderElement enabled;
	RenderElement disabled;
	BoxWidget button;

	public BooleanEntry(String label, ForgeConfigSpec.ConfigValue<Boolean> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		enabled = AllIcons.I_CONFIRM.asStencil()
			.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.p(Theme.Key.BUTTON_SUCCESS)))
			.at(10, 0);

		disabled = AllIcons.I_DISABLE.asStencil()
			.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.p(Theme.Key.BUTTON_FAIL)))
			.at(10, 0);

		button = new BoxWidget().showingElement(enabled)
			.withCallback(() -> setValue(!getValue()));

		listeners.add(button);
		onReset();
	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		button.active = b;
	}

	@Override
	public void tick() {
		super.tick();
		button.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY,
		boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.x = x + width - 80 - resetWidth;
		button.y = y + 10;
		button.setWidth(35);
		button.setHeight(height - 20);
		button.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onValueChange(Boolean newValue) {
		super.onValueChange(newValue);
		button.showingElement(newValue ? enabled : disabled);
		bumpCog(newValue ? 15f : -16f);
	}
}
