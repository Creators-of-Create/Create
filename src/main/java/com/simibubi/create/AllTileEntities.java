package com.simibubi.create;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.actors.DrillRenderer;
import com.simibubi.create.content.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.content.contraptions.components.actors.HarvesterRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterTileEntity;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterRenderer;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.crank.HandCrankRenderer;
import com.simibubi.create.content.contraptions.components.crank.HandCrankTileEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerTileEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerRenderer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanRenderer;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.fan.NozzleTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineTileEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneTileEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerRenderer;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorRenderer;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressRenderer;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.components.turntable.TurntableTileEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import com.simibubi.create.content.contraptions.fluids.PumpRenderer;
import com.simibubi.create.content.contraptions.fluids.PumpTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutRenderer;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.TransparentStraightPipeRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinRenderer;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltRenderer;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.*;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxRenderer;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverRenderer;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelRenderer;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteRenderer;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.content.logistics.block.depot.DepotRenderer;
import com.simibubi.create.content.logistics.block.depot.DepotTileEntity;
import com.simibubi.create.content.logistics.block.diodes.AdjustablePulseRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterRenderer;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.extractor.ExtractorTileEntity;
import com.simibubi.create.content.logistics.block.extractor.LinkedExtractorTileEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelRenderer;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateTileEntity;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity;
import com.simibubi.create.content.logistics.block.packager.PackagerRenderer;
import com.simibubi.create.content.logistics.block.packager.PackagerTileEntity;
import com.simibubi.create.content.logistics.block.redstone.*;
import com.simibubi.create.content.logistics.block.transposer.LinkedTransposerTileEntity;
import com.simibubi.create.content.logistics.block.transposer.TransposerTileEntity;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.tterrag.registrate.util.entry.TileEntityEntry;

public class AllTileEntities {

	// Schematics
	public static final TileEntityEntry<SchematicannonTileEntity> SCHEMATICANNON = Create.registrate()
			.tileEntity("schematicannon", SchematicannonTileEntity::new)
			.validBlocks(AllBlocks.SCHEMATICANNON)
			.renderer(() -> SchematicannonRenderer::new)
			.register();

	public static final TileEntityEntry<SchematicTableTileEntity> SCHEMATIC_TABLE = Create.registrate()
			.tileEntity("schematic_table", SchematicTableTileEntity::new)
			.validBlocks(AllBlocks.SCHEMATIC_TABLE)
			//.renderer(() -> renderer)
			.register();

	// Kinetics
	public static final TileEntityEntry<SimpleKineticTileEntity> SIMPLE_KINETIC = Create.registrate()
			.tileEntity("simple_kinetic", SimpleKineticTileEntity::new)
			.validBlocks(AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL, AllBlocks.ENCASED_SHAFT)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<CreativeMotorTileEntity> MOTOR = Create.registrate()
			.tileEntity("motor", CreativeMotorTileEntity::new)
			.validBlocks(AllBlocks.CREATIVE_MOTOR)
			.renderer(() -> CreativeMotorRenderer::new)
			.register();

	public static final TileEntityEntry<GearboxTileEntity> GEARBOX = Create.registrate()
			.tileEntity("gearbox", GearboxTileEntity::new)
			.validBlocks(AllBlocks.GEARBOX)
			.renderer(() -> GearboxRenderer::new)
			.register();

	public static final TileEntityEntry<EncasedShaftTileEntity> ENCASED_SHAFT = Create.registrate()
			.tileEntity("encased_shaft", EncasedShaftTileEntity::new)
			.validBlocks(AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT)
			.renderer(() -> EncasedShaftRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustablePulleyTileEntity> ADJUSTABLE_PULLEY = Create.registrate()
			.tileEntity("adjustable_pulley", AdjustablePulleyTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_PULLEY)
			.renderer(() -> EncasedShaftRenderer::new)
			.register();

	public static final TileEntityEntry<EncasedFanTileEntity> ENCASED_FAN = Create.registrate()
			.tileEntity("encased_fan", EncasedFanTileEntity::new)
			.validBlocks(AllBlocks.ENCASED_FAN)
			.renderer(() -> EncasedFanRenderer::new)
			.register();

	public static final TileEntityEntry<NozzleTileEntity> NOZZLE = Create.registrate()
			.tileEntity("nozzle", NozzleTileEntity::new)
			.validBlocks(AllBlocks.NOZZLE)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<ClutchTileEntity> CLUTCH = Create.registrate()
			.tileEntity("clutch", ClutchTileEntity::new)
			.validBlocks(AllBlocks.CLUTCH)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<GearshiftTileEntity> GEARSHIFT = Create.registrate()
			.tileEntity("gearshift", GearshiftTileEntity::new)
			.validBlocks(AllBlocks.GEARSHIFT)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<TurntableTileEntity> TURNTABLE = Create.registrate()
			.tileEntity("turntable", TurntableTileEntity::new)
			.validBlocks(AllBlocks.TURNTABLE)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<HandCrankTileEntity> HAND_CRANK = Create.registrate()
			.tileEntity("hand_crank", HandCrankTileEntity::new)
			.validBlocks(AllBlocks.HAND_CRANK)
			.renderer(() -> HandCrankRenderer::new)
			.register();

	public static final TileEntityEntry<CuckooClockTileEntity> CUCKOO_CLOCK = Create.registrate()
			.tileEntity("cuckoo_clock", CuckooClockTileEntity::new)
			.validBlocks(AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK)
			.renderer(() -> CuckooClockRenderer::new)
			.register();

	public static final TileEntityEntry<PumpTileEntity> MECHANICAL_PUMP = Create.registrate()
			.tileEntity("mechanical_pump", PumpTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PUMP)
			.renderer(() -> PumpRenderer::new)
			.register();

	public static final TileEntityEntry<FluidPipeTileEntity> FLUID_PIPE = Create.registrate()
			.tileEntity("fluid_pipe", FluidPipeTileEntity::new)
			.validBlocks(AllBlocks.FLUID_PIPE)
			.register();

	public static final TileEntityEntry<StraightPipeTileEntity> ENCASED_FLUID_PIPE = Create.registrate()
			.tileEntity("encased_fluid_pipe", StraightPipeTileEntity::new)
			.validBlocks(AllBlocks.ENCASED_FLUID_PIPE)
			.register();

	public static final TileEntityEntry<StraightPipeTileEntity> GLASS_FLUID_PIPE = Create.registrate()
			.tileEntity("glass_fluid_pipe", StraightPipeTileEntity::new)
			.validBlocks(AllBlocks.GLASS_FLUID_PIPE)
			.renderer(() -> TransparentStraightPipeRenderer::new)
			.register();

	public static final TileEntityEntry<FluidTankTileEntity> FLUID_TANK = Create.registrate()
			.tileEntity("fluid_tank", FluidTankTileEntity::new)
			.validBlocks(AllBlocks.FLUID_TANK)
			.renderer(() -> FluidTankRenderer::new)
			.register();
	
	public static final TileEntityEntry<SpoutTileEntity> SPOUT = Create.registrate()
			.tileEntity("spout", SpoutTileEntity::new)
			.validBlocks(AllBlocks.SPOUT)
			.renderer(() -> SpoutRenderer::new)
			.register();

	public static final TileEntityEntry<BeltTileEntity> BELT = Create.registrate()
			.tileEntity("belt", BeltTileEntity::new)
			.validBlocks(AllBlocks.BELT)
			.renderer(() -> BeltRenderer::new)
			.register();

	public static final TileEntityEntry<ChuteTileEntity> CHUTE = Create.registrate()
			.tileEntity("chute", ChuteTileEntity::new)
			.validBlocks(AllBlocks.CHUTE)
			.renderer(() -> ChuteRenderer::new)
			.register();

	public static final TileEntityEntry<BeltTunnelTileEntity> ANDESITE_TUNNEL = Create.registrate()
			.tileEntity("andesite_tunnel", BeltTunnelTileEntity::new)
			.validBlocks(AllBlocks.ANDESITE_TUNNEL)
			.renderer(() -> BeltTunnelRenderer::new)
			.register();

	public static final TileEntityEntry<BrassTunnelTileEntity> BRASS_TUNNEL = Create.registrate()
			.tileEntity("brass_tunnel", BrassTunnelTileEntity::new)
			.validBlocks(AllBlocks.BRASS_TUNNEL)
			.renderer(() -> BeltTunnelRenderer::new)
			.register();

	public static final TileEntityEntry<ArmTileEntity> MECHANICAL_ARM = Create.registrate()
			.tileEntity("mechanical_arm", ArmTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_ARM)
			.renderer(() -> ArmRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalPistonTileEntity> MECHANICAL_PISTON = Create.registrate()
			.tileEntity("mechanical_piston", MechanicalPistonTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.renderer(() -> MechanicalPistonRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalBearingTileEntity> MECHANICAL_BEARING = Create.registrate()
			.tileEntity("mechanical_bearing", MechanicalBearingTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_BEARING)
			.renderer(() -> BearingRenderer::new)
			.register();

	public static final TileEntityEntry<ClockworkBearingTileEntity> CLOCKWORK_BEARING = Create.registrate()
			.tileEntity("clockwork_bearing", ClockworkBearingTileEntity::new)
			.validBlocks(AllBlocks.CLOCKWORK_BEARING)
			.renderer(() -> BearingRenderer::new)
			.register();

	public static final TileEntityEntry<PulleyTileEntity> ROPE_PULLEY = Create.registrate()
			.tileEntity("rope_pulley", PulleyTileEntity::new)
			.validBlocks(AllBlocks.ROPE_PULLEY)
			.renderer(() -> PulleyRenderer::new)
			.register();

	public static final TileEntityEntry<ChassisTileEntity> CHASSIS = Create.registrate()
			.tileEntity("chassis", ChassisTileEntity::new)
			.validBlocks(AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<DrillTileEntity> DRILL = Create.registrate()
			.tileEntity("drill", DrillTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_DRILL)
			.renderer(() -> DrillRenderer::new)
			.register();

	public static final TileEntityEntry<SawTileEntity> SAW = Create.registrate()
			.tileEntity("saw", SawTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_SAW)
			.renderer(() -> SawRenderer::new)
			.register();

	public static final TileEntityEntry<HarvesterTileEntity> HARVESTER = Create.registrate()
			.tileEntity("harvester", HarvesterTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_HARVESTER)
			.renderer(() -> HarvesterRenderer::new)
			.register();

	public static final TileEntityEntry<FlywheelTileEntity> FLYWHEEL = Create.registrate()
			.tileEntity("flywheel", FlywheelTileEntity::new)
			.validBlocks(AllBlocks.FLYWHEEL)
			.renderer(() -> FlywheelRenderer::new)
			.register();

	public static final TileEntityEntry<FurnaceEngineTileEntity> FURNACE_ENGINE = Create.registrate()
			.tileEntity("furnace_engine", FurnaceEngineTileEntity::new)
			.validBlocks(AllBlocks.FURNACE_ENGINE)
			.renderer(() -> EngineRenderer::new)
			.register();

	public static final TileEntityEntry<MillstoneTileEntity> MILLSTONE = Create.registrate()
			.tileEntity("millstone", MillstoneTileEntity::new)
			.validBlocks(AllBlocks.MILLSTONE)
			.renderer(() -> MillstoneRenderer::new)
			.register();

	public static final TileEntityEntry<CrushingWheelTileEntity> CRUSHING_WHEEL = Create.registrate()
			.tileEntity("crushing_wheel", CrushingWheelTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<CrushingWheelControllerTileEntity> CRUSHING_WHEEL_CONTROLLER = Create.registrate()
			.tileEntity("crushing_wheel_controller", CrushingWheelControllerTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL_CONTROLLER)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<WaterWheelTileEntity> WATER_WHEEL = Create.registrate()
			.tileEntity("water_wheel", WaterWheelTileEntity::new)
			.validBlocks(AllBlocks.WATER_WHEEL)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalPressTileEntity> MECHANICAL_PRESS = Create.registrate()
			.tileEntity("mechanical_press", MechanicalPressTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PRESS)
			.renderer(() -> MechanicalPressRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalMixerTileEntity> MECHANICAL_MIXER = Create.registrate()
			.tileEntity("mechanical_mixer", MechanicalMixerTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_MIXER)
			.renderer(() -> MechanicalMixerRenderer::new)
			.register();

	public static final TileEntityEntry<DeployerTileEntity> DEPLOYER = Create.registrate()
			.tileEntity("deployer", DeployerTileEntity::new)
			.validBlocks(AllBlocks.DEPLOYER)
			.renderer(() -> DeployerRenderer::new)
			.register();

	public static final TileEntityEntry<BasinTileEntity> BASIN = Create.registrate()
			.tileEntity("basin", BasinTileEntity::new)
			.validBlocks(AllBlocks.BASIN)
			.renderer(() -> BasinRenderer::new)
			.register();

	public static final TileEntityEntry<BlazeBurnerTileEntity> HEATER = Create.registrate()
			.tileEntity("blaze_heater", BlazeBurnerTileEntity::new)
			.validBlocks(AllBlocks.BLAZE_BURNER)
			.renderer(() -> BlazeBurnerRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalCrafterTileEntity> MECHANICAL_CRAFTER = Create.registrate()
			.tileEntity("mechanical_crafter", MechanicalCrafterTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_CRAFTER)
			.renderer(() -> MechanicalCrafterRenderer::new)
			.register();

	public static final TileEntityEntry<SequencedGearshiftTileEntity> SEQUENCED_GEARSHIFT = Create.registrate()
			.tileEntity("sequenced_gearshift", SequencedGearshiftTileEntity::new)
			.validBlocks(AllBlocks.SEQUENCED_GEARSHIFT)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<SpeedControllerTileEntity> ROTATION_SPEED_CONTROLLER = Create.registrate()
			.tileEntity("rotation_speed_controller", SpeedControllerTileEntity::new)
			.validBlocks(AllBlocks.ROTATION_SPEED_CONTROLLER)
			.renderer(() -> SpeedControllerRenderer::new)
			.register();

	public static final TileEntityEntry<SpeedGaugeTileEntity> SPEEDOMETER = Create.registrate()
			.tileEntity("speedometer", SpeedGaugeTileEntity::new)
			.validBlocks(AllBlocks.SPEEDOMETER)
			.renderer(() -> GaugeRenderer::speed)
			.register();

	public static final TileEntityEntry<StressGaugeTileEntity> STRESSOMETER = Create.registrate()
			.tileEntity("stressometer", StressGaugeTileEntity::new)
			.validBlocks(AllBlocks.STRESSOMETER)
			.renderer(() -> GaugeRenderer::stress)
			.register();

	public static final TileEntityEntry<AnalogLeverTileEntity> ANALOG_LEVER = Create.registrate()
			.tileEntity("analog_lever", AnalogLeverTileEntity::new)
			.validBlocks(AllBlocks.ANALOG_LEVER)
			.renderer(() -> AnalogLeverRenderer::new)
			.register();

	public static final TileEntityEntry<CartAssemblerTileEntity> CART_ASSEMBLER = Create.registrate()
			.tileEntity("cart_assembler", CartAssemblerTileEntity::new)
			.validBlocks(AllBlocks.CART_ASSEMBLER)
			//.renderer(() -> renderer)
			.register();

	// Logistics
	public static final TileEntityEntry<RedstoneLinkTileEntity> REDSTONE_LINK = Create.registrate()
			.tileEntity("redstone_link", RedstoneLinkTileEntity::new)
			.validBlocks(AllBlocks.REDSTONE_LINK)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<NixieTubeTileEntity> NIXIE_TUBE = Create.registrate()
			.tileEntity("nixie_tube", NixieTubeTileEntity::new)
			.validBlocks(AllBlocks.NIXIE_TUBE)
			.renderer(() -> NixieTubeRenderer::new)
			.register();

	public static final TileEntityEntry<StockpileSwitchTileEntity> STOCKPILE_SWITCH = Create.registrate()
			.tileEntity("stockpile_switch", StockpileSwitchTileEntity::new)
			.validBlocks(AllBlocks.STOCKPILE_SWITCH)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<AdjustableCrateTileEntity> ADJUSTABLE_CRATE = Create.registrate()
			.tileEntity("adjustable_crate", AdjustableCrateTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_CRATE)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<CreativeCrateTileEntity> CREATIVE_CRATE = Create.registrate()
			.tileEntity("creative_crate", CreativeCrateTileEntity::new)
			.validBlocks(AllBlocks.CREATIVE_CRATE)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<DepotTileEntity> DEPOT = Create.registrate()
			.tileEntity("depot", DepotTileEntity::new)
			.validBlocks(AllBlocks.DEPOT)
			.renderer(() -> DepotRenderer::new)
			.register();

	public static final TileEntityEntry<FunnelTileEntity> FUNNEL = Create.registrate()
			.tileEntity("funnel", FunnelTileEntity::new)
			.validBlocks(AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.BRASS_CHUTE_FUNNEL, AllBlocks.ANDESITE_FUNNEL, AllBlocks.ANDESITE_BELT_FUNNEL, AllBlocks.ANDESITE_CHUTE_FUNNEL)
			.renderer(() -> FunnelRenderer::new)
			.register();

	public static final TileEntityEntry<PackagerTileEntity> PACKAGER = Create.registrate()
			.tileEntity("packager", PackagerTileEntity::new)
			.validBlocks(AllBlocks.PACKAGER)
			.renderer(() -> PackagerRenderer::new)
			.register();

	public static final TileEntityEntry<ExtractorTileEntity> EXTRACTOR = Create.registrate()
			.tileEntity("extractor", ExtractorTileEntity::new)
			.validBlocks(AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<LinkedExtractorTileEntity> LINKED_EXTRACTOR = Create.registrate()
			.tileEntity("linked_extractor", LinkedExtractorTileEntity::new)
			.validBlocks(AllBlocks.LINKED_EXTRACTOR, AllBlocks.VERTICAL_LINKED_EXTRACTOR)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<TransposerTileEntity> TRANSPOSER = Create.registrate()
			.tileEntity("transposer", TransposerTileEntity::new)
			.validBlocks(AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<LinkedTransposerTileEntity> LINKED_TRANSPOSER = Create.registrate()
			.tileEntity("linked_transposer", LinkedTransposerTileEntity::new)
			.validBlocks(AllBlocks.LINKED_TRANSPOSER, AllBlocks.VERTICAL_LINKED_TRANSPOSER)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<BeltObserverTileEntity> BELT_OBSERVER = Create.registrate()
			.tileEntity("belt_observer", BeltObserverTileEntity::new)
			.validBlocks(AllBlocks.BELT_OBSERVER)
			.renderer(() -> BeltObserverRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustableRepeaterTileEntity> ADJUSTABLE_REPEATER = Create.registrate()
			.tileEntity("adjustable_repeater", AdjustableRepeaterTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_REPEATER)
			.renderer(() -> AdjustableRepeaterRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustablePulseRepeaterTileEntity> ADJUSTABLE_PULSE_REPEATER = Create.registrate()
			.tileEntity("adjustable_pulse_repeater", AdjustablePulseRepeaterTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_PULSE_REPEATER)
			.renderer(() -> AdjustableRepeaterRenderer::new)
			.register();

	public static void register() {
	}
}
