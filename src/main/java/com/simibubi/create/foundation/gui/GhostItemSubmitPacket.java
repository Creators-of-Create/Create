package com.simibubi.create.foundation.gui;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class GhostItemSubmitPacket extends SimplePacketBase {

	private final ItemStack item;
	private final int slot;

	public GhostItemSubmitPacket(ItemStack item, int slot) {
		this.item = item;
		this.slot = slot;
	}

	public GhostItemSubmitPacket(PacketBuffer buffer) {
		item = buffer.readItem();
		slot = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeItem(item);
		buffer.writeInt(slot);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
				.enqueueWork(() -> {
					ServerPlayerEntity player = context.get()
							.getSender();
					if (player == null)
						return;

					if (player.containerMenu instanceof GhostItemContainer) {
						GhostItemContainer<?> c = (GhostItemContainer<?>) player.containerMenu;
						c.ghostInventory.setStackInSlot(slot, item);
						c.getSlot(36 + slot).setChanged();
					}

				});
		context.get()
				.setPacketHandled(true);
	}

}
