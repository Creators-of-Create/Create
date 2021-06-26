package com.simibubi.create.content.logistics.block.redstone;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class StockpileSwitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private ScrollInput onAbove;
	private IconButton confirmButton;
	private IconButton flipSignals;

	private final ITextComponent invertSignal = Lang.translate("gui.stockpile_switch.invert_signal");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.STOCKPILE_SWITCH.get());

	private AllGuiTextures background;
	private StockpileSwitchTileEntity te;
	private int lastModification;

	private LerpedFloat cursor;
	private LerpedFloat cursorLane;

	public StockpileSwitchScreen(StockpileSwitchTileEntity te) {
		super(Lang.translate("gui.stockpile_switch.title"));
		background = AllGuiTextures.STOCKSWITCH;
		this.te = te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-20, 0);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		cursor = LerpedFloat.linear()
			.startWithValue(te.getLevelForDisplay());
		cursorLane = LerpedFloat.linear()
			.startWithValue(te.getState() ? 1 : 0);

		offBelow = new ScrollInput(x + 36, y + 40, 102, 18).withRange(0, 100)
			.titled(StringTextComponent.EMPTY.copy())
			.calling(state -> {
				lastModification = 0;
				offBelow.titled(Lang.translate("gui.stockpile_switch.move_to_upper_at", state));
				if (onAbove.getState() <= state) {
					onAbove.setState(state + 1);
					onAbove.onChanged();
				}
			})
			.setState((int) (te.offWhenBelow * 100));

		onAbove = new ScrollInput(x + 36, y + 18, 102, 18).withRange(1, 101)
			.titled(StringTextComponent.EMPTY.copy())
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
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		flipSignals = new IconButton(x + 14, y + 40, AllIcons.I_FLIP);
		flipSignals.setToolTip(invertSignal);
		widgets.add(flipSignals);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);

		AllGuiTextures.STOCKSWITCH_POWERED_LANE.draw(ms, this, x + 36, y + (te.isInverted() ? 18 : 40));
		AllGuiTextures.STOCKSWITCH_UNPOWERED_LANE.draw(ms, this, x + 36, y + (te.isInverted() ? 40 : 18));
		drawCenteredText(ms, textRenderer, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		AllGuiTextures sprite = AllGuiTextures.STOCKSWITCH_INTERVAL;
		float lowerBound = offBelow.getState();
		float upperBound = onAbove.getState();

		sprite.bind();
		drawTexture(ms, (int) (x + upperBound) + 37, y + 18, (int) (sprite.startX + upperBound), sprite.startY,
			(int) (sprite.width - upperBound), sprite.height);
		drawTexture(ms, x + 37, y + 40, sprite.startX, sprite.startY, (int) (lowerBound), sprite.height);

		AllGuiTextures.STOCKSWITCH_ARROW_UP.draw(ms, this, (int) (x + lowerBound + 36) - 2, y + 35);
		AllGuiTextures.STOCKSWITCH_ARROW_DOWN.draw(ms, this, (int) (x + upperBound + 36) - 3, y + 17);

		if (te.currentLevel != -1) {
			AllGuiTextures cursor = AllGuiTextures.STOCKSWITCH_CURSOR;
			ms.push();
			ms.translate(Math.min(99, this.cursor.getValue(partialTicks) * sprite.width),
				cursorLane.getValue(partialTicks) * 22, 0);
			cursor.draw(ms, this, x + 34, y + 19);
			ms.pop();
		}

		GuiGameElement.of(renderedItem)
				.<GuiGameElement.GuiRenderBuilder>at(x + background.width + 6, y + background.height - 56, -200)
				.scale(5)
				.render(ms);
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
			client.player.closeScreen();
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

}
