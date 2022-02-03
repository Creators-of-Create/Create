package com.simibubi.create.content.logistics.trains.management;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StationEditPacket extends TileEntityConfigurationPacket<StationTileEntity> {

	boolean assemblyMode;
	Boolean tryAssemble;
	String name;

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

		if (!name.isBlank()) {
			GlobalStation station = te.getOrCreateGlobalStation();
			if (station != null)
				station.name = name;
			Create.RAILWAYS.markTracksDirty();
		}

		if (!(blockState.getBlock() instanceof StationBlock))
			return;
		Boolean isAssemblyMode = blockState.getValue(StationBlock.ASSEMBLING);
		if (tryAssemble != null) {
			if (!isAssemblyMode)
				return;
			if (tryAssemble)
				te.assemble(player.getUUID());
			else {
				if (disassembleAndEnterMode(te))
					te.refreshAssemblyInfo();
			}
			return;
		}
		if (isAssemblyMode == assemblyMode)
			return;

		BlockState newState = blockState.cycle(StationBlock.ASSEMBLING);
		Boolean nowAssembling = newState.getValue(StationBlock.ASSEMBLING);
		if (nowAssembling) {
			if (!disassembleAndEnterMode(te))
				return;
		} else {
			te.cancelAssembly();
		}

		level.setBlock(blockPos, newState, 3);
		te.refreshBlockState();

		if (nowAssembling)
			te.refreshAssemblyInfo();
	}

	private boolean disassembleAndEnterMode(StationTileEntity te) {
		GlobalStation station = te.getOrCreateGlobalStation();
		if (station != null) {
			Train train = station.getPresentTrain();
			if (train != null && !train.disassemble(te.getAssemblyDirection(), te.getTarget()
				.getGlobalPosition()
				.above()))
				return false;
		}
		return te.tryEnterAssemblyMode();
	}

	@Override
	protected void applySettings(StationTileEntity te) {}

}
