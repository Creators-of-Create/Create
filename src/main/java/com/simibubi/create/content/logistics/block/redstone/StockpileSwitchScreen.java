package com.simibubi.create.content.logistics.block.redstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class StockpileSwitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private ScrollInput onAbove;
	private IconButton confirmButton;
	private IconButton flipSignals;

	private final Component invertSignal = Lang.translateDirect("gui.stockpile_switch.invert_signal");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.STOCKPILE_SWITCH.get());

	private AllGuiTextures background;
	private StockpileSwitchTileEntity te;
	private int lastModification;

	private LerpedFloat cursor;
	private LerpedFloat cursorLane;

	public StockpileSwitchScreen(StockpileSwitchTileEntity te) {
		super(Lang.translateDirect("gui.stockpile_switch.title"));
		background = AllGuiTextures.STOCKSWITCH;
		this.te = te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-20, 0);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		cursor = LerpedFloat.linear()
			.startWithValue(te.getLevelForDisplay());
		cursorLane = LerpedFloat.linear()
			.startWithValue(te.getState() ? 1 : 0);

		offBelow = new ScrollInput(x + 36, y + 40, 102, 18).withRange(0, 100)
			.titled(Components.empty())
			.calling(state -> {
				lastModification = 0;
				offBelow.titled(Lang.translateDirect("gui.stockpile_switch.move_to_upper_at", state));
				if (onAbove.getState() <= state) {
					onAbove.setState(state + 1);
					onAbove.onChanged();
				}
			})
			.setState((int) (te.offWhenBelow * 100));

		onAbove = new ScrollInput(x + 36, y + 18, 102, 18).withRange(1, 101)
			.titled(Components.empty())
			.calling(state -> {
				lastModification = 0;
				onAbove.titled(Lang.translateDirect("gui.stockpile_switch.move_to_lower_at", state));
				if (offBelow.getState() >= state) {
					offBelow.setState(state - 1);
					offBelow.onChanged();
				}
			})
			.setState((int) (te.onWhenAbove * 100));

		onAbove.onChanged();
		offBelow.onChanged();

		addRenderableWidget(onAbove);
		addRenderableWidget(offBelow);

		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			onClose();
		});
		addRenderableWidget(confirmButton);

		flipSignals = new IconButton(x + 14, y + 40, AllIcons.I_FLIP);
		flipSignals.withCallback(() -> {
			send(!te.isInverted());
		});
		flipSignals.setToolTip(invertSignal);
		addRenderableWidget(flipSignals);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(ms, x, y, this);

		AllGuiTextures.STOCKSWITCH_POWERED_LANE.render(ms, x + 36, y + (te.isInverted() ? 18 : 40), this);
		AllGuiTextures.STOCKSWITCH_UNPOWERED_LANE.render(ms, x + 36, y + (te.isInverted() ? 40 : 18), this);
		drawCenteredString(ms, font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		AllGuiTextures sprite = AllGuiTextures.STOCKSWITCH_INTERVAL;
		float lowerBound = offBelow.getState();
		float upperBound = onAbove.getState();

		sprite.bind();
		blit(ms, (int) (x + upperBound) + 37, y + 18, (int) (sprite.startX + upperBound), sprite.startY,
			(int) (sprite.width - upperBound), sprite.height);
		blit(ms, x + 37, y + 40, sprite.startX, sprite.startY, (int) (lowerBound), sprite.height);

		AllGuiTextures.STOCKSWITCH_ARROW_UP.render(ms, (int) (x + lowerBound + 36) - 2, y + 35, this);
		AllGuiTextures.STOCKSWITCH_ARROW_DOWN.render(ms, (int) (x + upperBound + 36) - 3, y + 17, this);

		if (te.currentLevel != -1) {
			AllGuiTextures cursor = AllGuiTextures.STOCKSWITCH_CURSOR;
			ms.pushPose();
			ms.translate(Math.min(99, this.cursor.getValue(partialTicks) * sprite.width),
				cursorLane.getValue(partialTicks) * 22, 0);
			cursor.render(ms, x + 34, y + 19, this);
			ms.popPose();
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
		AllPackets.channel.sendToServer(new ConfigureStockswitchPacket(te.getBlockPos(), offBelow.getState() / 100f,
			onAbove.getState() / 100f, invert));
	}

}
