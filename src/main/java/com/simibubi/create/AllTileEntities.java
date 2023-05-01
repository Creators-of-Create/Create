package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours.assignDataBehaviourTE;

import com.simibubi.create.content.contraptions.base.CutoutRotatingInstance;
import com.simibubi.create.content.contraptions.base.HalfShaftInstance;
import com.simibubi.create.content.contraptions.base.HorizontalHalfShaftInstance;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillRenderer;
import com.simibubi.create.content.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.content.contraptions.components.actors.HarvesterRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterTileEntity;
import com.simibubi.create.content.contraptions.components.actors.PSIInstance;
import com.simibubi.create.content.contraptions.components.actors.PortableFluidInterfaceTileEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableItemInterfaceTileEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterInstance;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterRenderer;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.crank.HandCrankInstance;
import com.simibubi.create.content.contraptions.components.crank.HandCrankRenderer;
import com.simibubi.create.content.contraptions.components.crank.HandCrankTileEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerTileEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerInstance;
import com.simibubi.create.content.contraptions.components.deployer.DeployerRenderer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanRenderer;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.fan.FanInstance;
import com.simibubi.create.content.contraptions.components.fan.NozzleTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.FlyWheelInstance;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillStoneCogInstance;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneTileEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerRenderer;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.components.mixer.MixerInstance;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorRenderer;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressRenderer;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.press.PressInstance;
import com.simibubi.create.content.contraptions.components.saw.SawInstance;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.components.steam.PoweredShaftTileEntity;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineInstance;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineRenderer;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineTileEntity;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleRenderer;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.HosePulleyInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.RopePulleyInstance;
import com.simibubi.create.content.contraptions.components.turntable.TurntableTileEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import com.simibubi.create.content.contraptions.fluids.PumpCogInstance;
import com.simibubi.create.content.contraptions.fluids.PumpRenderer;
import com.simibubi.create.content.contraptions.fluids.PumpTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveInstance;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveRenderer;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.TransparentStraightPipeRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinRenderer;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstance;
import com.simibubi.create.content.contraptions.relays.belt.BeltRenderer;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticTileInstance;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticTileRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ClutchTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogInstance;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.content.contraptions.relays.encased.ShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftInstance;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeInstance;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxInstance;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxRenderer;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.create.content.curiosities.armor.CopperBacktankInstance;
import com.simibubi.create.content.curiosities.armor.CopperBacktankRenderer;
import com.simibubi.create.content.curiosities.armor.CopperBacktankTileEntity;
import com.simibubi.create.content.curiosities.bell.BellRenderer;
import com.simibubi.create.content.curiosities.bell.HauntedBellTileEntity;
import com.simibubi.create.content.curiosities.bell.PeculiarBellTileEntity;
import com.simibubi.create.content.curiosities.deco.PlacardRenderer;
import com.simibubi.create.content.curiosities.deco.PlacardTileEntity;
import com.simibubi.create.content.curiosities.deco.SlidingDoorRenderer;
import com.simibubi.create.content.curiosities.deco.SlidingDoorTileEntity;
import com.simibubi.create.content.curiosities.toolbox.ToolBoxInstance;
import com.simibubi.create.content.curiosities.toolbox.ToolboxRenderer;
import com.simibubi.create.content.curiosities.toolbox.ToolboxTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelInstance;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelRenderer;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteRenderer;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.content.logistics.block.chute.SmartChuteRenderer;
import com.simibubi.create.content.logistics.block.chute.SmartChuteTileEntity;
import com.simibubi.create.content.logistics.block.depot.DepotRenderer;
import com.simibubi.create.content.logistics.block.depot.DepotTileEntity;
import com.simibubi.create.content.logistics.block.depot.EjectorInstance;
import com.simibubi.create.content.logistics.block.depot.EjectorRenderer;
import com.simibubi.create.content.logistics.block.depot.EjectorTileEntity;
import com.simibubi.create.content.logistics.block.diodes.BrassDiodeInstance;
import com.simibubi.create.content.logistics.block.diodes.BrassDiodeRenderer;
import com.simibubi.create.content.logistics.block.diodes.PulseExtenderTileEntity;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkRenderer;
import com.simibubi.create.content.logistics.block.display.DisplayLinkTileEntity;
import com.simibubi.create.content.logistics.block.display.source.NixieTubeDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.NixieTubeDisplayTarget;
import com.simibubi.create.content.logistics.block.funnel.FunnelInstance;
import com.simibubi.create.content.logistics.block.funnel.FunnelRenderer;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInstance;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverInstance;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverRenderer;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverTileEntity;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeRenderer;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkTileEntity;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.content.logistics.block.vault.ItemVaultTileEntity;
import com.simibubi.create.content.logistics.item.LecternControllerRenderer;
import com.simibubi.create.content.logistics.item.LecternControllerTileEntity;
import com.simibubi.create.content.logistics.trains.BogeyTileEntityRenderer;
import com.simibubi.create.content.logistics.trains.TrackMaterial;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayRenderer;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserverRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserverTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;
import com.simibubi.create.content.logistics.trains.track.FakeTrackTileEntity;
import com.simibubi.create.content.logistics.trains.track.StandardBogeyTileEntity;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackInstance;
import com.simibubi.create.content.logistics.trains.track.TrackRenderer;
import com.simibubi.create.content.logistics.trains.track.TrackTileEntity;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonInstance;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;

public class AllTileEntities {

	// Schematics
	public static final BlockEntityEntry<SchematicannonTileEntity> SCHEMATICANNON = REGISTRATE
		.tileEntity("schematicannon", SchematicannonTileEntity::new)
		.instance(() -> SchematicannonInstance::new)
		.validBlocks(AllBlocks.SCHEMATICANNON)
		.renderer(() -> SchematicannonRenderer::new)
		.register();

	public static final BlockEntityEntry<SchematicTableTileEntity> SCHEMATIC_TABLE = REGISTRATE
		.tileEntity("schematic_table", SchematicTableTileEntity::new)
		.validBlocks(AllBlocks.SCHEMATIC_TABLE)
		.register();

	// Kinetics
	public static final BlockEntityEntry<BracketedKineticTileEntity> BRACKETED_KINETIC = REGISTRATE
		.tileEntity("simple_kinetic", BracketedKineticTileEntity::new)
		.instance(() -> BracketedKineticTileInstance::new, false)
		.validBlocks(AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL)
		.renderer(() -> BracketedKineticTileRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeMotorTileEntity> MOTOR = REGISTRATE
		.tileEntity("motor", CreativeMotorTileEntity::new)
		.instance(() -> HalfShaftInstance::new, false)
		.validBlocks(AllBlocks.CREATIVE_MOTOR)
		.renderer(() -> CreativeMotorRenderer::new)
		.register();

	public static final BlockEntityEntry<GearboxTileEntity> GEARBOX = REGISTRATE
		.tileEntity("gearbox", GearboxTileEntity::new)
		.instance(() -> GearboxInstance::new, false)
		.validBlocks(AllBlocks.GEARBOX)
		.renderer(() -> GearboxRenderer::new)
		.register();

	public static final BlockEntityEntry<KineticTileEntity> ENCASED_SHAFT = REGISTRATE
		.tileEntity("encased_shaft", KineticTileEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_SHAFT, AllBlocks.BRASS_ENCASED_SHAFT, AllBlocks.ENCASED_CHAIN_DRIVE,
			AllBlocks.METAL_GIRDER_ENCASED_SHAFT)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<SimpleKineticTileEntity> ENCASED_COGWHEEL = REGISTRATE
		.tileEntity("encased_cogwheel", SimpleKineticTileEntity::new)
		.instance(() -> EncasedCogInstance::small, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_COGWHEEL, AllBlocks.BRASS_ENCASED_COGWHEEL)
		.renderer(() -> EncasedCogRenderer::small)
		.register();

	public static final BlockEntityEntry<SimpleKineticTileEntity> ENCASED_LARGE_COGWHEEL = REGISTRATE
		.tileEntity("encased_large_cogwheel", SimpleKineticTileEntity::new)
		.instance(() -> EncasedCogInstance::large, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL, AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL)
		.renderer(() -> EncasedCogRenderer::large)
		.register();

	public static final BlockEntityEntry<AdjustablePulleyTileEntity> ADJUSTABLE_PULLEY = REGISTRATE
		.tileEntity("adjustable_pulley", AdjustablePulleyTileEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<EncasedFanTileEntity> ENCASED_FAN = REGISTRATE
		.tileEntity("encased_fan", EncasedFanTileEntity::new)
		.instance(() -> FanInstance::new, false)
		.validBlocks(AllBlocks.ENCASED_FAN)
		.renderer(() -> EncasedFanRenderer::new)
		.register();

	public static final BlockEntityEntry<NozzleTileEntity> NOZZLE = REGISTRATE
		.tileEntity("nozzle", NozzleTileEntity::new)
		.validBlocks(AllBlocks.NOZZLE)
		// .renderer(() -> renderer)
		.register();

	public static final BlockEntityEntry<ClutchTileEntity> CLUTCH = REGISTRATE
		.tileEntity("clutch", ClutchTileEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.CLUTCH)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<GearshiftTileEntity> GEARSHIFT = REGISTRATE
		.tileEntity("gearshift", GearshiftTileEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<TurntableTileEntity> TURNTABLE = REGISTRATE
		.tileEntity("turntable", TurntableTileEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.TURNTABLE)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<HandCrankTileEntity> HAND_CRANK = REGISTRATE
		.tileEntity("hand_crank", HandCrankTileEntity::new)
		.instance(() -> HandCrankInstance::new)
		.validBlocks(AllBlocks.HAND_CRANK, AllBlocks.COPPER_VALVE_HANDLE)
		.validBlocks(AllBlocks.DYED_VALVE_HANDLES.toArray())
		.renderer(() -> HandCrankRenderer::new)
		.register();

	public static final BlockEntityEntry<CuckooClockTileEntity> CUCKOO_CLOCK = REGISTRATE
		.tileEntity("cuckoo_clock", CuckooClockTileEntity::new)
		.instance(() -> HorizontalHalfShaftInstance::new)
		.validBlocks(AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK)
		.renderer(() -> CuckooClockRenderer::new)
		.register();

	public static final BlockEntityEntry<GantryShaftTileEntity> GANTRY_SHAFT = REGISTRATE
		.tileEntity("gantry_shaft", GantryShaftTileEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.GANTRY_SHAFT)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<GantryCarriageTileEntity> GANTRY_PINION = REGISTRATE
		.tileEntity("gantry_pinion", GantryCarriageTileEntity::new)
		.instance(() -> GantryCarriageInstance::new)
		.validBlocks(AllBlocks.GANTRY_CARRIAGE)
		.renderer(() -> GantryCarriageRenderer::new)
		.register();

	public static final BlockEntityEntry<PumpTileEntity> MECHANICAL_PUMP = REGISTRATE
		.tileEntity("mechanical_pump", PumpTileEntity::new)
		.instance(() -> PumpCogInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PUMP)
		.renderer(() -> PumpRenderer::new)
		.register();

	public static final BlockEntityEntry<SmartFluidPipeTileEntity> SMART_FLUID_PIPE = REGISTRATE
		.tileEntity("smart_fluid_pipe", SmartFluidPipeTileEntity::new)
		.validBlocks(AllBlocks.SMART_FLUID_PIPE)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidPipeTileEntity> FLUID_PIPE = REGISTRATE
		.tileEntity("fluid_pipe", FluidPipeTileEntity::new)
		.validBlocks(AllBlocks.FLUID_PIPE)
		.register();

	public static final BlockEntityEntry<FluidPipeTileEntity> ENCASED_FLUID_PIPE = REGISTRATE
		.tileEntity("encased_fluid_pipe", FluidPipeTileEntity::new)
		.validBlocks(AllBlocks.ENCASED_FLUID_PIPE)
		.register();

	public static final BlockEntityEntry<StraightPipeTileEntity> GLASS_FLUID_PIPE = REGISTRATE
		.tileEntity("glass_fluid_pipe", StraightPipeTileEntity::new)
		.validBlocks(AllBlocks.GLASS_FLUID_PIPE)
		.renderer(() -> TransparentStraightPipeRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidValveTileEntity> FLUID_VALVE = REGISTRATE
		.tileEntity("fluid_valve", FluidValveTileEntity::new)
		.instance(() -> FluidValveInstance::new)
		.validBlocks(AllBlocks.FLUID_VALVE)
		.renderer(() -> FluidValveRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidTankTileEntity> FLUID_TANK = REGISTRATE
		.tileEntity("fluid_tank", FluidTankTileEntity::new)
		.validBlocks(AllBlocks.FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeFluidTankTileEntity> CREATIVE_FLUID_TANK = REGISTRATE
		.tileEntity("creative_fluid_tank", CreativeFluidTankTileEntity::new)
		.validBlocks(AllBlocks.CREATIVE_FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final BlockEntityEntry<HosePulleyTileEntity> HOSE_PULLEY = REGISTRATE
		.tileEntity("hose_pulley", HosePulleyTileEntity::new)
		.instance(() -> HosePulleyInstance::new)
		.validBlocks(AllBlocks.HOSE_PULLEY)
		.renderer(() -> HosePulleyRenderer::new)
		.register();

	public static final BlockEntityEntry<SpoutTileEntity> SPOUT = REGISTRATE
		.tileEntity("spout", SpoutTileEntity::new)
		.validBlocks(AllBlocks.SPOUT)
		.renderer(() -> SpoutRenderer::new)
		.register();

	public static final BlockEntityEntry<ItemDrainTileEntity> ITEM_DRAIN = REGISTRATE
		.tileEntity("item_drain", ItemDrainTileEntity::new)
		.validBlocks(AllBlocks.ITEM_DRAIN)
		.renderer(() -> ItemDrainRenderer::new)
		.register();

	public static final BlockEntityEntry<BeltTileEntity> BELT = REGISTRATE
		.tileEntity("belt", BeltTileEntity::new)
		.instance(() -> BeltInstance::new, BeltTileEntity::shouldRenderNormally)
		.validBlocks(AllBlocks.BELT)
		.renderer(() -> BeltRenderer::new)
		.register();

	public static final BlockEntityEntry<ChuteTileEntity> CHUTE = REGISTRATE
		.tileEntity("chute", ChuteTileEntity::new)
		.validBlocks(AllBlocks.CHUTE)
		.renderer(() -> ChuteRenderer::new)
		.register();

	public static final BlockEntityEntry<SmartChuteTileEntity> SMART_CHUTE = REGISTRATE
		.tileEntity("smart_chute", SmartChuteTileEntity::new)
		.validBlocks(AllBlocks.SMART_CHUTE)
		.renderer(() -> SmartChuteRenderer::new)
		.register();

	public static final BlockEntityEntry<BeltTunnelTileEntity> ANDESITE_TUNNEL = REGISTRATE
		.tileEntity("andesite_tunnel", BeltTunnelTileEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.ANDESITE_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<BrassTunnelTileEntity> BRASS_TUNNEL = REGISTRATE
		.tileEntity("brass_tunnel", BrassTunnelTileEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<ArmTileEntity> MECHANICAL_ARM = REGISTRATE
		.tileEntity("mechanical_arm", ArmTileEntity::new)
		.instance(() -> ArmInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_ARM)
		.renderer(() -> ArmRenderer::new)
		.register();

	public static final BlockEntityEntry<ItemVaultTileEntity> ITEM_VAULT = REGISTRATE
		.tileEntity("item_vault", ItemVaultTileEntity::new)
		.validBlocks(AllBlocks.ITEM_VAULT)
		.register();

	public static final BlockEntityEntry<MechanicalPistonTileEntity> MECHANICAL_PISTON = REGISTRATE
		.tileEntity("mechanical_piston", MechanicalPistonTileEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
		.renderer(() -> MechanicalPistonRenderer::new)
		.register();

	public static final BlockEntityEntry<WindmillBearingTileEntity> WINDMILL_BEARING = REGISTRATE
		.tileEntity("windmill_bearing", WindmillBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.WINDMILL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalBearingTileEntity> MECHANICAL_BEARING = REGISTRATE
		.tileEntity("mechanical_bearing", MechanicalBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<ClockworkBearingTileEntity> CLOCKWORK_BEARING = REGISTRATE
		.tileEntity("clockwork_bearing", ClockworkBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.CLOCKWORK_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<PulleyTileEntity> ROPE_PULLEY = REGISTRATE
		.tileEntity("rope_pulley", PulleyTileEntity::new)
		.instance(() -> RopePulleyInstance::new, false)
		.validBlocks(AllBlocks.ROPE_PULLEY)
		.renderer(() -> PulleyRenderer::new)
		.register();

	public static final BlockEntityEntry<ChassisTileEntity> CHASSIS = REGISTRATE
		.tileEntity("chassis", ChassisTileEntity::new)
		.validBlocks(AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
		// .renderer(() -> renderer)
		.register();

	public static final BlockEntityEntry<StickerTileEntity> STICKER = REGISTRATE
		.tileEntity("sticker", StickerTileEntity::new)
		.instance(() -> StickerInstance::new, false)
		.validBlocks(AllBlocks.STICKER)
		.renderer(() -> StickerRenderer::new)
		.register();

	public static final BlockEntityEntry<DrillTileEntity> DRILL = REGISTRATE
		.tileEntity("drill", DrillTileEntity::new)
		.instance(() -> DrillInstance::new, false)
		.validBlocks(AllBlocks.MECHANICAL_DRILL)
		.renderer(() -> DrillRenderer::new)
		.register();

	public static final BlockEntityEntry<SawTileEntity> SAW = REGISTRATE
		.tileEntity("saw", SawTileEntity::new)
		.instance(() -> SawInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_SAW)
		.renderer(() -> SawRenderer::new)
		.register();

	public static final BlockEntityEntry<HarvesterTileEntity> HARVESTER = REGISTRATE
		.tileEntity("harvester", HarvesterTileEntity::new)
		.validBlocks(AllBlocks.MECHANICAL_HARVESTER)
		.renderer(() -> HarvesterRenderer::new)
		.register();

	public static final BlockEntityEntry<PortableItemInterfaceTileEntity> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE
			.tileEntity("portable_storage_interface", PortableItemInterfaceTileEntity::new)
			.instance(() -> PSIInstance::new)
			.validBlocks(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.renderer(() -> PortableStorageInterfaceRenderer::new)
			.register();

	public static final BlockEntityEntry<PortableFluidInterfaceTileEntity> PORTABLE_FLUID_INTERFACE =
		REGISTRATE
			.tileEntity("portable_fluid_interface", PortableFluidInterfaceTileEntity::new)
			.instance(() -> PSIInstance::new)
			.validBlocks(AllBlocks.PORTABLE_FLUID_INTERFACE)
			.renderer(() -> PortableStorageInterfaceRenderer::new)
			.register();

	public static final BlockEntityEntry<SteamEngineTileEntity> STEAM_ENGINE = REGISTRATE
		.tileEntity("steam_engine", SteamEngineTileEntity::new)
		.instance(() -> SteamEngineInstance::new, false)
		.validBlocks(AllBlocks.STEAM_ENGINE)
		.renderer(() -> SteamEngineRenderer::new)
		.register();

	public static final BlockEntityEntry<WhistleTileEntity> STEAM_WHISTLE = REGISTRATE
		.tileEntity("steam_whistle", WhistleTileEntity::new)
		.validBlocks(AllBlocks.STEAM_WHISTLE)
		.renderer(() -> WhistleRenderer::new)
		.register();

	public static final BlockEntityEntry<PoweredShaftTileEntity> POWERED_SHAFT = REGISTRATE
		.tileEntity("powered_shaft", PoweredShaftTileEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.POWERED_SHAFT)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<FlywheelTileEntity> FLYWHEEL = REGISTRATE
		.tileEntity("flywheel", FlywheelTileEntity::new)
		.instance(() -> FlyWheelInstance::new, false)
		.validBlocks(AllBlocks.FLYWHEEL)
		.renderer(() -> FlywheelRenderer::new)
		.register();

	public static final BlockEntityEntry<MillstoneTileEntity> MILLSTONE = REGISTRATE
		.tileEntity("millstone", MillstoneTileEntity::new)
		.instance(() -> MillStoneCogInstance::new, false)
		.validBlocks(AllBlocks.MILLSTONE)
		.renderer(() -> MillstoneRenderer::new)
		.register();

	public static final BlockEntityEntry<CrushingWheelTileEntity> CRUSHING_WHEEL = REGISTRATE
		.tileEntity("crushing_wheel", CrushingWheelTileEntity::new)
		.instance(() -> CutoutRotatingInstance::new, false)
		.validBlocks(AllBlocks.CRUSHING_WHEEL)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<CrushingWheelControllerTileEntity> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE
			.tileEntity("crushing_wheel_controller", CrushingWheelControllerTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL_CONTROLLER)
			// .renderer(() -> renderer)
			.register();

	public static final BlockEntityEntry<WaterWheelTileEntity> WATER_WHEEL = REGISTRATE
		.tileEntity("water_wheel", WaterWheelTileEntity::new)
		.instance(() -> CutoutRotatingInstance::new, false)
		.validBlocks(AllBlocks.WATER_WHEEL)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalPressTileEntity> MECHANICAL_PRESS = REGISTRATE
		.tileEntity("mechanical_press", MechanicalPressTileEntity::new)
		.instance(() -> PressInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PRESS)
		.renderer(() -> MechanicalPressRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalMixerTileEntity> MECHANICAL_MIXER = REGISTRATE
		.tileEntity("mechanical_mixer", MechanicalMixerTileEntity::new)
		.instance(() -> MixerInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_MIXER)
		.renderer(() -> MechanicalMixerRenderer::new)
		.register();

	public static final BlockEntityEntry<DeployerTileEntity> DEPLOYER = REGISTRATE
		.tileEntity("deployer", DeployerTileEntity::new)
		.instance(() -> DeployerInstance::new)
		.validBlocks(AllBlocks.DEPLOYER)
		.renderer(() -> DeployerRenderer::new)
		.register();

	public static final BlockEntityEntry<BasinTileEntity> BASIN = REGISTRATE
		.tileEntity("basin", BasinTileEntity::new)
		.validBlocks(AllBlocks.BASIN)
		.renderer(() -> BasinRenderer::new)
		.register();

	public static final BlockEntityEntry<BlazeBurnerTileEntity> HEATER = REGISTRATE
		.tileEntity("blaze_heater", BlazeBurnerTileEntity::new)
		.validBlocks(AllBlocks.BLAZE_BURNER)
		.renderer(() -> BlazeBurnerRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalCrafterTileEntity> MECHANICAL_CRAFTER = REGISTRATE
		.tileEntity("mechanical_crafter", MechanicalCrafterTileEntity::new)
		.instance(() -> MechanicalCrafterInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_CRAFTER)
		.renderer(() -> MechanicalCrafterRenderer::new)
		.register();

	public static final BlockEntityEntry<SequencedGearshiftTileEntity> SEQUENCED_GEARSHIFT = REGISTRATE
		.tileEntity("sequenced_gearshift", SequencedGearshiftTileEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.SEQUENCED_GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<SpeedControllerTileEntity> ROTATION_SPEED_CONTROLLER = REGISTRATE
		.tileEntity("rotation_speed_controller", SpeedControllerTileEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.ROTATION_SPEED_CONTROLLER)
		.renderer(() -> SpeedControllerRenderer::new)
		.register();

	public static final BlockEntityEntry<SpeedGaugeTileEntity> SPEEDOMETER = REGISTRATE
		.tileEntity("speedometer", SpeedGaugeTileEntity::new)
		.instance(() -> GaugeInstance.Speed::new)
		.validBlocks(AllBlocks.SPEEDOMETER)
		.renderer(() -> GaugeRenderer::speed)
		.register();

	public static final BlockEntityEntry<StressGaugeTileEntity> STRESSOMETER = REGISTRATE
		.tileEntity("stressometer", StressGaugeTileEntity::new)
		.instance(() -> GaugeInstance.Stress::new)
		.validBlocks(AllBlocks.STRESSOMETER)
		.renderer(() -> GaugeRenderer::stress)
		.register();

	public static final BlockEntityEntry<AnalogLeverTileEntity> ANALOG_LEVER = REGISTRATE
		.tileEntity("analog_lever", AnalogLeverTileEntity::new)
		.instance(() -> AnalogLeverInstance::new, false)
		.validBlocks(AllBlocks.ANALOG_LEVER)
		.renderer(() -> AnalogLeverRenderer::new)
		.register();

	public static final BlockEntityEntry<PlacardTileEntity> PLACARD = REGISTRATE
		.tileEntity("placard", PlacardTileEntity::new)
		.validBlocks(AllBlocks.PLACARD)
		.renderer(() -> PlacardRenderer::new)
		.register();

	public static final BlockEntityEntry<CartAssemblerTileEntity> CART_ASSEMBLER = REGISTRATE
		.tileEntity("cart_assembler", CartAssemblerTileEntity::new)
		.validBlocks(AllBlocks.CART_ASSEMBLER)
		// .renderer(() -> renderer)
		.register();

	// Logistics
	public static final BlockEntityEntry<RedstoneLinkTileEntity> REDSTONE_LINK = REGISTRATE
		.tileEntity("redstone_link", RedstoneLinkTileEntity::new)
		.validBlocks(AllBlocks.REDSTONE_LINK)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<NixieTubeTileEntity> NIXIE_TUBE = REGISTRATE
		.tileEntity("nixie_tube", NixieTubeTileEntity::new)
		.validBlocks(AllBlocks.ORANGE_NIXIE_TUBE)
		.validBlocks(AllBlocks.NIXIE_TUBES.toArray())
		.renderer(() -> NixieTubeRenderer::new)
		.onRegister(assignDataBehaviourTE(new NixieTubeDisplayTarget()))
		.onRegister(assignDataBehaviourTE(new NixieTubeDisplaySource()))
		.register();

	public static final BlockEntityEntry<DisplayLinkTileEntity> DISPLAY_LINK = REGISTRATE
		.tileEntity("display_link", DisplayLinkTileEntity::new)
		.validBlocks(AllBlocks.DISPLAY_LINK)
		.renderer(() -> DisplayLinkRenderer::new)
		.register();

	public static final BlockEntityEntry<StockpileSwitchTileEntity> STOCKPILE_SWITCH = REGISTRATE
		.tileEntity("stockpile_switch", StockpileSwitchTileEntity::new)
		.validBlocks(AllBlocks.STOCKPILE_SWITCH)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeCrateTileEntity> CREATIVE_CRATE = REGISTRATE
		.tileEntity("creative_crate", CreativeCrateTileEntity::new)
		.validBlocks(AllBlocks.CREATIVE_CRATE)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<DepotTileEntity> DEPOT = REGISTRATE
		.tileEntity("depot", DepotTileEntity::new)
		.validBlocks(AllBlocks.DEPOT)
		.renderer(() -> DepotRenderer::new)
		.register();

	public static final BlockEntityEntry<EjectorTileEntity> WEIGHTED_EJECTOR = REGISTRATE
		.tileEntity("weighted_ejector", EjectorTileEntity::new)
		.instance(() -> EjectorInstance::new)
		.validBlocks(AllBlocks.WEIGHTED_EJECTOR)
		.renderer(() -> EjectorRenderer::new)
		.register();

	public static final BlockEntityEntry<FunnelTileEntity> FUNNEL = REGISTRATE
		.tileEntity("funnel", FunnelTileEntity::new)
		.instance(() -> FunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.ANDESITE_FUNNEL,
			AllBlocks.ANDESITE_BELT_FUNNEL)
		.renderer(() -> FunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<ContentObserverTileEntity> CONTENT_OBSERVER = REGISTRATE
		.tileEntity("content_observer", ContentObserverTileEntity::new)
		.validBlocks(AllBlocks.CONTENT_OBSERVER)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<PulseExtenderTileEntity> PULSE_EXTENDER = REGISTRATE
		.tileEntity("pulse_extender", PulseExtenderTileEntity::new)
		.instance(() -> BrassDiodeInstance::new, false)
		.validBlocks(AllBlocks.PULSE_EXTENDER)
		.renderer(() -> BrassDiodeRenderer::new)
		.register();

	public static final BlockEntityEntry<PulseRepeaterTileEntity> PULSE_REPEATER = REGISTRATE
		.tileEntity("pulse_repeater", PulseRepeaterTileEntity::new)
		.instance(() -> BrassDiodeInstance::new, false)
		.validBlocks(AllBlocks.PULSE_REPEATER)
		.renderer(() -> BrassDiodeRenderer::new)
		.register();

	public static final BlockEntityEntry<LecternControllerTileEntity> LECTERN_CONTROLLER = REGISTRATE
		.tileEntity("lectern_controller", LecternControllerTileEntity::new)
		.validBlocks(AllBlocks.LECTERN_CONTROLLER)
		.renderer(() -> LecternControllerRenderer::new)
		.register();

	// Curiosities
	public static final BlockEntityEntry<CopperBacktankTileEntity> COPPER_BACKTANK = REGISTRATE
		.tileEntity("copper_backtank", CopperBacktankTileEntity::new)
		.instance(() -> CopperBacktankInstance::new)
		.validBlocks(AllBlocks.COPPER_BACKTANK)
		.renderer(() -> CopperBacktankRenderer::new)
		.register();

	public static final BlockEntityEntry<PeculiarBellTileEntity> PECULIAR_BELL = REGISTRATE
		.tileEntity("peculiar_bell", PeculiarBellTileEntity::new)
		.validBlocks(AllBlocks.PECULIAR_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final BlockEntityEntry<HauntedBellTileEntity> HAUNTED_BELL = REGISTRATE
		.tileEntity("cursed_bell", HauntedBellTileEntity::new)
		.validBlocks(AllBlocks.HAUNTED_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final BlockEntityEntry<ToolboxTileEntity> TOOLBOX = REGISTRATE
		.tileEntity("toolbox", ToolboxTileEntity::new)
		.instance(() -> ToolBoxInstance::new, false)
		.validBlocks(AllBlocks.TOOLBOXES.toArray())
		.renderer(() -> ToolboxRenderer::new)
		.register();

	public static final BlockEntityEntry<TrackTileEntity> TRACK = REGISTRATE
		.tileEntity("track", TrackTileEntity::new)
		.instance(() -> TrackInstance::new)
		.renderer(() -> TrackRenderer::new)
		.validBlocks((NonNullSupplier<? extends TrackBlock>[]) TrackMaterial.allBlocks().toArray(new NonNullSupplier[0]))
		.register();

	public static final BlockEntityEntry<FakeTrackTileEntity> FAKE_TRACK = REGISTRATE
		.tileEntity("fake_track", FakeTrackTileEntity::new)
		.validBlocks(AllBlocks.FAKE_TRACK)
		.register();

	public static final BlockEntityEntry<StandardBogeyTileEntity> BOGEY = REGISTRATE
		.tileEntity("bogey", StandardBogeyTileEntity::new)
		.renderer(() -> BogeyTileEntityRenderer::new)
		.validBlocks(AllBlocks.SMALL_BOGEY, AllBlocks.LARGE_BOGEY)
		.register();

	public static final BlockEntityEntry<StationTileEntity> TRACK_STATION = REGISTRATE
		.tileEntity("track_station", StationTileEntity::new)
		.renderer(() -> StationRenderer::new)
		.validBlocks(AllBlocks.TRACK_STATION)
		.register();

	public static final BlockEntityEntry<SlidingDoorTileEntity> SLIDING_DOOR = REGISTRATE
		.tileEntity("sliding_door", SlidingDoorTileEntity::new)
		.renderer(() -> SlidingDoorRenderer::new)
		.validBlocks(AllBlocks.TRAIN_DOOR, AllBlocks.FRAMED_GLASS_DOOR)
		.register();

	public static final BlockEntityEntry<FlapDisplayTileEntity> FLAP_DISPLAY = REGISTRATE
		.tileEntity("flap_display", FlapDisplayTileEntity::new)
		.instance(() -> MechanicalCrafterInstance::new)
		.renderer(() -> FlapDisplayRenderer::new)
		.validBlocks(AllBlocks.DISPLAY_BOARD)
		.register();

	public static final BlockEntityEntry<SignalTileEntity> TRACK_SIGNAL = REGISTRATE
		.tileEntity("track_signal", SignalTileEntity::new)
		.renderer(() -> SignalRenderer::new)
		.validBlocks(AllBlocks.TRACK_SIGNAL)
		.register();

	public static final BlockEntityEntry<TrackObserverTileEntity> TRACK_OBSERVER = REGISTRATE
		.tileEntity("track_observer", TrackObserverTileEntity::new)
		.renderer(() -> TrackObserverRenderer::new)
		.validBlocks(AllBlocks.TRACK_OBSERVER)
		.register();

	public static void register() {}
}
