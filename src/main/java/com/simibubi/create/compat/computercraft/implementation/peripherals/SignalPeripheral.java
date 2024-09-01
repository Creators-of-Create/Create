package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;


import com.simibubi.create.Create;
import com.simibubi.create.compat.computercraft.events.ComputerEvent;
import com.simibubi.create.compat.computercraft.events.SignalStateChangeEvent;
import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.signal.SignalBlock;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.utility.Iterate;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SignalPeripheral extends SyncedPeripheral<SignalBlockEntity> {

	public SignalPeripheral(SignalBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction
	public final String getState() {
		return blockEntity.getState().toString();
	}

	@LuaFunction
	public final boolean isForcedRed() {
		return blockEntity.getBlockState().getValue(SignalBlock.POWERED);
	}

	@LuaFunction(mainThread = true)
	public final void setForcedRed(boolean powered) {
		Level level = blockEntity.getLevel();
		if (level != null)
			level.setBlock(blockEntity.getBlockPos(),
					blockEntity.getBlockState().setValue(SignalBlock.POWERED, powered), 2);
	}

	@LuaFunction
	public final CreateLuaTable listBlockingTrainNames() throws LuaException {
		SignalBoundary signal = blockEntity.getSignal();
		if (signal == null)
			throw new LuaException("no signal");
		CreateLuaTable trainList = new CreateLuaTable();
		int trainCounter = 1;
		for (boolean current : Iterate.trueAndFalse) {
			Map<BlockPos, Boolean> set = signal.blockEntities.get(current);
			if (!set.containsKey(blockEntity.getBlockPos()))
				continue;
			UUID group = signal.groups.get(current);
			Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;
			SignalEdgeGroup signalEdgeGroup = signalEdgeGroups.get(group);
			for (Train train : signalEdgeGroup.trains) {
				trainList.put(trainCounter, train.name.getString());
				trainCounter += 1;
			}
		}
		return trainList;
	}

	@LuaFunction
	public final String getSignalType() throws LuaException {
		SignalBoundary signal = blockEntity.getSignal();
		if (signal != null) {
			return signal.getTypeFor(blockEntity.getBlockPos()).toString();
		} else {
			throw new LuaException("no signal");
		}
	}

	@LuaFunction(mainThread = true)
	public final void cycleSignalType() throws LuaException {
		SignalBoundary signal = blockEntity.getSignal();
		if (signal != null) {
			signal.cycleSignalType(blockEntity.getBlockPos());
		} else {
			throw new LuaException("no signal");
		}
	}

	@Override
	public void prepareComputerEvent(@NotNull ComputerEvent event) {
		if (event instanceof SignalStateChangeEvent ssce) {
			queueEvent("train_signal_state_change", ssce.state.toString());
		}
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Signal";
	}

}
