package com.simibubi.create.content.trains.station;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class StationScreen extends AbstractStationScreen {

	private EditBox nameBox;
	private EditBox trainNameBox;
	private IconButton newTrainButton;
	private IconButton disassembleTrainButton;
	private IconButton dropScheduleButton;

	private int leavingAnimation;
	private LerpedFloat trainPosition;
	private DoorControl doorControl;
	
	private ScrollInput colorTypeScroll;
	private int messedWithColors;

	private boolean switchingToAssemblyMode;

	public StationScreen(StationBlockEntity be, GlobalStation station) {
		super(be, station);
		background = AllGuiTextures.STATION;
		leavingAnimation = 0;
		trainPosition = LerpedFloat.linear()
			.startWithValue(0);
		switchingToAssemblyMode = false;
		doorControl = be.doorControls.mode;
	}

	@Override
	protected void init() {
		super.init();
		int x = guiLeft;
		int y = guiTop;

		Consumer<String> onTextChanged;

		onTextChanged = s -> nameBox.setX(nameBoxX(s, nameBox));
		nameBox = new EditBox(new NoShadowFontWrapper(font), x + 23, y + 4, background.width - 20, 10,
			Components.literal(station.name));
		nameBox.setBordered(false);
		nameBox.setMaxLength(25);
		nameBox.setTextColor(0x592424);
		nameBox.setValue(station.name);
		nameBox.setFocused(false);
		nameBox.mouseClicked(0, 0, 0);
		nameBox.setResponder(onTextChanged);
		nameBox.setX(nameBoxX(nameBox.getValue(), nameBox));
		addRenderableWidget(nameBox);

		Runnable assemblyCallback = () -> {
			switchingToAssemblyMode = true;
			minecraft.setScreen(new AssemblyScreen(blockEntity, station));
		};

		newTrainButton = new WideIconButton(x + 84, y + 65, AllGuiTextures.I_NEW_TRAIN);
		newTrainButton.withCallback(assemblyCallback);
		addRenderableWidget(newTrainButton);

		disassembleTrainButton = new WideIconButton(x + 94, y + 65, AllGuiTextures.I_DISASSEMBLE_TRAIN);
		disassembleTrainButton.active = false;
		disassembleTrainButton.visible = false;
		disassembleTrainButton.withCallback(assemblyCallback);
		addRenderableWidget(disassembleTrainButton);

		dropScheduleButton = new IconButton(x + 73, y + 65, AllIcons.I_VIEW_SCHEDULE);
		dropScheduleButton.active = false;
		dropScheduleButton.visible = false;
		dropScheduleButton.withCallback(() -> AllPackets.getChannel()
			.sendToServer(StationEditPacket.dropSchedule(blockEntity.getBlockPos())));
		addRenderableWidget(dropScheduleButton);
		
		colorTypeScroll = new ScrollInput(x + 166, y + 17, 22, 14).titled(Lang.translateDirect("station.train_map_color"));
		colorTypeScroll.withRange(0, 16);
		colorTypeScroll.withStepFunction(ctx -> -colorTypeScroll.standardStep()
			.apply(ctx));
		colorTypeScroll.calling(s -> {
			Train train = displayedTrain.get();
			if (train != null) {
				train.mapColorIndex = s;
				messedWithColors = 10;				
			}
		});
		colorTypeScroll.active = colorTypeScroll.visible = false;
		addRenderableWidget(colorTypeScroll);

		onTextChanged = s -> trainNameBox.setX(nameBoxX(s, trainNameBox));
		trainNameBox = new EditBox(font, x + 23, y + 47, background.width - 75, 10, Components.immutableEmpty());
		trainNameBox.setBordered(false);
		trainNameBox.setMaxLength(35);
		trainNameBox.setTextColor(0xC6C6C6);
		trainNameBox.setFocused(false);
		trainNameBox.mouseClicked(0, 0, 0);
		trainNameBox.setResponder(onTextChanged);
		trainNameBox.active = false;

		tickTrainDisplay();

		Pair<ScrollInput, Label> doorControlWidgets =
			DoorControl.createWidget(x + 35, y + 102, mode -> doorControl = mode, doorControl);
		addRenderableWidget(doorControlWidgets.getFirst());
		addRenderableWidget(doorControlWidgets.getSecond());
	}

	@Override
	public void tick() {
		tickTrainDisplay();
		if (getFocused() != nameBox) {
			nameBox.setCursorPosition(nameBox.getValue()
				.length());
			nameBox.setHighlightPos(nameBox.getCursorPosition());
		}
		if (getFocused() != trainNameBox || trainNameBox.active == false) {
			trainNameBox.setCursorPosition(trainNameBox.getValue()
				.length());
			trainNameBox.setHighlightPos(trainNameBox.getCursorPosition());
		}
		
		if (messedWithColors > 0) {
			messedWithColors--;
			if (messedWithColors == 0)
				syncTrainNameAndColor();
		}

		super.tick();

		updateAssemblyTooltip(blockEntity.edgePoint.isOnCurve() ? "no_assembly_curve"
			: !blockEntity.edgePoint.isOrthogonal() ? "no_assembly_diagonal"
				: trainPresent() && !blockEntity.trainCanDisassemble ? "train_not_aligned" : null);
	}

	private void tickTrainDisplay() {
		Train train = displayedTrain.get();

		if (train == null) {
			if (trainNameBox.active) {
				trainNameBox.active = false;
				removeWidget(trainNameBox);
			}

			leavingAnimation = 0;
			newTrainButton.active = blockEntity.edgePoint.isOrthogonal();
			newTrainButton.visible = true;
			colorTypeScroll.visible = false;
			colorTypeScroll.active = false;
			Train imminentTrain = getImminent();

			if (imminentTrain != null) {
				displayedTrain = new WeakReference<>(imminentTrain);
				newTrainButton.active = false;
				newTrainButton.visible = false;
				disassembleTrainButton.active = false;
				disassembleTrainButton.visible = true;
				dropScheduleButton.active = blockEntity.trainHasSchedule;
				dropScheduleButton.visible = true;
				colorTypeScroll.setState(imminentTrain.mapColorIndex);
				colorTypeScroll.visible = true;
				colorTypeScroll.active = true;
				trainNameBox.active = true;
				trainNameBox.setValue(imminentTrain.name.getString());
				trainNameBox.setX(nameBoxX(trainNameBox.getValue(), trainNameBox));
				addRenderableWidget(trainNameBox);

				int trainIconWidth = getTrainIconWidth(imminentTrain);
				int targetPos = background.width / 2 - trainIconWidth / 2;
				if (trainIconWidth > 130)
					targetPos -= trainIconWidth - 130;
				float f = (float) (imminentTrain.navigation.distanceToDestination / 15f);
				if (trainPresent())
					f = 0;
				trainPosition.startWithValue(targetPos - (targetPos + 5) * f);
			}
			return;
		}

		int trainIconWidth = getTrainIconWidth(train);
		int targetPos = background.width / 2 - trainIconWidth / 2;
		if (trainIconWidth > 130)
			targetPos -= trainIconWidth - 130;

		if (leavingAnimation > 0) {
			colorTypeScroll.visible = false;
			colorTypeScroll.active = false;
			disassembleTrainButton.active = false;
			float f = 1 - (leavingAnimation / 80f);
			trainPosition.setValue(targetPos + f * f * f * (background.width - targetPos + 5));
			leavingAnimation--;
			if (leavingAnimation > 0)
				return;

			displayedTrain = new WeakReference<>(null);
			disassembleTrainButton.visible = false;
			dropScheduleButton.active = false;
			dropScheduleButton.visible = false;
			return;
		}

		if (getImminent() != train) {
			leavingAnimation = 80;
			return;
		}

		boolean trainAtStation = trainPresent();
		disassembleTrainButton.active =
			trainAtStation && blockEntity.trainCanDisassemble && blockEntity.edgePoint.isOrthogonal();
		dropScheduleButton.active = blockEntity.trainHasSchedule;

		if (blockEntity.trainHasSchedule)
			dropScheduleButton.setToolTip(Lang.translateDirect(
				blockEntity.trainHasAutoSchedule ? "station.remove_auto_schedule" : "station.remove_schedule"));
		else
			dropScheduleButton.getToolTip()
				.clear();

		float f = trainAtStation ? 0 : (float) (train.navigation.distanceToDestination / 30f);
		trainPosition.setValue(targetPos - (targetPos + trainIconWidth) * f);
	}

	private int nameBoxX(String s, EditBox nameBox) {
		return guiLeft + background.width / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
	}

	private void updateAssemblyTooltip(String key) {
		if (key == null) {
			disassembleTrainButton.setToolTip(Lang.translateDirect("station.disassemble_train"));
			newTrainButton.setToolTip(Lang.translateDirect("station.create_train"));
			return;
		}
		for (IconButton ib : new IconButton[] { disassembleTrainButton, newTrainButton }) {
			List<Component> toolTip = ib.getToolTip();
			toolTip.clear();
			toolTip.add(Lang.translateDirect("station." + key)
				.withStyle(ChatFormatting.GRAY));
			toolTip.add(Lang.translateDirect("station." + key + "_1")
				.withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);
		int x = guiLeft;
		int y = guiTop;

		String text = nameBox.getValue();

		if (!nameBox.isFocused())
			AllGuiTextures.STATION_EDIT_NAME.render(graphics, nameBoxX(text, nameBox) + font.width(text) + 5, y + 1);

		graphics.renderItem(AllBlocks.TRAIN_DOOR.asStack(), x + 14, y + 103);

		Train train = displayedTrain.get();
		if (train == null) {
			MutableComponent header = Lang.translateDirect("station.idle");
			graphics.drawString(font, header, x + 97 - font.width(header) / 2, y + 47, 0x7A7A7A, false);
			return;
		}

		float position = trainPosition.getValue(partialTicks);

		PoseStack ms = graphics.pose();
		ms.pushPose();
		RenderSystem.enableBlend();
		ms.translate(position, 0, 0);
		TrainIconType icon = train.icon;
		int offset = 0;

		List<Carriage> carriages = train.carriages;
		for (int i = carriages.size() - 1; i > 0; i--) {
			RenderSystem.setShaderColor(1, 1, 1, Math.min(1f,
				Math.min((position + offset - 10) / 30f, (background.width - 40 - position - offset) / 30f)));
			Carriage carriage = carriages.get(blockEntity.trainBackwards ? carriages.size() - i - 1 : i);
			offset += icon.render(carriage.bogeySpacing, graphics, x + offset, y + 20) + 1;
		}

		RenderSystem.setShaderColor(1, 1, 1,
			Math.min(1f, Math.min((position + offset - 10) / 30f, (background.width - 40 - position - offset) / 30f)));
		offset += icon.render(TrainIconType.ENGINE, graphics, x + offset, y + 20);
		RenderSystem.disableBlend();
		ms.popPose();

		RenderSystem.setShaderColor(1, 1, 1, 1);

		AllGuiTextures.STATION_TEXTBOX_TOP.render(graphics, x + 21, y + 42);
		UIRenderHelper.drawStretched(graphics, x + 21, y + 60, 150, 26, 0, AllGuiTextures.STATION_TEXTBOX_MIDDLE);
		AllGuiTextures.STATION_TEXTBOX_BOTTOM.render(graphics, x + 21, y + 86);

		ms.pushPose();
		ms.translate(Mth.clamp(position + offset - 13, 25, 159), 0, 0);
		AllGuiTextures.STATION_TEXTBOX_SPEECH.render(graphics, x, y + 38);
		ms.popPose();

		text = trainNameBox.getValue();
		if (!trainNameBox.isFocused()) {
			int buttonX = nameBoxX(text, trainNameBox) + font.width(text) + 5;
			AllGuiTextures.STATION_EDIT_TRAIN_NAME.render(graphics, Math.min(buttonX, guiLeft + 156), y + 44);
			if (font.width(text) > trainNameBox.getWidth())
				graphics.drawString(font, "...", guiLeft + 26, guiTop + 47, 0xa6a6a6);
		}
		
		if (!Mods.FTBCHUNKS.isLoaded())
			return;
		
		AllGuiTextures sprite = AllGuiTextures.TRAINMAP_SPRITES;
		sprite.bind();
		int trainColorIndex = colorTypeScroll.getState();
		int colorRow = trainColorIndex / 4;
		int colorCol = trainColorIndex % 4;
		int rotation = (AnimationTickHolder.getTicks() / 5) % 8;

		for (int slice = 0; slice < 3; slice++) {
			int row = slice == 0 ? 1 : slice == 2 ? 2 : 3;
			int col = rotation;
			int positionX = colorTypeScroll.getX() + 4;
			int positionY = colorTypeScroll.getY() - 1;
			int sheetX = col * 16 + colorCol * 128;
			int sheetY = row * 16 + colorRow * 64;

			graphics.blit(sprite.location, positionX, positionY, sheetX, sheetY, 16, 16, sprite.width, sprite.height);
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (!nameBox.isFocused() && pMouseY > guiTop && pMouseY < guiTop + 14 && pMouseX > guiLeft
			&& pMouseX < guiLeft + background.width) {
			nameBox.setFocused(true);
			nameBox.setHighlightPos(0);
			setFocused(nameBox);
			return true;
		}
		if (trainNameBox.active && !trainNameBox.isFocused() && pMouseY > guiTop + 45 && pMouseY < guiTop + 58
			&& pMouseX > guiLeft + 25 && pMouseX < guiLeft + 168) {
			trainNameBox.setFocused(true);
			trainNameBox.setHighlightPos(0);
			setFocused(trainNameBox);
			return true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		boolean hitEnter = getFocused() instanceof EditBox
			&& (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER);

		if (hitEnter && nameBox.isFocused()) {
			nameBox.setFocused(false);
			syncStationName();
			return true;
		}

		if (hitEnter && trainNameBox.isFocused()) {
			trainNameBox.setFocused(false);
			syncTrainNameAndColor();
			return true;
		}

		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	private void syncTrainNameAndColor() {
		Train train = displayedTrain.get();
		if (train != null && !trainNameBox.getValue()
			.equals(train.name.getString()))
			AllPackets.getChannel()
				.sendToServer(new TrainEditPacket(train.id, trainNameBox.getValue(), train.icon.getId(), train.mapColorIndex));
	}

	private void syncStationName() {
		if (!nameBox.getValue()
			.equals(station.name))
			AllPackets.getChannel()
				.sendToServer(
					StationEditPacket.configure(blockEntity.getBlockPos(), false, nameBox.getValue(), doorControl));
	}

	@Override
	public void removed() {
		super.removed();
		if (nameBox == null || trainNameBox == null)
			return;
		AllPackets.getChannel()
			.sendToServer(StationEditPacket.configure(blockEntity.getBlockPos(), switchingToAssemblyMode,
				nameBox.getValue(), doorControl));
		Train train = displayedTrain.get();
		if (train == null)
			return;
		if (!switchingToAssemblyMode)
			AllPackets.getChannel()
				.sendToServer(
					new TrainEditPacket(train.id, trainNameBox.getValue(), train.icon.getId(), train.mapColorIndex));
		else
			blockEntity.imminentTrain = null;
	}

	@Override
	protected PartialModel getFlag(float partialTicks) {
		return blockEntity.flag.getValue(partialTicks) > 0.75f ? AllPartialModels.STATION_ON
			: AllPartialModels.STATION_OFF;
	}

}
