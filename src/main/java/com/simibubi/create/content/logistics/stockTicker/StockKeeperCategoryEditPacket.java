package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StockKeeperCategoryEditPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
	public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryEditPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, p -> p.pos,
		ItemStack.OPTIONAL_LIST_STREAM_CODEC, p -> p.schedule,
	    StockKeeperCategoryEditPacket::new
	);

	private final List<ItemStack> schedule;

	public StockKeeperCategoryEditPacket(BlockPos pos, List<ItemStack> schedule) {
		super(pos);
		this.schedule = schedule;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_STOCK_KEEPER_CATEGORIES;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		be.categories = schedule;
		be.notifyUpdate();
	}
}
