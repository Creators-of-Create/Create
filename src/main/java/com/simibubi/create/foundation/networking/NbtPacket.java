package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

@Deprecated
public class NbtPacket extends SimplePacketBase {

	public ItemStack stack;
	public int slot;
	public Hand hand;

	public NbtPacket(ItemStack stack, Hand hand) {
		this(stack, -1);
		this.hand = hand;
	}
	
	public NbtPacket(ItemStack stack, int slot) {
		this.stack = stack;
		this.slot = slot;
		this.hand = Hand.MAIN_HAND;
	}

	public NbtPacket(PacketBuffer buffer) {
		stack = buffer.readItemStack();
		slot = buffer.readInt();
		hand = Hand.values()[buffer.readInt()];
	}
	
	public void write(PacketBuffer buffer) {
		buffer.writeItemStack(stack);
		buffer.writeInt(slot);
		buffer.writeInt(hand.ordinal());
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			if (!(stack.getItem() instanceof SymmetryWandItem || stack.getItem() instanceof ZapperItem)) {
				return;
			}
			stack.removeChildTag("AttributeModifiers");
			if (slot == -1) {
				ItemStack heldItem = player.getHeldItem(hand);
				if (heldItem.getItem() == stack.getItem()) {
					heldItem.setTag(stack.getTag());
				}
				return;
			}
			
			ItemStack heldInSlot = player.inventory.getStackInSlot(slot);
			if (heldInSlot.getItem() == stack.getItem()) {
				heldInSlot.setTag(stack.getTag());
			}
			
		});
		context.get().setPacketHandled(true);
	}

}
