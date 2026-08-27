package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class PackagePortConfigurationPacket extends BlockEntityConfigurationPacket<PackagePortBlockEntity> {
	public static final StreamCodec<ByteBuf, PackagePortConfigurationPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		ByteBufCodecs.STRING_UTF8, packet -> packet.newFilter,
	    ByteBufCodecs.BOOL, packet -> packet.acceptPackages,
		ByteBufCodecs.BOOL, packet -> packet.explicitFetch,
		ByteBufCodecs.BOOL, packet -> packet.explicitDeliver,
	    PackagePortConfigurationPacket::new
	);

	private final String newFilter;
	private final boolean acceptPackages;
	private final boolean explicitFetch;
	private final boolean explicitDeliver;

	public PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages, boolean explicitFetch, boolean explicitDeliver) {
		super(pos);
		this.newFilter = newFilter;
		this.acceptPackages = acceptPackages;
		this.explicitFetch = explicitFetch;
		this.explicitDeliver = explicitDeliver;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PACKAGE_PORT_CONFIGURATION;
	}

	@Override
	protected void applySettings(ServerPlayer player, PackagePortBlockEntity be) {
		boolean isSame = be.addressFilter.equals(newFilter) && be.acceptsPackages == acceptPackages;
		if(be instanceof PostboxBlockEntity postbox)
			isSame = isSame && postbox.explicitFetch == explicitFetch && postbox.explicitDeliver == explicitDeliver;
		if(isSame)
			return;

		if(be instanceof PostboxBlockEntity postbox){
			postbox.explicitFetch = explicitFetch;
			postbox.explicitDeliver = explicitDeliver;
		}

		be.addressFilter = newFilter;
		be.acceptsPackages = acceptPackages;
		be.filterChanged();
		be.notifyUpdate();
	}

}
