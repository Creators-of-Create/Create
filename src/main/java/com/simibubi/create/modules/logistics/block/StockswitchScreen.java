package com.simibubi.create.modules.logistics.block;

import static com.simibubi.create.foundation.gui.ScreenResources.STOCKSWITCH;

import java.util.Arrays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.gui.ScreenResources;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.modules.logistics.packet.ConfigureStockswitchPacket;

import net.minecraft.block.BlockState;

public class StockswitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private Label offBelowLabel;
	private ScrollInput onAbove;
	private Label onAboveLabel;

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

		offBelowLabel = new Label(guiLeft + 116, guiTop + 72, "").colored(0xD3CBBE).withShadow();
		offBelow = new ScrollInput(guiLeft + 113, guiTop + 69, 33, 14).withRange(0, 96).titled("Lower Threshold")
				.calling(state -> {
					offBelowLabel.text = state + "%";
					lastModification = 0;
					if (onAbove.getState() - 4 <= state) {
						onAbove.setState(state + 5);
						onAbove.onChanged();
					}
				}).setState((int) (te.offWhenBelow * 100));

		onAboveLabel = new Label(guiLeft + 116, guiTop + 55, "").colored(0xD3CBBE).withShadow();
		onAbove = new ScrollInput(guiLeft + 113, guiTop + 52, 33, 14).withRange(5, 101).titled("Upper Threshold")
				.calling(state -> {
					onAboveLabel.text = state + "%";
					lastModification = 0;
					if (offBelow.getState() + 4 >= state) {
						offBelow.setState(state - 5);
						offBelow.onChanged();
					}
				}).setState((int) (te.onWhenAbove * 100));

		onAbove.onChanged();
		offBelow.onChanged();
		widgets.addAll(Arrays.asList(offBelowLabel, offBelow, onAbove, onAboveLabel));
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int hFontColor = 0xD3CBBE;
		int fontColor = 0x4B3A22;
		STOCKSWITCH.draw(this, guiLeft, guiTop);
		font.drawStringWithShadow("Stockpile Switch",
				guiLeft - 3 + (STOCKSWITCH.width - font.getStringWidth("Stockpile Switch")) / 2, guiTop + 10,
				hFontColor);
		font.drawString("Start Signal " + (onAbove.getState() == 100 ? "at" : "above"), guiLeft + 13, guiTop + 55,
				fontColor);
		font.drawString("Stop Signal " + (offBelow.getState() == 0 ? "at" : "below"), guiLeft + 13, guiTop + 72,
				fontColor);

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

		ScreenResources cursor = te.powered ? ScreenResources.STOCKSWITCH_CURSOR_ON
				: ScreenResources.STOCKSWITCH_CURSOR_OFF;
		GlStateManager.pushMatrix();
		GlStateManager.translatef((cursorPos * (sprite.width - 20) + 10), 0, 0);
		cursor.draw(this, guiLeft - 4, guiTop + 24);
		GlStateManager.popMatrix();

		ScreenElementRenderer.renderBlock(this::getRenderedBlock);
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

	public BlockState getRenderedBlock() {
		GlStateManager.translated(guiLeft + STOCKSWITCH.width + 50, guiTop + 100, 0);
		GlStateManager.rotatef(50, -.5f, 1, -.2f);
		return AllBlocks.STOCKSWITCH.get().getDefaultState();
	}

}
