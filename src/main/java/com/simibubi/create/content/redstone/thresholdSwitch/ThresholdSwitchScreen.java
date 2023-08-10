package com.simibubi.create.content.redstone.thresholdSwitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ThresholdSwitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private ScrollInput onAbove;
	private IconButton confirmButton;
	private IconButton flipSignals;

	private final Component invertSignal = CreateLang.translateDirect("gui.threshold_switch.invert_signal");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.THRESHOLD_SWITCH.get());

	private AllGuiTextures background;
	private ThresholdSwitchBlockEntity blockEntity;
	private int lastModification;

	private LerpedFloat cursor;
	private LerpedFloat cursorLane;

	public ThresholdSwitchScreen(ThresholdSwitchBlockEntity be) {
		super(CreateLang.translateDirect("gui.threshold_switch.title"));
		background = AllGuiTextures.STOCKSWITCH;
		this.blockEntity = be;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight());
		setWindowOffset(-20, 0);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		cursor = LerpedFloat.linear()
			.startWithValue(blockEntity.getLevelForDisplay());
		cursorLane = LerpedFloat.linear()
			.startWithValue(blockEntity.getState() ? 1 : 0);

		offBelow = new ScrollInput(x + 36, y + 42, 102, 18).withRange(0, 100)
			.titled(Components.empty())
			.calling(state -> {
				lastModification = 0;
				offBelow.titled(CreateLang.translateDirect("gui.threshold_switch.move_to_upper_at", state));
				if (onAbove.getState() <= state) {
					onAbove.setState(state + 1);
					onAbove.onChanged();
				}
			})
			.setState((int) (blockEntity.offWhenBelow * 100));

		onAbove = new ScrollInput(x + 36, y + 20, 102, 18).withRange(1, 101)
			.titled(Components.empty())
			.calling(state -> {
				lastModification = 0;
				onAbove.titled(CreateLang.translateDirect("gui.threshold_switch.move_to_lower_at", state));
				if (offBelow.getState() >= state) {
					offBelow.setState(state - 1);
					offBelow.onChanged();
				}
			})
			.setState((int) (blockEntity.onWhenAbove * 100));

		onAbove.onChanged();
		offBelow.onChanged();

		addRenderableWidget(onAbove);
		addRenderableWidget(offBelow);

		confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			onClose();
		});
		addRenderableWidget(confirmButton);

		flipSignals = new IconButton(x + background.getWidth() - 62, y + background.getHeight() - 24, AllIcons.I_FLIP);
		flipSignals.withCallback(() -> {
			send(!blockEntity.isInverted());
		});
		flipSignals.setToolTip(invertSignal);
		addRenderableWidget(flipSignals);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(ms, x, y, this);

		AllGuiTextures.STOCKSWITCH_POWERED_LANE.render(ms, x + 37, y + (blockEntity.isInverted() ? 20 : 42), this);
		AllGuiTextures.STOCKSWITCH_UNPOWERED_LANE.render(ms, x + 37, y + (blockEntity.isInverted() ? 42 : 20), this);
		font.draw(ms, title, x + (background.getWidth() - 8) / 2 - font.width(title) / 2, y + 4, 0x592424);

		AllGuiTextures sprite = AllGuiTextures.STOCKSWITCH_INTERVAL;
		float lowerBound = offBelow.getState();
		float upperBound = onAbove.getState();

		sprite.bind();
		blit(ms, (int) (x + upperBound) + 37, y + 20, (int) (sprite.getStartX() + upperBound), sprite.getStartY(),
			(int) (sprite.getWidth() - upperBound), sprite.getHeight());
		blit(ms, x + 37, y + 42, sprite.getStartX(), sprite.getStartY(), (int) (lowerBound), sprite.getHeight());

		AllGuiTextures.STOCKSWITCH_ARROW_UP.render(ms, (int) (x + lowerBound + 36) - 2, y + 37, this);
		AllGuiTextures.STOCKSWITCH_ARROW_DOWN.render(ms, (int) (x + upperBound + 36) - 3, y + 19, this);

		if (blockEntity.currentLevel != -1) {
			AllGuiTextures cursor = AllGuiTextures.STOCKSWITCH_CURSOR;
			ms.pushPose();
			ms.translate(Math.min(99, this.cursor.getValue(partialTicks) * sprite.getWidth()),
				cursorLane.getValue(partialTicks) * 22, 0);
			cursor.render(ms, x + 34, y + 21, this);
			ms.popPose();
		}

		GuiGameElement.of(renderedItem).<GuiGameElement
			.GuiRenderBuilder>at(x + background.getWidth() + 6, y + background.getHeight() - 56, -200)
			.scale(5)
			.render(ms);
	}

	@Override
	public void tick() {
		super.tick();

		cursor.chase(blockEntity.getLevelForDisplay(), 1 / 4f, Chaser.EXP);
		cursor.tickChaser();
		cursorLane.chase(blockEntity.getState() ? 1 : 0, 1 / 4f, Chaser.EXP);
		cursorLane.tickChaser();

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 20) {
			lastModification = -1;
			send(blockEntity.isInverted());
		}
	}

	@Override
	public void removed() {
		send(blockEntity.isInverted());
	}

	protected void send(boolean invert) {
		AllPackets.getChannel()
			.sendToServer(new ConfigureThresholdSwitchPacket(blockEntity.getBlockPos(), offBelow.getState() / 100f,
				onAbove.getState() / 100f, invert));
	}

}
