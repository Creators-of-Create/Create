package com.simibubi.create.content.trains.platform;

import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlatformEditPacket extends BlockEntityConfigurationPacket<PlatformBlockEntity> {

	boolean dropSchedule;
	boolean assemblyMode;
	Boolean tryAssemble;
	DoorControl doorControl;
	String name;

	public static PlatformEditPacket dropSchedule(BlockPos pos) {
		PlatformEditPacket packet = new PlatformEditPacket(pos);
		packet.dropSchedule = true;
		return packet;
	}

	public static PlatformEditPacket tryAssemble(BlockPos pos) {
		PlatformEditPacket packet = new PlatformEditPacket(pos);
		packet.tryAssemble = true;
		return packet;
	}

	public static PlatformEditPacket tryDisassemble(BlockPos pos) {
		PlatformEditPacket packet = new PlatformEditPacket(pos);
		packet.tryAssemble = false;
		return packet;
	}

	public static PlatformEditPacket configure(BlockPos pos, boolean assemble, String name, DoorControl doorControl) {
		PlatformEditPacket packet = new PlatformEditPacket(pos);
		packet.assemblyMode = assemble;
		packet.tryAssemble = null;
		packet.name = name;
		packet.doorControl = doorControl;
		return packet;
	}

	public PlatformEditPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	public PlatformEditPacket(BlockPos pos) {
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
	protected void applySettings(ServerPlayer player, PlatformBlockEntity be) {
		Level level = be.getLevel();
		BlockPos blockPos = be.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);
		GlobalPlatform station = be.getStation();

		if (dropSchedule) {
			if (station == null)
				return;
			be.dropSchedule(player, station.getPresentTrain());
			return;
		}

		if (doorControl != null)
			be.doorControls.set(doorControl);

		if (!name.isBlank())
			be.updateName(name);

		if (!(blockState.getBlock() instanceof PlatformBlock))
			return;

		Boolean isAssemblyMode = blockState.getValue(PlatformBlock.ASSEMBLING);
		boolean assemblyComplete = false;

		if (tryAssemble != null) {
			if (!isAssemblyMode)
				return;
			if (tryAssemble) {
				be.assemble(player.getUUID());
				assemblyComplete = station != null && station.getPresentTrain() != null;
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
	protected void applySettings(PlatformBlockEntity be) {}

}
