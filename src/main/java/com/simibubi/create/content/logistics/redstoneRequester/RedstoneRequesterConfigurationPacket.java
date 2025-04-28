package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class RedstoneRequesterConfigurationPacket extends BlockEntityConfigurationPacket<RedstoneRequesterBlockEntity> {

	private String address;
	private boolean allowPartial;
	private List<Integer> amounts;

	public RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial,
		List<Integer> amounts) {
		super(pos);
		this.address = address;
		this.allowPartial = allowPartial;
		this.amounts = amounts;
	}

	public RedstoneRequesterConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeUtf(address);
		buffer.writeBoolean(allowPartial);
		buffer.writeVarInt(amounts.size());
		amounts.forEach(buffer::writeVarInt);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		address = buffer.readUtf();
		allowPartial = buffer.readBoolean();
		int size = buffer.readVarInt();
		amounts = new ArrayList<>();
		for (int i = 0; i < size; i++)
			amounts.add(buffer.readVarInt());
	}

	@Override
	protected void applySettings(RedstoneRequesterBlockEntity be) {
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
