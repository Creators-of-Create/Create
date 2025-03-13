package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private PackageOrderWithCrafts order;
	private String address;
	private boolean encodeRequester;

	public PackageOrderRequestPacket(BlockPos pos, PackageOrderWithCrafts order, String address, boolean encodeRequester) {
		super(pos);
		this.order = order;
		this.address = address;
		this.encodeRequester = encodeRequester;
	}

	public PackageOrderRequestPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeUtf(address);
		order.write(buffer);
		buffer.writeBoolean(encodeRequester);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		address = buffer.readUtf();
		order = PackageOrderWithCrafts.read(buffer);
		encodeRequester = buffer.readBoolean();
	}

	@Override
	protected void applySettings(StockTickerBlockEntity be) {
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (encodeRequester) {
			if (!order.isEmpty())
				AllSoundEvents.CONFIRM.playOnServer(be.getLevel(), pos);
			player.closeContainer();
			RedstoneRequesterBlock.programRequester(player, be, order, address);
			return;
		}

		if (!order.isEmpty()) {
			AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(be.getLevel(), pos);
			AllAdvancements.STOCK_TICKER.awardTo(player);
			WiFiEffectPacket.send(player.level(), pos);
		}

		be.broadcastPackageRequest(RequestType.PLAYER, order, null, address);
		return;
	}

}
