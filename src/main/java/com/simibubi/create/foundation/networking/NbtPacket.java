package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

@Deprecated
public class NbtPacket extends SimplePacketBase {

	public ItemStack stack;
	public int slot;
	public InteractionHand hand;

	public NbtPacket(ItemStack stack, InteractionHand hand) {
		this(stack, -1);
		this.hand = hand;
	}

	public NbtPacket(ItemStack stack, int slot) {
		this.stack = stack;
		this.slot = slot;
		this.hand = InteractionHand.MAIN_HAND;
	}

	public NbtPacket(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
		slot = buffer.readInt();
		hand = InteractionHand.values()[buffer.readInt()];
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
		buffer.writeInt(slot);
		buffer.writeInt(hand.ordinal());
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer player = context.get()
					.getSender();
				if (player == null)
					return;
				if (!(stack.getItem() instanceof SymmetryWandItem || stack.getItem() instanceof ZapperItem)) {
					return;
				}
				stack.removeTagKey("AttributeModifiers");
				if (slot == -1) {
					ItemStack heldItem = player.getItemInHand(hand);
					if (heldItem.getItem() == stack.getItem()) {
						heldItem.setTag(stack.getTag());
					}
					return;
				}

				ItemStack heldInSlot = player.getInventory().getItem(slot);
				if (heldInSlot.getItem() == stack.getItem()) {
					heldInSlot.setTag(stack.getTag());
				}

			});
		context.get()
			.setPacketHandled(true);
	}

}
