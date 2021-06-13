package com.simibubi.create.content.logistics.item.filter;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractFilterScreen<F extends AbstractFilterContainer> extends AbstractSimiContainerScreen<F> {

	protected AllGuiTextures background;
    private List<Rectangle2d> extraAreas = Collections.emptyList();

	private IconButton resetButton;
	private IconButton confirmButton;

	protected AbstractFilterScreen(F container, PlayerInventory inv, ITextComponent title, AllGuiTextures background) {
		super(container, inv, title);
		this.background = background;
	}

	@Override
	protected void init() {
		setWindowSize(Math.max(background.width, PLAYER_INVENTORY.width), background.height + 4 + PLAYER_INVENTORY.height);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		resetButton = new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);

		extraAreas = ImmutableList.of(
			new Rectangle2d(x + background.width, y + background.height - 40, 80, 48)
		);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int invLeft = guiLeft - windowXOffset + (xSize - PLAYER_INVENTORY.width) / 2;
		int invTop = guiTop + background.height + 4;

		PLAYER_INVENTORY.draw(ms, this, invLeft, invTop);
		textRenderer.draw(ms, playerInventory.getDisplayName(), invLeft + 8, invTop + 6, 0x404040);

		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);
		drawCenteredText(ms, textRenderer, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		GuiGameElement.of(container.contentHolder)
				.<GuiGameElement.GuiRenderBuilder>at(x + background.width, y + background.height - 56, -200)
				.scale(5)
				.render(ms);
	}

	@Override
	public void tick() {
		handleTooltips();
		super.tick();
		handleIndicators();

		if (!container.player.getHeldItemMainhand()
			.equals(container.contentHolder, false))
			client.player.closeScreen();
	}

	public void handleIndicators() {
		List<IconButton> tooltipButtons = getTooltipButtons();
		for (IconButton button : tooltipButtons)
			button.active = isButtonEnabled(button);
		for (Widget w : widgets)
			if (w instanceof Indicator)
				((Indicator) w).state = isIndicatorOn((Indicator) w) ? State.ON : State.OFF;
	}

	protected abstract boolean isButtonEnabled(IconButton button);

	protected abstract boolean isIndicatorOn(Indicator indicator);

	protected void handleTooltips() {
		List<IconButton> tooltipButtons = getTooltipButtons();

		for (IconButton button : tooltipButtons) {
			if (!button.getToolTip()
				.isEmpty()) {
				button.setToolTip(button.getToolTip()
					.get(0));
				button.getToolTip()
					.add(TooltipHelper.holdShift(Palette.Yellow, hasShiftDown()));
			}
		}

		if (hasShiftDown()) {
			List<IFormattableTextComponent> tooltipDescriptions = getTooltipDescriptions();
			for (int i = 0; i < tooltipButtons.size(); i++)
				fillToolTip(tooltipButtons.get(i), tooltipDescriptions.get(i));
		}
	}

	protected List<IconButton> getTooltipButtons() {
		return Collections.emptyList();
	}

	protected List<IFormattableTextComponent> getTooltipDescriptions() {
		return Collections.emptyList();
	}

	private void fillToolTip(IconButton button, ITextComponent tooltip) {
		if (!button.isHovered())
			return;
		List<ITextComponent> tip = button.getToolTip();
		tip.addAll(TooltipHelper.cutTextComponent(tooltip, GRAY, GRAY));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				client.player.closeScreen();
				return true;
			}
			if (resetButton.isHovered()) {
				container.clearContents();
				contentsCleared();
				container.sendClearPacket();
				return true;
			}
		}

		return mouseClicked;
	}

	protected void contentsCleared() {}

	protected void sendOptionUpdate(Option option) {
		AllPackets.channel.sendToServer(new FilterScreenPacket(option));
	}

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}

}
