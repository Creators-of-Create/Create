package com.simibubi.create.foundation.config.ui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class EnumEntry extends ValueEntry<Enum<?>> {

	protected static final int cycleWidth = 34;//including 2px offset on either side

	protected TextStencilElement valueText;
	protected BoxWidget cycleLeft;
	protected BoxWidget cycleRight;

	public EnumEntry(String label, ForgeConfigSpec.ConfigValue<Enum<?>> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		valueText = new TextStencilElement(Minecraft.getInstance().fontRenderer, "YEP").centered(true, true);
		valueText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0 ,0, height/2, height, width, Theme.i(Theme.Key.TEXT_1), Theme.i(Theme.Key.TEXT_2)));

		TextStencilElement l = new TextStencilElement(Minecraft.getInstance().fontRenderer, "<").centered(true, true);
		cycleLeft = new BoxWidget(0, 0, 22, 22).showingElement(l).withCallback(() -> cycleValue(-1));
		l.withElementRenderer(BoxWidget.gradientFactory.apply(cycleLeft));

		TextStencilElement r = new TextStencilElement(Minecraft.getInstance().fontRenderer, ">").centered(true, true);
		cycleRight = new BoxWidget(0, 0, 22, 22).showingElement(r).withCallback(() -> cycleValue(1));
		r.withElementRenderer(BoxWidget.gradientFactory.apply(cycleRight));

		listeners.add(cycleLeft);
		listeners.add(cycleRight);

		onReset();
	}

	protected void cycleValue(int direction) {
		Enum<?> e = getValue();
		Enum<?>[] options = e.getDeclaringClass().getEnumConstants();
		e = options[Math.floorMod(e.ordinal() + direction, options.length)];
		setValue(e);
		bumpCog(direction * 15f);
	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		cycleLeft.active = b;
		cycleLeft.animateGradientFromState();
		cycleRight.active = b;
		cycleRight.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		cycleLeft.tick();
		cycleRight.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		cycleLeft.x = x + getLabelWidth(width) + 2;
		cycleLeft.y = y + 10;
		cycleLeft.render(ms, mouseX, mouseY, partialTicks);

		valueText
				.at(cycleLeft.x - 2 + cycleWidth, y + 10, 0)
				.withBounds(width - getLabelWidth(width) - 2 * cycleWidth - resetWidth, 30)
				.render(ms);

		cycleRight.x = x + width - cycleWidth - resetWidth + 2;
		cycleRight.y = y + 10;
		cycleRight.render(ms, mouseX, mouseY, partialTicks);

	}

	@Override
	public void onValueChange(Enum<?> newValue) {
		super.onValueChange(newValue);
		valueText.withText(newValue.name());
	}
}
