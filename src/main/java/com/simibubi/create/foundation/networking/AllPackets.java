package com.simibubi.create.foundation.networking;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.actors.controls.ContraptionDisableActorPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionBlockChangedPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionColliderLockPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionColliderLockPacket.ContraptionColliderLockPacketRequest;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRelocationPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.TrainCollisionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorContactEditPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorFloorListPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorTargetFloorPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraptionUpdatePacket;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.GlueEffectPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueRemovalPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueSelectionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsInputPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsStopControllingPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.HonkPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUDUpdatePacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ClientMotionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionFluidPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.LimbSwingUpdatePacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingCreationPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartControllerUpdatePacket;
import com.simibubi.create.content.contraptions.fluids.actors.FluidSplashPacket;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.ConfigureSequencedGearshiftPacket;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeObservedPacket;
import com.simibubi.create.content.curiosities.armor.NetheriteDivingHandler;
import com.simibubi.create.content.curiosities.bell.SoulPulseEffectPacket;
import com.simibubi.create.content.curiosities.clipboard.ClipboardEditPacket;
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
import com.simibubi.create.content.logistics.block.depot.EjectorAwardPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorElytraPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorPlacementPacket;
import com.simibubi.create.content.logistics.block.depot.EjectorTriggerPacket;
import com.simibubi.create.content.logistics.block.display.DisplayLinkConfigurationPacket;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerBindPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerInputPacket;
import com.simibubi.create.content.logistics.item.LinkedControllerStopLecternPacket;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.packet.ConfigureStockswitchPacket;
import com.simibubi.create.content.logistics.packet.FunnelFlapPacket;
import com.simibubi.create.content.logistics.packet.TunnelFlapPacket;
import com.simibubi.create.content.logistics.trains.TrackGraphRequestPacket;
import com.simibubi.create.content.logistics.trains.TrackGraphRollCallPacket;
import com.simibubi.create.content.logistics.trains.TrackGraphSyncPacket;
import com.simibubi.create.content.logistics.trains.entity.TrainPacket;
import com.simibubi.create.content.logistics.trains.entity.TrainPromptPacket;
import com.simibubi.create.content.logistics.trains.entity.TrainRelocationPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.CurvedTrackSelectionPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroupPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationEditPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.TrainEditPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.TrainEditPacket.TrainEditReturnPacket;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleEditPacket;
import com.simibubi.create.content.logistics.trains.track.CurvedTrackDestroyPacket;
import com.simibubi.create.content.logistics.trains.track.PlaceExtendedCurvePacket;
import com.simibubi.create.content.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import com.simibubi.create.foundation.command.HighlightPacket;
import com.simibubi.create.foundation.command.SConfigureConfigPacket;
import com.simibubi.create.foundation.config.ui.CConfigureConfigPacket;
import com.simibubi.create.foundation.gui.menu.ClearMenuPacket;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

public enum AllPackets {

	// Client to Server
	CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new, PLAY_TO_SERVER),
	CONFIGURE_STOCKSWITCH(ConfigureStockswitchPacket.class, ConfigureStockswitchPacket::new, PLAY_TO_SERVER),
	CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket::new,
		PLAY_TO_SERVER),
	PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new, PLAY_TO_SERVER),
	UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new, PLAY_TO_SERVER),
	CLEAR_CONTAINER(ClearMenuPacket.class, ClearMenuPacket::new, PLAY_TO_SERVER),
	CONFIGURE_FILTER(FilterScreenPacket.class, FilterScreenPacket::new, PLAY_TO_SERVER),
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
	LINKED_CONTROLLER_USE_LECTERN(LinkedControllerStopLecternPacket.class, LinkedControllerStopLecternPacket::new,
		PLAY_TO_SERVER),
	C_CONFIGURE_CONFIG(CConfigureConfigPacket.class, CConfigureConfigPacket::new, PLAY_TO_SERVER),
	SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket::new, PLAY_TO_SERVER),
	BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class, BlueprintAssignCompleteRecipePacket::new,
		PLAY_TO_SERVER),
	CONFIGURE_SYMMETRY_WAND(ConfigureSymmetryWandPacket.class, ConfigureSymmetryWandPacket::new, PLAY_TO_SERVER),
	CONFIGURE_WORLDSHAPER(ConfigureWorldshaperPacket.class, ConfigureWorldshaperPacket::new, PLAY_TO_SERVER),
	TOOLBOX_EQUIP(ToolboxEquipPacket.class, ToolboxEquipPacket::new, PLAY_TO_SERVER),
	TOOLBOX_DISPOSE_ALL(ToolboxDisposeAllPacket.class, ToolboxDisposeAllPacket::new, PLAY_TO_SERVER),
	CONFIGURE_SCHEDULE(ScheduleEditPacket.class, ScheduleEditPacket::new, PLAY_TO_SERVER),
	CONFIGURE_STATION(StationEditPacket.class, StationEditPacket::new, PLAY_TO_SERVER),
	C_CONFIGURE_TRAIN(TrainEditPacket.class, TrainEditPacket::new, PLAY_TO_SERVER),
	RELOCATE_TRAIN(TrainRelocationPacket.class, TrainRelocationPacket::new, PLAY_TO_SERVER),
	CONTROLS_INPUT(ControlsInputPacket.class, ControlsInputPacket::new, PLAY_TO_SERVER),
	CONFIGURE_DATA_GATHERER(DisplayLinkConfigurationPacket.class, DisplayLinkConfigurationPacket::new, PLAY_TO_SERVER),
	DESTROY_CURVED_TRACK(CurvedTrackDestroyPacket.class, CurvedTrackDestroyPacket::new, PLAY_TO_SERVER),
	SELECT_CURVED_TRACK(CurvedTrackSelectionPacket.class, CurvedTrackSelectionPacket::new, PLAY_TO_SERVER),
	PLACE_CURVED_TRACK(PlaceExtendedCurvePacket.class, PlaceExtendedCurvePacket::new, PLAY_TO_SERVER),
	GLUE_IN_AREA(SuperGlueSelectionPacket.class, SuperGlueSelectionPacket::new, PLAY_TO_SERVER),
	GLUE_REMOVED(SuperGlueRemovalPacket.class, SuperGlueRemovalPacket::new, PLAY_TO_SERVER),
	TRAIN_COLLISION(TrainCollisionPacket.class, TrainCollisionPacket::new, PLAY_TO_SERVER),
	C_TRAIN_HUD(TrainHUDUpdatePacket.Serverbound.class, TrainHUDUpdatePacket.Serverbound::new, PLAY_TO_SERVER),
	C_TRAIN_HONK(HonkPacket.Serverbound.class, HonkPacket.Serverbound::new, PLAY_TO_SERVER),
	OBSERVER_STRESSOMETER(GaugeObservedPacket.class, GaugeObservedPacket::new, PLAY_TO_SERVER),
	EJECTOR_AWARD(EjectorAwardPacket.class, EjectorAwardPacket::new, PLAY_TO_SERVER),
	TRACK_GRAPH_REQUEST(TrackGraphRequestPacket.class, TrackGraphRequestPacket::new, PLAY_TO_SERVER),
	CONFIGURE_ELEVATOR_CONTACT(ElevatorContactEditPacket.class, ElevatorContactEditPacket::new, PLAY_TO_SERVER),
	REQUEST_FLOOR_LIST(ElevatorFloorListPacket.RequestFloorList.class, ElevatorFloorListPacket.RequestFloorList::new,
		PLAY_TO_SERVER),
	ELEVATOR_SET_FLOOR(ElevatorTargetFloorPacket.class, ElevatorTargetFloorPacket::new, PLAY_TO_SERVER),
	VALUE_SETTINGS(ValueSettingsPacket.class, ValueSettingsPacket::new, PLAY_TO_SERVER),
	CLIPBOARD_EDIT(ClipboardEditPacket.class, ClipboardEditPacket::new, PLAY_TO_SERVER),
	CONTRAPTION_COLLIDER_LOCK_REQUEST(ContraptionColliderLockPacketRequest.class,
		ContraptionColliderLockPacketRequest::new, PLAY_TO_SERVER),

	// Server to Client
	SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket::new, PLAY_TO_CLIENT),
	SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet::new, PLAY_TO_CLIENT),
	BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket::new, PLAY_TO_CLIENT),
	S_CONFIGURE_CONFIG(SConfigureConfigPacket.class, SConfigureConfigPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_BLOCK_CHANGED(ContraptionBlockChangedPacket.class, ContraptionBlockChangedPacket::new, PLAY_TO_CLIENT),
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
	PERSISTENT_DATA(ISyncPersistentData.PersistentDataPacket.class, ISyncPersistentData.PersistentDataPacket::new,
		PLAY_TO_CLIENT),
	SYNC_POTATO_PROJECTILE_TYPES(PotatoProjectileTypeManager.SyncPacket.class,
		PotatoProjectileTypeManager.SyncPacket::new, PLAY_TO_CLIENT),
	SYNC_RAIL_GRAPH(TrackGraphSyncPacket.class, TrackGraphSyncPacket::new, PLAY_TO_CLIENT),
	SYNC_EDGE_GROUP(SignalEdgeGroupPacket.class, SignalEdgeGroupPacket::new, PLAY_TO_CLIENT),
	SYNC_TRAIN(TrainPacket.class, TrainPacket::new, PLAY_TO_CLIENT),
	REMOVE_TE(RemoveBlockEntityPacket.class, RemoveBlockEntityPacket::new, PLAY_TO_CLIENT),
	S_CONFIGURE_TRAIN(TrainEditReturnPacket.class, TrainEditReturnPacket::new, PLAY_TO_CLIENT),
	CONTROLS_ABORT(ControlsStopControllingPacket.class, ControlsStopControllingPacket::new, PLAY_TO_CLIENT),
	S_TRAIN_HUD(TrainHUDUpdatePacket.class, TrainHUDUpdatePacket::new, PLAY_TO_CLIENT),
	S_TRAIN_HONK(HonkPacket.class, HonkPacket::new, PLAY_TO_CLIENT),
	S_TRAIN_PROMPT(TrainPromptPacket.class, TrainPromptPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_RELOCATION(ContraptionRelocationPacket.class, ContraptionRelocationPacket::new, PLAY_TO_CLIENT),
	TRACK_GRAPH_ROLL_CALL(TrackGraphRollCallPacket.class, TrackGraphRollCallPacket::new, PLAY_TO_CLIENT),
	S_PLACE_EJECTOR(ArmPlacementPacket.ClientBoundRequest.class, ArmPlacementPacket.ClientBoundRequest::new,
		PLAY_TO_CLIENT),
	S_PLACE_ARM(EjectorPlacementPacket.ClientBoundRequest.class, EjectorPlacementPacket.ClientBoundRequest::new,
		PLAY_TO_CLIENT),
	UPDATE_ELEVATOR_FLOORS(ElevatorFloorListPacket.class, ElevatorFloorListPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_ACTOR_TOGGLE(ContraptionDisableActorPacket.class, ContraptionDisableActorPacket::new, PLAY_TO_CLIENT),
	SET_FIRE_IMMUNE(NetheriteDivingHandler.SetFireImmunePacket.class, NetheriteDivingHandler.SetFireImmunePacket::new,
		PLAY_TO_CLIENT),
	CONTRAPTION_COLLIDER_LOCK(ContraptionColliderLockPacket.class, ContraptionColliderLockPacket::new, PLAY_TO_CLIENT),

	;

	public static final ResourceLocation CHANNEL_NAME = Create.asResource("main");
	public static final int NETWORK_VERSION = 2;
	public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
	private static SimpleChannel channel;

	private PacketType<?> packetType;

	<T extends SimplePacketBase> AllPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
		NetworkDirection direction) {
		packetType = new PacketType<>(type, factory, direction);
	}

	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
			.serverAcceptedVersions(NETWORK_VERSION_STR::equals)
			.clientAcceptedVersions(NETWORK_VERSION_STR::equals)
			.networkProtocolVersion(() -> NETWORK_VERSION_STR)
			.simpleChannel();

		for (AllPackets packet : values())
			packet.packetType.register();
	}

	public static SimpleChannel getChannel() {
		return channel;
	}

	public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
		getChannel().send(
			PacketDistributor.NEAR.with(TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.dimension())),
			message);
	}

	private static class PacketType<T extends SimplePacketBase> {
		private static int index = 0;

		private BiConsumer<T, FriendlyByteBuf> encoder;
		private Function<FriendlyByteBuf, T> decoder;
		private BiConsumer<T, Supplier<Context>> handler;
		private Class<T> type;
		private NetworkDirection direction;

		private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
			encoder = T::write;
			decoder = factory;
			handler = (packet, contextSupplier) -> {
				Context context = contextSupplier.get();
				if (packet.handle(context)) {
					context.setPacketHandled(true);
				}
			};
			this.type = type;
			this.direction = direction;
		}

		private void register() {
			getChannel().messageBuilder(type, index++, direction)
				.encoder(encoder)
				.decoder(decoder)
				.consumerNetworkThread(handler)
				.add();
		}
	}

}
