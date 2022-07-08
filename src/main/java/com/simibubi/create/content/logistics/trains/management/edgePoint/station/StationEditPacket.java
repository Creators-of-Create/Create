package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
			scheduleDropRequested(player, te);
			return;
		}

		if (!name.isBlank()) {
			GlobalStation station = te.getStation();
			GraphLocation graphLocation = te.edgePoint.determineGraphLocation();
			if (station != null && graphLocation != null) {
				station.name = name;
				Create.RAILWAYS.sync.pointAdded(graphLocation.graph, station);
				Create.RAILWAYS.markTracksDirty();
			}
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
				if (disassembleAndEnterMode(player, te))
					te.refreshAssemblyInfo();
			}
			if (!assemblyComplete)
				return;
		}
		if (isAssemblyMode == assemblyMode)
			return;

		BlockState newState = blockState.cycle(StationBlock.ASSEMBLING);
		Boolean nowAssembling = newState.getValue(StationBlock.ASSEMBLING);

		if (nowAssembling) {
			if (!disassembleAndEnterMode(player, te))
				return;
		} else {
			te.cancelAssembly();
		}

		level.setBlock(blockPos, newState, 3);
		te.refreshBlockState();

		if (nowAssembling)
			te.refreshAssemblyInfo();

		GlobalStation station = te.getStation();
		GraphLocation graphLocation = te.edgePoint.determineGraphLocation();
		if (station != null && graphLocation != null) {
			station.assembling = nowAssembling;
			Create.RAILWAYS.sync.pointAdded(graphLocation.graph, station);
			Create.RAILWAYS.markTracksDirty();

			if (nowAssembling)
				for (Train train : Create.RAILWAYS.sided(level).trains.values()) {
					if (train.navigation.destination != station)
						continue;
					GlobalStation preferredDestination = train.runtime.startCurrentInstruction();
					if (preferredDestination != null)
						train.navigation.startNavigation(preferredDestination, Double.MAX_VALUE, false);
					else
						train.navigation.startNavigation(station, Double.MAX_VALUE, false);
				}
		}
	}

	private void scheduleDropRequested(ServerPlayer sender, StationTileEntity te) {
		GlobalStation station = te.getStation();
		if (station == null)
			return;
		Train train = station.getPresentTrain();
		if (train == null)
			return;
		ItemStack schedule = train.runtime.returnSchedule();
		dropSchedule(sender, te, schedule);
	}

	private boolean disassembleAndEnterMode(ServerPlayer sender, StationTileEntity te) {
		GlobalStation station = te.getStation();
		if (station != null) {
			Train train = station.getPresentTrain();
			BlockPos trackPosition = te.edgePoint.getGlobalPosition();
			ItemStack schedule = train == null ? ItemStack.EMPTY : train.runtime.returnSchedule();
			if (train != null && !train.disassemble(te.getAssemblyDirection(), trackPosition.above()))
				return false;
			dropSchedule(sender, te, schedule);
		}
		return te.tryEnterAssemblyMode();
	}

	private void dropSchedule(ServerPlayer sender, StationTileEntity te, ItemStack schedule) {
		if (schedule.isEmpty())
			return;
		if (sender.getMainHandItem()
			.isEmpty()) {
			sender.getInventory()
				.placeItemBackInInventory(schedule);
			return;
		}
		Vec3 v = VecHelper.getCenterOf(te.getBlockPos());
		ItemEntity itemEntity = new ItemEntity(te.getLevel(), v.x, v.y, v.z, schedule);
		itemEntity.setDeltaMovement(Vec3.ZERO);
		te.getLevel()
			.addFreshEntity(itemEntity);
	}

	@Override
	protected void applySettings(StationTileEntity te) {}

}
