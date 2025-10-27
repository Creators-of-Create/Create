package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class StockKeeperLockPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
	public static final StreamCodec<ByteBuf, StockKeeperLockPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, p -> p.pos,
		ByteBufCodecs.BOOL, p -> p.lock,
	    StockKeeperLockPacket::new
	);

	private final boolean lock;

	public StockKeeperLockPacket(BlockPos pos, boolean lock) {
		super(pos);
		this.lock = lock;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LOCK_STOCK_KEEPER;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (!be.behaviour.mayAdministrate(player))
			return;
		LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(be.behaviour.freqId);
		if (network != null) {
			network.locked = lock;
			Create.LOGISTICS.markDirty();
		}
	}

}
