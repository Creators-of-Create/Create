package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours.assignDataBehaviourBE;

import com.simibubi.create.content.contraptions.base.CutoutRotatingInstance;
import com.simibubi.create.content.contraptions.base.HalfShaftInstance;
import com.simibubi.create.content.contraptions.base.HorizontalHalfShaftInstance;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.DrillInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.HarvesterRenderer;
import com.simibubi.create.content.contraptions.components.actors.PSIInstance;
import com.simibubi.create.content.contraptions.components.actors.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceRenderer;
import com.simibubi.create.content.contraptions.components.actors.RollerBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.RollerRenderer;
import com.simibubi.create.content.contraptions.components.actors.controls.ContraptionControlsBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.controls.ContraptionControlsRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockBlockEntity;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockRenderer;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterRenderer;
import com.simibubi.create.content.contraptions.components.crafter.ShaftlessCogwheelInstance;
import com.simibubi.create.content.contraptions.components.crank.HandCrankBlockEntity;
import com.simibubi.create.content.contraptions.components.crank.HandCrankInstance;
import com.simibubi.create.content.contraptions.components.crank.HandCrankRenderer;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlockEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlockEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerInstance;
import com.simibubi.create.content.contraptions.components.deployer.DeployerRenderer;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanRenderer;
import com.simibubi.create.content.contraptions.components.fan.FanInstance;
import com.simibubi.create.content.contraptions.components.fan.NozzleBlockEntity;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlockEntity;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelInstance;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelRenderer;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneCogInstance;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerRenderer;
import com.simibubi.create.content.contraptions.components.mixer.MixerInstance;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorRenderer;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressRenderer;
import com.simibubi.create.content.contraptions.components.press.PressInstance;
import com.simibubi.create.content.contraptions.components.saw.SawBlockEntity;
import com.simibubi.create.content.contraptions.components.saw.SawInstance;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.steam.PoweredShaftBlockEntity;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineBlockEntity;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineInstance;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineRenderer;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleBlockEntity;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorPulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.HosePulleyInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.RopePulleyInstance;
import com.simibubi.create.content.contraptions.components.turntable.TurntableBlockEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.LargeWaterWheelBlockEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelInstance;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelRenderer;
import com.simibubi.create.content.contraptions.fluids.PumpBlockEntity;
import com.simibubi.create.content.contraptions.fluids.PumpCogInstance;
import com.simibubi.create.content.contraptions.fluids.PumpRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyBlockEntity;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainBlockEntity;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlockEntity;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutRenderer;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveBlockEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveInstance;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveRenderer;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.TransparentStraightPipeRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankRenderer;
import com.simibubi.create.content.contraptions.processing.BasinBlockEntity;
import com.simibubi.create.content.contraptions.processing.BasinRenderer;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlockEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlockEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstance;
import com.simibubi.create.content.contraptions.relays.belt.BeltRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticBlockEntity;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticBlockEntityInstance;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.ClutchBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogInstance;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.content.contraptions.relays.encased.ShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftInstance;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeInstance;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlockEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxInstance;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxRenderer;
import com.simibubi.create.content.contraptions.relays.gearbox.GearshiftBlockEntity;
import com.simibubi.create.content.curiosities.armor.BacktankBlockEntity;
import com.simibubi.create.content.curiosities.armor.BacktankInstance;
import com.simibubi.create.content.curiosities.armor.BacktankRenderer;
import com.simibubi.create.content.curiosities.bell.BellRenderer;
import com.simibubi.create.content.curiosities.bell.HauntedBellBlockEntity;
import com.simibubi.create.content.curiosities.bell.PeculiarBellBlockEntity;
import com.simibubi.create.content.curiosities.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.curiosities.deco.PlacardBlockEntity;
import com.simibubi.create.content.curiosities.deco.PlacardRenderer;
import com.simibubi.create.content.curiosities.deco.SlidingDoorBlockEntity;
import com.simibubi.create.content.curiosities.deco.SlidingDoorRenderer;
import com.simibubi.create.content.curiosities.frames.CopycatBlockEntity;
import com.simibubi.create.content.curiosities.toolbox.ToolBoxInstance;
import com.simibubi.create.content.curiosities.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.curiosities.toolbox.ToolboxRenderer;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelInstance;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelRenderer;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteRenderer;
import com.simibubi.create.content.logistics.block.chute.SmartChuteBlockEntity;
import com.simibubi.create.content.logistics.block.chute.SmartChuteRenderer;
import com.simibubi.create.content.logistics.block.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.block.depot.DepotRenderer;
import com.simibubi.create.content.logistics.block.depot.EjectorBlockEntity;
import com.simibubi.create.content.logistics.block.depot.EjectorInstance;
import com.simibubi.create.content.logistics.block.depot.EjectorRenderer;
import com.simibubi.create.content.logistics.block.diodes.BrassDiodeInstance;
import com.simibubi.create.content.logistics.block.diodes.BrassDiodeRenderer;
import com.simibubi.create.content.logistics.block.diodes.PulseExtenderBlockEntity;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkRenderer;
import com.simibubi.create.content.logistics.block.display.source.NixieTubeDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.NixieTubeDisplayTarget;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlockEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelInstance;
import com.simibubi.create.content.logistics.block.funnel.FunnelRenderer;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInstance;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmRenderer;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverInstance;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverRenderer;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeRenderer;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.SmartObserverBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.logistics.block.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.logistics.item.LecternControllerBlockEntity;
import com.simibubi.create.content.logistics.item.LecternControllerRenderer;
import com.simibubi.create.content.logistics.trains.BogeyBlockEntityRenderer;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserverBlockEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserverRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBlockEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalRenderer;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationBlockEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationRenderer;
import com.simibubi.create.content.logistics.trains.track.FakeTrackBlockEntity;
import com.simibubi.create.content.logistics.trains.track.StandardBogeyBlockEntity;
import com.simibubi.create.content.logistics.trains.track.TrackBlockEntity;
import com.simibubi.create.content.logistics.trains.track.TrackInstance;
import com.simibubi.create.content.logistics.trains.track.TrackRenderer;
import com.simibubi.create.content.schematics.block.SchematicTableBlockEntity;
import com.simibubi.create.content.schematics.block.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.block.SchematicannonInstance;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class AllBlockEntityTypes {

	// Schematics
	public static final BlockEntityEntry<SchematicannonBlockEntity> SCHEMATICANNON = REGISTRATE
		.blockEntity("schematicannon", SchematicannonBlockEntity::new)
		.instance(() -> SchematicannonInstance::new)
		.validBlocks(AllBlocks.SCHEMATICANNON)
		.renderer(() -> SchematicannonRenderer::new)
		.register();

	public static final BlockEntityEntry<SchematicTableBlockEntity> SCHEMATIC_TABLE = REGISTRATE
		.blockEntity("schematic_table", SchematicTableBlockEntity::new)
		.validBlocks(AllBlocks.SCHEMATIC_TABLE)
		.register();

	// Kinetics
	public static final BlockEntityEntry<BracketedKineticBlockEntity> BRACKETED_KINETIC = REGISTRATE
		.blockEntity("simple_kinetic", BracketedKineticBlockEntity::new)
		.instance(() -> BracketedKineticBlockEntityInstance::new, false)
		.validBlocks(AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL)
		.renderer(() -> BracketedKineticBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeMotorBlockEntity> MOTOR = REGISTRATE
		.blockEntity("motor", CreativeMotorBlockEntity::new)
		.instance(() -> HalfShaftInstance::new, false)
		.validBlocks(AllBlocks.CREATIVE_MOTOR)
		.renderer(() -> CreativeMotorRenderer::new)
		.register();

	public static final BlockEntityEntry<GearboxBlockEntity> GEARBOX = REGISTRATE
		.blockEntity("gearbox", GearboxBlockEntity::new)
		.instance(() -> GearboxInstance::new, false)
		.validBlocks(AllBlocks.GEARBOX)
		.renderer(() -> GearboxRenderer::new)
		.register();

	public static final BlockEntityEntry<KineticBlockEntity> ENCASED_SHAFT = REGISTRATE
		.blockEntity("encased_shaft", KineticBlockEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_SHAFT, AllBlocks.BRASS_ENCASED_SHAFT, AllBlocks.ENCASED_CHAIN_DRIVE,
			AllBlocks.METAL_GIRDER_ENCASED_SHAFT)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_COGWHEEL = REGISTRATE
		.blockEntity("encased_cogwheel", SimpleKineticBlockEntity::new)
		.instance(() -> EncasedCogInstance::small, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_COGWHEEL, AllBlocks.BRASS_ENCASED_COGWHEEL)
		.renderer(() -> EncasedCogRenderer::small)
		.register();

	public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_LARGE_COGWHEEL = REGISTRATE
		.blockEntity("encased_large_cogwheel", SimpleKineticBlockEntity::new)
		.instance(() -> EncasedCogInstance::large, false)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL, AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL)
		.renderer(() -> EncasedCogRenderer::large)
		.register();

	public static final BlockEntityEntry<AdjustablePulleyBlockEntity> ADJUSTABLE_PULLEY = REGISTRATE
		.blockEntity("adjustable_pulley", AdjustablePulleyBlockEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<EncasedFanBlockEntity> ENCASED_FAN = REGISTRATE
		.blockEntity("encased_fan", EncasedFanBlockEntity::new)
		.instance(() -> FanInstance::new, false)
		.validBlocks(AllBlocks.ENCASED_FAN)
		.renderer(() -> EncasedFanRenderer::new)
		.register();

	public static final BlockEntityEntry<NozzleBlockEntity> NOZZLE = REGISTRATE
		.blockEntity("nozzle", NozzleBlockEntity::new)
		.validBlocks(AllBlocks.NOZZLE)
		// .renderer(() -> renderer)
		.register();

	public static final BlockEntityEntry<ClutchBlockEntity> CLUTCH = REGISTRATE
		.blockEntity("clutch", ClutchBlockEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.CLUTCH)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<GearshiftBlockEntity> GEARSHIFT = REGISTRATE
		.blockEntity("gearshift", GearshiftBlockEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<TurntableBlockEntity> TURNTABLE = REGISTRATE
		.blockEntity("turntable", TurntableBlockEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.TURNTABLE)
		.renderer(() -> KineticBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<HandCrankBlockEntity> HAND_CRANK = REGISTRATE
		.blockEntity("hand_crank", HandCrankBlockEntity::new)
		.instance(() -> HandCrankInstance::new)
		.validBlocks(AllBlocks.HAND_CRANK)
		.renderer(() -> HandCrankRenderer::new)
		.register();
	
	public static final BlockEntityEntry<ValveHandleBlockEntity> VALVE_HANDLE = REGISTRATE
		.blockEntity("valve_handle", ValveHandleBlockEntity::new)
		.instance(() -> HandCrankInstance::new)
		.validBlocks(AllBlocks.COPPER_VALVE_HANDLE)
		.validBlocks(AllBlocks.DYED_VALVE_HANDLES.toArray())
		.renderer(() -> HandCrankRenderer::new)
		.register();

	public static final BlockEntityEntry<CuckooClockBlockEntity> CUCKOO_CLOCK = REGISTRATE
		.blockEntity("cuckoo_clock", CuckooClockBlockEntity::new)
		.instance(() -> HorizontalHalfShaftInstance::new)
		.validBlocks(AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK)
		.renderer(() -> CuckooClockRenderer::new)
		.register();

	public static final BlockEntityEntry<GantryShaftBlockEntity> GANTRY_SHAFT = REGISTRATE
		.blockEntity("gantry_shaft", GantryShaftBlockEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.GANTRY_SHAFT)
		.renderer(() -> KineticBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<GantryCarriageBlockEntity> GANTRY_PINION = REGISTRATE
		.blockEntity("gantry_pinion", GantryCarriageBlockEntity::new)
		.instance(() -> GantryCarriageInstance::new)
		.validBlocks(AllBlocks.GANTRY_CARRIAGE)
		.renderer(() -> GantryCarriageRenderer::new)
		.register();

	public static final BlockEntityEntry<PumpBlockEntity> MECHANICAL_PUMP = REGISTRATE
		.blockEntity("mechanical_pump", PumpBlockEntity::new)
		.instance(() -> PumpCogInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PUMP)
		.renderer(() -> PumpRenderer::new)
		.register();

	public static final BlockEntityEntry<SmartFluidPipeBlockEntity> SMART_FLUID_PIPE = REGISTRATE
		.blockEntity("smart_fluid_pipe", SmartFluidPipeBlockEntity::new)
		.validBlocks(AllBlocks.SMART_FLUID_PIPE)
		.renderer(() -> SmartBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidPipeBlockEntity> FLUID_PIPE = REGISTRATE
		.blockEntity("fluid_pipe", FluidPipeBlockEntity::new)
		.validBlocks(AllBlocks.FLUID_PIPE)
		.register();

	public static final BlockEntityEntry<FluidPipeBlockEntity> ENCASED_FLUID_PIPE = REGISTRATE
		.blockEntity("encased_fluid_pipe", FluidPipeBlockEntity::new)
		.validBlocks(AllBlocks.ENCASED_FLUID_PIPE)
		.register();

	public static final BlockEntityEntry<StraightPipeBlockEntity> GLASS_FLUID_PIPE = REGISTRATE
		.blockEntity("glass_fluid_pipe", StraightPipeBlockEntity::new)
		.validBlocks(AllBlocks.GLASS_FLUID_PIPE)
		.renderer(() -> TransparentStraightPipeRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidValveBlockEntity> FLUID_VALVE = REGISTRATE
		.blockEntity("fluid_valve", FluidValveBlockEntity::new)
		.instance(() -> FluidValveInstance::new)
		.validBlocks(AllBlocks.FLUID_VALVE)
		.renderer(() -> FluidValveRenderer::new)
		.register();

	public static final BlockEntityEntry<FluidTankBlockEntity> FLUID_TANK = REGISTRATE
		.blockEntity("fluid_tank", FluidTankBlockEntity::new)
		.validBlocks(AllBlocks.FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeFluidTankBlockEntity> CREATIVE_FLUID_TANK = REGISTRATE
		.blockEntity("creative_fluid_tank", CreativeFluidTankBlockEntity::new)
		.validBlocks(AllBlocks.CREATIVE_FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final BlockEntityEntry<HosePulleyBlockEntity> HOSE_PULLEY = REGISTRATE
		.blockEntity("hose_pulley", HosePulleyBlockEntity::new)
		.instance(() -> HosePulleyInstance::new)
		.validBlocks(AllBlocks.HOSE_PULLEY)
		.renderer(() -> HosePulleyRenderer::new)
		.register();

	public static final BlockEntityEntry<SpoutBlockEntity> SPOUT = REGISTRATE
		.blockEntity("spout", SpoutBlockEntity::new)
		.validBlocks(AllBlocks.SPOUT)
		.renderer(() -> SpoutRenderer::new)
		.register();

	public static final BlockEntityEntry<ItemDrainBlockEntity> ITEM_DRAIN = REGISTRATE
		.blockEntity("item_drain", ItemDrainBlockEntity::new)
		.validBlocks(AllBlocks.ITEM_DRAIN)
		.renderer(() -> ItemDrainRenderer::new)
		.register();

	public static final BlockEntityEntry<BeltBlockEntity> BELT = REGISTRATE
		.blockEntity("belt", BeltBlockEntity::new)
		.instance(() -> BeltInstance::new, BeltBlockEntity::shouldRenderNormally)
		.validBlocks(AllBlocks.BELT)
		.renderer(() -> BeltRenderer::new)
		.register();

	public static final BlockEntityEntry<ChuteBlockEntity> CHUTE = REGISTRATE
		.blockEntity("chute", ChuteBlockEntity::new)
		.validBlocks(AllBlocks.CHUTE)
		.renderer(() -> ChuteRenderer::new)
		.register();

	public static final BlockEntityEntry<SmartChuteBlockEntity> SMART_CHUTE = REGISTRATE
		.blockEntity("smart_chute", SmartChuteBlockEntity::new)
		.validBlocks(AllBlocks.SMART_CHUTE)
		.renderer(() -> SmartChuteRenderer::new)
		.register();

	public static final BlockEntityEntry<BeltTunnelBlockEntity> ANDESITE_TUNNEL = REGISTRATE
		.blockEntity("andesite_tunnel", BeltTunnelBlockEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.ANDESITE_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<BrassTunnelBlockEntity> BRASS_TUNNEL = REGISTRATE
		.blockEntity("brass_tunnel", BrassTunnelBlockEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<ArmBlockEntity> MECHANICAL_ARM = REGISTRATE
		.blockEntity("mechanical_arm", ArmBlockEntity::new)
		.instance(() -> ArmInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_ARM)
		.renderer(() -> ArmRenderer::new)
		.register();

	public static final BlockEntityEntry<ItemVaultBlockEntity> ITEM_VAULT = REGISTRATE
		.blockEntity("item_vault", ItemVaultBlockEntity::new)
		.validBlocks(AllBlocks.ITEM_VAULT)
		.register();

	public static final BlockEntityEntry<MechanicalPistonBlockEntity> MECHANICAL_PISTON = REGISTRATE
		.blockEntity("mechanical_piston", MechanicalPistonBlockEntity::new)
		.instance(() -> ShaftInstance::new, false)
		.validBlocks(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
		.renderer(() -> MechanicalPistonRenderer::new)
		.register();

	public static final BlockEntityEntry<WindmillBearingBlockEntity> WINDMILL_BEARING = REGISTRATE
		.blockEntity("windmill_bearing", WindmillBearingBlockEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.WINDMILL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalBearingBlockEntity> MECHANICAL_BEARING = REGISTRATE
		.blockEntity("mechanical_bearing", MechanicalBearingBlockEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<ClockworkBearingBlockEntity> CLOCKWORK_BEARING = REGISTRATE
		.blockEntity("clockwork_bearing", ClockworkBearingBlockEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.CLOCKWORK_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final BlockEntityEntry<PulleyBlockEntity> ROPE_PULLEY = REGISTRATE
		.blockEntity("rope_pulley", PulleyBlockEntity::new)
		.instance(() -> RopePulleyInstance::new, false)
		.validBlocks(AllBlocks.ROPE_PULLEY)
		.renderer(() -> PulleyRenderer::new)
		.register();

	public static final BlockEntityEntry<ElevatorPulleyBlockEntity> ELEVATOR_PULLEY =
		REGISTRATE.blockEntity("elevator_pulley", ElevatorPulleyBlockEntity::new)
//		.instance(() -> ElevatorPulleyInstance::new, false)
			.validBlocks(AllBlocks.ELEVATOR_PULLEY)
			.renderer(() -> ElevatorPulleyRenderer::new)
			.register();

	public static final BlockEntityEntry<ElevatorContactBlockEntity> ELEVATOR_CONTACT =
		REGISTRATE.blockEntity("elevator_contact", ElevatorContactBlockEntity::new)
			.validBlocks(AllBlocks.ELEVATOR_CONTACT)
			.register();

	public static final BlockEntityEntry<ChassisBlockEntity> CHASSIS = REGISTRATE
		.blockEntity("chassis", ChassisBlockEntity::new)
		.validBlocks(AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
		// .renderer(() -> renderer)
		.register();

	public static final BlockEntityEntry<StickerBlockEntity> STICKER = REGISTRATE
		.blockEntity("sticker", StickerBlockEntity::new)
		.instance(() -> StickerInstance::new, false)
		.validBlocks(AllBlocks.STICKER)
		.renderer(() -> StickerRenderer::new)
		.register();

	public static final BlockEntityEntry<ContraptionControlsBlockEntity> CONTRAPTION_CONTROLS =
		REGISTRATE.blockEntity("contraption_controls", ContraptionControlsBlockEntity::new)
			.validBlocks(AllBlocks.CONTRAPTION_CONTROLS)
			.renderer(() -> ContraptionControlsRenderer::new)
			.register();

	public static final BlockEntityEntry<DrillBlockEntity> DRILL = REGISTRATE
		.blockEntity("drill", DrillBlockEntity::new)
		.instance(() -> DrillInstance::new, false)
		.validBlocks(AllBlocks.MECHANICAL_DRILL)
		.renderer(() -> DrillRenderer::new)
		.register();

	public static final BlockEntityEntry<SawBlockEntity> SAW = REGISTRATE
		.blockEntity("saw", SawBlockEntity::new)
		.instance(() -> SawInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_SAW)
		.renderer(() -> SawRenderer::new)
		.register();

	public static final BlockEntityEntry<HarvesterBlockEntity> HARVESTER = REGISTRATE
		.blockEntity("harvester", HarvesterBlockEntity::new)
		.validBlocks(AllBlocks.MECHANICAL_HARVESTER)
		.renderer(() -> HarvesterRenderer::new)
		.register();

	public static final BlockEntityEntry<RollerBlockEntity> MECHANICAL_ROLLER =
		REGISTRATE.blockEntity("mechanical_roller", RollerBlockEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_ROLLER)
			.renderer(() -> RollerRenderer::new)
			.register();

	public static final BlockEntityEntry<PortableItemInterfaceBlockEntity> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE
			.blockEntity("portable_storage_interface", PortableItemInterfaceBlockEntity::new)
			.instance(() -> PSIInstance::new)
			.validBlocks(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.renderer(() -> PortableStorageInterfaceRenderer::new)
			.register();

	public static final BlockEntityEntry<PortableFluidInterfaceBlockEntity> PORTABLE_FLUID_INTERFACE =
		REGISTRATE
			.blockEntity("portable_fluid_interface", PortableFluidInterfaceBlockEntity::new)
			.instance(() -> PSIInstance::new)
			.validBlocks(AllBlocks.PORTABLE_FLUID_INTERFACE)
			.renderer(() -> PortableStorageInterfaceRenderer::new)
			.register();

	public static final BlockEntityEntry<SteamEngineBlockEntity> STEAM_ENGINE = REGISTRATE
		.blockEntity("steam_engine", SteamEngineBlockEntity::new)
		.instance(() -> SteamEngineInstance::new, false)
		.validBlocks(AllBlocks.STEAM_ENGINE)
		.renderer(() -> SteamEngineRenderer::new)
		.register();

	public static final BlockEntityEntry<WhistleBlockEntity> STEAM_WHISTLE = REGISTRATE
		.blockEntity("steam_whistle", WhistleBlockEntity::new)
		.validBlocks(AllBlocks.STEAM_WHISTLE)
		.renderer(() -> WhistleRenderer::new)
		.register();

	public static final BlockEntityEntry<PoweredShaftBlockEntity> POWERED_SHAFT = REGISTRATE
		.blockEntity("powered_shaft", PoweredShaftBlockEntity::new)
		.instance(() -> SingleRotatingInstance::new, false)
		.validBlocks(AllBlocks.POWERED_SHAFT)
		.renderer(() -> KineticBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<FlywheelBlockEntity> FLYWHEEL = REGISTRATE
		.blockEntity("flywheel", FlywheelBlockEntity::new)
		.instance(() -> FlywheelInstance::new, false)
		.validBlocks(AllBlocks.FLYWHEEL)
		.renderer(() -> FlywheelRenderer::new)
		.register();

	public static final BlockEntityEntry<MillstoneBlockEntity> MILLSTONE = REGISTRATE
		.blockEntity("millstone", MillstoneBlockEntity::new)
		.instance(() -> MillstoneCogInstance::new, false)
		.validBlocks(AllBlocks.MILLSTONE)
		.renderer(() -> MillstoneRenderer::new)
		.register();

	public static final BlockEntityEntry<CrushingWheelBlockEntity> CRUSHING_WHEEL = REGISTRATE
		.blockEntity("crushing_wheel", CrushingWheelBlockEntity::new)
		.instance(() -> CutoutRotatingInstance::new, false)
		.validBlocks(AllBlocks.CRUSHING_WHEEL)
		.renderer(() -> KineticBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<CrushingWheelControllerBlockEntity> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE
			.blockEntity("crushing_wheel_controller", CrushingWheelControllerBlockEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL_CONTROLLER)
			// .renderer(() -> renderer)
			.register();

	public static final BlockEntityEntry<WaterWheelBlockEntity> WATER_WHEEL = REGISTRATE
		.blockEntity("water_wheel", WaterWheelBlockEntity::new)
		.instance(() -> WaterWheelInstance::standard, false)
		.validBlocks(AllBlocks.WATER_WHEEL)
		.renderer(() -> WaterWheelRenderer::standard)
		.register();

	public static final BlockEntityEntry<LargeWaterWheelBlockEntity> LARGE_WATER_WHEEL = REGISTRATE
		.blockEntity("large_water_wheel", LargeWaterWheelBlockEntity::new)
		.instance(() -> WaterWheelInstance::large, false)
		.validBlocks(AllBlocks.LARGE_WATER_WHEEL)
		.renderer(() -> WaterWheelRenderer::large)
		.register();

	public static final BlockEntityEntry<MechanicalPressBlockEntity> MECHANICAL_PRESS = REGISTRATE
		.blockEntity("mechanical_press", MechanicalPressBlockEntity::new)
		.instance(() -> PressInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PRESS)
		.renderer(() -> MechanicalPressRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalMixerBlockEntity> MECHANICAL_MIXER = REGISTRATE
		.blockEntity("mechanical_mixer", MechanicalMixerBlockEntity::new)
		.instance(() -> MixerInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_MIXER)
		.renderer(() -> MechanicalMixerRenderer::new)
		.register();

	public static final BlockEntityEntry<DeployerBlockEntity> DEPLOYER = REGISTRATE
		.blockEntity("deployer", DeployerBlockEntity::new)
		.instance(() -> DeployerInstance::new)
		.validBlocks(AllBlocks.DEPLOYER)
		.renderer(() -> DeployerRenderer::new)
		.register();

	public static final BlockEntityEntry<BasinBlockEntity> BASIN = REGISTRATE
		.blockEntity("basin", BasinBlockEntity::new)
		.validBlocks(AllBlocks.BASIN)
		.renderer(() -> BasinRenderer::new)
		.register();

	public static final BlockEntityEntry<BlazeBurnerBlockEntity> HEATER = REGISTRATE
		.blockEntity("blaze_heater", BlazeBurnerBlockEntity::new)
		.validBlocks(AllBlocks.BLAZE_BURNER)
		.renderer(() -> BlazeBurnerRenderer::new)
		.register();

	public static final BlockEntityEntry<MechanicalCrafterBlockEntity> MECHANICAL_CRAFTER = REGISTRATE
		.blockEntity("mechanical_crafter", MechanicalCrafterBlockEntity::new)
		.instance(() -> ShaftlessCogwheelInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_CRAFTER)
		.renderer(() -> MechanicalCrafterRenderer::new)
		.register();

	public static final BlockEntityEntry<SequencedGearshiftBlockEntity> SEQUENCED_GEARSHIFT = REGISTRATE
		.blockEntity("sequenced_gearshift", SequencedGearshiftBlockEntity::new)
		.instance(() -> SplitShaftInstance::new, false)
		.validBlocks(AllBlocks.SEQUENCED_GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final BlockEntityEntry<SpeedControllerBlockEntity> ROTATION_SPEED_CONTROLLER = REGISTRATE
		.blockEntity("rotation_speed_controller", SpeedControllerBlockEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.ROTATION_SPEED_CONTROLLER)
		.renderer(() -> SpeedControllerRenderer::new)
		.register();

	public static final BlockEntityEntry<SpeedGaugeBlockEntity> SPEEDOMETER = REGISTRATE
		.blockEntity("speedometer", SpeedGaugeBlockEntity::new)
		.instance(() -> GaugeInstance.Speed::new)
		.validBlocks(AllBlocks.SPEEDOMETER)
		.renderer(() -> GaugeRenderer::speed)
		.register();

	public static final BlockEntityEntry<StressGaugeBlockEntity> STRESSOMETER = REGISTRATE
		.blockEntity("stressometer", StressGaugeBlockEntity::new)
		.instance(() -> GaugeInstance.Stress::new)
		.validBlocks(AllBlocks.STRESSOMETER)
		.renderer(() -> GaugeRenderer::stress)
		.register();

	public static final BlockEntityEntry<AnalogLeverBlockEntity> ANALOG_LEVER = REGISTRATE
		.blockEntity("analog_lever", AnalogLeverBlockEntity::new)
		.instance(() -> AnalogLeverInstance::new, false)
		.validBlocks(AllBlocks.ANALOG_LEVER)
		.renderer(() -> AnalogLeverRenderer::new)
		.register();
	
	public static final BlockEntityEntry<PlacardBlockEntity> PLACARD = REGISTRATE
		.blockEntity("placard", PlacardBlockEntity::new)
		.validBlocks(AllBlocks.PLACARD)
		.renderer(() -> PlacardRenderer::new)
		.register();

	public static final BlockEntityEntry<CartAssemblerBlockEntity> CART_ASSEMBLER = REGISTRATE
		.blockEntity("cart_assembler", CartAssemblerBlockEntity::new)
		.validBlocks(AllBlocks.CART_ASSEMBLER)
		// .renderer(() -> renderer)
		.register();

	// Logistics
	public static final BlockEntityEntry<RedstoneLinkBlockEntity> REDSTONE_LINK = REGISTRATE
		.blockEntity("redstone_link", RedstoneLinkBlockEntity::new)
		.validBlocks(AllBlocks.REDSTONE_LINK)
		.renderer(() -> SmartBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<NixieTubeBlockEntity> NIXIE_TUBE = REGISTRATE
		.blockEntity("nixie_tube", NixieTubeBlockEntity::new)
		.validBlocks(AllBlocks.ORANGE_NIXIE_TUBE)
		.validBlocks(AllBlocks.NIXIE_TUBES.toArray())
		.renderer(() -> NixieTubeRenderer::new)
		.onRegister(assignDataBehaviourBE(new NixieTubeDisplayTarget()))
		.onRegister(assignDataBehaviourBE(new NixieTubeDisplaySource()))
		.register();

	public static final BlockEntityEntry<DisplayLinkBlockEntity> DISPLAY_LINK = REGISTRATE
		.blockEntity("display_link", DisplayLinkBlockEntity::new)
		.validBlocks(AllBlocks.DISPLAY_LINK)
		.renderer(() -> DisplayLinkRenderer::new)
		.register();

	public static final BlockEntityEntry<ThresholdSwitchBlockEntity> THRESHOLD_SWITCH = REGISTRATE
		.blockEntity("stockpile_switch", ThresholdSwitchBlockEntity::new)
		.validBlocks(AllBlocks.THRESHOLD_SWITCH)
		.renderer(() -> SmartBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<CreativeCrateBlockEntity> CREATIVE_CRATE = REGISTRATE
		.blockEntity("creative_crate", CreativeCrateBlockEntity::new)
		.validBlocks(AllBlocks.CREATIVE_CRATE)
		.renderer(() -> SmartBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<DepotBlockEntity> DEPOT = REGISTRATE
		.blockEntity("depot", DepotBlockEntity::new)
		.validBlocks(AllBlocks.DEPOT)
		.renderer(() -> DepotRenderer::new)
		.register();

	public static final BlockEntityEntry<EjectorBlockEntity> WEIGHTED_EJECTOR = REGISTRATE
		.blockEntity("weighted_ejector", EjectorBlockEntity::new)
		.instance(() -> EjectorInstance::new)
		.validBlocks(AllBlocks.WEIGHTED_EJECTOR)
		.renderer(() -> EjectorRenderer::new)
		.register();

	public static final BlockEntityEntry<FunnelBlockEntity> FUNNEL = REGISTRATE
		.blockEntity("funnel", FunnelBlockEntity::new)
		.instance(() -> FunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.ANDESITE_FUNNEL,
			AllBlocks.ANDESITE_BELT_FUNNEL)
		.renderer(() -> FunnelRenderer::new)
		.register();

	public static final BlockEntityEntry<SmartObserverBlockEntity> SMART_OBSERVER = REGISTRATE
		.blockEntity("content_observer", SmartObserverBlockEntity::new)
		.validBlocks(AllBlocks.SMART_OBSERVER)
		.renderer(() -> SmartBlockEntityRenderer::new)
		.register();

	public static final BlockEntityEntry<PulseExtenderBlockEntity> PULSE_EXTENDER = REGISTRATE
		.blockEntity("pulse_extender", PulseExtenderBlockEntity::new)
		.instance(() -> BrassDiodeInstance::new, false)
		.validBlocks(AllBlocks.PULSE_EXTENDER)
		.renderer(() -> BrassDiodeRenderer::new)
		.register();

	public static final BlockEntityEntry<PulseRepeaterBlockEntity> PULSE_REPEATER = REGISTRATE
		.blockEntity("pulse_repeater", PulseRepeaterBlockEntity::new)
		.instance(() -> BrassDiodeInstance::new, false)
		.validBlocks(AllBlocks.PULSE_REPEATER)
		.renderer(() -> BrassDiodeRenderer::new)
		.register();

	public static final BlockEntityEntry<LecternControllerBlockEntity> LECTERN_CONTROLLER = REGISTRATE
		.blockEntity("lectern_controller", LecternControllerBlockEntity::new)
		.validBlocks(AllBlocks.LECTERN_CONTROLLER)
		.renderer(() -> LecternControllerRenderer::new)
		.register();

	// Curiosities
	public static final BlockEntityEntry<BacktankBlockEntity> BACKTANK = REGISTRATE
		.blockEntity("backtank", BacktankBlockEntity::new)
		.instance(() -> BacktankInstance::new)
		.validBlocks(AllBlocks.COPPER_BACKTANK, AllBlocks.NETHERITE_BACKTANK)
		.renderer(() -> BacktankRenderer::new)
		.register();

	public static final BlockEntityEntry<PeculiarBellBlockEntity> PECULIAR_BELL = REGISTRATE
		.blockEntity("peculiar_bell", PeculiarBellBlockEntity::new)
		.validBlocks(AllBlocks.PECULIAR_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final BlockEntityEntry<HauntedBellBlockEntity> HAUNTED_BELL = REGISTRATE
		.blockEntity("cursed_bell", HauntedBellBlockEntity::new)
		.validBlocks(AllBlocks.HAUNTED_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final BlockEntityEntry<ToolboxBlockEntity> TOOLBOX = REGISTRATE
		.blockEntity("toolbox", ToolboxBlockEntity::new)
		.instance(() -> ToolBoxInstance::new, false)
		.validBlocks(AllBlocks.TOOLBOXES.toArray())
		.renderer(() -> ToolboxRenderer::new)
		.register();

	public static final BlockEntityEntry<TrackBlockEntity> TRACK = REGISTRATE
		.blockEntity("track", TrackBlockEntity::new)
		.instance(() -> TrackInstance::new)
		.renderer(() -> TrackRenderer::new)
		.validBlocks(AllBlocks.TRACK)
		.register();
	
	public static final BlockEntityEntry<FakeTrackBlockEntity> FAKE_TRACK = REGISTRATE
		.blockEntity("fake_track", FakeTrackBlockEntity::new)
		.validBlocks(AllBlocks.FAKE_TRACK)
		.register();

	public static final BlockEntityEntry<StandardBogeyBlockEntity> BOGEY = REGISTRATE
		.blockEntity("bogey", StandardBogeyBlockEntity::new)
		.renderer(() -> BogeyBlockEntityRenderer::new)
		.validBlocks(AllBlocks.SMALL_BOGEY, AllBlocks.LARGE_BOGEY)
		.register();

	public static final BlockEntityEntry<StationBlockEntity> TRACK_STATION = REGISTRATE
		.blockEntity("track_station", StationBlockEntity::new)
		.renderer(() -> StationRenderer::new)
		.validBlocks(AllBlocks.TRACK_STATION)
		.register();

	public static final BlockEntityEntry<SlidingDoorBlockEntity> SLIDING_DOOR =
		REGISTRATE.blockEntity("sliding_door", SlidingDoorBlockEntity::new)
			.renderer(() -> SlidingDoorRenderer::new)
			.validBlocks(AllBlocks.TRAIN_DOOR, AllBlocks.FRAMED_GLASS_DOOR, AllBlocks.ANDESITE_DOOR,
				AllBlocks.BRASS_DOOR, AllBlocks.COPPER_DOOR)
			.register();

	public static final BlockEntityEntry<CopycatBlockEntity> COPYCAT =
		REGISTRATE.blockEntity("copycat", CopycatBlockEntity::new)
			.validBlocks(AllBlocks.COPYCAT_PANEL, AllBlocks.COPYCAT_STEP)
			.register();

	public static final BlockEntityEntry<FlapDisplayBlockEntity> FLAP_DISPLAY = REGISTRATE
		.blockEntity("flap_display", FlapDisplayBlockEntity::new)
		.instance(() -> ShaftlessCogwheelInstance::new)
		.renderer(() -> FlapDisplayRenderer::new)
		.validBlocks(AllBlocks.DISPLAY_BOARD)
		.register();

	public static final BlockEntityEntry<SignalBlockEntity> TRACK_SIGNAL = REGISTRATE
		.blockEntity("track_signal", SignalBlockEntity::new)
		.renderer(() -> SignalRenderer::new)
		.validBlocks(AllBlocks.TRACK_SIGNAL)
		.register();

	public static final BlockEntityEntry<TrackObserverBlockEntity> TRACK_OBSERVER = REGISTRATE
		.blockEntity("track_observer", TrackObserverBlockEntity::new)
		.renderer(() -> TrackObserverRenderer::new)
		.validBlocks(AllBlocks.TRACK_OBSERVER)
		.register();
	
	public static final BlockEntityEntry<ClipboardBlockEntity> CLIPBOARD = REGISTRATE
		.blockEntity("clipboard", ClipboardBlockEntity::new)
		.validBlocks(AllBlocks.CLIPBOARD)
		.register();

	public static void register() {}
}
