package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class Instruction {

	SequencerInstructions instruction;
	InstructionSpeedModifiers speedModifier;
	int value;

	public Instruction(SequencerInstructions instruction) {
		this(instruction, 1);
	}

	public Instruction(SequencerInstructions instruction, int value) {
		this.instruction = instruction;
		speedModifier = InstructionSpeedModifiers.FORWARD;
		this.value = value;
	}

	int getDuration(float initialProgress, float speed) {
		int offset = speed > 0 && speedModifier.value < 0 ? 1 : 2;
		speed *= speedModifier.value;
		speed = Math.abs(speed);

		double degreesPerTick = (speed * 360) / 60 / 20;
		double metersPerTick = speed / 512;
		switch (instruction) {

		case TURN_ANGLE:
			return (int) ((1 - initialProgress) * value / degreesPerTick + 1);

		case TURN_DISTANCE:
			return (int) ((1 - initialProgress) * value / metersPerTick + offset);

		case WAIT:
			return (int) ((1 - initialProgress) * value + 1);

		case PAUSED:
			return -1;

		case END:
		default:
			break;

		}
		return 0;
	}

	int getSpeedModifier() {
		switch (instruction) {

		case TURN_ANGLE:
		case TURN_DISTANCE:
			return speedModifier.value;

		case END:
		case WAIT:
		case PAUSED:
		default:
			break;

		}
		return 0;
	}

	OnIsPoweredResult onIsPowered() {
		switch (instruction)
		{
			case PAUSED:
				return OnIsPoweredResult.CONTINUE;
		}
		return OnIsPoweredResult.NOTHING;
	}

	public static ListNBT serializeAll(Vector<Instruction> instructions) {
		ListNBT list = new ListNBT();
		instructions.forEach(i -> list.add(i.serialize()));
		return list;
	}

	public static Vector<Instruction> deserializeAll(ListNBT list) {
		if (list.isEmpty())
			return createDefault();
		Vector<Instruction> instructions = new Vector<>(5);
		list.forEach(inbt -> instructions.add(deserialize((CompoundNBT) inbt)));
		return instructions;
	}

	public static Vector<Instruction> createDefault() {
		Vector<Instruction> instructions = new Vector<>(5);
		instructions.add(new Instruction(SequencerInstructions.TURN_ANGLE, 90));
		instructions.add(new Instruction(SequencerInstructions.END));
		return instructions;
	}

	CompoundNBT serialize() {
		CompoundNBT tag = new CompoundNBT();
		NBTHelper.writeEnum(tag, "Type", instruction);
		NBTHelper.writeEnum(tag, "Modifier", speedModifier);
		tag.putInt("Value", value);
		return tag;
	}

	static Instruction deserialize(CompoundNBT tag) {
		Instruction instruction =
			new Instruction(NBTHelper.readEnum(tag, "Type", SequencerInstructions.class));
		instruction.speedModifier = NBTHelper.readEnum(tag, "Modifier", InstructionSpeedModifiers.class);
		instruction.value = tag.getInt("Value");
		return instruction;
	}

}
