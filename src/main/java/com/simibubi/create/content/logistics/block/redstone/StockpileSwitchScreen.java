package com.simibubi.create.content.logistics.block.redstone;

import static com.simibubi.create.foundation.gui.AllGuiTextures.STOCKSWITCH;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class StockpileSwitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private ScrollInput onAbove;
	private IconButton confirmButton;
	private IconButton flipSignals;

	private final String title = Lang.translate("gui.stockpile_switch.title");
	private final String invertSignal = Lang.translate("gui.stockpile_switch.invert_signal");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.STOCKPILE_SWITCH.get());

	private int lastModification;
	private StockpileSwitchTileEntity te;

	private LerpedFloat cursor;
	private LerpedFloat cursorLane;

	public StockpileSwitchScreen(StockpileSwitchTileEntity te) {
		this.te = te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		AllGuiTextures background = STOCKSWITCH;
		setWindowSize(background.width + 50, background.height);
		super.init();
		widgets.clear();

		cursor = LerpedFloat.linear()
			.startWithValue(te.getLevelForDisplay());
		cursorLane = LerpedFloat.linear()
			.startWithValue(te.getState() ? 1 : 0);

		offBelow = new ScrollInput(guiLeft + 36, guiTop + 40, 102, 18).withRange(0, 100)
			.titled("")
			.calling(state -> {
				lastModification = 0;
				offBelow.titled(Lang.translate("gui.stockpile_switch.move_to_upper_at", state));
				if (onAbove.getState() <= state) {
					onAbove.setState(state + 1);
					onAbove.onChanged();
				}
			})
			.setState((int) (te.offWhenBelow * 100));

		onAbove = new ScrollInput(guiLeft + 36, guiTop + 18, 102, 18).withRange(1, 101)
			.titled("")
			.calling(state -> {
				lastModification = 0;
				onAbove.titled(Lang.translate("gui.stockpile_switch.move_to_lower_at", state));
				if (offBelow.getState() >= state) {
					offBelow.setState(state - 1);
					offBelow.onChanged();
				}
			})
			.setState((int) (te.onWhenAbove * 100));

		onAbove.onChanged();
		offBelow.onChanged();

		widgets.add(onAbove);
		widgets.add(offBelow);

		confirmButton =
			new IconButton(guiLeft + background.width - 33, guiTop + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		flipSignals = new IconButton(guiLeft + 14, guiTop + 40, AllIcons.I_FLIP);
		flipSignals.setToolTip(invertSignal);
		widgets.add(flipSignals);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		STOCKSWITCH.draw(this, guiLeft, guiTop);

		AllGuiTextures.STOCKSWITCH_POWERED_LANE.draw(this, guiLeft + 36, guiTop + (te.isInverted() ? 18 : 40));
		AllGuiTextures.STOCKSWITCH_UNPOWERED_LANE.draw(this, guiLeft + 36, guiTop + (te.isInverted() ? 40 : 18));

		font.drawStringWithShadow(title, guiLeft - 3 + (STOCKSWITCH.width - font.getStringWidth(title)) / 2, guiTop + 3,
			0xffffff);

		AllGuiTextures sprite = AllGuiTextures.STOCKSWITCH_INTERVAL;
		float lowerBound = offBelow.getState();
		float upperBound = onAbove.getState();

		sprite.bind();
		blit((int) (guiLeft + upperBound) + 37, guiTop + 18, (int) (sprite.startX + upperBound), sprite.startY,
			(int) (sprite.width - upperBound), sprite.height);
		blit(guiLeft + 37, guiTop + 40, sprite.startX, sprite.startY, (int) (lowerBound), sprite.height);

		AllGuiTextures.STOCKSWITCH_ARROW_UP.draw(this, (int) (guiLeft + lowerBound + 36) - 2, guiTop + 35);
		AllGuiTextures.STOCKSWITCH_ARROW_DOWN.draw(this, (int) (guiLeft + upperBound + 36) - 3, guiTop + 17);

		if (te.currentLevel != -1) {
			AllGuiTextures cursor = AllGuiTextures.STOCKSWITCH_CURSOR;
			RenderSystem.pushMatrix();
			RenderSystem.translatef(Math.min(99, this.cursor.getValue(partialTicks) * sprite.width),
				cursorLane.getValue(partialTicks) * 22, 0);
			cursor.draw(this, guiLeft + 34, guiTop + 19);
			RenderSystem.popMatrix();
		}

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

		cursor.chase(te.getLevelForDisplay(), 1 / 4f, Chaser.EXP);
		cursor.tickChaser();
		cursorLane.chase(te.getState() ? 1 : 0, 1 / 4f, Chaser.EXP);
		cursorLane.tickChaser();

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 20) {
			lastModification = -1;
			send(te.isInverted());
		}
	}

	@Override
	public void removed() {
		send(te.isInverted());
	}

	protected void send(boolean invert) {
		AllPackets.channel.sendToServer(new ConfigureStockswitchPacket(te.getPos(), offBelow.getState() / 100f,
			onAbove.getState() / 100f, invert));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (flipSignals.isHovered()) 
			send(!te.isInverted());
		if (confirmButton.isHovered()) {
			Minecraft.getInstance().player.closeScreen();
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

}
