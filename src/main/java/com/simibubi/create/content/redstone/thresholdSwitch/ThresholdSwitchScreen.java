package com.simibubi.create.content.redstone.thresholdSwitch;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity.ThresholdType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.createmod.catnip.platform.CatnipServices;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.ponder.foundation.ui.PonderTagScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;

public class ThresholdSwitchScreen extends AbstractSimiScreen {

	private ScrollInput offBelow;
	private ScrollInput onAbove;
	private SelectionScrollInput inStacks;

	private IconButton confirmButton;
	private IconButton flipSignals;

	private final Component invertSignal = CreateLang.translateDirect("gui.threshold_switch.invert_signal");
	private final ItemStack renderedItem = new ItemStack(AllBlocks.THRESHOLD_SWITCH.get());

	private AllGuiTextures background;
	private ThresholdSwitchBlockEntity blockEntity;
	private int lastModification;

	public ThresholdSwitchScreen(ThresholdSwitchBlockEntity be) {
		super(CreateLang.translateDirect("gui.threshold_switch.title"));
		background = AllGuiTextures.THRESHOLD_SWITCH;
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

		inStacks = (SelectionScrollInput) new SelectionScrollInput(x + 100, y + 23, 52, 42)
			.forOptions(List.of(CreateLang.translateDirect("schedule.condition.threshold.items"),
				CreateLang.translateDirect("schedule.condition.threshold.stacks")))
			.titled(CreateLang.translateDirect("schedule.condition.threshold.item_measure"))
			.setState(blockEntity.inStacks ? 1 : 0);

		offBelow = new ScrollInput(x + 48, y + 47, 1, 18)
			.withRange(blockEntity.getMinLevel(), blockEntity.getMaxLevel() + 1 - getValueStep())
			.titled(CreateLang.translateDirect("gui.threshold_switch.lower_threshold"))
			.calling(state -> {
				lastModification = 0;
				int valueStep = getValueStep();

				if (onAbove.getState() / valueStep == 0 && state / valueStep == 0)
					return;
				
				if (onAbove.getState() / valueStep <= state / valueStep) {
					onAbove.setState((state + valueStep) / valueStep * valueStep);
					onAbove.onChanged();
				}
			})
			.withStepFunction(sc -> sc.shift ? 10 * getValueStep() : getValueStep())
			.setState(blockEntity.offWhenBelow);

		onAbove = new ScrollInput(x + 48, y + 23, 1, 18)
			.withRange(blockEntity.getMinLevel() + getValueStep(), blockEntity.getMaxLevel() + 1)
			.titled(CreateLang.translateDirect("gui.threshold_switch.upper_threshold"))
			.calling(state -> {
				lastModification = 0;
				int valueStep = getValueStep();

				if (offBelow.getState() / valueStep == 0 && state / valueStep == 0)
					return;

				if (offBelow.getState() / valueStep >= state / valueStep) {
					offBelow.setState((state - valueStep) / valueStep * valueStep);
					offBelow.onChanged();
				}
			})
			.withStepFunction(sc -> sc.shift ? 10 * getValueStep() : getValueStep())
			.setState(blockEntity.onWhenAbove);

		onAbove.onChanged();
		offBelow.onChanged();

		addRenderableWidget(onAbove);
		addRenderableWidget(offBelow);
		addRenderableWidget(inStacks);

		confirmButton =
			new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> onClose());
		addRenderableWidget(confirmButton);

		flipSignals = new IconButton(x + background.getWidth() - 62, y + background.getHeight() - 24, AllIcons.I_FLIP);
		flipSignals.withCallback(() -> send(!blockEntity.isInverted()));
		flipSignals.setToolTip(invertSignal);
		addRenderableWidget(flipSignals);

		updateInputBoxes();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
		int itemX = guiLeft + 13;
		int itemY = guiTop + 80;
		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.THRESHOLD_SWITCH_TARGETS));
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, pButton);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);
		graphics.drawString(font, title, x + background.getWidth() / 2 - font.width(title) / 2, y + 4, 0x592424, false);

		ThresholdType typeOfCurrentTarget = blockEntity.getTypeOfCurrentTarget();
		boolean forItems = typeOfCurrentTarget == ThresholdType.ITEM;
		AllGuiTextures inputBg =
			forItems ? AllGuiTextures.THRESHOLD_SWITCH_ITEMCOUNT_INPUTS : AllGuiTextures.THRESHOLD_SWITCH_MISC_INPUTS;

		inputBg.render(graphics, x + 44, y + 21);
		inputBg.render(graphics, x + 44, y + 21 + 24);

		int valueStep = 1;
		boolean stacks = inStacks.getState() == 1;
		if (typeOfCurrentTarget == ThresholdType.FLUID)
			valueStep = 1000;

		if (forItems) {
			Component suffix =
				inStacks.getState() == 0 ? CreateLang.translateDirect("schedule.condition.threshold.items")
					: CreateLang.translateDirect("schedule.condition.threshold.stacks");
			valueStep = inStacks.getState() == 0 ? 1 : 64;
			graphics.drawString(font, suffix, x + 105, y + 28, 0xFFFFFFFF, true);
			graphics.drawString(font, suffix, x + 105, y + 28 + 24, 0xFFFFFFFF, true);

		}

		graphics.drawString(font,
			Component.literal("\u2265 " + (typeOfCurrentTarget == ThresholdType.UNSUPPORTED ? ""
				: forItems ? onAbove.getState() / valueStep
				: blockEntity.format(onAbove.getState() / valueStep, stacks)
				.getString())),
			x + 53, y + 28, 0xFFFFFFFF, true);
		graphics.drawString(font,
			Component.literal("\u2264 " + (typeOfCurrentTarget == ThresholdType.UNSUPPORTED ? ""
				: forItems ? offBelow.getState() / valueStep
				: blockEntity.format(offBelow.getState() / valueStep, stacks)
				.getString())),
			x + 53, y + 28 + 24, 0xFFFFFFFF, true);

		GuiGameElement.of(renderedItem).<GuiGameElement
				.GuiRenderBuilder>at(x + background.getWidth() + 6, y + background.getHeight() - 56, -200)
			.scale(5)
			.render(graphics);

		int itemX = x + 13;
		int itemY = y + 80;

		ItemStack displayItem = blockEntity.getDisplayItemForScreen();
		GuiGameElement.of(displayItem.isEmpty() ? new ItemStack(Items.BARRIER) : displayItem).<GuiGameElement
				.GuiRenderBuilder>at(itemX, itemY, 0)
			.render(graphics);

		int torchX = x + 23;
		int torchY = y + 24;

		boolean highlightTopRow = blockEntity.isInverted() ^ blockEntity.isPowered();
		AllGuiTextures.THRESHOLD_SWITCH_CURRENT_STATE.render(graphics, torchX - 3,
			torchY - 4 + (highlightTopRow ? 0 : 24));

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(torchX - 5, torchY + 14, 200);
		TransformStack.of(ms)
			.rotateXDegrees(-22.5f)
			.rotateYDegrees(45);

		for (boolean power : Iterate.trueAndFalse) {
			GuiGameElement.of(Blocks.REDSTONE_TORCH.defaultBlockState()
					.setValue(RedstoneTorchBlock.LIT, blockEntity.isInverted() ^ power))
				.scale(20)
				.render(graphics);
			ms.translate(0, 26, 0);
		}

		ms.popPose();

		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			ArrayList<Component> list = new ArrayList<>();
			if (displayItem.isEmpty()) {
				list.add(CreateLang.translateDirect("gui.threshold_switch.not_attached"));
				list.add(CreateLang.translateDirect("display_link.view_compatible")
					.withStyle(ChatFormatting.DARK_GRAY));
				graphics.renderComponentTooltip(font, list, mouseX, mouseY);
				return;
			}

			list.add(displayItem.getHoverName());
			if (typeOfCurrentTarget == ThresholdType.UNSUPPORTED) {
				list.add(CreateLang.translateDirect("gui.threshold_switch.incompatible")
					.withStyle(ChatFormatting.GRAY));
				list.add(CreateLang.translateDirect("display_link.view_compatible")
					.withStyle(ChatFormatting.DARK_GRAY));
				graphics.renderComponentTooltip(font, list, mouseX, mouseY);
				return;
			}

			CreateLang
				.translate("gui.threshold_switch.currently",
					blockEntity.format(blockEntity.currentLevel / valueStep, stacks))
				.style(ChatFormatting.DARK_AQUA)
				.addTo(list);

			if (blockEntity.currentMinLevel / valueStep == 0)
				CreateLang
					.translate("gui.threshold_switch.range_max",
						blockEntity.format(blockEntity.currentMaxLevel / valueStep, stacks))
					.style(ChatFormatting.GRAY)
					.addTo(list);
			else
				CreateLang
					.translate("gui.threshold_switch.range", blockEntity.currentMinLevel / valueStep,
						blockEntity.format(blockEntity.currentMaxLevel / valueStep, stacks))
					.style(ChatFormatting.GRAY)
					.addTo(list);

			list.add(CreateLang.translateDirect("display_link.view_compatible")
				.withStyle(ChatFormatting.DARK_GRAY));

			graphics.renderComponentTooltip(font, list, mouseX, mouseY);
			return;
		}

		for (boolean power : Iterate.trueAndFalse) {
			int thisTorchY = power ? torchY : torchY + 26;
			if (mouseX >= torchX && mouseX < torchX + 16 && mouseY >= thisTorchY && mouseY < thisTorchY + 16) {
				graphics.renderComponentTooltip(font,
					List.of(CreateLang
						.translate(power ^ blockEntity.isInverted() ? "gui.threshold_switch.power_on_when"
							: "gui.threshold_switch.power_off_when")
						.color(AbstractSimiWidget.HEADER_RGB)
						.component()),
					mouseX, mouseY);
				return;
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 20) {
			lastModification = -1;
			send(blockEntity.isInverted());
		}

		if (inStacks == null)
			return;

		updateInputBoxes();
	}

	private void updateInputBoxes() {
		ThresholdType typeOfCurrentTarget = blockEntity.getTypeOfCurrentTarget();
		boolean forItems = typeOfCurrentTarget == ThresholdType.ITEM;
		final int valueStep = getValueStep();
		inStacks.active = inStacks.visible = forItems;
		onAbove.setWidth(forItems ? 48 : 103);
		offBelow.setWidth(forItems ? 48 : 103);

		onAbove.visible = typeOfCurrentTarget != ThresholdType.UNSUPPORTED;
		offBelow.visible = typeOfCurrentTarget != ThresholdType.UNSUPPORTED;

		int min = blockEntity.currentMinLevel + valueStep;
		int max = blockEntity.currentMaxLevel;
		onAbove.withRange(min, max + 1);
		int roundedState = Mth.clamp((onAbove.getState() / valueStep) * valueStep, min, max);
		if (roundedState != onAbove.getState()) {
			onAbove.setState(roundedState);
			onAbove.onChanged();
		}

		min = blockEntity.currentMinLevel;
		max = blockEntity.currentMaxLevel - valueStep;
		offBelow.withRange(min, max + 1);
		roundedState = Mth.clamp((offBelow.getState() / valueStep) * valueStep, min, max);
		if (roundedState != offBelow.getState()) {
			offBelow.setState(roundedState);
			offBelow.onChanged();
		}
	}

	private int getValueStep() {
		boolean stacks = inStacks.getState() == 1;
		int valueStep = 1;
		if (blockEntity.getTypeOfCurrentTarget() == ThresholdType.FLUID)
			valueStep = 1000;
		else if (stacks)
			valueStep = 64;
		return valueStep;
	}

	@Override
	public void removed() {
		send(blockEntity.isInverted());
	}

	protected void send(boolean invert) {
		CatnipServices.NETWORK.sendToServer(new ConfigureThresholdSwitchPacket(blockEntity.getBlockPos(), offBelow.getState(),
				onAbove.getState(), invert, inStacks.getState() == 1));
	}

}
