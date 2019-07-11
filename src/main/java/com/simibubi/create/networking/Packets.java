package com.simibubi.create.networking;

import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Packets {

	public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Create.ID, "simple_channel"), () -> "1", v -> v.equals("1"), v -> v.equals("1"));

	public static void registerPackets() {
		int i = 0;

		channel.registerMessage(i++, PacketNbt.class, PacketNbt::toBytes, PacketNbt::new,
				PacketNbt::handle);
		channel.registerMessage(i++, PacketSymmetryEffect.class, PacketSymmetryEffect::toBytes, PacketSymmetryEffect::new,
				PacketSymmetryEffect::handle);
	}

}
