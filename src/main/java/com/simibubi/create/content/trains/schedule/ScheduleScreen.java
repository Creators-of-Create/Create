package com.simibubi.create.content.trains.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.condition.ScheduledDelay;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ModularGuiLine;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Indicator.State;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ScheduleScreen extends AbstractSimiContainerScreen<ScheduleMenu> {

	private static final int CARD_HEADER = 22;
	private static final int CARD_WIDTH = 195;

	private List<Rect2i> extraAreas = Collections.emptyList();

	private List<LerpedFloat> horizontalScrolls = new ArrayList<>();
	private LerpedFloat scroll = LerpedFloat.linear()
		.startWithValue(0);

	private Schedule schedule;

	private IconButton confirmButton;
	private IconButton cyclicButton;
	private Indicator cyclicIndicator;

	private IconButton resetProgress;
	private IconButton skipProgress;

	private ScheduleInstruction editingDestination;
	private ScheduleWaitCondition editingCondition;
	private SelectionScrollInput scrollInput;
	private Label scrollInputLabel;
	private IconButton editorConfirm, editorDelete;
	private ModularGuiLine editorSubWidgets;
	private Consumer<Boolean> onEditorClose;

	private DestinationSuggestions destinationSuggestions;

	public ScheduleScreen(ScheduleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		schedule = new Schedule();
		CompoundTag tag = menu.contentHolder.getOrCreateTag()
			.getCompound("Schedule");
		if (!tag.isEmpty())
			schedule = Schedule.fromTag(tag);
		menu.slotsActive = false;
		editorSubWidgets = new ModularGuiLine();
	}

	@Override
	protected void init() {
		AllGuiTextures bg = AllGuiTextures.SCHEDULE;
		setWindowSize(bg.width, bg.height);
		super.init();
		clearWidgets();

		confirmButton = new IconButton(leftPos + bg.width - 42, topPos + bg.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.player.closeContainer());
		addRenderableWidget(confirmButton);

		cyclicIndicator = new Indicator(leftPos + 21, topPos + 196, Components.immutableEmpty());
		cyclicIndicator.state = schedule.cyclic ? State.ON : State.OFF;
		addRenderableWidget(cyclicIndicator);

		cyclicButton = new IconButton(leftPos + 21, topPos + 202, AllIcons.I_REFRESH);
		cyclicButton.withCallback(() -> {
			schedule.cyclic = !schedule.cyclic;
			cyclicIndicator.state = schedule.cyclic ? State.ON : State.OFF;
		});

		List<Component> tip = cyclicButton.getToolTip();
		tip.add(Lang.translateDirect("schedule.loop"));
		tip.add(Lang.translateDirect("schedule.loop1")
			.withStyle(ChatFormatting.GRAY));
		tip.add(Lang.translateDirect("schedule.loop2")
			.withStyle(ChatFormatting.GRAY));

		addRenderableWidget(cyclicButton);

		resetProgress = new IconButton(leftPos + 45, topPos + 202, AllIcons.I_PRIORITY_VERY_HIGH);
		resetProgress.withCallback(() -> {
			schedule.savedProgress = 0;
			resetProgress.active = false;
		});
		resetProgress.active = schedule.savedProgress > 0 && !schedule.entries.isEmpty();
		resetProgress.setToolTip(Lang.translateDirect("schedule.reset"));
		addRenderableWidget(resetProgress);

		skipProgress = new IconButton(leftPos + 63, topPos + 202, AllIcons.I_PRIORITY_LOW);
		skipProgress.withCallback(() -> {
			schedule.savedProgress++;
			schedule.savedProgress %= schedule.entries.size();
			resetProgress.active = schedule.savedProgress > 0;
		});
		skipProgress.active = schedule.entries.size() > 1;
		skipProgress.setToolTip(Lang.translateDirect("schedule.skip"));
		addRenderableWidget(skipProgress);

		stopEditing();
		extraAreas = ImmutableList.of(new Rect2i(leftPos + bg.width, topPos + bg.height - 56, 48, 48));
		horizontalScrolls.clear();
		for (int i = 0; i < schedule.entries.size(); i++)
			horizontalScrolls.add(LerpedFloat.linear()
				.startWithValue(0));
	}

	protected void startEditing(IScheduleInput field, Consumer<Boolean> onClose, boolean allowDeletion) {
		onEditorClose = onClose;
		confirmButton.visible = false;
		cyclicButton.visible = false;
		cyclicIndicator.visible = false;
		skipProgress.visible = false;
		resetProgress.visible = false;

		scrollInput = new SelectionScrollInput(leftPos + 56, topPos + 65, 143, 16);
		scrollInputLabel = new Label(leftPos + 59, topPos + 69, Components.immutableEmpty()).withShadow();
		editorConfirm = new IconButton(leftPos + 56 + 168, topPos + 65 + 22, AllIcons.I_CONFIRM);
		if (allowDeletion)
			editorDelete = new IconButton(leftPos + 56 - 45, topPos + 65 + 22, AllIcons.I_TRASH);
		menu.slotsActive = true;
		menu.targetSlotsActive = field.slotsTargeted();

		for (int i = 0; i < field.slotsTargeted(); i++) {
			ItemStack item = field.getItem(i);
			menu.ghostInventory.setStackInSlot(i, item);
			AllPackets.getChannel().sendToServer(new GhostItemSubmitPacket(item, i));
		}

		if (field instanceof ScheduleInstruction instruction) {
			int startIndex = 0;
			for (int i = 0; i < Schedule.INSTRUCTION_TYPES.size(); i++)
				if (Schedule.INSTRUCTION_TYPES.get(i)
					.getFirst()
					.equals(instruction.getId()))
					startIndex = i;
			editingDestination = instruction;
			updateEditorSubwidgets(editingDestination);
			scrollInput.forOptions(Schedule.getTypeOptions(Schedule.INSTRUCTION_TYPES))
				.titled(Lang.translateDirect("schedule.instruction_type"))
				.writingTo(scrollInputLabel)
				.calling(index -> {
					ScheduleInstruction newlyCreated = Schedule.INSTRUCTION_TYPES.get(index)
						.getSecond()
						.get();
					if (editingDestination.getId()
						.equals(newlyCreated.getId()))
						return;
					editingDestination = newlyCreated;
					updateEditorSubwidgets(editingDestination);
				})
				.setState(startIndex);
		}

		if (field instanceof ScheduleWaitCondition cond) {
			int startIndex = 0;
			for (int i = 0; i < Schedule.CONDITION_TYPES.size(); i++)
				if (Schedule.CONDITION_TYPES.get(i)
					.getFirst()
					.equals(cond.getId()))
					startIndex = i;
			editingCondition = cond;
			updateEditorSubwidgets(editingCondition);
			scrollInput.forOptions(Schedule.getTypeOptions(Schedule.CONDITION_TYPES))
				.titled(Lang.translateDirect("schedule.condition_type"))
				.writingTo(scrollInputLabel)
				.calling(index -> {
					ScheduleWaitCondition newlyCreated = Schedule.CONDITION_TYPES.get(index)
						.getSecond()
						.get();
					if (editingCondition.getId()
						.equals(newlyCreated.getId()))
						return;
					editingCondition = newlyCreated;
					updateEditorSubwidgets(editingCondition);
				})
				.setState(startIndex);
		}

		addRenderableWidget(scrollInput);
		addRenderableWidget(scrollInputLabel);
		addRenderableWidget(editorConfirm);
		if (allowDeletion)
			addRenderableWidget(editorDelete);
	}

	private void onDestinationEdited(String text) {
		if (destinationSuggestions != null)
			destinationSuggestions.updateCommandInfo();
	}

	protected void stopEditing() {
		confirmButton.visible = true;
		cyclicButton.visible = true;
		cyclicIndicator.visible = true;
		skipProgress.visible = true;
		resetProgress.visible = true;

		if (editingCondition == null && editingDestination == null)
			return;

		destinationSuggestions = null;

		removeWidget(scrollInput);
		removeWidget(scrollInputLabel);
		removeWidget(editorConfirm);
		removeWidget(editorDelete);

		IScheduleInput editing = editingCondition == null ? editingDestination : editingCondition;
		for (int i = 0; i < editing.slotsTargeted(); i++) {
			editing.setItem(i, menu.ghostInventory.getStackInSlot(i));
			AllPackets.getChannel().sendToServer(new GhostItemSubmitPacket(ItemStack.EMPTY, i));
		}

		editorSubWidgets.saveValues(editing.getData());
		editorSubWidgets.forEach(this::removeWidget);
		editorSubWidgets.clear();

		editingCondition = null;
		editingDestination = null;
		editorConfirm = null;
		editorDelete = null;
		menu.slotsActive = false;
		init();
	}

	protected void updateEditorSubwidgets(IScheduleInput field) {
		destinationSuggestions = null;
		menu.targetSlotsActive = field.slotsTargeted();

		editorSubWidgets.forEach(this::removeWidget);
		editorSubWidgets.clear();
		field.initConfigurationWidgets(
			new ModularGuiLineBuilder(font, editorSubWidgets, getGuiLeft() + 77, getGuiTop() + 92).speechBubble());
		editorSubWidgets.loadValues(field.getData(), this::addRenderableWidget, this::addRenderableOnly);

		if (!(field instanceof DestinationInstruction))
			return;

		editorSubWidgets.forEach(e -> {
			if (!(e instanceof EditBox destinationBox))
				return;
			destinationSuggestions = new DestinationSuggestions(this.minecraft, this, destinationBox, this.font,
				getViableStations(field), topPos + 33);
			destinationSuggestions.setAllowSuggestions(true);
			destinationSuggestions.updateCommandInfo();
			destinationBox.setResponder(this::onDestinationEdited);
		});
	}

	private List<IntAttached<String>> getViableStations(IScheduleInput field) {
		GlobalRailwayManager railwayManager = Create.RAILWAYS.sided(null);
		Set<TrackGraph> viableGraphs = new HashSet<>(railwayManager.trackNetworks.values());

		for (ScheduleEntry entry : schedule.entries) {
			if (!(entry.instruction instanceof DestinationInstruction destination))
				continue;
			if (destination == field)
				continue;
			String filter = destination.getFilterForRegex();
			if (filter.isBlank())
				continue;
			Graphs: for (Iterator<TrackGraph> iterator = viableGraphs.iterator(); iterator.hasNext();) {
				TrackGraph trackGraph = iterator.next();
				for (GlobalStation station : trackGraph.getPoints(EdgePointType.STATION)) {
					if (station.name.matches(filter))
						continue Graphs;
				}
				iterator.remove();
			}
		}

		if (viableGraphs.isEmpty())
			viableGraphs = new HashSet<>(railwayManager.trackNetworks.values());

		Vec3 position = minecraft.player.position();
		Set<String> visited = new HashSet<>();

		return viableGraphs.stream()
			.flatMap(g -> g.getPoints(EdgePointType.STATION)
				.stream())
			.filter(station -> station.blockEntityPos != null)
			.filter(station -> visited.add(station.name))
			.map(station -> IntAttached.with((int) Vec3.atBottomCenterOf(station.blockEntityPos)
				.distanceTo(position), station.name))
			.toList();
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		scroll.tickChaser();
		for (LerpedFloat lerpedFloat : horizontalScrolls)
			lerpedFloat.tickChaser();

		if (destinationSuggestions != null)
			destinationSuggestions.tick();

		schedule.savedProgress =
			schedule.entries.isEmpty() ? 0 : Mth.clamp(schedule.savedProgress, 0, schedule.entries.size() - 1);
		resetProgress.active = schedule.savedProgress > 0;
		skipProgress.active = schedule.entries.size() > 1;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		partialTicks = minecraft.getFrameTime();

		if (menu.slotsActive)
			super.render(graphics, mouseX, mouseY, partialTicks);
		else {
			renderBackground(graphics);
			renderBg(graphics, partialTicks, mouseX, mouseY);
			for (Renderable widget : this.renderables)
				widget.render(graphics, mouseX, mouseY, partialTicks);
			renderForeground(graphics, mouseX, mouseY, partialTicks);
		}
	}

	protected void renderSchedule(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack matrixStack = graphics.pose();
		UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);

		UIRenderHelper.drawStretched(graphics, leftPos + 33, topPos + 16, 3, 173, 200, AllGuiTextures.SCHEDULE_STRIP_DARK);

		int yOffset = 25;
		List<ScheduleEntry> entries = schedule.entries;
		float scrollOffset = -scroll.getValue(partialTicks);

		for (int i = 0; i <= entries.size(); i++) {

			if (schedule.savedProgress == i && !schedule.entries.isEmpty()) {
				matrixStack.pushPose();
				float expectedY = scrollOffset + topPos + yOffset + 4;
				float actualY = Mth.clamp(expectedY, topPos + 18, topPos + 170);
				matrixStack.translate(0, actualY, 0);
				(expectedY == actualY ? AllGuiTextures.SCHEDULE_POINTER : AllGuiTextures.SCHEDULE_POINTER_OFFSCREEN)
					.render(graphics, leftPos, 0);
				matrixStack.popPose();
			}

			startStencil(graphics, leftPos + 16, topPos + 16, 220, 173);
			matrixStack.pushPose();
			matrixStack.translate(0, scrollOffset, 0);
			if (i == 0 || entries.size() == 0)
				UIRenderHelper.drawStretched(graphics, leftPos + 33, topPos + 16, 3, 10,
					-100, AllGuiTextures.SCHEDULE_STRIP_LIGHT);

			if (i == entries.size()) {
				if (i > 0)
					yOffset += 9;
				AllGuiTextures.SCHEDULE_STRIP_END.render(graphics, leftPos + 29, topPos + yOffset);
				AllGuiTextures.SCHEDULE_CARD_NEW.render(graphics, leftPos + 43, topPos + yOffset);
				matrixStack.popPose();
				endStencil();
				break;
			}

			ScheduleEntry scheduleEntry = entries.get(i);
			int cardY = yOffset;
			int cardHeight = renderScheduleEntry(graphics, scheduleEntry, cardY, mouseX, mouseY, partialTicks);
			yOffset += cardHeight;

			if (i + 1 < entries.size()) {
				AllGuiTextures.SCHEDULE_STRIP_DOTTED.render(graphics, leftPos + 29, topPos + yOffset - 3);
				yOffset += 10;
			}

			matrixStack.popPose();
			endStencil();

			if (!scheduleEntry.instruction.supportsConditions())
				continue;

			float h = cardHeight - 26;
			float y1 = cardY + 24 + scrollOffset;
			float y2 = y1 + h;
			if (y2 > 189)
				h -= y2 - 189;
			if (y1 < 16) {
				float correction = 16 - y1;
				y1 += correction;
				h -= correction;
			}

			if (h <= 0)
				continue;

			startStencil(graphics, leftPos + 43, topPos + y1, 161, h);
			matrixStack.pushPose();
			matrixStack.translate(0, scrollOffset, 0);
			renderScheduleConditions(graphics, scheduleEntry, cardY, mouseX, mouseY, partialTicks, cardHeight, i);
			matrixStack.popPose();
			endStencil();

			if (isConditionAreaScrollable(scheduleEntry)) {
				startStencil(graphics, leftPos + 16, topPos + 16, 220, 173);
				matrixStack.pushPose();
				matrixStack.translate(0, scrollOffset, 0);
				int center = (cardHeight - 8 + CARD_HEADER) / 2;
				float chaseTarget = horizontalScrolls.get(i)
					.getChaseTarget();
				if (!Mth.equal(chaseTarget, 0))
					AllGuiTextures.SCHEDULE_SCROLL_LEFT.render(graphics, leftPos + 40, topPos + cardY + center);
				if (!Mth.equal(chaseTarget, scheduleEntry.conditions.size() - 1))
					AllGuiTextures.SCHEDULE_SCROLL_RIGHT.render(graphics, leftPos + 203, topPos + cardY + center);
				matrixStack.popPose();
				endStencil();
			}
		}

		int zLevel = 200;
		graphics.fillGradient(leftPos + 16, topPos + 16, leftPos + 16 + 220, topPos + 16 + 10, zLevel, 0x77000000,
			0x00000000);
		graphics.fillGradient(leftPos + 16, topPos + 179, leftPos + 16 + 220, topPos + 179 + 10, zLevel, 0x00000000,
			0x77000000);
		UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());
	}

	public int renderScheduleEntry(GuiGraphics graphics, ScheduleEntry entry, int yOffset, int mouseX, int mouseY,
		float partialTicks) {
		int zLevel = -100;

		AllGuiTextures light = AllGuiTextures.SCHEDULE_CARD_LIGHT;
		AllGuiTextures medium = AllGuiTextures.SCHEDULE_CARD_MEDIUM;
		AllGuiTextures dark = AllGuiTextures.SCHEDULE_CARD_DARK;

		int cardWidth = CARD_WIDTH;
		int cardHeader = CARD_HEADER;
		int maxRows = 0;
		for (List<ScheduleWaitCondition> list : entry.conditions)
			maxRows = Math.max(maxRows, list.size());
		boolean supportsConditions = entry.instruction.supportsConditions();
		int cardHeight = cardHeader + (supportsConditions ? 24 + maxRows * 18 : 4);

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(leftPos + 25, topPos + yOffset, 0);

		UIRenderHelper.drawStretched(graphics, 0, 1, cardWidth, cardHeight - 2, zLevel, light);
		UIRenderHelper.drawStretched(graphics, 1, 0, cardWidth - 2, cardHeight, zLevel, light);
		UIRenderHelper.drawStretched(graphics, 1, 1, cardWidth - 2, cardHeight - 2, zLevel, dark);
		UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeight - 4, zLevel, medium);
		UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeader, zLevel,
			supportsConditions ? light : medium);

		AllGuiTextures.SCHEDULE_CARD_REMOVE.render(graphics, cardWidth - 14, 2);
		AllGuiTextures.SCHEDULE_CARD_DUPLICATE.render(graphics, cardWidth - 14, cardHeight - 14);

		int i = schedule.entries.indexOf(entry);
		if (i > 0)
			AllGuiTextures.SCHEDULE_CARD_MOVE_UP.render(graphics, cardWidth, cardHeader - 14);
		if (i < schedule.entries.size() - 1)
			AllGuiTextures.SCHEDULE_CARD_MOVE_DOWN.render(graphics, cardWidth, cardHeader);

		UIRenderHelper.drawStretched(graphics, 8, 0, 3, cardHeight + 10, zLevel,
			AllGuiTextures.SCHEDULE_STRIP_LIGHT);
		(supportsConditions ? AllGuiTextures.SCHEDULE_STRIP_TRAVEL : AllGuiTextures.SCHEDULE_STRIP_ACTION)
			.render(graphics, 4, 6);

		if (supportsConditions)
			AllGuiTextures.SCHEDULE_STRIP_WAIT.render(graphics, 4, 28);

		Pair<ItemStack, Component> destination = entry.instruction.getSummary();
		renderInput(graphics, destination, 26, 5, false, 100);
		entry.instruction.renderSpecialIcon(graphics, 30, 5);

		matrixStack.popPose();

		return cardHeight;
	}

	public void renderScheduleConditions(GuiGraphics graphics, ScheduleEntry entry, int yOffset, int mouseX,
		int mouseY, float partialTicks, int cardHeight, int entryIndex) {
		int cardWidth = CARD_WIDTH;
		int cardHeader = CARD_HEADER;

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(leftPos + 25, topPos + yOffset, 0);
		int xOffset = 26;
		float scrollOffset = getConditionScroll(entry, partialTicks, entryIndex);

		matrixStack.pushPose();
		matrixStack.translate(-scrollOffset, 0, 0);

		for (List<ScheduleWaitCondition> list : entry.conditions) {
			int maxWidth = getConditionColumnWidth(list);
			for (int i = 0; i < list.size(); i++) {
				ScheduleWaitCondition scheduleWaitCondition = list.get(i);
				Math.max(maxWidth, renderInput(graphics, scheduleWaitCondition.getSummary(), xOffset, 29 + i * 18,
					i != 0, maxWidth));
				scheduleWaitCondition.renderSpecialIcon(graphics, xOffset + 4, 29 + i * 18);
			}

			AllGuiTextures.SCHEDULE_CONDITION_APPEND.render(graphics, xOffset + (maxWidth - 10) / 2,
				29 + list.size() * 18);
			xOffset += maxWidth + 10;
		}

		AllGuiTextures.SCHEDULE_CONDITION_NEW.render(graphics, xOffset - 3, 29);
		matrixStack.popPose();

		if (xOffset + 16 > cardWidth - 26) {
			TransformStack.of(matrixStack)
				.rotateZDegrees(-90);
			int zLevel = 200;
			graphics.fillGradient(-cardHeight + 2, 18, -2 - cardHeader, 28, zLevel, 0x44000000, 0x00000000);
			graphics.fillGradient(-cardHeight + 2, cardWidth - 26, -2 - cardHeader, cardWidth - 16, zLevel, 0x00000000,
				0x44000000);
		}

		matrixStack.popPose();
	}

	private boolean isConditionAreaScrollable(ScheduleEntry entry) {
		int xOffset = 26;
		for (List<ScheduleWaitCondition> list : entry.conditions)
			xOffset += getConditionColumnWidth(list) + 10;
		return xOffset + 16 > CARD_WIDTH - 26;
	}

	private float getConditionScroll(ScheduleEntry entry, float partialTicks, int entryIndex) {
		float scrollOffset = 0;
		float scrollIndex = horizontalScrolls.get(entryIndex)
			.getValue(partialTicks);
		for (List<ScheduleWaitCondition> list : entry.conditions) {
			int maxWidth = getConditionColumnWidth(list);
			float partialOfThisColumn = Math.min(1, scrollIndex);
			scrollOffset += (maxWidth + 10) * partialOfThisColumn;
			scrollIndex -= partialOfThisColumn;
		}
		return scrollOffset;
	}

	private int getConditionColumnWidth(List<ScheduleWaitCondition> list) {
		int maxWidth = 0;
		for (ScheduleWaitCondition scheduleWaitCondition : list)
			maxWidth = Math.max(maxWidth, getFieldSize(32, scheduleWaitCondition.getSummary()));
		return maxWidth;
	}

	protected int renderInput(GuiGraphics graphics, Pair<ItemStack, Component> pair, int x, int y, boolean clean,
		int minSize) {
		ItemStack stack = pair.getFirst();
		Component text = pair.getSecond();
		boolean hasItem = !stack.isEmpty();
		int fieldSize = Math.min(getFieldSize(minSize, pair), 150);
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();

		AllGuiTextures left =
			clean ? AllGuiTextures.SCHEDULE_CONDITION_LEFT_CLEAN : AllGuiTextures.SCHEDULE_CONDITION_LEFT;
		AllGuiTextures middle = AllGuiTextures.SCHEDULE_CONDITION_MIDDLE;
		AllGuiTextures item = AllGuiTextures.SCHEDULE_CONDITION_ITEM;
		AllGuiTextures right = AllGuiTextures.SCHEDULE_CONDITION_RIGHT;

		matrixStack.translate(x, y, 0);
		UIRenderHelper.drawStretched(graphics, 0, 0, fieldSize, 16, -100, middle);
		left.render(graphics, clean ? 0 : -3, 0);
		right.render(graphics, fieldSize - 2, 0);
		if (hasItem)
			item.render(graphics, 3, 0);
		if (hasItem) {
			item.render(graphics, 3, 0);
			if (stack.getItem() != Items.STRUCTURE_VOID)
				GuiGameElement.of(stack)
					.at(4, 0)
					.render(graphics);
		}

		if (text != null)
			graphics.drawString(font, font.substrByWidth(text, 120)
				.getString(), hasItem ? 28 : 8, 4, 0xff_f2f2ee);

		matrixStack.popPose();
		return fieldSize;
	}

	private Component clickToEdit = Lang.translateDirect("gui.schedule.lmb_edit")
		.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
	private Component rClickToDelete = Lang.translateDirect("gui.schedule.rmb_remove")
		.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);

	public boolean action(@Nullable GuiGraphics graphics, double mouseX, double mouseY, int click) {
		if (editingCondition != null || editingDestination != null)
			return false;

		Component empty = Components.immutableEmpty();

		int mx = (int) mouseX;
		int my = (int) mouseY;
		int x = mx - leftPos - 25;
		int y = my - topPos - 25;
		if (x < 0 || x >= 205)
			return false;
		if (y < 0 || y >= 173)
			return false;
		y += scroll.getValue(0);

		List<ScheduleEntry> entries = schedule.entries;
		for (int i = 0; i < entries.size(); i++) {
			ScheduleEntry entry = entries.get(i);
			int maxRows = 0;
			for (List<ScheduleWaitCondition> list : entry.conditions)
				maxRows = Math.max(maxRows, list.size());
			int cardHeight = CARD_HEADER + (entry.instruction.supportsConditions() ? 24 + maxRows * 18 : 4);

			if (y >= cardHeight + 5) {
				y -= cardHeight + 10;
				if (y < 0)
					return false;
				continue;
			}

			int fieldSize = getFieldSize(100, entry.instruction.getSummary());
			if (x > 25 && x <= 25 + fieldSize && y > 4 && y <= 20) {
				List<Component> components = new ArrayList<>();
				components.addAll(entry.instruction.getTitleAs("instruction"));
				components.add(empty);
				components.add(clickToEdit);
				renderActionTooltip(graphics, components, mx, my);
				if (click == 0)
					startEditing(entry.instruction, confirmed -> {
						if (confirmed)
							entry.instruction = editingDestination;
					}, false);
				return true;
			}

			if (x > 180 && x <= 192) {
				if (y > 0 && y <= 14) {
					renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.remove_entry")),
						mx, my);
					if (click == 0) {
						entries.remove(entry);
						init();
					}
					return true;
				}
				if (y > cardHeight - 14) {
					renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.duplicate")), mx,
						my);
					if (click == 0) {
						entries.add(entries.indexOf(entry), entry.clone());
						init();
					}
					return true;
				}
			}

			if (x > 194) {
				if (y > 7 && y <= 20 && i > 0) {
					renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.move_up")), mx,
						my);
					if (click == 0) {
						entries.remove(entry);
						entries.add(i - 1, entry);
						init();
					}
					return true;
				}
				if (y > 20 && y <= 33 && i < entries.size() - 1) {
					renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.move_down")), mx,
						my);
					if (click == 0) {
						entries.remove(entry);
						entries.add(i + 1, entry);
						init();
					}
					return true;
				}
			}

			int center = (cardHeight - 8 + CARD_HEADER) / 2;
			if (y > center - 1 && y <= center + 7 && isConditionAreaScrollable(entry)) {
				float chaseTarget = horizontalScrolls.get(i)
					.getChaseTarget();
				if (x > 12 && x <= 19 && !Mth.equal(chaseTarget, 0)) {
					if (click == 0)
						horizontalScrolls.get(i)
							.chase(chaseTarget - 1, 0.5f, Chaser.EXP);
					return true;
				}
				if (x > 177 && x <= 184 && !Mth.equal(chaseTarget, entry.conditions.size() - 1)) {
					if (click == 0)
						horizontalScrolls.get(i)
							.chase(chaseTarget + 1, 0.5f, Chaser.EXP);
					return true;
				}
			}

			x -= 18;
			y -= 28;
			if (x < 0 || y < 0 || x > 160)
				return false;
			x += getConditionScroll(entry, 0, i) - 8;

			List<List<ScheduleWaitCondition>> columns = entry.conditions;
			for (int j = 0; j < columns.size(); j++) {
				List<ScheduleWaitCondition> conditions = columns.get(j);
				if (x < 0)
					return false;
				int w = getConditionColumnWidth(conditions);
				if (x >= w) {
					x -= w + 10;
					continue;
				}

				int row = y / 18;
				if (row < conditions.size() && row >= 0) {
					boolean canRemove = conditions.size() > 1 || columns.size() > 1;
					List<Component> components = new ArrayList<>();
					components.add(Lang.translateDirect("schedule.condition_type")
						.withStyle(ChatFormatting.GRAY));
					ScheduleWaitCondition condition = conditions.get(row);
					components.addAll(condition.getTitleAs("condition"));
					components.add(empty);
					components.add(clickToEdit);
					if (canRemove)
						components.add(rClickToDelete);
					renderActionTooltip(graphics, components, mx, my);
					if (canRemove && click == 1) {
						conditions.remove(row);
						if (conditions.isEmpty())
							columns.remove(conditions);
					}
					if (click == 0)
						startEditing(condition, confirmed -> {
							conditions.remove(row);
							if (confirmed) {
								conditions.add(row, editingCondition);
								return;
							}
							if (conditions.isEmpty())
								columns.remove(conditions);
						}, canRemove);
					return true;
				}

				if (y > 18 * conditions.size() && y <= 18 * conditions.size() + 10 && x >= w / 2 - 5 && x < w / 2 + 5) {
					renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.add_condition")), mx, my);
					if (click == 0)
						startEditing(new ScheduledDelay(), confirmed -> {
							if (confirmed)
								conditions.add(editingCondition);
						}, true);
					return true;
				}

				return false;
			}

			if (x < 0 || x > 15 || y > 20)
				return false;

			renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.alternative_condition")),
				mx, my);
			if (click == 0)
				startEditing(new ScheduledDelay(), confirmed -> {
					if (!confirmed)
						return;
					ArrayList<ScheduleWaitCondition> conditions = new ArrayList<>();
					conditions.add(editingCondition);
					columns.add(conditions);
				}, true);
			return true;
		}

		if (x < 18 || x > 33 || y > 14)
			return false;

		renderActionTooltip(graphics, ImmutableList.of(Lang.translateDirect("gui.schedule.add_entry")), mx, my);
		if (click == 0)
			startEditing(new DestinationInstruction(), confirmed -> {
				if (!confirmed)
					return;

				ScheduleEntry entry = new ScheduleEntry();
				ScheduledDelay delay = new ScheduledDelay();
				ArrayList<ScheduleWaitCondition> initialConditions = new ArrayList<>();
				initialConditions.add(delay);
				entry.instruction = editingDestination;
				entry.conditions.add(initialConditions);
				schedule.entries.add(entry);
			}, true);
		return true;
	}

	private void renderActionTooltip(@Nullable GuiGraphics graphics, List<Component> tooltip, int mx, int my) {
		if (graphics != null)
			graphics.renderTooltip(font, tooltip, Optional.empty(), mx, my);
	}

	private int getFieldSize(int minSize, Pair<ItemStack, Component> pair) {
		ItemStack stack = pair.getFirst();
		Component text = pair.getSecond();
		boolean hasItem = !stack.isEmpty();
		return Math.max((text == null ? 0 : font.width(text)) + (hasItem ? 20 : 0) + 16, minSize);
	}

	protected void startStencil(GuiGraphics graphics, float x, float y, float w, float h) {
		RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(x, y, 0);
		matrixStack.scale(w, h, 1);
		graphics.fillGradient(0, 0, 1, 1, -100, 0xff000000, 0xff000000);
		matrixStack.popPose();

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	protected void endStencil() {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (destinationSuggestions != null
			&& destinationSuggestions.mouseClicked((int) pMouseX, (int) pMouseY, pButton))
			return true;
		if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY) && onEditorClose != null) {
			onEditorClose.accept(true);
			stopEditing();
			return true;
		}
		if (editorDelete != null && editorDelete.isMouseOver(pMouseX, pMouseY) && onEditorClose != null) {
			onEditorClose.accept(false);
			stopEditing();
			return true;
		}
		if (action(null, pMouseX, pMouseY, pButton))
			return true;

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (destinationSuggestions != null && destinationSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers))
			return true;
		if (editingCondition == null && editingDestination == null)
			return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		boolean hitEnter = getFocused() instanceof EditBox && (pKeyCode == 257 || pKeyCode == 335);
		boolean hitE = getFocused() == null && minecraft.options.keyInventory.isActiveAndMatches(mouseKey);
		if (hitE || hitEnter) {
			onEditorClose.accept(true);
			stopEditing();
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (destinationSuggestions != null && destinationSuggestions.mouseScrolled(Mth.clamp(pDelta, -1.0D, 1.0D)))
			return true;
		if (editingCondition != null || editingDestination != null)
			return super.mouseScrolled(pMouseX, pMouseY, pDelta);

		if (hasShiftDown()) {
			List<ScheduleEntry> entries = schedule.entries;
			int y = (int) (pMouseY - topPos - 25 + scroll.getValue());
			for (int i = 0; i < entries.size(); i++) {
				ScheduleEntry entry = entries.get(i);
				int maxRows = 0;
				for (List<ScheduleWaitCondition> list : entry.conditions)
					maxRows = Math.max(maxRows, list.size());
				int cardHeight = CARD_HEADER + 24 + maxRows * 18;

				if (y >= cardHeight) {
					y -= cardHeight + 9;
					if (y < 0)
						break;
					continue;
				}

				if (!isConditionAreaScrollable(entry))
					break;
				if (y < 24)
					break;
				if (pMouseX < leftPos + 25)
					break;
				if (pMouseX > leftPos + 205)
					break;
				float chaseTarget = horizontalScrolls.get(i)
					.getChaseTarget();
				if (pDelta > 0 && !Mth.equal(chaseTarget, 0)) {
					horizontalScrolls.get(i)
						.chase(chaseTarget - 1, 0.5f, Chaser.EXP);
					return true;
				}
				if (pDelta < 0 && !Mth.equal(chaseTarget, entry.conditions.size() - 1)) {
					horizontalScrolls.get(i)
						.chase(chaseTarget + 1, 0.5f, Chaser.EXP);
					return true;
				}
				return false;
			}
		}

		float chaseTarget = scroll.getChaseTarget();
		float max = 40 - 173;
		for (ScheduleEntry scheduleEntry : schedule.entries) {
			int maxRows = 0;
			for (List<ScheduleWaitCondition> list : scheduleEntry.conditions)
				maxRows = Math.max(maxRows, list.size());
			max += CARD_HEADER + 24 + maxRows * 18 + 10;
		}
		if (max > 0) {
			chaseTarget -= pDelta * 12;
			chaseTarget = Mth.clamp(chaseTarget, 0, max);
			scroll.chase((int) chaseTarget, 0.7f, Chaser.EXP);
		} else
			scroll.chase(0, 0.7f, Chaser.EXP);

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack matrixStack = graphics.pose();
		if (destinationSuggestions != null) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 500);
			destinationSuggestions.render(graphics, mouseX, mouseY);
			matrixStack.popPose();
		}

		super.renderForeground(graphics, mouseX, mouseY, partialTicks);

		GuiGameElement.of(menu.contentHolder).<GuiGameElement
			.GuiRenderBuilder>at(leftPos + AllGuiTextures.SCHEDULE.width, topPos + AllGuiTextures.SCHEDULE.height - 56,
				-200)
			.scale(3)
			.render(graphics);
		action(graphics, mouseX, mouseY, -1);

		if (editingCondition == null && editingDestination == null)
			return;

		int x = leftPos + 53;
		int y = topPos + 87;
		if (mouseX < x || mouseY < y || mouseX >= x + 120 || mouseY >= y + 18)
			return;

		IScheduleInput rendered = editingCondition == null ? editingDestination : editingCondition;

		for (int i = 0; i < Math.max(1, rendered.slotsTargeted()); i++) {
			List<Component> secondLineTooltip = rendered.getSecondLineTooltip(i);
			if (secondLineTooltip == null || (hoveredSlot != menu.getSlot(36 + i) || !hoveredSlot.getItem()
				.isEmpty()))
				continue;
			renderActionTooltip(graphics, secondLineTooltip, mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
		AllGuiTextures.SCHEDULE.render(graphics, leftPos, topPos);
		FormattedCharSequence formattedcharsequence = title.getVisualOrderText();
		int center = leftPos + (AllGuiTextures.SCHEDULE.width - 8) / 2;
		graphics.drawString(font, formattedcharsequence, (float) (center - font.width(formattedcharsequence) / 2),
			(float) topPos + 4, 0x505050, false);
		renderSchedule(graphics, pMouseX, pMouseY, pPartialTick);

		if (editingCondition == null && editingDestination == null)
			return;

		graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		AllGuiTextures.SCHEDULE_EDITOR.render(graphics, leftPos - 2, topPos + 40);
		AllGuiTextures.PLAYER_INVENTORY.render(graphics, leftPos + 38, topPos + 122);
		graphics.drawString(font, playerInventoryTitle, leftPos + 46, topPos + 128, 0x505050, false);

		formattedcharsequence = editingCondition == null ? Lang.translateDirect("schedule.instruction.editor")
			.getVisualOrderText()
			: Lang.translateDirect("schedule.condition.editor")
				.getVisualOrderText();
		graphics.drawString(font, formattedcharsequence, (float) (center - font.width(formattedcharsequence) / 2),
			(float) topPos + 44, 0x505050, false);

		IScheduleInput rendered = editingCondition == null ? editingDestination : editingCondition;

		for (int i = 0; i < rendered.slotsTargeted(); i++)
			AllGuiTextures.SCHEDULE_EDITOR_ADDITIONAL_SLOT.render(graphics, leftPos + 53 + 20 * i, topPos + 87);

		if (rendered.slotsTargeted() == 0 && !rendered.renderSpecialIcon(graphics, leftPos + 54, topPos + 88)) {
			Pair<ItemStack, Component> summary = rendered.getSummary();
			ItemStack icon = summary.getFirst();
			if (icon.isEmpty())
				icon = rendered.getSecondLineIcon();
			if (icon.isEmpty())
				AllGuiTextures.SCHEDULE_EDITOR_INACTIVE_SLOT.render(graphics, leftPos + 53, topPos + 87);
			else
				GuiGameElement.of(icon)
					.at(leftPos + 54, topPos + 88)
					.render(graphics);
		}

		PoseStack pPoseStack = graphics.pose();
		pPoseStack.pushPose();
		pPoseStack.translate(0, getGuiTop() + 87, 0);
		editorSubWidgets.renderWidgetBG(getGuiLeft() + 77, graphics);
		pPoseStack.popPose();
	}

	@Override
	public void removed() {
		super.removed();
		AllPackets.getChannel().sendToServer(new ScheduleEditPacket(schedule));
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	public Font getFont() {
		return font;
	}

}
