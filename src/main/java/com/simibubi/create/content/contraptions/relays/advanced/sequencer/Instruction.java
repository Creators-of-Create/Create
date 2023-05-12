package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Instruction {

	SequencerInstructions instruction;
	InstructionSpeedModifiers speedModifier;
	int value;

	public Instruction(SequencerInstructions instruction) {
		this(instruction, 1);
	}

	public Instruction(SequencerInstructions instruction, int value) {
		this(instruction, InstructionSpeedModifiers.FORWARD, value);
	}

	public Instruction(SequencerInstructions instruction, InstructionSpeedModifiers speedModifier, int value) {
		this.instruction = instruction;
		this.speedModifier = speedModifier;
		this.value = value;
	}

	int getDuration(float currentProgress, float speed) {
		speed *= speedModifier.value;
		speed = Math.abs(speed);

		double target = value - currentProgress;

		switch (instruction) {

		case TURN_ANGLE:
			double degreesPerTick = KineticTileEntity.convertToAngular(speed);
			int ticks = (int) (target / degreesPerTick);
			double degreesErr = target - degreesPerTick*ticks;
			return ticks + (degreesPerTick > 2*degreesErr ? 0 : 1);

		case TURN_DISTANCE:
			double metersPerTick = KineticTileEntity.convertToLinear(speed);
			int offset = speed > 0 && speedModifier.value < 0 ? 1 : 2;
			return (int) (target / metersPerTick + offset);

		case DELAY:
			return (int) target;

		case AWAIT:
			return -1;

		case END:
		default:
			break;

		}
		return 0;
	}

	float getTickProgress(float speed) {
		switch(instruction) {

		case TURN_ANGLE:
			return KineticTileEntity.convertToAngular(speed);

		case TURN_DISTANCE:
			return KineticTileEntity.convertToLinear(speed);

		case DELAY:
			return 1;

		case AWAIT:
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
		case DELAY:
		case AWAIT:
		default:
			break;

		}
		return 0;
	}

	OnIsPoweredResult onRedstonePulse() {
		return instruction == SequencerInstructions.AWAIT ? OnIsPoweredResult.CONTINUE : OnIsPoweredResult.NOTHING;
	}

	public static ListTag serializeAll(Vector<Instruction> instructions) {
		ListTag list = new ListTag();
		instructions.forEach(i -> list.add(i.serialize()));
		return list;
	}

	public static Vector<Instruction> deserializeAll(ListTag list) {
		if (list.isEmpty())
			return createDefault();
		Vector<Instruction> instructions = new Vector<>(5);
		list.forEach(inbt -> instructions.add(deserialize((CompoundTag) inbt)));
		return instructions;
	}

	public static Vector<Instruction> createDefault() {
		Vector<Instruction> instructions = new Vector<>(5);
		instructions.add(new Instruction(SequencerInstructions.TURN_ANGLE, 90));
		instructions.add(new Instruction(SequencerInstructions.END));
		return instructions;
	}

	CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		NBTHelper.writeEnum(tag, "Type", instruction);
		NBTHelper.writeEnum(tag, "Modifier", speedModifier);
		tag.putInt("Value", value);
		return tag;
	}

	static Instruction deserialize(CompoundTag tag) {
		Instruction instruction = new Instruction(NBTHelper.readEnum(tag, "Type", SequencerInstructions.class));
		instruction.speedModifier = NBTHelper.readEnum(tag, "Modifier", InstructionSpeedModifiers.class);
		instruction.value = tag.getInt("Value");
		return instruction;
	}

}
