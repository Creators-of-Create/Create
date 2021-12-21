package com.simibubi.create.content.logistics.item.filter;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.lib.util.PlayerEntityHelper;
import com.simibubi.create.lib.util.ItemStackUtil;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractFilterScreen<F extends AbstractFilterContainer> extends AbstractSimiContainerScreen<F> {

	protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

	private IconButton resetButton;
	private IconButton confirmButton;

	protected AbstractFilterScreen(F container, Inventory inv, Component title, AllGuiTextures background) {
		super(container, inv, title);
		this.background = background;
	}

	@Override
	protected void init() {
		setWindowSize(Math.max(background.width, PLAYER_INVENTORY.width), background.height + 4 + PLAYER_INVENTORY.height);
		super.init();

		int x = leftPos;
		int y = topPos;

		resetButton = new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		resetButton.withCallback(() -> {
			menu.clearContents();
			contentsCleared();
			menu.sendClearPacket();
		});
		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			minecraft.player.closeContainer();
		});

		addRenderableWidget(resetButton);
		addRenderableWidget(confirmButton);

		extraAreas = ImmutableList.of(
			new Rect2i(x + background.width, y + background.height - 40, 80, 48)
		);
	}

	@Override
	protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.render(ms, x, y, this);
		drawCenteredString(ms, font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		GuiGameElement.of(menu.contentHolder)
				.<GuiGameElement.GuiRenderBuilder>at(x + background.width, y + background.height - 56, -200)
				.scale(5)
				.render(ms);
	}

	@Override
	protected void containerTick() {
		if (!ItemStackUtil.equals(menu.player.getMainHandItem(), menu.contentHolder, false))
			PlayerEntityHelper.closeScreen(menu.player);

		super.containerTick();

		handleTooltips();
		handleIndicators();
	}

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
			List<MutableComponent> tooltipDescriptions = getTooltipDescriptions();
			for (int i = 0; i < tooltipButtons.size(); i++)
				fillToolTip(tooltipButtons.get(i), tooltipDescriptions.get(i));
		}
	}

	public void handleIndicators() {
		for (IconButton button : getTooltipButtons())
			button.active = isButtonEnabled(button);
		for (Indicator indicator : getIndicators())
			indicator.state = isIndicatorOn(indicator) ? State.ON : State.OFF;
	}

	protected abstract boolean isButtonEnabled(IconButton button);

	protected abstract boolean isIndicatorOn(Indicator indicator);

	protected List<IconButton> getTooltipButtons() {
		return Collections.emptyList();
	}

	protected List<MutableComponent> getTooltipDescriptions() {
		return Collections.emptyList();
	}

	protected List<Indicator> getIndicators() {
		return Collections.emptyList();
	}

	private void fillToolTip(IconButton button, Component tooltip) {
		if (!button.isHoveredOrFocused())
			return;
		List<Component> tip = button.getToolTip();
		tip.addAll(TooltipHelper.cutTextComponent(tooltip, GRAY, GRAY));
	}

	protected void contentsCleared() {}

	protected void sendOptionUpdate(Option option) {
		AllPackets.channel.sendToServer(new FilterScreenPacket(option));
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
