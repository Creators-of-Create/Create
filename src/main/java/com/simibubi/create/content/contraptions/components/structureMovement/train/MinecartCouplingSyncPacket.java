package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MinecartCouplingSyncPacket extends MinecartCouplingCreationPacket {

	public MinecartCouplingSyncPacket(AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
		super(cart1, cart2);
	}

	public MinecartCouplingSyncPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> MinecartCouplingHandler.connectCarts(null, Minecraft.getInstance().world, id1, id2));
		context.get()
			.setPacketHandled(true);
	}

}