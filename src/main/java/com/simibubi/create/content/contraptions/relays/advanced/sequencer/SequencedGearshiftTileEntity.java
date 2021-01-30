package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class SequencedGearshiftTileEntity extends SplitShaftTileEntity {

	Vector<Instruction> instructions;
	int currentInstruction;
	int currentInstructionDuration;
	int timer;

	public SequencedGearshiftTileEntity(TileEntityType<? extends SequencedGearshiftTileEntity> type) {
		super(type);
		instructions = Instruction.createDefault();
		currentInstruction = -1;
		currentInstructionDuration = -1;
		timer = 0;
	}

	@Override
	public void tick() {
		super.tick();

		if (isIdle())
			return;
		if (world.isRemote)
			return;
		if (currentInstructionDuration < 0)
			return;
		if (timer < currentInstructionDuration) {
			timer++;
			return;
		}
		run(currentInstruction + 1);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		if (isIdle())
			return;
		float currentSpeed = Math.abs(speed);
		if (Math.abs(previousSpeed) == currentSpeed)
			return;
		Instruction instruction = getInstruction(currentInstruction);
		if (instruction == null)
			return;
		if (getSpeed() == 0)
			run(-1);

		// Update instruction time with regards to new speed
		float initialProgress = timer / (float) currentInstructionDuration;
		currentInstructionDuration = instruction.getDuration(initialProgress, getTheoreticalSpeed());
		timer = 0;
	}

	public boolean isIdle() {
		return currentInstruction == -1;
	}

	public void onRedstoneUpdate() {
		if (!isIdle())
			return;

		if (!world.isBlockPowered(pos)) {
			world.setBlockState(pos, getBlockState().with(SequencedGearshiftBlock.STATE, 0), 3);
			return;
		}
		if (getSpeed() == 0)
			return;
		run(0);
	}

	public void onIsPowered() {

		Instruction instruction = getInstruction(currentInstruction);
		if (instruction == null)
			return;

		switch (instruction.onIsPowered())
		{
			case CONTINUE:
				run(currentInstruction + 1);
		}
	}

	protected void run(int instructionIndex) {
		Instruction instruction = getInstruction(instructionIndex);
		if (instruction == null || instruction.instruction == SequencerInstructions.END) {
			if (getModifier() != 0)
				detachKinetics();
			currentInstruction = -1;
			currentInstructionDuration = -1;
			timer = 0;
			if (!world.isBlockPowered(pos))
				world.setBlockState(pos, getBlockState().with(SequencedGearshiftBlock.STATE, 0), 3);
			else
				sendData();
			return;
		}

		detachKinetics();
		currentInstructionDuration = instruction.getDuration(0, getTheoreticalSpeed());
		currentInstruction = instructionIndex;
		timer = 0;
		world.setBlockState(pos, getBlockState().with(SequencedGearshiftBlock.STATE, instructionIndex + 1), 3);
	}

	public Instruction getInstruction(int instructionIndex) {
		return instructionIndex >= 0 && instructionIndex < instructions.size() ? instructions.get(instructionIndex)
				: null;
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("InstructionIndex", currentInstruction);
		compound.putInt("InstructionDuration", currentInstructionDuration);
		compound.putInt("Timer", timer);
		compound.put("Instructions", Instruction.serializeAll(instructions));
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		currentInstruction = compound.getInt("InstructionIndex");
		currentInstructionDuration = compound.getInt("InstructionDuration");
		timer = compound.getInt("Timer");
		instructions = Instruction.deserializeAll(compound.getList("Instructions", NBT.TAG_COMPOUND));
		super.read(compound, clientPacket);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		return (!hasSource() || face == getSourceFacing()) ? 1 : getModifier();
	}

	public int getModifier() {
		if (currentInstruction >= instructions.size())
			return 0;
		return isIdle() ? 0 : instructions.get(currentInstruction).getSpeedModifier();
	}

}
