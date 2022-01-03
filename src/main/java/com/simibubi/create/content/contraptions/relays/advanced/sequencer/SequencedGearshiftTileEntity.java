package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Vector;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SequencedGearshiftTileEntity extends KineticTileEntity {

	Vector<Instruction> instructions;
	int currentInstruction;
	int currentInstructionDuration;
	float currentInstructionProgress;
	int timer;
	boolean poweredPreviously;

	public SequencedGearshiftTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		instructions = Instruction.createDefault();
		currentInstruction = -1;
		currentInstructionDuration = -1;
		currentInstructionProgress = 0;
		timer = 0;
		poweredPreviously = false;
	}

	@Override
	public void tick() {
		super.tick();

		if (isIdle())
			return;
		if (level.isClientSide)
			return;
		if (currentInstructionDuration < 0)
			return;
		if (timer < currentInstructionDuration) {
			timer++;
			currentInstructionProgress += getInstruction(currentInstruction).getTickProgress(theoreticalSpeed);
			return;
		}
		run(currentInstruction + 1);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		if (isIdle())
			return;
		float currentSpeed = Math.abs(theoreticalSpeed);
		if (Math.abs(previousSpeed) == currentSpeed)
			return;
		Instruction instruction = getInstruction(currentInstruction);
		if (instruction == null)
			return;
		if (getSpeed() == 0)
			run(-1);

		// Update instruction time with regards to new speed
		currentInstructionDuration = instruction.getDuration(currentInstructionProgress, getTheoreticalSpeed());
		timer = 0;
	}

	public boolean isIdle() {
		return currentInstruction == -1;
	}

	public void onRedstoneUpdate(boolean isPowered, boolean isRunning) {
		if (!poweredPreviously && isPowered)
			risingFlank();
		poweredPreviously = isPowered;
		if (!isIdle())
			return;
		if (isPowered == isRunning)
			return;
		if (!level.hasNeighborSignal(worldPosition)) {
			level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, 0), 3);
			return;
		}
		if (getSpeed() == 0)
			return;
		run(0);
	}

	public void risingFlank() {
		Instruction instruction = getInstruction(currentInstruction);
		if (instruction == null)
			return;
		if (poweredPreviously)
			return;
		poweredPreviously = true;

		switch (instruction.onRedstonePulse()) {
		case CONTINUE:
			run(currentInstruction + 1);
			break;
		default:
			break;
		}
	}

	protected void run(int instructionIndex) {
		Instruction instruction = getInstruction(instructionIndex);
		if (instruction == null || instruction.instruction == SequencerInstructions.END) {
			currentInstruction = -1;
			currentInstructionDuration = -1;
			currentInstructionProgress = 0;
			timer = 0;
			if (!level.hasNeighborSignal(worldPosition))
				level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, 0), 3);
			else
				sendData();
			return;
		}

		currentInstructionDuration = instruction.getDuration(0, getTheoreticalSpeed());
		currentInstruction = instructionIndex;
		currentInstructionProgress = 0;
		timer = 0;
		level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, instructionIndex + 1), 3);
	}

	public Instruction getInstruction(int instructionIndex) {
		return instructionIndex >= 0 && instructionIndex < instructions.size() ? instructions.get(instructionIndex)
			: null;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("InstructionIndex", currentInstruction);
		compound.putInt("InstructionDuration", currentInstructionDuration);
		compound.putFloat("InstructionProgress", currentInstructionProgress);
		compound.putInt("Timer", timer);
		compound.putBoolean("PrevPowered", poweredPreviously);
		compound.put("Instructions", Instruction.serializeAll(instructions));
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		currentInstruction = compound.getInt("InstructionIndex");
		currentInstructionDuration = compound.getInt("InstructionDuration");
		currentInstructionProgress = compound.getFloat("InstructionProgress");
		poweredPreviously = compound.getBoolean("PrevPowered");
		timer = compound.getInt("Timer");
		instructions = Instruction.deserializeAll(compound.getList("Instructions", Tag.TAG_COMPOUND));
		super.read(compound, clientPacket);
	}

	@Override
	public KineticConnections getConnections() {
		Direction.Axis axis = getBlockState().getValue(BlockStateProperties.HORIZONTAL_AXIS);

		if (isVirtual()) return AllConnections.FULL_SHAFT.apply(axis);

		return getSpeedSource()
				.map(p -> {
					Direction dir = Direction.fromNormal(p.subtract(getBlockPos()));
					float modifier = getModifier();
					if (modifier == 0 || isRemoved()) return AllConnections.HALF_SHAFT.apply(dir);

					Direction opp = dir.getOpposite();
					String from = AllConnections.type(AllConnections.TYPE_SHAFT, opp);
					String to = AllConnections.type(AllConnections.TYPE_SHAFT, dir);
					return new KineticConnections(new Entry(opp.getNormal(), from, to, modifier))
							.merge(AllConnections.HALF_SHAFT.apply(dir));
				})
				.orElse(AllConnections.FULL_SHAFT.apply(axis));
	}

	public int getModifier() {
		if (currentInstruction >= instructions.size())
			return 0;
		return isIdle() ? 0
			: instructions.get(currentInstruction)
				.getSpeedModifier();
	}

}
