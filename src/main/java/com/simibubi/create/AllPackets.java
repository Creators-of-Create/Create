package com.simibubi.create;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.content.contraptions.ContraptionBlockChangedPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket.ContraptionColliderLockPacketRequest;
import com.simibubi.create.content.contraptions.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.ContraptionRelocationPacket;
import com.simibubi.create.content.contraptions.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.TrainCollisionPacket;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionDisableActorPacket;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsInputPacket;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsStopControllingPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactEditPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorFloorListPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorTargetFloorPacket;
import com.simibubi.create.content.contraptions.gantry.GantryContraptionUpdatePacket;
import com.simibubi.create.content.contraptions.glue.GlueEffectPacket;
import com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket;
import com.simibubi.create.content.contraptions.minecart.CouplingCreationPacket;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartControllerUpdatePacket;
import com.simibubi.create.content.contraptions.sync.ClientMotionPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionFluidPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.sync.LimbSwingUpdatePacket;
import com.simibubi.create.content.equipment.bell.SoulPulseEffectPacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripInteractionPacket;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonPacket;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileTypeManager;
import com.simibubi.create.content.equipment.symmetryWand.ConfigureSymmetryWandPacket;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryEffectPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.equipment.zapper.ZapperBeamPacket;
import com.simibubi.create.content.equipment.zapper.terrainzapper.ConfigureWorldshaperPacket;
import com.simibubi.create.content.fluids.transfer.FluidSplashPacket;
import com.simibubi.create.content.kinetics.gauge.GaugeObservedPacket;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.kinetics.transmission.sequencer.ConfigureSequencedGearshiftPacket;
import com.simibubi.create.content.logistics.depot.EjectorAwardPacket;
import com.simibubi.create.content.logistics.depot.EjectorElytraPacket;
import com.simibubi.create.content.logistics.depot.EjectorPlacementPacket;
import com.simibubi.create.content.logistics.depot.EjectorTriggerPacket;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.funnel.FunnelFlapPacket;
import com.simibubi.create.content.logistics.tunnel.TunnelFlapPacket;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkConfigurationPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerBindPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerInputPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerStopLecternPacket;
import com.simibubi.create.content.redstone.thresholdSwitch.ConfigureThresholdSwitchPacket;
import com.simibubi.create.content.schematics.cannon.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.content.trains.HonkPacket;
import com.simibubi.create.content.trains.TrainHUDUpdatePacket;
import com.simibubi.create.content.trains.entity.TrainPacket;
import com.simibubi.create.content.trains.entity.TrainPromptPacket;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.content.trains.graph.TrackGraphRequestPacket;
import com.simibubi.create.content.trains.graph.TrackGraphRollCallPacket;
import com.simibubi.create.content.trains.graph.TrackGraphSyncPacket;
import com.simibubi.create.content.trains.schedule.ScheduleEditPacket;
import com.simibubi.create.content.trains.signal.SignalEdgeGroupPacket;
import com.simibubi.create.content.trains.station.StationEditPacket;
import com.simibubi.create.content.trains.station.TrainEditPacket;
import com.simibubi.create.content.trains.station.TrainEditPacket.TrainEditReturnPacket;
import com.simibubi.create.content.trains.track.CurvedTrackDestroyPacket;
import com.simibubi.create.content.trains.track.CurvedTrackSelectionPacket;
import com.simibubi.create.content.trains.track.PlaceExtendedCurvePacket;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import com.simibubi.create.foundation.config.ui.CConfigureConfigPacket;
import com.simibubi.create.foundation.gui.menu.ClearMenuPacket;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.infrastructure.command.HighlightPacket;
import com.simibubi.create.infrastructure.command.SConfigureConfigPacket;
import com.simibubi.create.infrastructure.debugInfo.ServerDebugInfoPacket;

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
	CONFIGURE_STOCKSWITCH(ConfigureThresholdSwitchPacket.class, ConfigureThresholdSwitchPacket::new, PLAY_TO_SERVER),
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
	UPDATE_ELEVATOR_FLOORS(ElevatorFloorListPacket.class, ElevatorFloorListPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_ACTOR_TOGGLE(ContraptionDisableActorPacket.class, ContraptionDisableActorPacket::new, PLAY_TO_CLIENT),
	CONTRAPTION_COLLIDER_LOCK(ContraptionColliderLockPacket.class, ContraptionColliderLockPacket::new, PLAY_TO_CLIENT),
	ATTACHED_COMPUTER(AttachedComputerPacket.class, AttachedComputerPacket::new, PLAY_TO_CLIENT),
	SERVER_DEBUG_INFO(ServerDebugInfoPacket.class, ServerDebugInfoPacket::new, PLAY_TO_CLIENT)
	;

	public static final ResourceLocation CHANNEL_NAME = Create.asResource("main");
	public static final int NETWORK_VERSION = 3;
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
				.consumer(handler)
				.add();
		}
	}

}
