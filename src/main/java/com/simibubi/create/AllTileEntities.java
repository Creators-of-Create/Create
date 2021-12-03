package com.simibubi.create;

import com.simibubi.create.content.contraptions.base.CutoutRotatingInstance;
import com.simibubi.create.content.contraptions.base.HalfShaftInstance;
import com.simibubi.create.content.contraptions.base.HorizontalHalfShaftInstance;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.base.ShaftlessCogInstance;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillInstance;
import com.simibubi.create.content.contraptions.components.actors.DrillRenderer;
import com.simibubi.create.content.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.content.contraptions.components.actors.HarvesterRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterTileEntity;
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
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineInstance;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineTileEntity;
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
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.content.contraptions.relays.encased.ShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftlessCogRenderer;
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
import com.simibubi.create.content.logistics.block.vault.VaultTileEntity;
import com.simibubi.create.content.logistics.item.LecternControllerRenderer;
import com.simibubi.create.content.logistics.item.LecternControllerTileEntity;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonInstance;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.tterrag.registrate.util.entry.TileEntityEntry;

public class AllTileEntities {

	// Schematics
	public static final TileEntityEntry<SchematicannonTileEntity> SCHEMATICANNON = Create.registrate()
		.tileEntity("schematicannon", SchematicannonTileEntity::new)
		.instance(() -> SchematicannonInstance::new)
		.validBlocks(AllBlocks.SCHEMATICANNON)
		.renderer(() -> SchematicannonRenderer::new)
		.register();

	public static final TileEntityEntry<SchematicTableTileEntity> SCHEMATIC_TABLE = Create.registrate()
		.tileEntity("schematic_table", SchematicTableTileEntity::new)
		.validBlocks(AllBlocks.SCHEMATIC_TABLE)
		.register();

	// Kinetics
	public static final TileEntityEntry<BracketedKineticTileEntity> BRACKETED_KINETIC = Create.registrate()
		.tileEntity("simple_kinetic", BracketedKineticTileEntity::new)
		.instance(() -> BracketedKineticTileInstance::new)
		.validBlocks(AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL)
		.renderer(() -> BracketedKineticTileRenderer::new)
		.register();

	public static final TileEntityEntry<CreativeMotorTileEntity> MOTOR = Create.registrate()
		.tileEntity("motor", CreativeMotorTileEntity::new)
		.instance(() -> HalfShaftInstance::new)
		.validBlocks(AllBlocks.CREATIVE_MOTOR)
		.renderer(() -> CreativeMotorRenderer::new)
		.register();

	public static final TileEntityEntry<GearboxTileEntity> GEARBOX = Create.registrate()
		.tileEntity("gearbox", GearboxTileEntity::new)
		.instance(() -> GearboxInstance::new)
		.validBlocks(AllBlocks.GEARBOX)
		.renderer(() -> GearboxRenderer::new)
		.register();

	public static final TileEntityEntry<KineticTileEntity> ENCASED_SHAFT = Create.registrate()
		.tileEntity("encased_shaft", KineticTileEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_SHAFT, AllBlocks.BRASS_ENCASED_SHAFT, AllBlocks.ENCASED_CHAIN_DRIVE)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final TileEntityEntry<SimpleKineticTileEntity> ENCASED_COGWHEEL = Create.registrate()
		.tileEntity("encased_cogwheel", SimpleKineticTileEntity::new)
		.instance(() -> ShaftlessCogInstance::small)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_COGWHEEL, AllBlocks.BRASS_ENCASED_COGWHEEL)
		.renderer(() -> ShaftlessCogRenderer::small)
		.register();

	public static final TileEntityEntry<SimpleKineticTileEntity> ENCASED_LARGE_COGWHEEL = Create.registrate()
		.tileEntity("encased_large_cogwheel", SimpleKineticTileEntity::new)
		.instance(() -> ShaftlessCogInstance::large)
		.validBlocks(AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL, AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL)
		.renderer(() -> ShaftlessCogRenderer::large)
		.register();

	public static final TileEntityEntry<AdjustablePulleyTileEntity> ADJUSTABLE_PULLEY = Create.registrate()
		.tileEntity("adjustable_pulley", AdjustablePulleyTileEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
		.renderer(() -> ShaftRenderer::new)
		.register();

	public static final TileEntityEntry<EncasedFanTileEntity> ENCASED_FAN = Create.registrate()
		.tileEntity("encased_fan", EncasedFanTileEntity::new)
		.instance(() -> FanInstance::new)
		.validBlocks(AllBlocks.ENCASED_FAN)
		.renderer(() -> EncasedFanRenderer::new)
		.register();

	public static final TileEntityEntry<NozzleTileEntity> NOZZLE = Create.registrate()
		.tileEntity("nozzle", NozzleTileEntity::new)
		.validBlocks(AllBlocks.NOZZLE)
		// .renderer(() -> renderer)
		.register();

	public static final TileEntityEntry<ClutchTileEntity> CLUTCH = Create.registrate()
		.tileEntity("clutch", ClutchTileEntity::new)
		.instance(() -> SplitShaftInstance::new)
		.validBlocks(AllBlocks.CLUTCH)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final TileEntityEntry<GearshiftTileEntity> GEARSHIFT = Create.registrate()
		.tileEntity("gearshift", GearshiftTileEntity::new)
		.instance(() -> SplitShaftInstance::new)
		.validBlocks(AllBlocks.GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final TileEntityEntry<TurntableTileEntity> TURNTABLE = Create.registrate()
		.tileEntity("turntable", TurntableTileEntity::new)
		.instance(() -> SingleRotatingInstance::new)
		.validBlocks(AllBlocks.TURNTABLE)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<HandCrankTileEntity> HAND_CRANK = Create.registrate()
			.tileEntity("hand_crank", HandCrankTileEntity::new)
			.instance(() -> HandCrankInstance::new)
			.validBlocks(AllBlocks.HAND_CRANK, AllBlocks.COPPER_VALVE_HANDLE)
			.validBlocks(AllBlocks.DYED_VALVE_HANDLES.toArray())
		.renderer(() -> HandCrankRenderer::new)
		.register();

	public static final TileEntityEntry<CuckooClockTileEntity> CUCKOO_CLOCK = Create.registrate()
		.tileEntity("cuckoo_clock", CuckooClockTileEntity::new)
		.instance(() -> HorizontalHalfShaftInstance::new)
		.validBlocks(AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK)
		.renderer(() -> CuckooClockRenderer::new)
		.register();

	public static final TileEntityEntry<GantryShaftTileEntity> GANTRY_SHAFT = Create.registrate()
		.tileEntity("gantry_shaft", GantryShaftTileEntity::new)
		.instance(() -> SingleRotatingInstance::new)
		.validBlocks(AllBlocks.GANTRY_SHAFT)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<GantryCarriageTileEntity> GANTRY_PINION = Create.registrate()
		.tileEntity("gantry_pinion", GantryCarriageTileEntity::new)
		.instance(() -> GantryCarriageInstance::new)
		.validBlocks(AllBlocks.GANTRY_CARRIAGE)
		.renderer(() -> GantryCarriageRenderer::new)
		.register();

	public static final TileEntityEntry<PumpTileEntity> MECHANICAL_PUMP = Create.registrate()
		.tileEntity("mechanical_pump", PumpTileEntity::new)
		.instance(() -> PumpCogInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PUMP)
		.renderer(() -> PumpRenderer::new)
		.register();

	public static final TileEntityEntry<SmartFluidPipeTileEntity> SMART_FLUID_PIPE = Create.registrate()
		.tileEntity("smart_fluid_pipe", SmartFluidPipeTileEntity::new)
		.validBlocks(AllBlocks.SMART_FLUID_PIPE)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<FluidPipeTileEntity> FLUID_PIPE = Create.registrate()
		.tileEntity("fluid_pipe", FluidPipeTileEntity::new)
		.validBlocks(AllBlocks.FLUID_PIPE)
		.register();

	public static final TileEntityEntry<FluidPipeTileEntity> ENCASED_FLUID_PIPE = Create.registrate()
		.tileEntity("encased_fluid_pipe", FluidPipeTileEntity::new)
		.validBlocks(AllBlocks.ENCASED_FLUID_PIPE)
		.register();

	public static final TileEntityEntry<StraightPipeTileEntity> GLASS_FLUID_PIPE = Create.registrate()
		.tileEntity("glass_fluid_pipe", StraightPipeTileEntity::new)
		.validBlocks(AllBlocks.GLASS_FLUID_PIPE)
		.renderer(() -> TransparentStraightPipeRenderer::new)
		.register();

	public static final TileEntityEntry<FluidValveTileEntity> FLUID_VALVE = Create.registrate()
		.tileEntity("fluid_valve", FluidValveTileEntity::new)
		.instance(() -> FluidValveInstance::new)
		.validBlocks(AllBlocks.FLUID_VALVE)
		.renderer(() -> FluidValveRenderer::new)
		.register();

	public static final TileEntityEntry<FluidTankTileEntity> FLUID_TANK = Create.registrate()
		.tileEntity("fluid_tank", FluidTankTileEntity::new)
		.validBlocks(AllBlocks.FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final TileEntityEntry<CreativeFluidTankTileEntity> CREATIVE_FLUID_TANK = Create.registrate()
		.tileEntity("creative_fluid_tank", CreativeFluidTankTileEntity::new)
		.validBlocks(AllBlocks.CREATIVE_FLUID_TANK)
		.renderer(() -> FluidTankRenderer::new)
		.register();

	public static final TileEntityEntry<HosePulleyTileEntity> HOSE_PULLEY = Create.registrate()
		.tileEntity("hose_pulley", HosePulleyTileEntity::new)
		.instance(() -> HosePulleyInstance::new)
		.validBlocks(AllBlocks.HOSE_PULLEY)
		.renderer(() -> HosePulleyRenderer::new)
		.register();

	public static final TileEntityEntry<SpoutTileEntity> SPOUT = Create.registrate()
		.tileEntity("spout", SpoutTileEntity::new)
		.validBlocks(AllBlocks.SPOUT)
		.renderer(() -> SpoutRenderer::new)
		.register();

	public static final TileEntityEntry<ItemDrainTileEntity> ITEM_DRAIN = Create.registrate()
		.tileEntity("item_drain", ItemDrainTileEntity::new)
		.validBlocks(AllBlocks.ITEM_DRAIN)
		.renderer(() -> ItemDrainRenderer::new)
		.register();

	public static final TileEntityEntry<BeltTileEntity> BELT = Create.registrate()
		.tileEntity("belt", BeltTileEntity::new)
		.instance(() -> BeltInstance::new)
		.validBlocks(AllBlocks.BELT)
		.renderer(() -> BeltRenderer::new)
		.register();

	public static final TileEntityEntry<ChuteTileEntity> CHUTE = Create.registrate()
		.tileEntity("chute", ChuteTileEntity::new)
		.validBlocks(AllBlocks.CHUTE)
		.renderer(() -> ChuteRenderer::new)
		.register();

	public static final TileEntityEntry<SmartChuteTileEntity> SMART_CHUTE = Create.registrate()
		.tileEntity("smart_chute", SmartChuteTileEntity::new)
		.validBlocks(AllBlocks.SMART_CHUTE)
		.renderer(() -> SmartChuteRenderer::new)
		.register();

	public static final TileEntityEntry<BeltTunnelTileEntity> ANDESITE_TUNNEL = Create.registrate()
		.tileEntity("andesite_tunnel", BeltTunnelTileEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.ANDESITE_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final TileEntityEntry<BrassTunnelTileEntity> BRASS_TUNNEL = Create.registrate()
		.tileEntity("brass_tunnel", BrassTunnelTileEntity::new)
		.instance(() -> BeltTunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_TUNNEL)
		.renderer(() -> BeltTunnelRenderer::new)
		.register();

	public static final TileEntityEntry<ArmTileEntity> MECHANICAL_ARM = Create.registrate()
		.tileEntity("mechanical_arm", ArmTileEntity::new)
		.instance(() -> ArmInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_ARM)
		.renderer(() -> ArmRenderer::new)
		.register();

	public static final TileEntityEntry<VaultTileEntity> ITEM_VAULT = Create.registrate()
		.tileEntity("item_vault", VaultTileEntity::new)
		.validBlocks(AllBlocks.ITEM_VAULT)
		.register();

	public static final TileEntityEntry<MechanicalPistonTileEntity> MECHANICAL_PISTON = Create.registrate()
		.tileEntity("mechanical_piston", MechanicalPistonTileEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
		.renderer(() -> MechanicalPistonRenderer::new)
		.register();

	public static final TileEntityEntry<WindmillBearingTileEntity> WINDMILL_BEARING = Create.registrate()
		.tileEntity("windmill_bearing", WindmillBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.WINDMILL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final TileEntityEntry<MechanicalBearingTileEntity> MECHANICAL_BEARING = Create.registrate()
		.tileEntity("mechanical_bearing", MechanicalBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final TileEntityEntry<ClockworkBearingTileEntity> CLOCKWORK_BEARING = Create.registrate()
		.tileEntity("clockwork_bearing", ClockworkBearingTileEntity::new)
		.instance(() -> BearingInstance::new)
		.validBlocks(AllBlocks.CLOCKWORK_BEARING)
		.renderer(() -> BearingRenderer::new)
		.register();

	public static final TileEntityEntry<PulleyTileEntity> ROPE_PULLEY = Create.registrate()
		.tileEntity("rope_pulley", PulleyTileEntity::new)
		.instance(() -> RopePulleyInstance::new)
		.validBlocks(AllBlocks.ROPE_PULLEY)
		.renderer(() -> PulleyRenderer::new)
		.register();

	public static final TileEntityEntry<ChassisTileEntity> CHASSIS = Create.registrate()
		.tileEntity("chassis", ChassisTileEntity::new)
		.validBlocks(AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
		// .renderer(() -> renderer)
		.register();

	public static final TileEntityEntry<StickerTileEntity> STICKER = Create.registrate()
		.tileEntity("sticker", StickerTileEntity::new)
		.instance(() -> StickerInstance::new)
		.validBlocks(AllBlocks.STICKER)
		.renderer(() -> StickerRenderer::new)
		.register();

	public static final TileEntityEntry<DrillTileEntity> DRILL = Create.registrate()
		.tileEntity("drill", DrillTileEntity::new)
		.instance(() -> DrillInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_DRILL)
		.renderer(() -> DrillRenderer::new)
		.register();

	public static final TileEntityEntry<SawTileEntity> SAW = Create.registrate()
		.tileEntity("saw", SawTileEntity::new)
		.instance(() -> SawInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_SAW)
		.renderer(() -> SawRenderer::new)
		.register();

	public static final TileEntityEntry<HarvesterTileEntity> HARVESTER = Create.registrate()
		.tileEntity("harvester", HarvesterTileEntity::new)
		.validBlocks(AllBlocks.MECHANICAL_HARVESTER)
		.renderer(() -> HarvesterRenderer::new)
		.register();

	public static final TileEntityEntry<PortableItemInterfaceTileEntity> PORTABLE_STORAGE_INTERFACE =
		Create.registrate()
			.tileEntity("portable_storage_interface", PortableItemInterfaceTileEntity::new)
			.validBlocks(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.renderer(() -> PortableStorageInterfaceRenderer::new)
			.register();

	public static final TileEntityEntry<PortableFluidInterfaceTileEntity> PORTABLE_FLUID_INTERFACE = Create.registrate()
		.tileEntity("portable_fluid_interface", PortableFluidInterfaceTileEntity::new)
		.validBlocks(AllBlocks.PORTABLE_FLUID_INTERFACE)
		.renderer(() -> PortableStorageInterfaceRenderer::new)
		.register();

	public static final TileEntityEntry<FlywheelTileEntity> FLYWHEEL = Create.registrate()
		.tileEntity("flywheel", FlywheelTileEntity::new)
		.instance(() -> FlyWheelInstance::new)
		.validBlocks(AllBlocks.FLYWHEEL)
		.renderer(() -> FlywheelRenderer::new)
		.register();

	public static final TileEntityEntry<FurnaceEngineTileEntity> FURNACE_ENGINE = Create.registrate()
		.tileEntity("furnace_engine", FurnaceEngineTileEntity::new)
		.instance(() -> EngineInstance::new)
		.validBlocks(AllBlocks.FURNACE_ENGINE)
		.renderer(() -> EngineRenderer::new)
		.register();

	public static final TileEntityEntry<MillstoneTileEntity> MILLSTONE = Create.registrate()
		.tileEntity("millstone", MillstoneTileEntity::new)
		.instance(() -> MillStoneCogInstance::new)
		.validBlocks(AllBlocks.MILLSTONE)
		.renderer(() -> MillstoneRenderer::new)
		.register();

	public static final TileEntityEntry<CrushingWheelTileEntity> CRUSHING_WHEEL = Create.registrate()
		.tileEntity("crushing_wheel", CrushingWheelTileEntity::new)
		.instance(() -> CutoutRotatingInstance::new)
		.validBlocks(AllBlocks.CRUSHING_WHEEL)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<CrushingWheelControllerTileEntity> CRUSHING_WHEEL_CONTROLLER =
		Create.registrate()
			.tileEntity("crushing_wheel_controller", CrushingWheelControllerTileEntity::new)
			.validBlocks(AllBlocks.CRUSHING_WHEEL_CONTROLLER)
			// .renderer(() -> renderer)
			.register();

	public static final TileEntityEntry<WaterWheelTileEntity> WATER_WHEEL = Create.registrate()
		.tileEntity("water_wheel", WaterWheelTileEntity::new)
		.instance(() -> CutoutRotatingInstance::new)
		.validBlocks(AllBlocks.WATER_WHEEL)
		.renderer(() -> KineticTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<MechanicalPressTileEntity> MECHANICAL_PRESS = Create.registrate()
		.tileEntity("mechanical_press", MechanicalPressTileEntity::new)
		.instance(() -> PressInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_PRESS)
		.renderer(() -> MechanicalPressRenderer::new)
		.register();

	public static final TileEntityEntry<MechanicalMixerTileEntity> MECHANICAL_MIXER = Create.registrate()
		.tileEntity("mechanical_mixer", MechanicalMixerTileEntity::new)
		.instance(() -> MixerInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_MIXER)
		.renderer(() -> MechanicalMixerRenderer::new)
		.register();

	public static final TileEntityEntry<DeployerTileEntity> DEPLOYER = Create.registrate()
		.tileEntity("deployer", DeployerTileEntity::new)
		.instance(() -> DeployerInstance::new)
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
		.instance(() -> MechanicalCrafterInstance::new)
		.validBlocks(AllBlocks.MECHANICAL_CRAFTER)
		.renderer(() -> MechanicalCrafterRenderer::new)
		.register();

	public static final TileEntityEntry<SequencedGearshiftTileEntity> SEQUENCED_GEARSHIFT = Create.registrate()
		.tileEntity("sequenced_gearshift", SequencedGearshiftTileEntity::new)
		.instance(() -> SplitShaftInstance::new)
		.validBlocks(AllBlocks.SEQUENCED_GEARSHIFT)
		.renderer(() -> SplitShaftRenderer::new)
		.register();

	public static final TileEntityEntry<SpeedControllerTileEntity> ROTATION_SPEED_CONTROLLER = Create.registrate()
		.tileEntity("rotation_speed_controller", SpeedControllerTileEntity::new)
		.instance(() -> ShaftInstance::new)
		.validBlocks(AllBlocks.ROTATION_SPEED_CONTROLLER)
		.renderer(() -> SpeedControllerRenderer::new)
		.register();

	public static final TileEntityEntry<SpeedGaugeTileEntity> SPEEDOMETER = Create.registrate()
		.tileEntity("speedometer", SpeedGaugeTileEntity::new)
		.instance(() -> GaugeInstance.Speed::new)
		.validBlocks(AllBlocks.SPEEDOMETER)
		.renderer(() -> GaugeRenderer::speed)
		.register();

	public static final TileEntityEntry<StressGaugeTileEntity> STRESSOMETER = Create.registrate()
		.tileEntity("stressometer", StressGaugeTileEntity::new)
		.instance(() -> GaugeInstance.Stress::new)
		.validBlocks(AllBlocks.STRESSOMETER)
		.renderer(() -> GaugeRenderer::stress)
		.register();

	public static final TileEntityEntry<AnalogLeverTileEntity> ANALOG_LEVER = Create.registrate()
		.tileEntity("analog_lever", AnalogLeverTileEntity::new)
		.instance(() -> AnalogLeverInstance::new)
		.validBlocks(AllBlocks.ANALOG_LEVER)
		.renderer(() -> AnalogLeverRenderer::new)
		.register();

	public static final TileEntityEntry<CartAssemblerTileEntity> CART_ASSEMBLER = Create.registrate()
		.tileEntity("cart_assembler", CartAssemblerTileEntity::new)
		.validBlocks(AllBlocks.CART_ASSEMBLER)
		// .renderer(() -> renderer)
		.register();

	// Logistics
	public static final TileEntityEntry<RedstoneLinkTileEntity> REDSTONE_LINK = Create.registrate()
		.tileEntity("redstone_link", RedstoneLinkTileEntity::new)
		.validBlocks(AllBlocks.REDSTONE_LINK)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<NixieTubeTileEntity> NIXIE_TUBE = Create.registrate()
		.tileEntity("nixie_tube", NixieTubeTileEntity::new)
		.validBlocks(AllBlocks.ORANGE_NIXIE_TUBE)
		.validBlocks(AllBlocks.NIXIE_TUBES.toArray())
		.renderer(() -> NixieTubeRenderer::new)
		.register();

	public static final TileEntityEntry<StockpileSwitchTileEntity> STOCKPILE_SWITCH = Create.registrate()
		.tileEntity("stockpile_switch", StockpileSwitchTileEntity::new)
		.validBlocks(AllBlocks.STOCKPILE_SWITCH)
		.renderer(() -> SmartTileEntityRenderer::new)
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

	public static final TileEntityEntry<EjectorTileEntity> WEIGHTED_EJECTOR = Create.registrate()
		.tileEntity("weighted_ejector", EjectorTileEntity::new)
		.instance(() -> EjectorInstance::new)
		.validBlocks(AllBlocks.WEIGHTED_EJECTOR)
		.renderer(() -> EjectorRenderer::new)
		.register();

	public static final TileEntityEntry<FunnelTileEntity> FUNNEL = Create.registrate()
		.tileEntity("funnel", FunnelTileEntity::new)
		.instance(() -> FunnelInstance::new)
		.validBlocks(AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.ANDESITE_FUNNEL,
			AllBlocks.ANDESITE_BELT_FUNNEL)
		.renderer(() -> FunnelRenderer::new)
		.register();

	public static final TileEntityEntry<ContentObserverTileEntity> CONTENT_OBSERVER = Create.registrate()
		.tileEntity("content_observer", ContentObserverTileEntity::new)
		.validBlocks(AllBlocks.CONTENT_OBSERVER)
		.renderer(() -> SmartTileEntityRenderer::new)
		.register();

	public static final TileEntityEntry<PulseExtenderTileEntity> PULSE_EXTENDER = Create.registrate()
		.tileEntity("adjustable_repeater", PulseExtenderTileEntity::new)
		.instance(() -> BrassDiodeInstance::new)
		.validBlocks(AllBlocks.PULSE_EXTENDER)
		.renderer(() -> BrassDiodeRenderer::new)
		.register();

	public static final TileEntityEntry<PulseRepeaterTileEntity> PULSE_REPEATER =
		Create.registrate()
			.tileEntity("adjustable_pulse_repeater", PulseRepeaterTileEntity::new)
			.instance(() -> BrassDiodeInstance::new)
			.validBlocks(AllBlocks.PULSE_REPEATER)
			.renderer(() -> BrassDiodeRenderer::new)
			.register();

	public static final TileEntityEntry<LecternControllerTileEntity> LECTERN_CONTROLLER =
		Create.registrate()
			.tileEntity("lectern_controller", LecternControllerTileEntity::new)
			.validBlocks(AllBlocks.LECTERN_CONTROLLER)
			.renderer(() -> LecternControllerRenderer::new)
			.register();

	// Curiosities
	public static final TileEntityEntry<CopperBacktankTileEntity> COPPER_BACKTANK = Create.registrate()
		.tileEntity("copper_backtank", CopperBacktankTileEntity::new)
		.instance(() -> CopperBacktankInstance::new)
		.validBlocks(AllBlocks.COPPER_BACKTANK)
		.renderer(() -> CopperBacktankRenderer::new)
		.register();

	public static final TileEntityEntry<PeculiarBellTileEntity> PECULIAR_BELL = Create.registrate()
		.tileEntity("peculiar_bell", PeculiarBellTileEntity::new)
		.validBlocks(AllBlocks.PECULIAR_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final TileEntityEntry<HauntedBellTileEntity> HAUNTED_BELL = Create.registrate()
		.tileEntity("cursed_bell", HauntedBellTileEntity::new)
		.validBlocks(AllBlocks.HAUNTED_BELL)
		.renderer(() -> BellRenderer::new)
		.register();

	public static final TileEntityEntry<ToolboxTileEntity> TOOLBOX = Create.registrate()
		.tileEntity("toolbox", ToolboxTileEntity::new)
		.instance(() -> ToolBoxInstance::new)
		.validBlocks(AllBlocks.TOOLBOXES.toArray())
		.renderer(() -> ToolboxRenderer::new)
		.register();

	public static void register() {}
}
