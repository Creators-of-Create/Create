package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours.assignDataBehaviourBE;

import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsRenderer;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterRenderer;
import com.simibubi.create.content.contraptions.actors.psi.PSIInstance;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import com.simibubi.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.simibubi.create.content.contraptions.actors.roller.RollerRenderer;
import com.simibubi.create.content.contraptions.bearing.BearingInstance;
import com.simibubi.create.content.contraptions.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.bearing.ClockworkBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.contraptions.chassis.ChassisBlockEntity;
import com.simibubi.create.content.contraptions.chassis.StickerBlockEntity;
import com.simibubi.create.content.contraptions.chassis.StickerInstance;
import com.simibubi.create.content.contraptions.chassis.StickerRenderer;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyRenderer;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageInstance;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageRenderer;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlockEntity;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonRenderer;
import com.simibubi.create.content.contraptions.pulley.HosePulleyInstance;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.pulley.RopePulleyInstance;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.decoration.placard.PlacardBlockEntity;
import com.simibubi.create.content.decoration.placard.PlacardRenderer;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.simibubi.create.content.decoration.steamWhistle.WhistleRenderer;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.equipment.armor.BacktankInstance;
import com.simibubi.create.content.equipment.armor.BacktankRenderer;
import com.simibubi.create.content.equipment.bell.BellRenderer;
import com.simibubi.create.content.equipment.bell.HauntedBellBlockEntity;
import com.simibubi.create.content.equipment.bell.PeculiarBellBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolBoxInstance;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxRenderer;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.drain.ItemDrainRenderer;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyRenderer;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.TransparentStraightPipeRenderer;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveInstance;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveRenderer;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpCogInstance;
import com.simibubi.create.content.fluids.pump.PumpRenderer;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.fluids.spout.SpoutRenderer;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import com.simibubi.create.content.kinetics.base.CutoutRotatingInstance;
import com.simibubi.create.content.kinetics.base.HalfShaftInstance;
import com.simibubi.create.content.kinetics.base.HorizontalHalfShaftInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.chainDrive.ChainGearshiftBlockEntity;
import com.simibubi.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.simibubi.create.content.kinetics.clock.CuckooClockRenderer;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterRenderer;
import com.simibubi.create.content.kinetics.crafter.ShaftlessCogwheelInstance;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
import com.simibubi.create.content.kinetics.crank.HandCrankInstance;
import com.simibubi.create.content.kinetics.crank.HandCrankRenderer;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerInstance;
import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import com.simibubi.create.content.kinetics.drill.DrillBlockEntity;
import com.simibubi.create.content.kinetics.drill.DrillInstance;
import com.simibubi.create.content.kinetics.drill.DrillRenderer;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanRenderer;
import com.simibubi.create.content.kinetics.fan.FanInstance;
import com.simibubi.create.content.kinetics.fan.NozzleBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelInstance;
import com.simibubi.create.content.kinetics.flywheel.FlywheelRenderer;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.simibubi.create.content.kinetics.gauge.GaugeInstance;
import com.simibubi.create.content.kinetics.gauge.GaugeRenderer;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.simibubi.create.content.kinetics.gearbox.GearboxInstance;
import com.simibubi.create.content.kinetics.gearbox.GearboxRenderer;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInstance;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmRenderer;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneCogInstance;
import com.simibubi.create.content.kinetics.millstone.MillstoneRenderer;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerRenderer;
import com.simibubi.create.content.kinetics.mixer.MixerInstance;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorRenderer;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressRenderer;
import com.simibubi.create.content.kinetics.press.PressInstance;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawInstance;
import com.simibubi.create.content.kinetics.saw.SawRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogInstance;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerRenderer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineInstance;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineRenderer;
import com.simibubi.create.content.kinetics.transmission.ClutchBlockEntity;
import com.simibubi.create.content.kinetics.transmission.GearshiftBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftInstance;
import com.simibubi.create.content.kinetics.transmission.SplitShaftRenderer;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.kinetics.turntable.TurntableBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelInstance;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteRenderer;
import com.simibubi.create.content.logistics.chute.SmartChuteBlockEntity;
import com.simibubi.create.content.logistics.chute.SmartChuteRenderer;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.content.logistics.depot.EjectorInstance;
import com.simibubi.create.content.logistics.depot.EjectorRenderer;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.content.logistics.funnel.FunnelInstance;
import com.simibubi.create.content.logistics.funnel.FunnelRenderer;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelInstance;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelRenderer;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverInstance;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverRenderer;
import com.simibubi.create.content.redstone.diodes.BrassDiodeInstance;
import com.simibubi.create.content.redstone.diodes.BrassDiodeRenderer;
import com.simibubi.create.content.redstone.diodes.PulseExtenderBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseRepeaterBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkRenderer;
import com.simibubi.create.content.redstone.displayLink.source.NixieTubeDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.NixieTubeDisplayTarget;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.content.redstone.link.controller.LecternControllerBlockEntity;
import com.simibubi.create.content.redstone.link.controller.LecternControllerRenderer;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeRenderer;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInstance;
import com.simibubi.create.content.schematics.cannon.SchematicannonRenderer;
import com.simibubi.create.content.schematics.table.SchematicTableBlockEntity;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayRenderer;
import com.simibubi.create.content.trains.observer.TrackObserverBlockEntity;
import com.simibubi.create.content.trains.observer.TrackObserverRenderer;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.signal.SignalRenderer;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationRenderer;
import com.simibubi.create.content.trains.track.FakeTrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackInstance;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackRenderer;
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

	public static final BlockEntityEntry<ChainGearshiftBlockEntity> ADJUSTABLE_CHAIN_GEARSHIFT = REGISTRATE
		.blockEntity("adjustable_chain_gearshift", ChainGearshiftBlockEntity::new)
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
		.instance(() -> BeltVisual::new, BeltBlockEntity::shouldRenderNormally)
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
		.validBlocksDeferred(TrackMaterial::allBlocks)
		.renderer(() -> TrackRenderer::new)
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
