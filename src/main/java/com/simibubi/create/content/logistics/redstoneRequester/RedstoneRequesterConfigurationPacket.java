package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RedstoneRequesterConfigurationPacket extends BlockEntityConfigurationPacket<RedstoneRequesterBlockEntity> {
	public static final StreamCodec<ByteBuf, RedstoneRequesterConfigurationPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		ByteBufCodecs.STRING_UTF8, packet -> packet.address,
	    ByteBufCodecs.BOOL, packet -> packet.allowPartial,
		CatnipStreamCodecBuilders.list(ByteBufCodecs.INT), packet -> packet.amounts,
	    RedstoneRequesterConfigurationPacket::new
	);

	private final String address;
	private final boolean allowPartial;
	private final List<Integer> amounts;

	public RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial,
		List<Integer> amounts) {
		super(pos);
		this.address = address;
		this.allowPartial = allowPartial;
		this.amounts = amounts;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_REDSTONE_REQUESTER;
	}

	@Override
	protected void applySettings(ServerPlayer player, RedstoneRequesterBlockEntity be) {
		be.encodedTargetAdress = address;
		List<BigItemStack> stacks = be.encodedRequest.stacks();
		for (int i = 0; i < stacks.size() && i < amounts.size(); i++) {
			ItemStack stack = stacks.get(i).stack;
			if (!stack.isEmpty())
				stacks.set(i, new BigItemStack(stack, amounts.get(i)));
		}
		if (!be.encodedRequest.orderedStacksMatchOrderedRecipes())
			be.encodedRequest = PackageOrderWithCrafts.simple(be.encodedRequest.stacks());
		be.allowPartialRequests = allowPartial;
	}

}
