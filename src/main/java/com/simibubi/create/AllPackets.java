package com.simibubi.create;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.foundation.packet.SimplePacketBase;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ConfigureChassisPacket;
import com.simibubi.create.modules.contraptions.components.mixer.ConfigureMixerPacket;
import com.simibubi.create.modules.contraptions.components.motor.ConfigureMotorPacket;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunBeamPacket;
import com.simibubi.create.modules.curiosities.symmetry.SymmetryEffectPacket;
import com.simibubi.create.modules.logistics.block.diodes.ConfigureFlexpeaterPacket;
import com.simibubi.create.modules.logistics.management.controller.LogisticalControllerConfigurationPacket;
import com.simibubi.create.modules.logistics.management.index.IndexContainerUpdatePacket;
import com.simibubi.create.modules.logistics.management.index.IndexOrderRequest;
import com.simibubi.create.modules.logistics.packet.ConfigureFlexcratePacket;
import com.simibubi.create.modules.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.modules.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.modules.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.modules.schematics.packet.SchematicUploadPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public enum AllPackets {

	// Client to Server
	NBT(NbtPacket.class, NbtPacket::new),
	CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new),
	CONFIGURE_FLEXCRATE(ConfigureFlexcratePacket.class, ConfigureFlexcratePacket::new),
	CONFIGURE_STOCKSWITCH(ConfigureStockswitchPacket.class, ConfigureStockswitchPacket::new),
	CONFIGURE_CHASSIS(ConfigureChassisPacket.class, ConfigureChassisPacket::new),
	CONFIGURE_MOTOR(ConfigureMotorPacket.class, ConfigureMotorPacket::new),
	CONFIGURE_FLEXPEATER(ConfigureFlexpeaterPacket.class, ConfigureFlexpeaterPacket::new),
	CONFIGURE_LOGISTICAL_CONTROLLER(LogisticalControllerConfigurationPacket.class,
			LogisticalControllerConfigurationPacket::new),
	CONFIGURE_MIXER(ConfigureMixerPacket.class, ConfigureMixerPacket::new),
	PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new),
	UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new),
	INDEX_ORDER_REQUEST(IndexOrderRequest.class, IndexOrderRequest::new),

	// Server to Client
	SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket::new),
	BEAM_EFFECT(BuilderGunBeamPacket.class, BuilderGunBeamPacket::new),
	INDEX_CONTAINER_UPDATE(IndexContainerUpdatePacket.class, IndexContainerUpdatePacket::new),

	;

	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Create.ID, "network");
	public static final String NETWORK_VERSION = new ResourceLocation(Create.ID, "1").toString();
	public static SimpleChannel channel;

	private LoadedPacket<?> packet;

	private <T extends SimplePacketBase> AllPackets(Class<T> type, Function<PacketBuffer, T> factory) {
		packet = new LoadedPacket<>(type, factory);
	}

	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME).serverAcceptedVersions(s -> true)
				.clientAcceptedVersions(s -> true).networkProtocolVersion(() -> NETWORK_VERSION).simpleChannel();
		for (AllPackets packet : values())
			packet.packet.register();

	}

	private static class LoadedPacket<T extends SimplePacketBase> {
		private static int index = 0;
		BiConsumer<T, PacketBuffer> encoder;
		Function<PacketBuffer, T> decoder;
		BiConsumer<T, Supplier<Context>> handler;
		Class<T> type;

		private LoadedPacket(Class<T> type, Function<PacketBuffer, T> factory) {
			encoder = T::write;
			decoder = factory;
			handler = T::handle;
			this.type = type;
		}

		private void register() {
			channel.messageBuilder(type, index++).encoder(encoder).decoder(decoder).consumer(handler).add();
		}
	}

}
