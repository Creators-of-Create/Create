package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class GoggleConfigScreen extends AbstractSimiScreen {

	private int offsetX;
	private int offsetY;
	private final List<ITextComponent> tooltip;

	public GoggleConfigScreen() {
		ITextComponent componentSpacing = new StringTextComponent("    ");
		tooltip = new ArrayList<>();
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay1")));
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay2")
				.withStyle(TextFormatting.GRAY)));
		tooltip.add(StringTextComponent.EMPTY);
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay3")));
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay4")));
		tooltip.add(StringTextComponent.EMPTY);
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay5")
				.withStyle(TextFormatting.GRAY)));
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay6")
				.withStyle(TextFormatting.GRAY)));
		tooltip.add(StringTextComponent.EMPTY);
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay7")));
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.config.overlay8")));
	}

	@Override
	protected void init() {
		this.width = minecraft.getWindow()
			.getGuiScaledWidth();
		this.height = minecraft.getWindow()
			.getGuiScaledHeight();

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
	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_,
		double p_mouseDragged_6_, double p_mouseDragged_8_) {
		updateOffset(p_mouseDragged_1_, p_mouseDragged_3_);

		return true;
	}

	private void updateOffset(double windowX, double windowY) {
		offsetX = (int) (windowX - (this.width / 2));
		offsetY = (int) (windowY - (this.height / 2));

		int titleLinesCount = 1;
		int tooltipTextWidth = 0;
		for (ITextProperties textLine : tooltip) {
			int textLineWidth = minecraft.font.width(textLine);
			if (textLineWidth > tooltipTextWidth)
				tooltipTextWidth = textLineWidth;
		}
		int tooltipHeight = 8;
		if (tooltip.size() > 1) {
			tooltipHeight += (tooltip.size() - 1) * 10;
			if (tooltip.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		offsetX = MathHelper.clamp(offsetX, -(width / 2) - 5, (width / 2) - tooltipTextWidth - 20);
		offsetY = MathHelper.clamp(offsetY, -(height / 2) + 17, (height / 2) - tooltipHeight + 5);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int posX = this.width / 2 + offsetX;
		int posY = this.height / 2 + offsetY;
		renderComponentTooltip(ms, tooltip, posX, posY);

		// UIRenderHelper.breadcrumbArrow(ms, 50, 50, 100, 50, 20, 10, 0x80aa9999, 0x10aa9999);
		// UIRenderHelper.breadcrumbArrow(ms, 100, 80, 0, -50, 20, -10, 0x80aa9999, 0x10aa9999);

		ItemStack item = AllItems.GOGGLES.asStack();
		GuiGameElement.of(item)
			.at(posX + 10, posY - 16, 450)
			.render(ms);
		// GuiGameElement.of(item).at(0, 0, 450).render(ms);
	}
}
