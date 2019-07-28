package com.simibubi.create;

import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunBeamPacket;
import com.simibubi.create.modules.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.modules.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.modules.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.modules.symmetry.SymmetryEffectPacket;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class AllPackets {

	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Create.ID, "network");
	public static final String NETWORK_VERSION = new ResourceLocation(Create.ID, "1").toString();
	public static SimpleChannel channel;

	public static void registerPackets() {
		int i = 0;

		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME).serverAcceptedVersions(s -> true)
				.clientAcceptedVersions(s -> true).networkProtocolVersion(() -> NETWORK_VERSION).simpleChannel();

		channel.messageBuilder(NbtPacket.class, i++).decoder(NbtPacket::new).encoder(NbtPacket::toBytes)
				.consumer(NbtPacket::handle).add();
		channel.messageBuilder(SchematicPlacePacket.class, i++).decoder(SchematicPlacePacket::new)
				.encoder(SchematicPlacePacket::toBytes).consumer(SchematicPlacePacket::handle).add();
		channel.messageBuilder(ConfigureSchematicannonPacket.class, i++).decoder(ConfigureSchematicannonPacket::new)
				.encoder(ConfigureSchematicannonPacket::toBytes).consumer(ConfigureSchematicannonPacket::handle).add();
		channel.messageBuilder(SchematicUploadPacket.class, i++).decoder(SchematicUploadPacket::new)
				.encoder(SchematicUploadPacket::toBytes).consumer(SchematicUploadPacket::handle).add();
		channel.messageBuilder(SymmetryEffectPacket.class, i++).decoder(SymmetryEffectPacket::new)
				.encoder(SymmetryEffectPacket::toBytes).consumer(SymmetryEffectPacket::handle).add();
		channel.messageBuilder(BuilderGunBeamPacket.class, i++).decoder(BuilderGunBeamPacket::new)
				.encoder(BuilderGunBeamPacket::toBytes).consumer(BuilderGunBeamPacket::handle).add();

	}

}
