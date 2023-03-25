package com.simibubi.create.content.curiosities.clipboard;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ClipboardEditPacket extends SimplePacketBase {

	private int hotbarSlot;
	private CompoundTag data;

	public ClipboardEditPacket(int hotbarSlot, CompoundTag data) {
		this.hotbarSlot = hotbarSlot;
		this.data = data;
	}

	public ClipboardEditPacket(FriendlyByteBuf buffer) {
		hotbarSlot = buffer.readVarInt();
		data = buffer.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(hotbarSlot);
		buffer.writeNbt(data);
	}

	@Override
	public boolean handle(Context context) {
		ServerPlayer sender = context.getSender();
		ItemStack itemStack = sender.getInventory()
			.getItem(hotbarSlot);
		if (!AllItems.CLIPBOARD.isIn(itemStack))
			return true;
		itemStack.setTag(data.isEmpty() ? null : data);
		return true;
	}

}
