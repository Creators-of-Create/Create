package com.simibubi.create.foundation.config.ui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class BooleanEntry extends ValueEntry<Boolean> {

	TextStencilElement enabled;
	TextStencilElement disabled;
	PonderButton button;

	public BooleanEntry(String label, ForgeConfigSpec.ConfigValue<Boolean> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		enabled = new TextStencilElement(Minecraft.getInstance().fontRenderer, "Enabled")
				.centered(true, true)
				.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height/2, height, width, 0xff_88f788, 0xff_20cc20));

		disabled = new TextStencilElement(Minecraft.getInstance().fontRenderer, "Disabled")
				.centered(true, true)
				.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, 0, height/2, height, width, 0xff_f78888, 0xff_cc2020));

		button = new PonderButton(0, 0, () -> {
			value.set(!value.get());
			onValueChange();
		}).showingUnscaled(enabled);
		button.fade(1);

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
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.x = x + getLabelWidth(width);
		button.y = y + 10;
		button.setWidth(width - getLabelWidth(width) - resetWidth - 4);
		button.setHeight(height - 20);
		button.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void onValueChange() {
		super.onValueChange();
		button.showingUnscaled(value.get() ? enabled : disabled);
		bumpCog(value.get() ? 15f : -16f);
	}

	/*@Override
	public boolean mouseClicked(double mX, double mY, int button) {
		return this.button.mouseClicked(mX, mY, button) || super.mouseClicked(mX, mY, button);
	}*/
}
