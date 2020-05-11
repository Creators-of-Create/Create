package com.simibubi.create.modules.logistics.block;

import static com.simibubi.create.ScreenResources.STOCKSWITCH;

import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.logistics.packet.ConfigureStockswitchPacket;

import net.minecraft.item.ItemStack;

public class StockswitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private Label offBelowLabel;
	private ScrollInput onAbove;
	private Label onAboveLabel;

	private final String title = Lang.translate("gui.stockswitch.title");
	private final String startAbove = Lang.translate("gui.stockswitch.startAbove");
	private final String startAt = Lang.translate("gui.stockswitch.startAt");
	private final String stopBelow = Lang.translate("gui.stockswitch.stopBelow");
	private final String stopAt = Lang.translate("gui.stockswitch.stopAt");
	private final String lowerLimit = Lang.translate("gui.stockswitch.lowerLimit");
	private final String upperLimit = Lang.translate("gui.stockswitch.upperLimit");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.STOCKSWITCH.get());

	private int lastModification;
	private StockswitchTileEntity te;
	private float cursorPos;

	public StockswitchScreen(StockswitchTileEntity te) {
		this.te = te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(STOCKSWITCH.width + 50, STOCKSWITCH.height);
		super.init();
		widgets.clear();
		cursorPos = te.currentLevel == -1 ? 0 : te.currentLevel;

		offBelowLabel = new Label(guiLeft + 116, guiTop + 72, "").colored(0xD3CBBE)
				.withShadow();
		offBelow = new ScrollInput(guiLeft + 113, guiTop + 69, 33, 14).withRange(0, 96)
				.titled(lowerLimit)
				.calling(state -> {
					offBelowLabel.text = state + "%";
					lastModification = 0;
					if (onAbove.getState() - 4 <= state) {
						onAbove.setState(state + 5);
						onAbove.onChanged();
					}
				})
				.setState((int) (te.offWhenBelow * 100));

		onAboveLabel = new Label(guiLeft + 116, guiTop + 55, "").colored(0xD3CBBE)
				.withShadow();
		onAbove = new ScrollInput(guiLeft + 113, guiTop + 52, 33, 14).withRange(5, 101)
				.titled(upperLimit)
				.calling(state -> {
					onAboveLabel.text = state + "%";
					lastModification = 0;
					if (offBelow.getState() + 4 >= state) {
						offBelow.setState(state - 5);
						offBelow.onChanged();
					}
				})
				.setState((int) (te.onWhenAbove * 100));

		onAbove.onChanged();
		offBelow.onChanged();
		widgets.addAll(Arrays.asList(offBelowLabel, offBelow, onAbove, onAboveLabel));
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int hFontColor = 0xD3CBBE;
		int fontColor = 0x4B3A22;
		STOCKSWITCH.draw(this, guiLeft, guiTop);
		font.drawStringWithShadow(title, guiLeft - 3 + (STOCKSWITCH.width - font.getStringWidth(title)) / 2,
				guiTop + 10, hFontColor);
		font.drawString(onAbove.getState() == 100 ? startAt : startAbove, guiLeft + 13, guiTop + 55, fontColor);
		font.drawString(offBelow.getState() == 0 ? stopAt : stopBelow, guiLeft + 13, guiTop + 72, fontColor);

		ScreenResources sprite = ScreenResources.STOCKSWITCH_INTERVAL;
		float lowerBound = offBelow.getState() / 100f * (sprite.width - 20) + 10;
		float upperBound = onAbove.getState() / 100f * (sprite.width - 20) + 10;

		sprite.bind();
		blit((int) (guiLeft + lowerBound), guiTop + 26, (int) (sprite.startX + lowerBound), sprite.startY,
				(int) (upperBound - lowerBound), sprite.height);

		sprite = ScreenResources.STOCKSWITCH_INTERVAL_END;
		sprite.bind();
		blit((int) (guiLeft + upperBound), guiTop + 26, (int) (sprite.startX + upperBound), sprite.startY,
				(int) (sprite.width - upperBound), sprite.height);

		ScreenResources.STOCKSWITCH_BOUND_LEFT.draw(this, (int) (guiLeft + lowerBound) - 1, guiTop + 24);
		ScreenResources.STOCKSWITCH_BOUND_RIGHT.draw(this, (int) (guiLeft + upperBound) - 5, guiTop + 24);

		ScreenResources cursor =
			te.powered ? ScreenResources.STOCKSWITCH_CURSOR_ON : ScreenResources.STOCKSWITCH_CURSOR_OFF;
		RenderSystem.pushMatrix();
		RenderSystem.translatef((cursorPos * (sprite.width - 20) + 10), 0, 0);
		cursor.draw(this, guiLeft - 4, guiTop + 24);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		GuiGameElement.of(renderedItem)
				.at(guiLeft + STOCKSWITCH.width + 15, guiTop + 20)
				.scale(5)
				.render();
		RenderSystem.popMatrix();
	}

	@Override
	public void tick() {
		super.tick();

		if (te.currentLevel == -1)
			cursorPos = 0;
		else
			cursorPos += (te.currentLevel - cursorPos) / 4;

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 20) {
			lastModification = -1;
			AllPackets.channel.sendToServer(
					new ConfigureStockswitchPacket(te.getPos(), offBelow.getState() / 100f, onAbove.getState() / 100f));
		}
	}

	@Override
	public void removed() {
		AllPackets.channel.sendToServer(
				new ConfigureStockswitchPacket(te.getPos(), offBelow.getState() / 100f, onAbove.getState() / 100f));
	}

}
