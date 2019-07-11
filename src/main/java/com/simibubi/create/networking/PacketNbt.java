package com.simibubi.create.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketNbt {

	public ItemStack stack;

	public PacketNbt(ItemStack stack) {
		this.stack = stack;
	}

	public PacketNbt(PacketBuffer buffer) {
		stack = buffer.readItemStack();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeItemStack(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			ItemStack heldItem = player.getHeldItemMainhand();
			if (heldItem.getItem() == stack.getItem()) {
				heldItem.setTag(stack.getTag());
			}
		});
	}

}
