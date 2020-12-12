package com.simibubi.create.foundation.networking;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.GlueEffectPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ClientMotionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionFluidPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.LimbSwingUpdatePacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingCreationPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartControllerUpdatePacket;
import com.simibubi.create.content.contraptions.fluids.actors.FluidSplashPacket;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.ConfigureSequencedGearshiftPacket;
import com.simibubi.create.content.curiosities.symmetry.SymmetryEffectPacket;
import com.simibubi.create.content.curiosities.tools.ExtendoGripInteractionPacket;
import com.simibubi.create.content.curiosities.zapper.ZapperBeamPacket;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.packet.ConfigureFlexcratePacket;
import com.simibubi.create.content.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.content.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.foundation.command.ConfigureConfigPacket;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringCountUpdatePacket;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueUpdatePacket;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public enum AllPackets {

	// Client to Server
	NBT(NbtPacket.class, NbtPacket::new),
	CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new),
	CONFIGURE_FLEXCRATE(ConfigureFlexcratePacket.class, ConfigureFlexcratePacket::new),
	CONFIGURE_STOCKSWITCH(ConfigureStockswitchPacket.class, ConfigureStockswitchPacket::new),
	CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket::new),
	PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new),
	UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new),
	CONFIGURE_FILTER(FilterScreenPacket.class, FilterScreenPacket::new),
	CONFIGURE_FILTERING_AMOUNT(FilteringCountUpdatePacket.class, FilteringCountUpdatePacket::new),
	CONFIGURE_SCROLLABLE(ScrollValueUpdatePacket.class, ScrollValueUpdatePacket::new),
	EXTENDO_INTERACT(ExtendoGripInteractionPacket.class, ExtendoGripInteractionPacket::new),
	CONTRAPTION_INTERACT(ContraptionInteractionPacket.class, ContraptionInteractionPacket::new),
	CLIENT_MOTION(ClientMotionPacket.class, ClientMotionPacket::new),
	PLACE_ARM(ArmPlacementPacket.class, ArmPlacementPacket::new),
	MINECART_COUPLING_CREATION(CouplingCreationPacket.class, CouplingCreationPacket::new),
	INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket::new),
	SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket::new),
	LEFT_CLICK(LeftClickPacket.class, LeftClickPacket::new),

	// Server to Client
	SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket::new),
	SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet::new),
	BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket::new),
	CONFIGURE_CONFIG(ConfigureConfigPacket.class, ConfigureConfigPacket::new),
	CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket::new),
	CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket::new),
	GLUE_EFFECT(GlueEffectPacket.class, GlueEffectPacket::new),
	CONTRAPTION_SEAT_MAPPING(ContraptionSeatMappingPacket.class, ContraptionSeatMappingPacket::new),
	LIMBSWING_UPDATE(LimbSwingUpdatePacket.class, LimbSwingUpdatePacket::new),
	MINECART_CONTROLLER(MinecartControllerUpdatePacket.class, MinecartControllerUpdatePacket::new),
	FLUID_SPLASH(FluidSplashPacket.class, FluidSplashPacket::new),
	CONTRAPTION_FLUID(ContraptionFluidPacket.class, ContraptionFluidPacket::new),

	;

	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Create.ID, "network");
	public static final String NETWORK_VERSION = new ResourceLocation(Create.ID, "1").toString();
	public static SimpleChannel channel;

	private LoadedPacket<?> packet;

	private <T extends SimplePacketBase> AllPackets(Class<T> type, Function<PacketBuffer, T> factory) {
		packet = new LoadedPacket<>(type, factory);
	}

	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
			.serverAcceptedVersions(s -> true)
			.clientAcceptedVersions(s -> true)
			.networkProtocolVersion(() -> NETWORK_VERSION)
			.simpleChannel();
		for (AllPackets packet : values())
			packet.packet.register();
	}

	public static void sendToNear(World world, BlockPos pos, int range, Object message) {
		channel.send(
			PacketDistributor.NEAR.with(TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.getDimension()
				.getType())),
			message);
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
			channel.messageBuilder(type, index++)
				.encoder(encoder)
				.decoder(decoder)
				.consumer(handler)
				.add();
		}
	}

}
