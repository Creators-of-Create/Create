package com.simibubi.create.content.logistics.item;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class LinkedControllerPacketBase extends SimplePacketBase {

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;

				ItemStack heldItem = player.getHeldItemMainhand();
				if (!AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
					heldItem = player.getHeldItemOffhand();
					if (!AllItems.LINKED_CONTROLLER.isIn(heldItem))
						return;
				}
				handle(player, heldItem);
			});
		context.get()
			.setPacketHandled(true);
	}

	protected abstract void handle(ServerPlayerEntity player, ItemStack heldItem);

}
