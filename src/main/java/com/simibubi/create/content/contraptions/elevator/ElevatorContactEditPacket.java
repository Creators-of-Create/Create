package com.simibubi.create.content.contraptions.elevator;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class ElevatorContactEditPacket extends BlockEntityConfigurationPacket<ElevatorContactBlockEntity> {
	public static final StreamCodec<FriendlyByteBuf, ElevatorContactEditPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			ByteBufCodecs.stringUtf8(4), packet -> packet.shortName,
			ByteBufCodecs.stringUtf8(90), packet -> packet.longName,
			DoorControl.STREAM_CODEC, packet -> packet.doorControl,
			ElevatorContactEditPacket::new
	);

	private final String shortName;
	private final String longName;
	private final DoorControl doorControl;

	public ElevatorContactEditPacket(BlockPos pos, String shortName, String longName, DoorControl doorControl) {
		super(pos);
		this.shortName = shortName;
		this.longName = longName;
		this.doorControl = doorControl;
	}

	@Override
	protected void applySettings(ServerPlayer player, ElevatorContactBlockEntity be) {
		be.updateName(shortName, longName);
		be.doorControls.set(doorControl);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_ELEVATOR_CONTACT;
	}
}
