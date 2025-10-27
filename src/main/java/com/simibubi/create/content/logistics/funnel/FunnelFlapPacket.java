package com.simibubi.create.content.logistics.funnel;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class FunnelFlapPacket extends BlockEntityDataPacket<FunnelBlockEntity> {
	public static final StreamCodec<ByteBuf, FunnelFlapPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			ByteBufCodecs.BOOL, packet -> packet.inwards,
			FunnelFlapPacket::new
	);

    private final boolean inwards;

    public FunnelFlapPacket(FunnelBlockEntity blockEntity, boolean inwards) {
        this(blockEntity.getBlockPos(), inwards);
    }

	private FunnelFlapPacket(BlockPos pos, boolean inwards) {
		super(pos);
		this.inwards = inwards;
	}

    @Override
    protected void handlePacket(FunnelBlockEntity blockEntity) {
        blockEntity.flap(inwards);
    }

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.FUNNEL_FLAP;
	}
}
