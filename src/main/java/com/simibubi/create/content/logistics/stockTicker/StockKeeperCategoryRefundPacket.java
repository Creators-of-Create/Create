package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StockKeeperCategoryRefundPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
	public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryRefundPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, p -> p.pos,
	    ItemStack.STREAM_CODEC, p -> p.filter,
	    StockKeeperCategoryRefundPacket::new
	);

	private final ItemStack filter;

	public StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) {
		super(pos);
		this.filter = filter;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.REFUND_STOCK_KEEPER_CATEGORY;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
			player.getInventory()
				.placeItemBackInInventory(filter);
	}

}
