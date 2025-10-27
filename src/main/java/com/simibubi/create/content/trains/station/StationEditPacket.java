package com.simibubi.create.content.trains.station;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StationEditPacket extends BlockEntityConfigurationPacket<StationBlockEntity> {
	public static final StreamCodec<ByteBuf, StationEditPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			ByteBufCodecs.BOOL, packet -> packet.dropSchedule,
			ByteBufCodecs.BOOL, packet -> packet.assemblyMode,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.BOOL), packet -> packet.tryAssemble,
			CatnipStreamCodecBuilders.nullable(DoorControl.STREAM_CODEC), packet -> packet.doorControl,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.stringUtf8(256)), packet -> packet.name,
			StationEditPacket::new
	);

	private final boolean dropSchedule;
	private final boolean assemblyMode;
	private final Boolean tryAssemble;
	private final DoorControl doorControl;
	private final String name;

	public static StationEditPacket dropSchedule(BlockPos pos) {
		return new StationEditPacket(pos, true, false, false, null, null);
	}

	public static StationEditPacket tryAssemble(BlockPos pos) {
		return new StationEditPacket(pos, false, false, true, null, null);
	}

	public static StationEditPacket tryDisassemble(BlockPos pos) {
		return new StationEditPacket(pos, false, false, false, null, null);
	}

	public static StationEditPacket configure(BlockPos pos, boolean assemble, String name, DoorControl doorControl) {
		return new StationEditPacket(pos, false, assemble, null, doorControl, name);
	}

	private StationEditPacket(BlockPos pos, boolean dropSchedule, boolean assemblyMode, Boolean tryAssemble, DoorControl doorControl, String name) {
		super(pos);
		this.dropSchedule = dropSchedule;
		this.assemblyMode = assemblyMode;
		this.tryAssemble = tryAssemble;
		this.doorControl = doorControl;
		this.name = name;
	}

	@Override
	protected void applySettings(ServerPlayer player, StationBlockEntity be) {
		Level level = be.getLevel();
		BlockPos blockPos = be.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);
		GlobalStation station = be.getStation();
		
		if (dropSchedule) {
			if (station == null)
				return;
			be.dropSchedule(player, station.getPresentTrain());
			return;
		}

		if (doorControl != null)
			be.doorControls.set(doorControl);

		if (name != null && !name.isBlank())
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
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_STATION;
	}
}
