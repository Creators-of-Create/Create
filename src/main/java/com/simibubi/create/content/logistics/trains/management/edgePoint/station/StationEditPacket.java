package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.actors.DoorControl;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
			scheduleDropRequested(player, be);
			return;
		}
		
		if (doorControl != null)
			be.doorControls.set(doorControl);

		if (!name.isBlank()) {
			GlobalStation station = be.getStation();
			GraphLocation graphLocation = be.edgePoint.determineGraphLocation();
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
				be.assemble(player.getUUID());
				assemblyComplete = be.getStation() != null && be.getStation()
					.getPresentTrain() != null;
			} else {
				if (disassembleAndEnterMode(player, be))
					be.refreshAssemblyInfo();
			}
			if (!assemblyComplete)
				return;
		}
		if (isAssemblyMode == assemblyMode)
			return;

		BlockState newState = blockState.cycle(StationBlock.ASSEMBLING);
		Boolean nowAssembling = newState.getValue(StationBlock.ASSEMBLING);

		if (nowAssembling) {
			if (!disassembleAndEnterMode(player, be))
				return;
		} else {
			be.cancelAssembly();
		}

		level.setBlock(blockPos, newState, 3);
		be.refreshBlockState();

		if (nowAssembling)
			be.refreshAssemblyInfo();

		GlobalStation station = be.getStation();
		GraphLocation graphLocation = be.edgePoint.determineGraphLocation();
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

	private void scheduleDropRequested(ServerPlayer sender, StationBlockEntity be) {
		GlobalStation station = be.getStation();
		if (station == null)
			return;
		Train train = station.getPresentTrain();
		if (train == null)
			return;
		ItemStack schedule = train.runtime.returnSchedule();
		dropSchedule(sender, be, schedule);
	}

	private boolean disassembleAndEnterMode(ServerPlayer sender, StationBlockEntity be) {
		GlobalStation station = be.getStation();
		if (station != null) {
			Train train = station.getPresentTrain();
			BlockPos trackPosition = be.edgePoint.getGlobalPosition();
			ItemStack schedule = train == null ? ItemStack.EMPTY : train.runtime.returnSchedule();
			if (train != null && !train.disassemble(be.getAssemblyDirection(), trackPosition.above()))
				return false;
			dropSchedule(sender, be, schedule);
		}
		return be.tryEnterAssemblyMode();
	}

	private void dropSchedule(ServerPlayer sender, StationBlockEntity be, ItemStack schedule) {
		if (schedule.isEmpty())
			return;
		if (sender.getMainHandItem()
			.isEmpty()) {
			sender.getInventory()
				.placeItemBackInInventory(schedule);
			return;
		}
		Vec3 v = VecHelper.getCenterOf(be.getBlockPos());
		ItemEntity itemEntity = new ItemEntity(be.getLevel(), v.x, v.y, v.z, schedule);
		itemEntity.setDeltaMovement(Vec3.ZERO);
		be.getLevel()
			.addFreshEntity(itemEntity);
	}

	@Override
	protected void applySettings(StationBlockEntity be) {}

}
