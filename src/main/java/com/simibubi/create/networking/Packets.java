package com.simibubi.create.networking;

import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Packets {

	private static final String PROTOCOL_VERSION = "1";

	public static SimpleChannel channel;

	public static void registerPackets() {
		int i = 0;

		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Create.ID, "main"), () -> PROTOCOL_VERSION,
				PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

		channel.registerMessage(i++, PacketNbt.class, PacketNbt::toBytes, PacketNbt::new, PacketNbt::handle);
		channel.registerMessage(i++, PacketConfigureSchematicannon.class, PacketConfigureSchematicannon::toBytes,
				PacketConfigureSchematicannon::new, PacketConfigureSchematicannon::handle);
		channel.registerMessage(i++, PacketSchematicTableContainer.class, PacketSchematicTableContainer::toBytes,
				PacketSchematicTableContainer::new, PacketSchematicTableContainer::handle);
		channel.registerMessage(i++, PacketSchematicUpload.class, PacketSchematicUpload::toBytes,
				PacketSchematicUpload::new, PacketSchematicUpload::handle);
		channel.registerMessage(i++, PacketSymmetryEffect.class, PacketSymmetryEffect::toBytes,
				PacketSymmetryEffect::new, PacketSymmetryEffect::handle);
	}

}
