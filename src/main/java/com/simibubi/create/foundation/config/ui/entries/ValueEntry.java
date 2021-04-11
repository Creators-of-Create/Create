package com.simibubi.create.foundation.config.ui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigButton;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.gui.TextStencilElement;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected ConfigButton reset;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;

		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "R").centered(true, true);
		reset = ConfigButton.createFromStencilElement(0, 0, text)
				.withBounds(30, 30)
				.withCallback(() -> {
					value.set((T) spec.getDefault());
					this.onReset();
				});
	}

	@Override
	public void tick() {
		reset.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		reset.x = x + width - 32;
		reset.y = y + 10;
		reset.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double mX, double mY, int button) {
		return reset.mouseClicked(mX, mY, button);
	}

	protected void onReset() {}
}
