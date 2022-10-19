package com.simibubi.create.compat.computercraft.peripherals;

import com.simibubi.create.compat.computercraft.peripherals.SyncedPeripheralBase;
import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.advanced.sequencer.Instruction;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.InstructionSpeedModifiers;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencerInstructions;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class SequencedGearshiftPeripheral extends SyncedPeripheralBase<SequencedGearshiftTileEntity> {

	public SequencedGearshiftPeripheral(SequencedGearshiftTileEntity tile) {
		super(tile);
	}

	@LuaFunction(mainThread = true)
	public void rotate(IArguments arguments) throws LuaException {
		runInstruction(arguments, SequencerInstructions.TURN_ANGLE);
	}

	@LuaFunction(mainThread = true)
	public void move(IArguments arguments) throws LuaException {
		runInstruction(arguments, SequencerInstructions.TURN_DISTANCE);
	}

	@LuaFunction
	public boolean isRunning() {
		return !this.tile.isIdle();
	}

	private void runInstruction(IArguments arguments, SequencerInstructions instructionType) throws LuaException {
		int speedModifier = arguments.count() > 1 ? arguments.getInt(1) : 1;
		this.tile.getInstructions().clear();

		this.tile.getInstructions().add(new Instruction(
				instructionType,
				InstructionSpeedModifiers.getByModifier(speedModifier),
				Math.abs(arguments.getInt(0))));
		this.tile.getInstructions().add(new Instruction(SequencerInstructions.END));

		this.tile.run(0);
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_SequencedGearshift";
	}

}
