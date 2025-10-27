package com.simibubi.create.compat.computercraft;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AttachedComputerPacket extends BlockEntityDataPacket<SyncedBlockEntity> {
	public static final StreamCodec<ByteBuf, AttachedComputerPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			ByteBufCodecs.BOOL, packet -> packet.hasAttachedComputer,
			AttachedComputerPacket::new
	);

	private final boolean hasAttachedComputer;

	public AttachedComputerPacket(BlockPos blockEntityPos, boolean hasAttachedComputer) {
		super(blockEntityPos);
		this.hasAttachedComputer = hasAttachedComputer;
	}

	@Override
	protected void handlePacket(SyncedBlockEntity blockEntity) {
		if (blockEntity instanceof SmartBlockEntity sbe) {
			sbe.getBehaviour(AbstractComputerBehaviour.TYPE)
				.setHasAttachedComputer(hasAttachedComputer);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.ATTACHED_COMPUTER;
	}
}
