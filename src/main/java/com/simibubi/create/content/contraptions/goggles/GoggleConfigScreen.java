package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class GoggleConfigScreen extends AbstractSimiScreen {

	private int offsetX;
	private int offsetY;
	private final List<String> tooltip;

	public GoggleConfigScreen() {
		String spacing = "    ";
		tooltip = new ArrayList<>();
		tooltip.add(spacing + Lang.translate("gui.config.overlay1"));
		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.config.overlay2"));
		tooltip.add("");
		tooltip.add(spacing + Lang.translate("gui.config.overlay3"));
		tooltip.add(spacing + Lang.translate("gui.config.overlay4"));
		tooltip.add("");
		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.config.overlay5"));
		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.config.overlay6"));
		tooltip.add("");
		tooltip.add(spacing + Lang.translate("gui.config.overlay7"));
		tooltip.add(spacing + Lang.translate("gui.config.overlay8"));
	}

	@Override
	protected void init() {
		Minecraft mc = Minecraft.getInstance();
		this.width = mc.getWindow().getScaledWidth();
		this.height = mc.getWindow().getScaledHeight();

		offsetX = AllConfigs.CLIENT.overlayOffsetX.get();
		offsetY = AllConfigs.CLIENT.overlayOffsetY.get();
	}

	@Override
	public void removed() {
		AllConfigs.CLIENT.overlayOffsetX.set(offsetX);
		AllConfigs.CLIENT.overlayOffsetY.set(offsetY);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		updateOffset(x, y);

		return true;
	}

	@Override
	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
		updateOffset(p_mouseDragged_1_, p_mouseDragged_3_);

		return true;
	}

	private void updateOffset(double windowX, double windowY) {
		offsetX = (int) (windowX - (this.width / 2));
		offsetY = (int) (windowY - (this.height / 2));
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		RenderSystem.pushMatrix();
		int posX = this.width / 2 + offsetX;
		int posY = this.height / 2 + offsetY;
		//tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2, tooltipScreen.height / 2);
		renderTooltip(tooltip, posX, posY);

		ItemStack item = AllItems.GOGGLES.asStack();
		//GuiGameElement.of(item).at(tooltipScreen.width / 2 + 10, tooltipScreen.height / 2 - 16).render();
		GuiGameElement.of(item).at(posX + 10, posY - 16).render();
		RenderSystem.popMatrix();
	}
}
