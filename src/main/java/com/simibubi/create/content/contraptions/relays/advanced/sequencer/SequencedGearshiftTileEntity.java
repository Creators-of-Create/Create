package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Optional;
import java.util.Vector;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.ConnectionsBuilder;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
			currentInstructionProgress += getInstruction(currentInstruction).getTickProgress(getTheoreticalSpeed());
			return;
		}
		run(currentInstruction + 1);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		if (isIdle())
			return;
		float currentSpeed = Math.abs(getTheoreticalSpeed());
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

		if (instruction.onRedstonePulse() == OnIsPoweredResult.CONTINUE) {
			run(currentInstruction + 1);
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
		BlockState state = getBlockState();
		SequencedGearshiftBlock block = (SequencedGearshiftBlock) state.getBlock();
		Direction facing = block.getFacing(state);

		ConnectionsBuilder builder = ConnectionsBuilder.builder();
		if (isVirtual()) return builder.withFullShaft(facing.getAxis()).build();

		builder = builder.withHalfShaft(facing.getOpposite());

		Optional<InstructionSpeedModifiers> modifier = getModifier();
		if (modifier.isEmpty() || isRemoved()) return builder.build();

		AllConnections.Shafts shaft = switch(modifier.get()) {
			case FORWARD_FAST -> AllConnections.Shafts.SHAFT_X2;
			case FORWARD -> AllConnections.Shafts.SHAFT;
			case BACK -> AllConnections.Shafts.SHAFT_REV;
			case BACK_FAST -> AllConnections.Shafts.SHAFT_REV_X2;
		};
		return builder.withHalfShaft(shaft, facing).build();
	}

	public Optional<InstructionSpeedModifiers> getModifier() {
		if (currentInstruction >= instructions.size() || isIdle())
			return Optional.empty();
		return instructions.get(currentInstruction).getSpeedModifier();
	}

}
