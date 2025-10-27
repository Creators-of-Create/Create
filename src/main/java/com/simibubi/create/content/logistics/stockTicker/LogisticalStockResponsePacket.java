package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<BigItemStack> items) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, LogisticalStockResponsePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, LogisticalStockResponsePacket::lastPacket,
		BlockPos.STREAM_CODEC, LogisticalStockResponsePacket::pos,
		CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC), LogisticalStockResponsePacket::items,
		LogisticalStockResponsePacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LOGISTICS_STOCK_RESPONSE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe)
			stbe.receiveStockPacket(items, lastPacket);
	}
}
