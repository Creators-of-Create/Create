package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.computercraft.ComputerScreen;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SequencedGearshiftScreen extends AbstractSimiScreen {

	private final ItemStack renderedItem = AllBlocks.SEQUENCED_GEARSHIFT.asStack();
	private final AllGuiTextures background = AllGuiTextures.SEQUENCER;
	private IconButton confirmButton;
	private SequencedGearshiftTileEntity te;

	private ListTag compareTag;
	private Vector<Instruction> instructions;

	private Vector<Vector<ScrollInput>> inputs;

	public SequencedGearshiftScreen(SequencedGearshiftTileEntity te) {
		super(Lang.translateDirect("gui.sequenced_gearshift.title"));
		this.instructions = te.instructions;
		this.te = te;
		compareTag = Instruction.serializeAll(instructions);
	}

	@Override
	protected void init() {
		if (te.computerBehaviour.hasAttachedComputer())
			minecraft.setScreen(new ComputerScreen(title, this::renderAdditional,
					this, te.computerBehaviour::hasAttachedComputer));

		setWindowSize(background.width, background.height);
		setWindowOffset(-20, 0);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		inputs = new Vector<>(5);
		for (int row = 0; row < inputs.capacity(); row++)
			inputs.add(new Vector<>(3));

		for (int row = 0; row < instructions.size(); row++)
			initInputsOfRow(row, x, y);

		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			onClose();
		});
		addRenderableWidget(confirmButton);
	}

	public void initInputsOfRow(int row, int backgroundX, int backgroundY) {
		int x = backgroundX + 30;
		int y = backgroundY + 18;
		int rowHeight = 22;

		Vector<ScrollInput> rowInputs = inputs.get(row);
		removeWidgets(rowInputs);
		rowInputs.clear();
		int index = row;
		Instruction instruction = instructions.get(row);

		ScrollInput type =
			new SelectionScrollInput(x, y + rowHeight * row, 50, 18).forOptions(SequencerInstructions.getOptions())
				.calling(state -> instructionUpdated(index, state))
				.setState(instruction.instruction.ordinal())
				.titled(Lang.translateDirect("gui.sequenced_gearshift.instruction"));
		ScrollInput value =
			new ScrollInput(x + 58, y + rowHeight * row, 28, 18).calling(state -> instruction.value = state);
		ScrollInput direction = new SelectionScrollInput(x + 88, y + rowHeight * row, 28, 18)
			.forOptions(InstructionSpeedModifiers.getOptions())
			.calling(state -> instruction.speedModifier = InstructionSpeedModifiers.values()[state])
			.titled(Lang.translateDirect("gui.sequenced_gearshift.speed"));

		rowInputs.add(type);
		rowInputs.add(value);
		rowInputs.add(direction);

		addRenderableWidgets(rowInputs);
		updateParamsOfRow(row);
	}

	public void updateParamsOfRow(int row) {
		Instruction instruction = instructions.get(row);
		Vector<ScrollInput> rowInputs = inputs.get(row);
		SequencerInstructions def = instruction.instruction;
		boolean hasValue = def.hasValueParameter;
		boolean hasModifier = def.hasSpeedParameter;

		ScrollInput value = rowInputs.get(1);
		value.active = value.visible = hasValue;
		if (hasValue)
			value.withRange(1, def.maxValue + 1)
				.titled(Lang.translateDirect(def.parameterKey))
				.withShiftStep(def.shiftStep)
				.setState(instruction.value)
				.onChanged();
		if (def == SequencerInstructions.DELAY) {
			value.withStepFunction(context -> {
				int v = context.currentValue;
				if (!context.forward)
					v--;
				if (v < 20)
					return context.shift ? 20 : 1;
				return context.shift ? 100 : 20;
			});
		} else
			value.withStepFunction(value.standardStep());

		ScrollInput modifier = rowInputs.get(2);
		modifier.active = modifier.visible = hasModifier;
		if (hasModifier)
			modifier.setState(instruction.speedModifier.ordinal());
	}

	@Override
	public void tick() {
		super.tick();

		if (te.computerBehaviour.hasAttachedComputer())
			minecraft.setScreen(new ComputerScreen(title, this::renderAdditional,
					this, te.computerBehaviour::hasAttachedComputer));
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(ms, x, y, this);

		for (int row = 0; row < instructions.capacity(); row++) {
			AllGuiTextures toDraw = AllGuiTextures.SEQUENCER_EMPTY;
			int yOffset = toDraw.height * row;

			toDraw.render(ms, x, y + 14 + yOffset, this);
		}

		for (int row = 0; row < instructions.capacity(); row++) {
			AllGuiTextures toDraw = AllGuiTextures.SEQUENCER_EMPTY;
			int yOffset = toDraw.height * row;
			if (row >= instructions.size()) {
				toDraw.render(ms, x, y + 14 + yOffset, this);
				continue;
			}

			Instruction instruction = instructions.get(row);
			SequencerInstructions def = instruction.instruction;
			def.background.render(ms, x, y + 14 + yOffset, this);

			label(ms, 36, yOffset - 3, Lang.translateDirect(def.translationKey));
			if (def.hasValueParameter) {
				String text = def.formatValue(instruction.value);
				int stringWidth = font.width(text);
				label(ms, 90 + (12 - stringWidth / 2), yOffset - 3, Components.literal(text));
			}
			if (def.hasSpeedParameter)
				label(ms, 127, yOffset - 3, instruction.speedModifier.label);
		}

		renderAdditional(ms, mouseX, mouseY, partialTicks, x, y, background);
		drawCenteredString(ms, font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
	}

	private void renderAdditional(PoseStack ms, int mouseX, int mouseY, float partialTicks, int guiLeft, int guiTop, AllGuiTextures background) {
		GuiGameElement.of(renderedItem)
			.<GuiGameElement.GuiRenderBuilder>at(guiLeft + background.width + 6, guiTop + background.height - 56, 100)
			.scale(5)
			.render(ms);
	}

	private void label(PoseStack ms, int x, int y, Component text) {
		font.drawShadow(ms, text, guiLeft + x, guiTop + 26 + y, 0xFFFFEE);
	}

	public void sendPacket() {
		ListTag serialized = Instruction.serializeAll(instructions);
		if (serialized.equals(compareTag))
			return;
		AllPackets.channel.sendToServer(new ConfigureSequencedGearshiftPacket(te.getBlockPos(), serialized));
	}

	@Override
	public void removed() {
		sendPacket();
	}

	private void instructionUpdated(int index, int state) {
		SequencerInstructions newValue = SequencerInstructions.values()[state];
		instructions.get(index).instruction = newValue;
		instructions.get(index).value = newValue.defaultValue;
		updateParamsOfRow(index);
		if (newValue == SequencerInstructions.END) {
			for (int i = instructions.size() - 1; i > index; i--) {
				instructions.remove(i);
				Vector<ScrollInput> rowInputs = inputs.get(i);
				removeWidgets(rowInputs);
				rowInputs.clear();
			}
		} else {
			if (index + 1 < instructions.capacity() && index + 1 == instructions.size()) {
				instructions.add(new Instruction(SequencerInstructions.END));
				initInputsOfRow(index + 1, guiLeft, guiTop);
			}
		}
	}

}
