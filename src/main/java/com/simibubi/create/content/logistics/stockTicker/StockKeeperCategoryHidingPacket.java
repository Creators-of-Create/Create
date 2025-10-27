package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class StockKeeperCategoryHidingPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
	public static final StreamCodec<ByteBuf, StockKeeperCategoryHidingPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, i -> i.pos,
		CatnipStreamCodecBuilders.list(ByteBufCodecs.INT), i -> i.indices,
		StockKeeperCategoryHidingPacket::new
	);

	private final List<Integer> indices;

	public StockKeeperCategoryHidingPacket(BlockPos pos, List<Integer> indices) {
		super(pos);
		this.indices = indices;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.STOCK_KEEPER_HIDE_CATEGORY;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (indices.isEmpty()) {
			be.hiddenCategoriesByPlayer.remove(player.getUUID());
		} else {
			be.hiddenCategoriesByPlayer.put(player.getUUID(), indices);
			be.notifyUpdate();
		}
		return;
	}

}
