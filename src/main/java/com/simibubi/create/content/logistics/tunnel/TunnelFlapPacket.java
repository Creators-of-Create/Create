package com.simibubi.create.content.logistics.tunnel;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class TunnelFlapPacket extends BlockEntityDataPacket<BeltTunnelBlockEntity> {
	public static final StreamCodec<ByteBuf, TunnelFlapPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.pair(Direction.STREAM_CODEC, ByteBufCodecs.BOOL)), packet -> packet.flaps,
			TunnelFlapPacket::new
	);

    private final List<Pair<Direction, Boolean>> flaps;

    public TunnelFlapPacket(BeltTunnelBlockEntity blockEntity, List<Pair<Direction, Boolean>> flaps) {
        this(blockEntity.getBlockPos(), new ArrayList<>(flaps));
    }

	private TunnelFlapPacket(BlockPos pos, List<Pair<Direction, Boolean>> flaps) {
		super(pos);
		this.flaps = flaps;
	}

    @Override
    protected void handlePacket(BeltTunnelBlockEntity blockEntity) {
        for (Pair<Direction, Boolean> flap : flaps) {
            blockEntity.flap(flap.getFirst(), flap.getSecond());
        }
    }

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TUNNEL_FLAP;
	}
}
