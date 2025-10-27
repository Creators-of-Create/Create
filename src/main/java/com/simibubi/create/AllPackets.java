package com.simibubi.create;

import java.util.Locale;

import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.compat.trainmap.TrainMapSyncPacket;
import com.simibubi.create.compat.trainmap.TrainMapSyncRequestPacket;
import com.simibubi.create.content.contraptions.ContraptionBlockChangedPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket.ContraptionColliderLockPacketRequest;
import com.simibubi.create.content.contraptions.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.ContraptionRelocationPacket;
import com.simibubi.create.content.contraptions.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.MountedStorageSyncPacket;
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
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.sync.LimbSwingUpdatePacket;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.equipment.bell.SoulPulseEffectPacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripInteractionPacket;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonPacket;
import com.simibubi.create.content.equipment.symmetryWand.ConfigureSymmetryWandPacket;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryEffectPacket;
import com.simibubi.create.content.equipment.tool.KnockbackPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.equipment.zapper.ZapperBeamPacket;
import com.simibubi.create.content.equipment.zapper.terrainzapper.ConfigureWorldshaperPacket;
import com.simibubi.create.content.fluids.transfer.FluidSplashPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ChainPackageInteractionPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ClientboundChainConveyorRidingPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ServerboundChainConveyorRidingPacket;
import com.simibubi.create.content.kinetics.gauge.GaugeObservedPacket;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.kinetics.transmission.sequencer.ConfigureSequencedGearshiftPacket;
import com.simibubi.create.content.logistics.box.PackageDestroyPacket;
import com.simibubi.create.content.logistics.depot.EjectorAwardPacket;
import com.simibubi.create.content.logistics.depot.EjectorElytraPacket;
import com.simibubi.create.content.logistics.depot.EjectorPlacementPacket;
import com.simibubi.create.content.logistics.depot.EjectorTriggerPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConfigurationPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelEffectPacket;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.funnel.FunnelFlapPacket;
import com.simibubi.create.content.logistics.packagePort.PackagePortConfigurationPacket;
import com.simibubi.create.content.logistics.packagePort.PackagePortPlacementPacket;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryEditPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryHidingPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperLockPacket;
import com.simibubi.create.content.logistics.tableCloth.ShopUpdatePacket;
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
import com.simibubi.create.content.trains.entity.AddTrainPacket;
import com.simibubi.create.content.trains.entity.RemoveTrainPacket;
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
import com.simibubi.create.foundation.gui.menu.ClearMenuPacket;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.infrastructure.command.HighlightPacket;
import com.simibubi.create.infrastructure.command.SimpleCreateActions;
import com.simibubi.create.infrastructure.debugInfo.ServerDebugInfoPacket;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.packets.ClientboundSimpleActionPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum AllPackets implements BasePacketPayload.PacketTypeProvider {
	// Client to Server
	CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket.STREAM_CODEC),
	CONFIGURE_STOCKSWITCH(ConfigureThresholdSwitchPacket.class, ConfigureThresholdSwitchPacket.STREAM_CODEC),
	CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket.STREAM_CODEC),
	PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket.STREAM_CODEC),
	UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket.STREAM_CODEC),
	CLEAR_CONTAINER(ClearMenuPacket.class, ClearMenuPacket.STREAM_CODEC),
	CONFIGURE_FILTER(FilterScreenPacket.class, FilterScreenPacket.STREAM_CODEC),
	EXTENDO_INTERACT(ExtendoGripInteractionPacket.class, ExtendoGripInteractionPacket.STREAM_CODEC),
	CONTRAPTION_INTERACT(ContraptionInteractionPacket.class, ContraptionInteractionPacket.STREAM_CODEC),
	CLIENT_MOTION(ClientMotionPacket.class, ClientMotionPacket.STREAM_CODEC),
	PLACE_ARM(ArmPlacementPacket.class, ArmPlacementPacket.STREAM_CODEC),
	PLACE_PACKAGE_PORT(PackagePortPlacementPacket.class, PackagePortPlacementPacket.STREAM_CODEC),
	MINECART_COUPLING_CREATION(CouplingCreationPacket.class, CouplingCreationPacket.STREAM_CODEC),
	INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket.STREAM_CODEC),
	SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket.STREAM_CODEC),
	LEFT_CLICK(LeftClickPacket.class, LeftClickPacket.STREAM_CODEC),
	PLACE_EJECTOR(EjectorPlacementPacket.class, EjectorPlacementPacket.STREAM_CODEC),
	TRIGGER_EJECTOR(EjectorTriggerPacket.class, EjectorTriggerPacket.STREAM_CODEC),
	EJECTOR_ELYTRA(EjectorElytraPacket.class, EjectorElytraPacket.STREAM_CODEC),
	LINKED_CONTROLLER_INPUT(LinkedControllerInputPacket.class, LinkedControllerInputPacket.STREAM_CODEC),
	LINKED_CONTROLLER_BIND(LinkedControllerBindPacket.class, LinkedControllerBindPacket.STREAM_CODEC),
	LINKED_CONTROLLER_USE_LECTERN(LinkedControllerStopLecternPacket.class, LinkedControllerStopLecternPacket.STREAM_CODEC),
	SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket.STREAM_CODEC),
	BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class, BlueprintAssignCompleteRecipePacket.STREAM_CODEC),
	CONFIGURE_SYMMETRY_WAND(ConfigureSymmetryWandPacket.class, ConfigureSymmetryWandPacket.STREAM_CODEC),
	CONFIGURE_WORLDSHAPER(ConfigureWorldshaperPacket.class, ConfigureWorldshaperPacket.STREAM_CODEC),
	TOOLBOX_EQUIP(ToolboxEquipPacket.class, ToolboxEquipPacket.STREAM_CODEC),
	TOOLBOX_DISPOSE_ALL(ToolboxDisposeAllPacket.class, ToolboxDisposeAllPacket.STREAM_CODEC),
	CONFIGURE_SCHEDULE(ScheduleEditPacket.class, ScheduleEditPacket.STREAM_CODEC),
	CONFIGURE_STATION(StationEditPacket.class, StationEditPacket.STREAM_CODEC),
	C_CONFIGURE_TRAIN(TrainEditPacket.Serverbound.class, TrainEditPacket.Serverbound.STREAM_CODEC),
	RELOCATE_TRAIN(TrainRelocationPacket.class, TrainRelocationPacket.STREAM_CODEC),
	CONTROLS_INPUT(ControlsInputPacket.class, ControlsInputPacket.STREAM_CODEC),
	CONFIGURE_DATA_GATHERER(DisplayLinkConfigurationPacket.class, DisplayLinkConfigurationPacket.STREAM_CODEC),
	DESTROY_CURVED_TRACK(CurvedTrackDestroyPacket.class, CurvedTrackDestroyPacket.STREAM_CODEC),
	SELECT_CURVED_TRACK(CurvedTrackSelectionPacket.class, CurvedTrackSelectionPacket.STREAM_CODEC),
	PLACE_CURVED_TRACK(PlaceExtendedCurvePacket.class, PlaceExtendedCurvePacket.STREAM_CODEC),
	GLUE_IN_AREA(SuperGlueSelectionPacket.class, SuperGlueSelectionPacket.STREAM_CODEC),
	GLUE_REMOVED(SuperGlueRemovalPacket.class, SuperGlueRemovalPacket.STREAM_CODEC),
	TRAIN_COLLISION(TrainCollisionPacket.class, TrainCollisionPacket.STREAM_CODEC),
	C_TRAIN_HUD(TrainHUDUpdatePacket.Serverbound.class, TrainHUDUpdatePacket.Serverbound.STREAM_CODEC),
	C_TRAIN_HONK(HonkPacket.Serverbound.class, HonkPacket.Serverbound.STREAM_CODEC),
	OBSERVER_STRESSOMETER(GaugeObservedPacket.class, GaugeObservedPacket.STREAM_CODEC),
	EJECTOR_AWARD(EjectorAwardPacket.class, EjectorAwardPacket.STREAM_CODEC),
	TRACK_GRAPH_REQUEST(TrackGraphRequestPacket.class, TrackGraphRequestPacket.STREAM_CODEC),
	CONFIGURE_ELEVATOR_CONTACT(ElevatorContactEditPacket.class, ElevatorContactEditPacket.STREAM_CODEC),
	REQUEST_FLOOR_LIST(ElevatorFloorListPacket.RequestFloorList.class, ElevatorFloorListPacket.RequestFloorList.STREAM_CODEC),
	ELEVATOR_SET_FLOOR(ElevatorTargetFloorPacket.class, ElevatorTargetFloorPacket.STREAM_CODEC),
	VALUE_SETTINGS(ValueSettingsPacket.class, ValueSettingsPacket.STREAM_CODEC),
	CLIPBOARD_EDIT(ClipboardEditPacket.class, ClipboardEditPacket.STREAM_CODEC),
	CONTRAPTION_COLLIDER_LOCK_REQUEST(ContraptionColliderLockPacketRequest.class, ContraptionColliderLockPacketRequest.STREAM_CODEC),
	RADIAL_WRENCH_MENU_SUBMIT(RadialWrenchMenuSubmitPacket.class, RadialWrenchMenuSubmitPacket.STREAM_CODEC),
	LOGISTICS_STOCK_REQUEST(LogisticalStockRequestPacket.class, LogisticalStockRequestPacket.STREAM_CODEC),
	LOGISTICS_PACKAGE_REQUEST(PackageOrderRequestPacket.class, PackageOrderRequestPacket.STREAM_CODEC),
	CHAIN_CONVEYOR_CONNECT(ChainConveyorConnectionPacket.class, ChainConveyorConnectionPacket.STREAM_CODEC),
	CHAIN_CONVEYOR_RIDING(ServerboundChainConveyorRidingPacket.class, ServerboundChainConveyorRidingPacket.STREAM_CODEC),
	CHAIN_PACKAGE_INTERACTION(ChainPackageInteractionPacket.class, ChainPackageInteractionPacket.STREAM_CODEC),
	PACKAGE_PORT_CONFIGURATION(PackagePortConfigurationPacket.class, PackagePortConfigurationPacket.STREAM_CODEC),
	TRAIN_MAP_REQUEST(TrainMapSyncRequestPacket.class, TrainMapSyncRequestPacket.STREAM_CODEC),
	CONNECT_FACTORY_PANEL(FactoryPanelConnectionPacket.class, FactoryPanelConnectionPacket.STREAM_CODEC),
	CONFIGURE_FACTORY_PANEL(FactoryPanelConfigurationPacket.class, FactoryPanelConfigurationPacket.STREAM_CODEC),
	CONFIGURE_REDSTONE_REQUESTER(RedstoneRequesterConfigurationPacket.class, RedstoneRequesterConfigurationPacket.STREAM_CODEC),
	CONFIGURE_STOCK_KEEPER_CATEGORIES(StockKeeperCategoryEditPacket.class, StockKeeperCategoryEditPacket.STREAM_CODEC),
	REFUND_STOCK_KEEPER_CATEGORY(StockKeeperCategoryRefundPacket.class, StockKeeperCategoryRefundPacket.STREAM_CODEC),
	LOCK_STOCK_KEEPER(StockKeeperLockPacket.class, StockKeeperLockPacket.STREAM_CODEC),
	STOCK_KEEPER_HIDE_CATEGORY(StockKeeperCategoryHidingPacket.class, StockKeeperCategoryHidingPacket.STREAM_CODEC),

	// Server to Client
	SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket.STREAM_CODEC),
	SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet.STREAM_CODEC),
	BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket.STREAM_CODEC),
	CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket.STREAM_CODEC),
	CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket.STREAM_CODEC),
	CONTRAPTION_BLOCK_CHANGED(ContraptionBlockChangedPacket.class, ContraptionBlockChangedPacket.STREAM_CODEC),
	GLUE_EFFECT(GlueEffectPacket.class, GlueEffectPacket.STREAM_CODEC),
	CONTRAPTION_SEAT_MAPPING(ContraptionSeatMappingPacket.class, ContraptionSeatMappingPacket.STREAM_CODEC),
	LIMBSWING_UPDATE(LimbSwingUpdatePacket.class, LimbSwingUpdatePacket.STREAM_CODEC),
	MINECART_CONTROLLER(MinecartControllerUpdatePacket.class, MinecartControllerUpdatePacket.STREAM_CODEC),
	FLUID_SPLASH(FluidSplashPacket.class, FluidSplashPacket.STREAM_CODEC),
	MOUNTED_STORAGE_SYNC(MountedStorageSyncPacket.class, MountedStorageSyncPacket.STREAM_CODEC),
	GANTRY_UPDATE(GantryContraptionUpdatePacket.class, GantryContraptionUpdatePacket.STREAM_CODEC),
	BLOCK_HIGHLIGHT(HighlightPacket.class, HighlightPacket.STREAM_CODEC),
	TUNNEL_FLAP(TunnelFlapPacket.class, TunnelFlapPacket.STREAM_CODEC),
	FUNNEL_FLAP(FunnelFlapPacket.class, FunnelFlapPacket.STREAM_CODEC),
	POTATO_CANNON(PotatoCannonPacket.class, PotatoCannonPacket.STREAM_CODEC),
	SOUL_PULSE(SoulPulseEffectPacket.class, SoulPulseEffectPacket.STREAM_CODEC),
	PERSISTENT_DATA(ISyncPersistentData.PersistentDataPacket.class, ISyncPersistentData.PersistentDataPacket.STREAM_CODEC),
	SYNC_RAIL_GRAPH(TrackGraphSyncPacket.class, TrackGraphSyncPacket.STREAM_CODEC),
	SYNC_EDGE_GROUP(SignalEdgeGroupPacket.class, SignalEdgeGroupPacket.STREAM_CODEC),
	ADD_TRAIN(AddTrainPacket.class, AddTrainPacket.STREAM_CODEC),
	REMOVE_TRAIN(RemoveTrainPacket.class, RemoveTrainPacket.STREAM_CODEC),
	REMOVE_TE(RemoveBlockEntityPacket.class, RemoveBlockEntityPacket.STREAM_CODEC),
	S_CONFIGURE_TRAIN(TrainEditReturnPacket.class, TrainEditReturnPacket.STREAM_CODEC),
	CONTROLS_ABORT(ControlsStopControllingPacket.class, ControlsStopControllingPacket.STREAM_CODEC),
	S_TRAIN_HUD(TrainHUDUpdatePacket.Clientbound.class, TrainHUDUpdatePacket.Clientbound.STREAM_CODEC),
	S_TRAIN_HONK(HonkPacket.Clientbound.class, HonkPacket.Clientbound.STREAM_CODEC),
	S_TRAIN_PROMPT(TrainPromptPacket.class, TrainPromptPacket.STREAM_CODEC),
	CONTRAPTION_RELOCATION(ContraptionRelocationPacket.class, ContraptionRelocationPacket.STREAM_CODEC),
	TRACK_GRAPH_ROLL_CALL(TrackGraphRollCallPacket.class, TrackGraphRollCallPacket.STREAM_CODEC),
	S_PLACE_ARM(ArmPlacementPacket.ClientBoundRequest.class, ArmPlacementPacket.ClientBoundRequest.STREAM_CODEC),
	S_PLACE_EJECTOR(EjectorPlacementPacket.ClientBoundRequest.class, EjectorPlacementPacket.ClientBoundRequest.STREAM_CODEC),
	S_PLACE_PACKAGE_PORT(PackagePortPlacementPacket.ClientBoundRequest.class, PackagePortPlacementPacket.ClientBoundRequest.STREAM_CODEC),
	UPDATE_ELEVATOR_FLOORS(ElevatorFloorListPacket.class, ElevatorFloorListPacket.STREAM_CODEC),
	CONTRAPTION_ACTOR_TOGGLE(ContraptionDisableActorPacket.class, ContraptionDisableActorPacket.STREAM_CODEC),
	CONTRAPTION_COLLIDER_LOCK(ContraptionColliderLockPacket.class, ContraptionColliderLockPacket.STREAM_CODEC),
	ATTACHED_COMPUTER(AttachedComputerPacket.class, AttachedComputerPacket.STREAM_CODEC),
	SERVER_DEBUG_INFO(ServerDebugInfoPacket.class, ServerDebugInfoPacket.STREAM_CODEC),
	PACKAGE_DESTROYED(PackageDestroyPacket.class, PackageDestroyPacket.STREAM_CODEC),
	LOGISTICS_STOCK_RESPONSE(LogisticalStockResponsePacket.class, LogisticalStockResponsePacket.STREAM_CODEC),
	FACTORY_PANEL_EFFECT(FactoryPanelEffectPacket.class, FactoryPanelEffectPacket.STREAM_CODEC),
	PACKAGER_LINK_EFFECT(WiFiEffectPacket.class, WiFiEffectPacket.STREAM_CODEC),
	REDSTONE_REQUESTER_EFFECT(RedstoneRequesterEffectPacket.class, RedstoneRequesterEffectPacket.STREAM_CODEC),
	KNOCKBACK(KnockbackPacket.class, KnockbackPacket.STREAM_CODEC),
	TRAIN_MAP_SYNC(TrainMapSyncPacket.class, TrainMapSyncPacket.STREAM_CODEC),
	CLIENTBOUND_CHAIN_CONVEYOR(ClientboundChainConveyorRidingPacket.class, ClientboundChainConveyorRidingPacket.STREAM_CODEC),
	SHOP_UPDATE(ShopUpdatePacket.class, ShopUpdatePacket.STREAM_CODEC);
	;

	static {
		ClientboundSimpleActionPacket.addAction("rainbowDebug", () -> SimpleCreateActions::rainbowDebug);
		ClientboundSimpleActionPacket.addAction("overlayReset", () -> SimpleCreateActions::overlayReset);
		ClientboundSimpleActionPacket.addAction("overlayScreen", () -> SimpleCreateActions::overlayScreen);
		ClientboundSimpleActionPacket.addAction("experimentalLighting", () -> SimpleCreateActions::experimentalLighting);
		ClientboundSimpleActionPacket.addAction("fabulousWarning", () -> SimpleCreateActions::fabulousWarning);
		ClientboundSimpleActionPacket.addAction("zoomMultiplier", () -> SimpleCreateActions::zoomMultiplier);
		ClientboundSimpleActionPacket.addAction("camAngleYawTarget", () -> value -> SimpleCreateActions.camAngleTarget(value, true));
		ClientboundSimpleActionPacket.addAction("camAnglePitchTarget", () -> value -> SimpleCreateActions.camAngleTarget(value, false));
		ClientboundSimpleActionPacket.addAction("camAngleFunction", () -> SimpleCreateActions::camAngleFunction);
	}

	private final CatnipPacketRegistry.PacketType<?> type;

	<T extends BasePacketPayload> AllPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		String name = this.name().toLowerCase(Locale.ROOT);
		this.type = new CatnipPacketRegistry.PacketType<>(
				new CustomPacketPayload.Type<>(Create.asResource(name)),
				clazz, codec
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
		return (CustomPacketPayload.Type<T>) this.type.type();
	}

	public static void register() {
		CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(Create.ID, CreateBuildInfo.VERSION);
		for (AllPackets packet : AllPackets.values()) {
			packetRegistry.registerPacket(packet.type);
		}
		packetRegistry.registerAllPackets();
	}
}
