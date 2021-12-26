package com.simibubi.create.foundation.networking;

import static com.simibubi.create.foundation.networking.SimplePacketBase.NetworkDirection.PLAY_TO_CLIENT;
import static com.simibubi.create.foundation.networking.SimplePacketBase.NetworkDirection.PLAY_TO_SERVER;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraptionUpdatePacket;
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
import com.simibubi.create.content.curiosities.bell.SoulPulseEffectPacket;
import com.simibubi.create.content.curiosities.symmetry.ConfigureSymmetryWandPacket;
import com.simibubi.create.content.curiosities.symmetry.SymmetryEffectPacket;
import com.simibubi.create.content.curiosities.toolbox.ToolboxDisposeAllPacket;
import com.simibubi.create.content.curiosities.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.curiosities.tools.ExtendoGripInteractionPacket;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonPacket;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;
import com.simibubi.create.content.curiosities.zapper.ZapperBeamPacket;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.ConfigureWorldshaperPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorElytraPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorPlacementPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorTriggerPacket;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerBindPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerInputPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerStopLecternPacket;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.content.logistics.packet.FunnelFlapPacket;
import com.simibubi.create.content.logistics.packet.TunnelFlapPacket;
import com.simibubi.create.content.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.foundation.command.HighlightPacket;
import com.simibubi.create.foundation.command.SConfigureConfigPacket;
import com.simibubi.create.foundation.config.ui.CConfigureConfigPacket;
import com.simibubi.create.foundation.gui.container.ClearContainerPacket;
import com.simibubi.create.foundation.gui.container.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase.NetworkDirection;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringCountUpdatePacket;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueUpdatePacket;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public enum AllPackets {

	// Client to Server
	CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new, PLAY_TO_SERVER),
	CONFIGURE_STOCKSWITCH(ConfigureStockswitchPacket.class, ConfigureStockswitchPacket::new, PLAY_TO_SERVER),
	CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket::new,
			PLAY_TO_SERVER),
	PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new, PLAY_TO_SERVER),
	UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new, PLAY_TO_SERVER),
	CLEAR_CONTAINER(ClearContainerPacket.class, ClearContainerPacket::new, PLAY_TO_SERVER),
	CONFIGURE_FILTER(FilterScreenPacket.class, FilterScreenPacket::new, PLAY_TO_SERVER),
	CONFIGURE_FILTERING_AMOUNT(FilteringCountUpdatePacket.class, FilteringCountUpdatePacket::new, PLAY_TO_SERVER),
	CONFIGURE_SCROLLABLE(ScrollValueUpdatePacket.class, ScrollValueUpdatePacket::new, PLAY_TO_SERVER),
	EXTENDO_INTERACT(ExtendoGripInteractionPacket.class, ExtendoGripInteractionPacket::new, PLAY_TO_SERVER),
	CONTRAPTION_INTERACT(ContraptionInteractionPacket.class, ContraptionInteractionPacket::new, PLAY_TO_SERVER),
	CLIENT_MOTION(ClientMotionPacket.class, ClientMotionPacket::new, PLAY_TO_SERVER),
	PLACE_ARM(ArmPlacementPacket.class, ArmPlacementPacket::new, PLAY_TO_SERVER),
	MINECART_COUPLING_CREATION(CouplingCreationPacket.class, CouplingCreationPacket::new, PLAY_TO_SERVER),
	INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket::new, PLAY_TO_SERVER),
	SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket::new, PLAY_TO_SERVER),
	LEFT_CLICK(LeftClickPacket.class, LeftClickPacket::new, PLAY_TO_SERVER),
	PLACE_EJECTOR(EjectorPlacementPacket.class, EjectorPlacementPacket::new, PLAY_TO_SERVER),
	TRIGGER_EJECTOR(EjectorTriggerPacket.class, EjectorTriggerPacket::new, PLAY_TO_SERVER),
	EJECTOR_ELYTRA(EjectorElytraPacket.class, EjectorElytraPacket::new, PLAY_TO_SERVER),
	LINKED_CONTROLLER_INPUT(LinkedControllerInputPacket.class, LinkedControllerInputPacket::new, PLAY_TO_SERVER),
	LINKED_CONTROLLER_BIND(LinkedControllerBindPacket.class, LinkedControllerBindPacket::new, PLAY_TO_SERVER),
	LINKED_CONTROLLER_USE_LECTERN(LinkedControllerStopLecternPacket.class, LinkedControllerStopLecternPacket::new, PLAY_TO_SERVER),
	C_CONFIGURE_CONFIG(CConfigureConfigPacket.class, CConfigureConfigPacket::new, PLAY_TO_SERVER),
	SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket::new, PLAY_TO_SERVER),
	BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class, BlueprintAssignCompleteRecipePacket::new, PLAY_TO_SERVER),
	CONFIGURE_SYMMETRY_WAND(ConfigureSymmetryWandPacket.class, ConfigureSymmetryWandPacket::new, PLAY_TO_SERVER),
	CONFIGURE_WORLDSHAPER(ConfigureWorldshaperPacket.class, ConfigureWorldshaperPacket::new, PLAY_TO_SERVER),
	TOOLBOX_EQUIP(ToolboxEquipPacket.class, ToolboxEquipPacket::new, PLAY_TO_SERVER),
	TOOLBOX_DISPOSE_ALL(ToolboxDisposeAllPacket.class, ToolboxDisposeAllPacket::new, PLAY_TO_SERVER),

	// Server to Client
	SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket::new, PLAY_TO_CLIENT),
	SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet::new, PLAY_TO_CLIENT),
	BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket::new, PLAY_TO_CLIENT),
	S_CONFIGURE_CONFIG(SConfigureConfigPacket.class, SConfigureConfigPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket::new, PLAY_TO_CLIENT),
	GLUE_EFFECT(GlueEffectPacket.class, GlueEffectPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_SEAT_MAPPING(ContraptionSeatMappingPacket.class, ContraptionSeatMappingPacket::new, PLAY_TO_CLIENT),
	LIMBSWING_UPDATE(LimbSwingUpdatePacket.class, LimbSwingUpdatePacket::new, PLAY_TO_CLIENT),
	MINECART_CONTROLLER(MinecartControllerUpdatePacket.class, MinecartControllerUpdatePacket::new, PLAY_TO_CLIENT),
	FLUID_SPLASH(FluidSplashPacket.class, FluidSplashPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_FLUID(ContraptionFluidPacket.class, ContraptionFluidPacket::new, PLAY_TO_CLIENT),
	GANTRY_UPDATE(GantryContraptionUpdatePacket.class, GantryContraptionUpdatePacket::new, PLAY_TO_CLIENT),
	BLOCK_HIGHLIGHT(HighlightPacket.class, HighlightPacket::new, PLAY_TO_CLIENT),
	TUNNEL_FLAP(TunnelFlapPacket.class, TunnelFlapPacket::new, PLAY_TO_CLIENT),
	FUNNEL_FLAP(FunnelFlapPacket.class, FunnelFlapPacket::new, PLAY_TO_CLIENT),
	POTATO_CANNON(PotatoCannonPacket.class, PotatoCannonPacket::new, PLAY_TO_CLIENT),
	SOUL_PULSE(SoulPulseEffectPacket.class, SoulPulseEffectPacket::new, PLAY_TO_CLIENT),
	PERSISTENT_DATA(ISyncPersistentData.PersistentDataPacket.class, ISyncPersistentData.PersistentDataPacket::new, PLAY_TO_CLIENT),
	SYNC_POTATO_PROJECTILE_TYPES(PotatoProjectileTypeManager.SyncPacket.class, PotatoProjectileTypeManager.SyncPacket::new, PLAY_TO_CLIENT),

	;

	public static final ResourceLocation CHANNEL_NAME = Create.asResource("main");
	public static final int NETWORK_VERSION = 1;
	public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
	public static SimpleChannel channel;

	private LoadedPacket<?> packet;

	<T extends SimplePacketBase> AllPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
		NetworkDirection direction) {
		packet = new LoadedPacket<>(type, factory, direction);
	}

	public static void registerPackets() {
		channel = new SimpleChannel(CHANNEL_NAME);
		int id = 0;
		for (AllPackets packet : values()) {
			boolean registered = false;
			if (packet.packet.direction == PLAY_TO_SERVER) {
				channel.registerC2SPacket(packet.packet.type, id++);
				registered = true;
			}
			if (packet.packet.direction == PLAY_TO_CLIENT) {
				channel.registerS2CPacket(packet.packet.type, id++);
				registered = true;
			}
			if (!registered) {
				Create.LOGGER.error("Could not register packet with type " + packet.packet.type);
			}
		}
	}

	public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
		channel.sendToClientsAround((S2CPacket) message, (ServerLevel) world, pos, range);
	}

	private static class LoadedPacket<T extends SimplePacketBase> {
		private static int index = 0;

		private BiConsumer<T, FriendlyByteBuf> encoder;
		private Function<FriendlyByteBuf, T> decoder;
		private BiConsumer<T, Supplier<SimplePacketBase.Context>> handler;
		private Class<T> type;
		private NetworkDirection direction;

		private LoadedPacket(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
			encoder = T::write;
			decoder = factory;
			handler = T::handle;
			this.type = type;
			this.direction = direction;
		}

//		private void register() {
//			channel.registerC2SPacket();
//			channel.messageBuilder(type, index++, direction)
//				.encoder(encoder)
//				.decoder(decoder)
//				.consumer(handler)
//				.add();
//		}
	}

}
