package com.simibubi.create.content.curiosities.symmetry;

import java.util.function.Supplier;

import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ConfigureSymmetryWandPacket extends SimplePacketBase {

	protected Hand hand;
	protected SymmetryMirror mirror;

	public ConfigureSymmetryWandPacket(Hand hand, SymmetryMirror mirror) {
		this.hand = hand;
		this.mirror = mirror;
	}

	public ConfigureSymmetryWandPacket(PacketBuffer buffer) {
		hand = buffer.readEnum(Hand.class);
		mirror = SymmetryMirror.fromNBT(buffer.readNbt());
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeEnum(hand);
		buffer.writeNbt(mirror.writeToNbt());
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null) {
				return;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (stack.getItem() instanceof SymmetryWandItem) {
				SymmetryWandItem.configureSettings(stack, mirror);
			}
		});
		context.get().setPacketHandled(true);
	}

}
