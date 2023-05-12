package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.simibubi.create.content.contraptions.components.actors.DoorControl;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StationEditPacket extends BlockEntityConfigurationPacket<StationBlockEntity> {

	boolean dropSchedule;
	boolean assemblyMode;
	Boolean tryAssemble;
	DoorControl doorControl;
	String name;

	public static StationEditPacket dropSchedule(BlockPos pos) {
		StationEditPacket packet = new StationEditPacket(pos);
		packet.dropSchedule = true;
		return packet;
	}

	public static StationEditPacket tryAssemble(BlockPos pos) {
		StationEditPacket packet = new StationEditPacket(pos);
		packet.tryAssemble = true;
		return packet;
	}

	public static StationEditPacket tryDisassemble(BlockPos pos) {
		StationEditPacket packet = new StationEditPacket(pos);
		packet.tryAssemble = false;
		return packet;
	}

	public static StationEditPacket configure(BlockPos pos, boolean assemble, String name, DoorControl doorControl) {
		StationEditPacket packet = new StationEditPacket(pos);
		packet.assemblyMode = assemble;
		packet.tryAssemble = null;
		packet.name = name;
		packet.doorControl = doorControl;
		return packet;
	}

	public StationEditPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	public StationEditPacket(BlockPos pos) {
		super(pos);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBoolean(dropSchedule);
		if (dropSchedule)
			return;
		buffer.writeBoolean(doorControl != null);
		if (doorControl != null)
			buffer.writeVarInt(doorControl.ordinal());
		buffer.writeBoolean(tryAssemble != null);
		if (tryAssemble != null) {
			buffer.writeBoolean(tryAssemble);
			return;
		}
		buffer.writeBoolean(assemblyMode);
		buffer.writeUtf(name);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			dropSchedule = true;
			return;
		}
		if (buffer.readBoolean())
			doorControl = DoorControl.values()[Mth.clamp(buffer.readVarInt(), 0, DoorControl.values().length)];
		name = "";
		if (buffer.readBoolean()) {
			tryAssemble = buffer.readBoolean();
			return;
		}
		assemblyMode = buffer.readBoolean();
		name = buffer.readUtf(256);
	}

	@Override
	protected void applySettings(ServerPlayer player, StationBlockEntity be) {
		Level level = be.getLevel();
		BlockPos blockPos = be.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);

		if (dropSchedule) {
			be.dropSchedule(player);
			return;
		}
		
		if (doorControl != null)
			be.doorControls.set(doorControl);

		if (!name.isBlank())
			be.updateName(name);

		if (!(blockState.getBlock() instanceof StationBlock))
			return;

		Boolean isAssemblyMode = blockState.getValue(StationBlock.ASSEMBLING);
		boolean assemblyComplete = false;

		if (tryAssemble != null) {
			if (!isAssemblyMode)
				return;
			if (tryAssemble) {
				be.assemble(player.getUUID());
				assemblyComplete = be.getStation() != null && be.getStation()
					.getPresentTrain() != null;
			} else {
				if (be.tryDisassembleTrain(player) && be.tryEnterAssemblyMode())
					be.refreshAssemblyInfo();
			}
			if (!assemblyComplete)
				return;
		}

		if (assemblyMode)
			be.enterAssemblyMode(player);
		else
			be.exitAssemblyMode();
	}

	@Override
	protected void applySettings(StationBlockEntity be) {}

}
