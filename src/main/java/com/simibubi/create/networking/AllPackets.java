package com.simibubi.create.networking;

import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class AllPackets {

	private static final String PROTOCOL_VERSION = "1";

	public static SimpleChannel channel;

	public static void registerPackets() {
		int i = 0;

		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Create.ID, "main"), () -> PROTOCOL_VERSION,
				PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

		channel.registerMessage(i++, NbtPacket.class, NbtPacket::toBytes, NbtPacket::new, NbtPacket::handle);
		channel.registerMessage(i++, ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::toBytes,
				ConfigureSchematicannonPacket::new, ConfigureSchematicannonPacket::handle);
		channel.registerMessage(i++, SchematicUploadPacket.class, SchematicUploadPacket::toBytes,
				SchematicUploadPacket::new, SchematicUploadPacket::handle);
		channel.registerMessage(i++, SymmetryEffectPacket.class, SymmetryEffectPacket::toBytes,
				SymmetryEffectPacket::new, SymmetryEffectPacket::handle);
	}

}
