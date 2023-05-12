package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StationEditPacket extends TileEntityConfigurationPacket<StationTileEntity> {

	boolean dropSchedule;
	boolean assemblyMode;
	Boolean tryAssemble;
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

	public static StationEditPacket configure(BlockPos pos, boolean assemble, String name) {
		StationEditPacket packet = new StationEditPacket(pos);
		packet.assemblyMode = assemble;
		packet.tryAssemble = null;
		packet.name = name;
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
		name = "";
		if (buffer.readBoolean()) {
			tryAssemble = buffer.readBoolean();
			return;
		}
		assemblyMode = buffer.readBoolean();
		name = buffer.readUtf(256);
	}

	@Override
	protected void applySettings(ServerPlayer player, StationTileEntity te) {
		Level level = te.getLevel();
		BlockPos blockPos = te.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);

		if (dropSchedule) {
			te.dropSchedule(player);
			return;
		}

		if (!name.isBlank()) {
			te.updateName(name);
		}

		if (!(blockState.getBlock() instanceof StationBlock))
			return;

		Boolean isAssemblyMode = blockState.getValue(StationBlock.ASSEMBLING);
		boolean assemblyComplete = false;

		if (tryAssemble != null) {
			if (!isAssemblyMode)
				return;
			if (tryAssemble) {
				te.assemble(player.getUUID());
				assemblyComplete = te.getStation() != null && te.getStation()
					.getPresentTrain() != null;
			} else {
				if (te.tryDisassembleTrain(player) && te.tryEnterAssemblyMode())
					te.refreshAssemblyInfo();
			}
			if (!assemblyComplete)
				return;
		}

		if (assemblyMode)
			te.enterAssemblyMode(player);
		else
			te.exitAssemblyMode();
	}

	@Override
	protected void applySettings(StationTileEntity te) {}

}
