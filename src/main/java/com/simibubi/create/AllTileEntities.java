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
import com.simibubi.create.content.contraptions.fluids.FluidPipeTileEntity;
import com.simibubi.create.content.contraptions.fluids.FluidTankRenderer;
import com.simibubi.create.content.contraptions.fluids.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.PumpRenderer;
import com.simibubi.create.content.contraptions.fluids.PumpTileEntity;
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
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ClutchTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
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
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverRenderer;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverTileEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeRenderer;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkTileEntity;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.content.logistics.block.transposer.LinkedTransposerTileEntity;
import com.simibubi.create.content.logistics.block.transposer.TransposerTileEntity;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.tterrag.registrate.util.entry.TileEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.tileentity.TileEntityType;

public class AllTileEntities {

	// Schematics
	public static final TileEntityEntry<SchematicannonTileEntity> SCHEMATICANNON = Create.registrate()
			.tileEntity("schematicannon", (NonNullFunction<TileEntityType<SchematicannonTileEntity>, ? extends SchematicannonTileEntity>) SchematicannonTileEntity::new)
			.validBlocks(AllBlocks.SCHEMATICANNON)
			.renderer(() -> SchematicannonRenderer::new)
			.register();

	public static final TileEntityEntry<SchematicTableTileEntity> SCHEMATIC_TABLE = Create.registrate()
			.tileEntity("schematic_table", (NonNullFunction<TileEntityType<SchematicTableTileEntity>, ? extends SchematicTableTileEntity>) SchematicTableTileEntity::new)
			.validBlocks(AllBlocks.SCHEMATIC_TABLE)
			//.renderer(() -> renderer)
			.register();

	// Kinetics
	public static final TileEntityEntry<SimpleKineticTileEntity> SIMPLE_KINETIC = Create.registrate()
			.tileEntity("simple_kinetic", (NonNullFunction<TileEntityType<SimpleKineticTileEntity>, ? extends SimpleKineticTileEntity>) SimpleKineticTileEntity::new)
			.validBlocks(AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL, AllBlocks.ENCASED_SHAFT)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<CreativeMotorTileEntity> MOTOR = Create.registrate()
			.tileEntity("motor", (NonNullFunction<TileEntityType<CreativeMotorTileEntity>, ? extends CreativeMotorTileEntity>) CreativeMotorTileEntity::new)
			.validBlocks(AllBlocks.CREATIVE_MOTOR)
			.renderer(() -> CreativeMotorRenderer::new)
			.register();

	public static final TileEntityEntry<GearboxTileEntity> GEARBOX = Create.registrate()
			.tileEntity("gearbox", (NonNullFunction<TileEntityType<GearboxTileEntity>, ? extends GearboxTileEntity>) GearboxTileEntity::new)
			.validBlocks(AllBlocks.GEARBOX)
			.renderer(() -> GearboxRenderer::new)
			.register();

	public static final TileEntityEntry<EncasedShaftTileEntity> ENCASED_SHAFT = Create.registrate()
			.tileEntity("encased_shaft", (NonNullFunction<TileEntityType<EncasedShaftTileEntity>, ? extends EncasedShaftTileEntity>) EncasedShaftTileEntity::new)
			.validBlocks(AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT)
			.renderer(() -> EncasedShaftRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustablePulleyTileEntity> ADJUSTABLE_PULLEY = Create.registrate()
			.tileEntity("adjustable_pulley", (NonNullFunction<TileEntityType<AdjustablePulleyTileEntity>, ? extends AdjustablePulleyTileEntity>) AdjustablePulleyTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_PULLEY)
			.renderer(() -> EncasedShaftRenderer::new)
			.register();

	public static final TileEntityEntry<EncasedFanTileEntity> ENCASED_FAN = Create.registrate()
			.tileEntity("encased_fan", (NonNullFunction<TileEntityType<EncasedFanTileEntity>, ? extends EncasedFanTileEntity>) EncasedFanTileEntity::new)
			.validBlocks(AllBlocks.ENCASED_FAN)
			.renderer(() -> EncasedFanRenderer::new)
			.register();

	public static final TileEntityEntry<NozzleTileEntity> NOZZLE = Create.registrate()
			.tileEntity("nozzle", (NonNullFunction<TileEntityType<NozzleTileEntity>, ? extends NozzleTileEntity>) NozzleTileEntity::new)
			.validBlocks(AllBlocks.NOZZLE)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<ClutchTileEntity> CLUTCH = Create.registrate()
			.tileEntity("clutch", (NonNullFunction<TileEntityType<ClutchTileEntity>, ? extends ClutchTileEntity>) ClutchTileEntity::new)
			.validBlocks(AllBlocks.CLUTCH)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<GearshiftTileEntity> GEARSHIFT = Create.registrate()
			.tileEntity("gearshift", (NonNullFunction<TileEntityType<GearshiftTileEntity>, ? extends GearshiftTileEntity>) GearshiftTileEntity::new)
			.validBlocks(AllBlocks.GEARSHIFT)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<TurntableTileEntity> TURNTABLE = Create.registrate()
			.tileEntity("turntable", (NonNullFunction<TileEntityType<TurntableTileEntity>, ? extends TurntableTileEntity>) TurntableTileEntity::new)
			.validBlocks(AllBlocks.TURNTABLE)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<HandCrankTileEntity> HAND_CRANK = Create.registrate()
			.tileEntity("hand_crank", (NonNullFunction<TileEntityType<HandCrankTileEntity>, ? extends HandCrankTileEntity>) HandCrankTileEntity::new)
			.validBlocks(AllBlocks.HAND_CRANK)
			.renderer(() -> HandCrankRenderer::new)
			.register();

	public static final TileEntityEntry<CuckooClockTileEntity> CUCKOO_CLOCK = Create.registrate()
			.tileEntity("cuckoo_clock", (NonNullFunction<TileEntityType<CuckooClockTileEntity>, ? extends CuckooClockTileEntity>) CuckooClockTileEntity::new)
			.validBlocks(AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK)
			.renderer(() -> CuckooClockRenderer::new)
			.register();

	public static final TileEntityEntry<PumpTileEntity> MECHANICAL_PUMP = Create.registrate()
			.tileEntity("mechanical_pump", (NonNullFunction<TileEntityType<PumpTileEntity>, ? extends PumpTileEntity>) PumpTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PUMP)
			.renderer(() -> PumpRenderer::new)
			.register();

	public static final TileEntityEntry<FluidPipeTileEntity> FLUID_PIPE = Create.registrate()
			.tileEntity("fluid_pipe", (NonNullFunction<TileEntityType<FluidPipeTileEntity>, ? extends FluidPipeTileEntity>) FluidPipeTileEntity::new)
			.validBlocks(AllBlocks.FLUID_PIPE)
			.register();
	
	public static final TileEntityEntry<FluidTankTileEntity> FLUID_TANK = Create.registrate()
			.tileEntity("fluid_tank", (NonNullFunction<TileEntityType<FluidTankTileEntity>, ? extends FluidTankTileEntity>) FluidTankTileEntity::new)
			.validBlocks(AllBlocks.FLUID_TANK)
			.renderer(() -> FluidTankRenderer::new)
			.register();

	public static final TileEntityEntry<BeltTileEntity> BELT = Create.registrate()
			.tileEntity("belt", (NonNullFunction<TileEntityType<BeltTileEntity>, ? extends BeltTileEntity>) BeltTileEntity::new)
			.validBlocks(AllBlocks.BELT)
			.renderer(() -> BeltRenderer::new)
			.register();

	public static final TileEntityEntry<ChuteTileEntity> CHUTE = Create.registrate()
			.tileEntity("chute", (NonNullFunction<TileEntityType<ChuteTileEntity>, ? extends ChuteTileEntity>) ChuteTileEntity::new)
			.validBlocks(AllBlocks.CHUTE)
			.renderer(() -> ChuteRenderer::new)
			.register();

	public static final TileEntityEntry<BeltTunnelTileEntity> ANDESITE_TUNNEL = Create.registrate()
			.tileEntity("andesite_tunnel", (NonNullFunction<TileEntityType<BeltTunnelTileEntity>, ? extends BeltTunnelTileEntity>) BeltTunnelTileEntity::new)
			.validBlocks(AllBlocks.ANDESITE_TUNNEL)
			.renderer(() -> BeltTunnelRenderer::new)
			.register();

	public static final TileEntityEntry<BrassTunnelTileEntity> BRASS_TUNNEL = Create.registrate()
			.tileEntity("brass_tunnel", (NonNullFunction<TileEntityType<BrassTunnelTileEntity>, ? extends BrassTunnelTileEntity>) BrassTunnelTileEntity::new)
			.validBlocks(AllBlocks.BRASS_TUNNEL)
			.renderer(() -> BeltTunnelRenderer::new)
			.register();

	public static final TileEntityEntry<ArmTileEntity> MECHANICAL_ARM = Create.registrate()
			.tileEntity("mechanical_arm", (NonNullFunction<TileEntityType<ArmTileEntity>, ? extends ArmTileEntity>) ArmTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_ARM)
			.renderer(() -> ArmRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalPistonTileEntity> MECHANICAL_PISTON = Create.registrate()
			.tileEntity("mechanical_piston", (NonNullFunction<TileEntityType<MechanicalPistonTileEntity>, ? extends MechanicalPistonTileEntity>) MechanicalPistonTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.renderer(() -> MechanicalPistonRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalBearingTileEntity> MECHANICAL_BEARING = Create.registrate()
			.tileEntity("mechanical_bearing", (NonNullFunction<TileEntityType<MechanicalBearingTileEntity>, ? extends MechanicalBearingTileEntity>) MechanicalBearingTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_BEARING)
			.renderer(() -> BearingRenderer::new)
			.register();

	public static final TileEntityEntry<ClockworkBearingTileEntity> CLOCKWORK_BEARING = Create.registrate()
			.tileEntity("clockwork_bearing", (NonNullFunction<TileEntityType<ClockworkBearingTileEntity>, ? extends ClockworkBearingTileEntity>) ClockworkBearingTileEntity::new)
			.validBlocks(AllBlocks.CLOCKWORK_BEARING)
			.renderer(() -> BearingRenderer::new)
			.register();

	public static final TileEntityEntry<PulleyTileEntity> ROPE_PULLEY = Create.registrate()
			.tileEntity("rope_pulley", (NonNullFunction<TileEntityType<PulleyTileEntity>, ? extends PulleyTileEntity>) PulleyTileEntity::new)
			.validBlocks(AllBlocks.ROPE_PULLEY)
			.renderer(() -> PulleyRenderer::new)
			.register();

	public static final TileEntityEntry<ChassisTileEntity> CHASSIS = Create.registrate()
			.tileEntity("chassis", (NonNullFunction<TileEntityType<ChassisTileEntity>, ? extends ChassisTileEntity>) ChassisTileEntity::new)
			.validBlocks(AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<DrillTileEntity> DRILL = Create.registrate()
			.tileEntity("drill", (NonNullFunction<TileEntityType<DrillTileEntity>, ? extends DrillTileEntity>) DrillTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_DRILL)
			.renderer(() -> DrillRenderer::new)
			.register();

	public static final TileEntityEntry<SawTileEntity> SAW = Create.registrate()
			.tileEntity("saw", (NonNullFunction<TileEntityType<SawTileEntity>, ? extends SawTileEntity>) SawTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_SAW)
			.renderer(() -> SawRenderer::new)
			.register();

	public static final TileEntityEntry<HarvesterTileEntity> HARVESTER = Create.registrate()
			.tileEntity("harvester", (NonNullFunction<TileEntityType<HarvesterTileEntity>, ? extends HarvesterTileEntity>) HarvesterTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_HARVESTER)
			.renderer(() -> HarvesterRenderer::new)
			.register();

	public static final TileEntityEntry<FlywheelTileEntity> FLYWHEEL = Create.registrate()
			.tileEntity("flywheel", (NonNullFunction<TileEntityType<FlywheelTileEntity>, ? extends FlywheelTileEntity>) FlywheelTileEntity::new)
			.validBlocks(AllBlocks.FLYWHEEL)
			.renderer(() -> FlywheelRenderer::new)
			.register();

	public static final TileEntityEntry<FurnaceEngineTileEntity> FURNACE_ENGINE = Create.registrate()
			.tileEntity("furnace_engine", (NonNullFunction<TileEntityType<FurnaceEngineTileEntity>, ? extends FurnaceEngineTileEntity>) FurnaceEngineTileEntity::new)
			.validBlocks(AllBlocks.FURNACE_ENGINE)
			.renderer(() -> EngineRenderer::new)
			.register();

	public static final TileEntityEntry<MillstoneTileEntity> MILLSTONE = Create.registrate()
			.tileEntity("millstone", (NonNullFunction<TileEntityType<MillstoneTileEntity>, ? extends MillstoneTileEntity>) MillstoneTileEntity::new)
			.validBlocks(AllBlocks.MILLSTONE)
			.renderer(() -> MillstoneRenderer::new)
			.register();

	public static final TileEntityEntry<CrushingWheelTileEntity> CRUSHING_WHEEL = Create.registrate()
			.tileEntity("crushing_wheel", (NonNullFunction<TileEntityType<CrushingWheelTileEntity>, ? extends CrushingWheelTileEntity>) CrushingWheelTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<CrushingWheelControllerTileEntity> CRUSHING_WHEEL_CONTROLLER = Create.registrate()
			.tileEntity("crushing_wheel_controller", (NonNullFunction<TileEntityType<CrushingWheelControllerTileEntity>, ? extends CrushingWheelControllerTileEntity>) CrushingWheelControllerTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL_CONTROLLER)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<WaterWheelTileEntity> WATER_WHEEL = Create.registrate()
			.tileEntity("water_wheel", (NonNullFunction<TileEntityType<WaterWheelTileEntity>, ? extends WaterWheelTileEntity>) WaterWheelTileEntity::new)
			.validBlocks(AllBlocks.WATER_WHEEL)
			.renderer(() -> KineticTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalPressTileEntity> MECHANICAL_PRESS = Create.registrate()
			.tileEntity("mechanical_press", (NonNullFunction<TileEntityType<MechanicalPressTileEntity>, ? extends MechanicalPressTileEntity>) MechanicalPressTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_PRESS)
			.renderer(() -> MechanicalPressRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalMixerTileEntity> MECHANICAL_MIXER = Create.registrate()
			.tileEntity("mechanical_mixer", (NonNullFunction<TileEntityType<MechanicalMixerTileEntity>, ? extends MechanicalMixerTileEntity>) MechanicalMixerTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_MIXER)
			.renderer(() -> MechanicalMixerRenderer::new)
			.register();

	public static final TileEntityEntry<DeployerTileEntity> DEPLOYER = Create.registrate()
			.tileEntity("deployer", (NonNullFunction<TileEntityType<DeployerTileEntity>, ? extends DeployerTileEntity>) DeployerTileEntity::new)
			.validBlocks(AllBlocks.DEPLOYER)
			.renderer(() -> DeployerRenderer::new)
			.register();

	public static final TileEntityEntry<BasinTileEntity> BASIN = Create.registrate()
			.tileEntity("basin", (NonNullFunction<TileEntityType<BasinTileEntity>, ? extends BasinTileEntity>) BasinTileEntity::new)
			.validBlocks(AllBlocks.BASIN)
			.renderer(() -> BasinRenderer::new)
			.register();

	public static final TileEntityEntry<BlazeBurnerTileEntity> HEATER = Create.registrate()
			.tileEntity("blaze_heater", (NonNullFunction<TileEntityType<BlazeBurnerTileEntity>, ? extends BlazeBurnerTileEntity>) BlazeBurnerTileEntity::new)
			.validBlocks(AllBlocks.BLAZE_BURNER)
			.renderer(() -> BlazeBurnerRenderer::new)
			.register();

	public static final TileEntityEntry<MechanicalCrafterTileEntity> MECHANICAL_CRAFTER = Create.registrate()
			.tileEntity("mechanical_crafter", (NonNullFunction<TileEntityType<MechanicalCrafterTileEntity>, ? extends MechanicalCrafterTileEntity>) MechanicalCrafterTileEntity::new)
			.validBlocks(AllBlocks.MECHANICAL_CRAFTER)
			.renderer(() -> MechanicalCrafterRenderer::new)
			.register();

	public static final TileEntityEntry<SequencedGearshiftTileEntity> SEQUENCED_GEARSHIFT = Create.registrate()
			.tileEntity("sequenced_gearshift", (NonNullFunction<TileEntityType<SequencedGearshiftTileEntity>, ? extends SequencedGearshiftTileEntity>) SequencedGearshiftTileEntity::new)
			.validBlocks(AllBlocks.SEQUENCED_GEARSHIFT)
			.renderer(() -> SplitShaftRenderer::new)
			.register();

	public static final TileEntityEntry<SpeedControllerTileEntity> ROTATION_SPEED_CONTROLLER = Create.registrate()
			.tileEntity("rotation_speed_controller", (NonNullFunction<TileEntityType<SpeedControllerTileEntity>, ? extends SpeedControllerTileEntity>) SpeedControllerTileEntity::new)
			.validBlocks(AllBlocks.ROTATION_SPEED_CONTROLLER)
			.renderer(() -> SpeedControllerRenderer::new)
			.register();

	public static final TileEntityEntry<SpeedGaugeTileEntity> SPEEDOMETER = Create.registrate()
			.tileEntity("speedometer", (NonNullFunction<TileEntityType<SpeedGaugeTileEntity>, ? extends SpeedGaugeTileEntity>) SpeedGaugeTileEntity::new)
			.validBlocks(AllBlocks.SPEEDOMETER)
			.renderer(() -> GaugeRenderer::speed)
			.register();

	public static final TileEntityEntry<StressGaugeTileEntity> STRESSOMETER = Create.registrate()
			.tileEntity("stressometer", (NonNullFunction<TileEntityType<StressGaugeTileEntity>, ? extends StressGaugeTileEntity>) StressGaugeTileEntity::new)
			.validBlocks(AllBlocks.STRESSOMETER)
			.renderer(() -> GaugeRenderer::stress)
			.register();

	public static final TileEntityEntry<AnalogLeverTileEntity> ANALOG_LEVER = Create.registrate()
			.tileEntity("analog_lever", (NonNullFunction<TileEntityType<AnalogLeverTileEntity>, ? extends AnalogLeverTileEntity>) AnalogLeverTileEntity::new)
			.validBlocks(AllBlocks.ANALOG_LEVER)
			.renderer(() -> AnalogLeverRenderer::new)
			.register();

	public static final TileEntityEntry<CartAssemblerTileEntity> CART_ASSEMBLER = Create.registrate()
			.tileEntity("cart_assembler", (NonNullFunction<TileEntityType<CartAssemblerTileEntity>, ? extends CartAssemblerTileEntity>) CartAssemblerTileEntity::new)
			.validBlocks(AllBlocks.CART_ASSEMBLER)
			//.renderer(() -> renderer)
			.register();

	// Logistics
	public static final TileEntityEntry<RedstoneLinkTileEntity> REDSTONE_LINK = Create.registrate()
			.tileEntity("redstone_link", (NonNullFunction<TileEntityType<RedstoneLinkTileEntity>, ? extends RedstoneLinkTileEntity>) RedstoneLinkTileEntity::new)
			.validBlocks(AllBlocks.REDSTONE_LINK)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<NixieTubeTileEntity> NIXIE_TUBE = Create.registrate()
			.tileEntity("nixie_tube", (NonNullFunction<TileEntityType<NixieTubeTileEntity>, ? extends NixieTubeTileEntity>) NixieTubeTileEntity::new)
			.validBlocks(AllBlocks.NIXIE_TUBE)
			.renderer(() -> NixieTubeRenderer::new)
			.register();

	public static final TileEntityEntry<StockpileSwitchTileEntity> STOCKPILE_SWITCH = Create.registrate()
			.tileEntity("stockpile_switch", (NonNullFunction<TileEntityType<StockpileSwitchTileEntity>, ? extends StockpileSwitchTileEntity>) StockpileSwitchTileEntity::new)
			.validBlocks(AllBlocks.STOCKPILE_SWITCH)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<AdjustableCrateTileEntity> ADJUSTABLE_CRATE = Create.registrate()
			.tileEntity("adjustable_crate", (NonNullFunction<TileEntityType<AdjustableCrateTileEntity>, ? extends AdjustableCrateTileEntity>) AdjustableCrateTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_CRATE)
			//.renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<CreativeCrateTileEntity> CREATIVE_CRATE = Create.registrate()
			.tileEntity("creative_crate", (NonNullFunction<TileEntityType<CreativeCrateTileEntity>, ? extends CreativeCrateTileEntity>) CreativeCrateTileEntity::new)
			.validBlocks(AllBlocks.CREATIVE_CRATE)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<DepotTileEntity> DEPOT = Create.registrate()
			.tileEntity("depot", (NonNullFunction<TileEntityType<DepotTileEntity>, ? extends DepotTileEntity>) DepotTileEntity::new)
			.validBlocks(AllBlocks.DEPOT)
			.renderer(() -> DepotRenderer::new)
			.register();

	public static final TileEntityEntry<FunnelTileEntity> FUNNEL = Create.registrate()
			.tileEntity("funnel", (NonNullFunction<TileEntityType<FunnelTileEntity>, ? extends FunnelTileEntity>) FunnelTileEntity::new)
			.validBlocks(AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.BRASS_CHUTE_FUNNEL, AllBlocks.ANDESITE_FUNNEL, AllBlocks.ANDESITE_BELT_FUNNEL, AllBlocks.ANDESITE_CHUTE_FUNNEL)
			.renderer(() -> FunnelRenderer::new)
			.register();

	public static final TileEntityEntry<PackagerTileEntity> PACKAGER = Create.registrate()
			.tileEntity("packager", (NonNullFunction<TileEntityType<PackagerTileEntity>, ? extends PackagerTileEntity>) PackagerTileEntity::new)
			.validBlocks(AllBlocks.PACKAGER)
			.renderer(() -> PackagerRenderer::new)
			.register();

	public static final TileEntityEntry<ExtractorTileEntity> EXTRACTOR = Create.registrate()
			.tileEntity("extractor", (NonNullFunction<TileEntityType<ExtractorTileEntity>, ? extends ExtractorTileEntity>) ExtractorTileEntity::new)
			.validBlocks(AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<LinkedExtractorTileEntity> LINKED_EXTRACTOR = Create.registrate()
			.tileEntity("linked_extractor", (NonNullFunction<TileEntityType<LinkedExtractorTileEntity>, ? extends LinkedExtractorTileEntity>) LinkedExtractorTileEntity::new)
			.validBlocks(AllBlocks.LINKED_EXTRACTOR, AllBlocks.VERTICAL_LINKED_EXTRACTOR)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<TransposerTileEntity> TRANSPOSER = Create.registrate()
			.tileEntity("transposer", (NonNullFunction<TileEntityType<TransposerTileEntity>, ? extends TransposerTileEntity>) TransposerTileEntity::new)
			.validBlocks(AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<LinkedTransposerTileEntity> LINKED_TRANSPOSER = Create.registrate()
			.tileEntity("linked_transposer", (NonNullFunction<TileEntityType<LinkedTransposerTileEntity>, ? extends LinkedTransposerTileEntity>) LinkedTransposerTileEntity::new)
			.validBlocks(AllBlocks.LINKED_TRANSPOSER, AllBlocks.VERTICAL_LINKED_TRANSPOSER)
			.renderer(() -> SmartTileEntityRenderer::new)
			.register();

	public static final TileEntityEntry<BeltObserverTileEntity> BELT_OBSERVER = Create.registrate()
			.tileEntity("belt_observer", (NonNullFunction<TileEntityType<BeltObserverTileEntity>, ? extends BeltObserverTileEntity>) BeltObserverTileEntity::new)
			.validBlocks(AllBlocks.BELT_OBSERVER)
			.renderer(() -> BeltObserverRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustableRepeaterTileEntity> ADJUSTABLE_REPEATER = Create.registrate()
			.tileEntity("adjustable_repeater", (NonNullFunction<TileEntityType<AdjustableRepeaterTileEntity>, ? extends AdjustableRepeaterTileEntity>) AdjustableRepeaterTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_REPEATER)
			.renderer(() -> AdjustableRepeaterRenderer::new)
			.register();

	public static final TileEntityEntry<AdjustablePulseRepeaterTileEntity> ADJUSTABLE_PULSE_REPEATER = Create.registrate()
			.tileEntity("adjustable_pulse_repeater", (NonNullFunction<TileEntityType<AdjustablePulseRepeaterTileEntity>, ? extends AdjustablePulseRepeaterTileEntity>) AdjustablePulseRepeaterTileEntity::new)
			.validBlocks(AllBlocks.ADJUSTABLE_PULSE_REPEATER)
			.renderer(() -> AdjustableRepeaterRenderer::new)
			.register();

	public static void register() {}
}
