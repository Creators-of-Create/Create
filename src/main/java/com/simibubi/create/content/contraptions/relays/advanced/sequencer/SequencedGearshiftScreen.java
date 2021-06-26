package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SequencedGearshiftScreen extends AbstractSimiScreen {

	private final ItemStack renderedItem = AllBlocks.SEQUENCED_GEARSHIFT.asStack();
	private final AllGuiTextures background = AllGuiTextures.SEQUENCER;
	private IconButton confirmButton;

	private ListNBT compareTag;
	private Vector<Instruction> instructions;
	private BlockPos pos;

	private Vector<Vector<ScrollInput>> inputs;

	public SequencedGearshiftScreen(SequencedGearshiftTileEntity te) {
		super(Lang.translate("gui.sequenced_gearshift.title"));
		this.instructions = te.instructions;
		this.pos = te.getPos();
		compareTag = Instruction.serializeAll(instructions);
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-20, 0);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		inputs = new Vector<>(5);
		for (int row = 0; row < inputs.capacity(); row++)
			inputs.add(new Vector<>(3));

		for (int row = 0; row < instructions.size(); row++)
			initInputsOfRow(row, x, y);

		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);
	}

	public void initInputsOfRow(int row, int backgroundX, int backgroundY) {
		int x = backgroundX + 30;
		int y = backgroundY + 18;
		int rowHeight = 22;

		Vector<ScrollInput> rowInputs = inputs.get(row);
		rowInputs.forEach(widgets::remove);
		rowInputs.clear();
		int index = row;
		Instruction instruction = instructions.get(row);

		ScrollInput type =
			new SelectionScrollInput(x, y + rowHeight * row, 50, 18).forOptions(SequencerInstructions.getOptions())
				.calling(state -> instructionUpdated(index, state))
				.setState(instruction.instruction.ordinal())
				.titled(Lang.translate("gui.sequenced_gearshift.instruction"));
		ScrollInput value =
			new ScrollInput(x + 58, y + rowHeight * row, 28, 18).calling(state -> instruction.value = state);
		ScrollInput direction = new SelectionScrollInput(x + 88, y + rowHeight * row, 28, 18)
			.forOptions(InstructionSpeedModifiers.getOptions())
			.calling(state -> instruction.speedModifier = InstructionSpeedModifiers.values()[state])
			.titled(Lang.translate("gui.sequenced_gearshift.speed"));

		rowInputs.add(type);
		rowInputs.add(value);
		rowInputs.add(direction);

		widgets.addAll(rowInputs);
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
				.titled(Lang.translate(def.parameterKey))
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
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);

		for (int row = 0; row < instructions.capacity(); row++) {
			AllGuiTextures toDraw = AllGuiTextures.SEQUENCER_EMPTY;
			int yOffset = toDraw.height * row;
			if (row >= instructions.size()) {
				toDraw.draw(ms, x, y + 14 + yOffset);
				continue;
			}

			Instruction instruction = instructions.get(row);
			SequencerInstructions def = instruction.instruction;
			def.background.draw(ms, x, y + 14 + yOffset);

			label(ms, 36, yOffset - 3, Lang.translate(def.translationKey));
			if (def.hasValueParameter) {
				String text = def.formatValue(instruction.value);
				int stringWidth = textRenderer.getStringWidth(text);
				label(ms, 90 + (12 - stringWidth / 2), yOffset - 3, new StringTextComponent(text));
			}
			if (def.hasSpeedParameter)
				label(ms, 127, yOffset - 3, instruction.speedModifier.label);
		}

		drawCenteredText(ms, textRenderer, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		GuiGameElement.of(renderedItem)
			.<GuiGameElement.GuiRenderBuilder>at(x + background.width + 6, y + background.height - 56, -200)
			.scale(5)
			.render(ms);
	}

	private void label(MatrixStack ms, int x, int y, ITextComponent text) {
		textRenderer.drawWithShadow(ms, text, guiLeft + x, guiTop + 26 + y, 0xFFFFEE);
	}

	public void sendPacket() {
		ListNBT serialized = Instruction.serializeAll(instructions);
		if (serialized.equals(compareTag))
			return;
		AllPackets.channel.sendToServer(new ConfigureSequencedGearshiftPacket(pos, serialized));
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
				rowInputs.forEach(widgets::remove);
				rowInputs.clear();
			}
		} else {
			if (index + 1 < instructions.capacity() && index + 1 == instructions.size()) {
				instructions.add(new Instruction(SequencerInstructions.END));
				initInputsOfRow(index + 1, guiLeft, guiTop);
			}
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.isHovered()) {
			client.player.closeScreen();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

}
