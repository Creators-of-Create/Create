package com.simibubi.create;

import java.util.function.Function;

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
import com.simibubi.create.content.contraptions.fluids.FluidTankRenderer;
import com.simibubi.create.content.contraptions.fluids.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.PumpRenderer;
import com.simibubi.create.content.contraptions.fluids.PumpTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinRenderer;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
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
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class AllTileEntities {

	// Schematics
	public static final TileEntityEntry<SchematicannonTileEntity> SCHEMATICANNON =
		register("schematicannon", SchematicannonTileEntity::new, AllBlocks.SCHEMATICANNON);
	public static final TileEntityEntry<SchematicTableTileEntity> SCHEMATIC_TABLE =
		register("schematic_table", SchematicTableTileEntity::new, AllBlocks.SCHEMATIC_TABLE);

	// Kinetics
	public static final TileEntityEntry<SimpleKineticTileEntity> SIMPLE_KINETIC =
		register("simple_kinetic", SimpleKineticTileEntity::new, AllBlocks.SHAFT, AllBlocks.COGWHEEL,
			AllBlocks.LARGE_COGWHEEL, AllBlocks.ENCASED_SHAFT);
	public static final TileEntityEntry<CreativeMotorTileEntity> MOTOR =
		register("motor", CreativeMotorTileEntity::new, AllBlocks.CREATIVE_MOTOR);
	public static final TileEntityEntry<GearboxTileEntity> GEARBOX =
		register("gearbox", GearboxTileEntity::new, AllBlocks.GEARBOX);
	public static final TileEntityEntry<EncasedShaftTileEntity> ENCASED_SHAFT =
		register("encased_shaft", EncasedShaftTileEntity::new, AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT);
	public static final TileEntityEntry<AdjustablePulleyTileEntity> ADJUSTABLE_PULLEY =
		register("adjustable_pulley", AdjustablePulleyTileEntity::new, AllBlocks.ADJUSTABLE_PULLEY);
	public static final TileEntityEntry<EncasedFanTileEntity> ENCASED_FAN =
		register("encased_fan", EncasedFanTileEntity::new, AllBlocks.ENCASED_FAN);
	public static final TileEntityEntry<NozzleTileEntity> NOZZLE =
		register("nozzle", NozzleTileEntity::new, AllBlocks.NOZZLE);
	public static final TileEntityEntry<ClutchTileEntity> CLUTCH =
		register("clutch", ClutchTileEntity::new, AllBlocks.CLUTCH);
	public static final TileEntityEntry<GearshiftTileEntity> GEARSHIFT =
		register("gearshift", GearshiftTileEntity::new, AllBlocks.GEARSHIFT);
	public static final TileEntityEntry<TurntableTileEntity> TURNTABLE =
		register("turntable", TurntableTileEntity::new, AllBlocks.TURNTABLE);
	public static final TileEntityEntry<HandCrankTileEntity> HAND_CRANK =
		register("hand_crank", HandCrankTileEntity::new, AllBlocks.HAND_CRANK);
	public static final TileEntityEntry<CuckooClockTileEntity> CUCKOO_CLOCK =
		register("cuckoo_clock", CuckooClockTileEntity::new, AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK);

	public static final TileEntityEntry<PumpTileEntity> MECHANICAL_PUMP =
		register("mechanical_pump", PumpTileEntity::new, AllBlocks.MECHANICAL_PUMP);
	public static final TileEntityEntry<FluidTankTileEntity> FLUID_TANK =
		register("fluid_tank", FluidTankTileEntity::new, AllBlocks.FLUID_TANK);

	public static final TileEntityEntry<BeltTileEntity> BELT = register("belt", BeltTileEntity::new, AllBlocks.BELT);
	public static final TileEntityEntry<ChuteTileEntity> CHUTE =
		register("chute", ChuteTileEntity::new, AllBlocks.CHUTE);
	public static final TileEntityEntry<BeltTunnelTileEntity> BELT_TUNNEL =
		register("belt_tunnel", BeltTunnelTileEntity::new, AllBlocks.ANDESITE_TUNNEL, AllBlocks.BRASS_TUNNEL);
	public static final TileEntityEntry<ArmTileEntity> MECHANICAL_ARM =
		register("mechanical_arm", ArmTileEntity::new, AllBlocks.MECHANICAL_ARM);
	public static final TileEntityEntry<MechanicalPistonTileEntity> MECHANICAL_PISTON = register("mechanical_piston",
		MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON);
	public static final TileEntityEntry<MechanicalBearingTileEntity> MECHANICAL_BEARING =
		register("mechanical_bearing", MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING);
	public static final TileEntityEntry<ClockworkBearingTileEntity> CLOCKWORK_BEARING =
		register("clockwork_bearing", ClockworkBearingTileEntity::new, AllBlocks.CLOCKWORK_BEARING);
	public static final TileEntityEntry<PulleyTileEntity> ROPE_PULLEY =
		register("rope_pulley", PulleyTileEntity::new, AllBlocks.ROPE_PULLEY);
	public static final TileEntityEntry<ChassisTileEntity> CHASSIS = register("chassis", ChassisTileEntity::new,
		AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS);
	public static final TileEntityEntry<DrillTileEntity> DRILL =
		register("drill", DrillTileEntity::new, AllBlocks.MECHANICAL_DRILL);
	public static final TileEntityEntry<SawTileEntity> SAW =
		register("saw", SawTileEntity::new, AllBlocks.MECHANICAL_SAW);
	public static final TileEntityEntry<HarvesterTileEntity> HARVESTER =
		register("harvester", HarvesterTileEntity::new, AllBlocks.MECHANICAL_HARVESTER);
	public static final TileEntityEntry<FlywheelTileEntity> FLYWHEEL =
		register("flywheel", FlywheelTileEntity::new, AllBlocks.FLYWHEEL);
	public static final TileEntityEntry<FurnaceEngineTileEntity> FURNACE_ENGINE =
		register("furnace_engine", FurnaceEngineTileEntity::new, AllBlocks.FURNACE_ENGINE);

	public static final TileEntityEntry<MillstoneTileEntity> MILLSTONE =
		register("millstone", MillstoneTileEntity::new, AllBlocks.MILLSTONE);
	public static final TileEntityEntry<CrushingWheelTileEntity> CRUSHING_WHEEL =
		register("crushing_wheel", CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL);
	public static final TileEntityEntry<CrushingWheelControllerTileEntity> CRUSHING_WHEEL_CONTROLLER = register(
		"crushing_wheel_controller", CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER);
	public static final TileEntityEntry<WaterWheelTileEntity> WATER_WHEEL =
		register("water_wheel", WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL);
	public static final TileEntityEntry<MechanicalPressTileEntity> MECHANICAL_PRESS =
		register("mechanical_press", MechanicalPressTileEntity::new, AllBlocks.MECHANICAL_PRESS);
	public static final TileEntityEntry<MechanicalMixerTileEntity> MECHANICAL_MIXER =
		register("mechanical_mixer", MechanicalMixerTileEntity::new, AllBlocks.MECHANICAL_MIXER);
	public static final TileEntityEntry<DeployerTileEntity> DEPLOYER =
		register("deployer", DeployerTileEntity::new, AllBlocks.DEPLOYER);
	public static final TileEntityEntry<BasinTileEntity> BASIN =
		register("basin", BasinTileEntity::new, AllBlocks.BASIN);
	public static final TileEntityEntry<MechanicalCrafterTileEntity> MECHANICAL_CRAFTER =
		register("mechanical_crafter", MechanicalCrafterTileEntity::new, AllBlocks.MECHANICAL_CRAFTER);
	public static final TileEntityEntry<SequencedGearshiftTileEntity> SEQUENCED_GEARSHIFT =
		register("sequenced_gearshift", SequencedGearshiftTileEntity::new, AllBlocks.SEQUENCED_GEARSHIFT);
	public static final TileEntityEntry<SpeedControllerTileEntity> ROTATION_SPEED_CONTROLLER =
		register("rotation_speed_controller", SpeedControllerTileEntity::new, AllBlocks.ROTATION_SPEED_CONTROLLER);
	public static final TileEntityEntry<SpeedGaugeTileEntity> SPEEDOMETER =
		register("speedometer", SpeedGaugeTileEntity::new, AllBlocks.SPEEDOMETER);
	public static final TileEntityEntry<StressGaugeTileEntity> STRESSOMETER =
		register("stressometer", StressGaugeTileEntity::new, AllBlocks.STRESSOMETER);
	public static final TileEntityEntry<AnalogLeverTileEntity> ANALOG_LEVER =
		register("analog_lever", AnalogLeverTileEntity::new, AllBlocks.ANALOG_LEVER);
	public static final TileEntityEntry<CartAssemblerTileEntity> CART_ASSEMBLER =
		register("cart_assembler", CartAssemblerTileEntity::new, AllBlocks.CART_ASSEMBLER);

	// Logistics
	public static final TileEntityEntry<RedstoneLinkTileEntity> REDSTONE_LINK =
		register("redstone_link", RedstoneLinkTileEntity::new, AllBlocks.REDSTONE_LINK);
	public static final TileEntityEntry<NixieTubeTileEntity> NIXIE_TUBE =
		register("nixie_tube", NixieTubeTileEntity::new, AllBlocks.NIXIE_TUBE);
	public static final TileEntityEntry<StockpileSwitchTileEntity> STOCKPILE_SWITCH =
		register("stockpile_switch", StockpileSwitchTileEntity::new, AllBlocks.STOCKPILE_SWITCH);
	public static final TileEntityEntry<AdjustableCrateTileEntity> ADJUSTABLE_CRATE =
		register("adjustable_crate", AdjustableCrateTileEntity::new, AllBlocks.ADJUSTABLE_CRATE);
	public static final TileEntityEntry<CreativeCrateTileEntity> CREATIVE_CRATE =
		register("creative_crate", CreativeCrateTileEntity::new, AllBlocks.CREATIVE_CRATE);

	public static final TileEntityEntry<DepotTileEntity> DEPOT =
		register("depot", DepotTileEntity::new, AllBlocks.DEPOT);
	public static final TileEntityEntry<FunnelTileEntity> FUNNEL = register("funnel",
		FunnelTileEntity::new, AllBlocks.BRASS_FUNNEL, AllBlocks.BRASS_BELT_FUNNEL, AllBlocks.BRASS_CHUTE_FUNNEL,
		AllBlocks.ANDESITE_FUNNEL, AllBlocks.ANDESITE_BELT_FUNNEL, AllBlocks.ANDESITE_CHUTE_FUNNEL);
	public static final TileEntityEntry<PackagerTileEntity> PACKAGER =
		register("packager", PackagerTileEntity::new, AllBlocks.PACKAGER);

	public static final TileEntityEntry<ExtractorTileEntity> EXTRACTOR =
		register("extractor", ExtractorTileEntity::new, AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR);
	public static final TileEntityEntry<LinkedExtractorTileEntity> LINKED_EXTRACTOR = register("linked_extractor",
		LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR, AllBlocks.VERTICAL_LINKED_EXTRACTOR);
	public static final TileEntityEntry<TransposerTileEntity> TRANSPOSER =
		register("transposer", TransposerTileEntity::new, AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER);
	public static final TileEntityEntry<LinkedTransposerTileEntity> LINKED_TRANSPOSER = register("linked_transposer",
		LinkedTransposerTileEntity::new, AllBlocks.LINKED_TRANSPOSER, AllBlocks.VERTICAL_LINKED_TRANSPOSER);
	public static final TileEntityEntry<BeltObserverTileEntity> BELT_OBSERVER =
		register("belt_observer", BeltObserverTileEntity::new, AllBlocks.BELT_OBSERVER);
	public static final TileEntityEntry<AdjustableRepeaterTileEntity> ADJUSTABLE_REPEATER =
		register("adjustable_repeater", AdjustableRepeaterTileEntity::new, AllBlocks.ADJUSTABLE_REPEATER);
	public static final TileEntityEntry<AdjustablePulseRepeaterTileEntity> ADJUSTABLE_PULSE_REPEATER = register(
		"adjustable_pulse_repeater", AdjustablePulseRepeaterTileEntity::new, AllBlocks.ADJUSTABLE_PULSE_REPEATER);

	@SafeVarargs
	public static <T extends TileEntity> TileEntityEntry<T> register(String name,
		NonNullFunction<TileEntityType<T>, ? extends T> supplier, NonNullSupplier<? extends Block>... blocks) {
		return Create.registrate()
			.<T>tileEntity(name, supplier)
			.validBlocks(blocks)
			.register();
	}

	// TODO move to TileEntityBuilder#renderer
	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SCHEMATICANNON, SchematicannonRenderer::new);

		bind(SIMPLE_KINETIC, KineticTileEntityRenderer::new);
		bind(TURNTABLE, KineticTileEntityRenderer::new);
		bind(MOTOR, CreativeMotorRenderer::new);
		bind(ENCASED_SHAFT, EncasedShaftRenderer::new);
		bind(ADJUSTABLE_PULLEY, EncasedShaftRenderer::new);
		bind(DRILL, DrillRenderer::new);
		bind(SAW, SawRenderer::new);
		bind(ENCASED_FAN, EncasedFanRenderer::new);
		bind(GEARBOX, GearboxRenderer::new);
		bind(GEARSHIFT, SplitShaftRenderer::new);
		bind(CLUTCH, SplitShaftRenderer::new);
		bind(SEQUENCED_GEARSHIFT, SplitShaftRenderer::new);
		bind(BELT, BeltRenderer::new);
		bind(WATER_WHEEL, KineticTileEntityRenderer::new);
		bind(HAND_CRANK, HandCrankRenderer::new);
		bind(CUCKOO_CLOCK, CuckooClockRenderer::new);
		bind(ANALOG_LEVER, AnalogLeverRenderer::new);

		bind(MECHANICAL_PUMP, PumpRenderer::new);
		bind(FLUID_TANK, FluidTankRenderer::new);

		bind(MECHANICAL_PISTON, MechanicalPistonRenderer::new);
		bind(MECHANICAL_BEARING, BearingRenderer::new);
		bind(CLOCKWORK_BEARING, BearingRenderer::new);
		bind(ROPE_PULLEY, PulleyRenderer::new);
		bind(HARVESTER, HarvesterRenderer::new);

		bind(MILLSTONE, MillstoneRenderer::new);
		bind(CRUSHING_WHEEL, KineticTileEntityRenderer::new);
		bind(MECHANICAL_PRESS, MechanicalPressRenderer::new);
		bind(MECHANICAL_MIXER, MechanicalMixerRenderer::new);
		bind(MECHANICAL_CRAFTER, MechanicalCrafterRenderer::new);
		bind(SPEEDOMETER, GaugeRenderer::speed);
		bind(STRESSOMETER, GaugeRenderer::stress);
		bind(BASIN, BasinRenderer::new);
		bind(DEPLOYER, DeployerRenderer::new);
		bind(FLYWHEEL, FlywheelRenderer::new);
		bind(FURNACE_ENGINE, EngineRenderer::new);
		bind(ROTATION_SPEED_CONTROLLER, SpeedControllerRenderer::new);
		bind(PACKAGER, PackagerRenderer::new);
		bind(DEPOT, DepotRenderer::new);
		bind(CHUTE, ChuteRenderer::new);

		bind(CREATIVE_CRATE, SmartTileEntityRenderer::new);
		bind(REDSTONE_LINK, SmartTileEntityRenderer::new);
		bind(NIXIE_TUBE, NixieTubeRenderer::new);
		bind(EXTRACTOR, SmartTileEntityRenderer::new);
		bind(LINKED_EXTRACTOR, SmartTileEntityRenderer::new);
		bind(TRANSPOSER, SmartTileEntityRenderer::new);
		bind(LINKED_TRANSPOSER, SmartTileEntityRenderer::new);
		bind(FUNNEL, FunnelRenderer::new);
		bind(BELT_TUNNEL, BeltTunnelRenderer::new);
		bind(MECHANICAL_ARM, ArmRenderer::new);
		bind(BELT_OBSERVER, BeltObserverRenderer::new);
		bind(ADJUSTABLE_REPEATER, AdjustableRepeaterRenderer::new);
		bind(ADJUSTABLE_PULSE_REPEATER, AdjustableRepeaterRenderer::new);
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(TileEntityEntry<T> type,
		Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>> renderer) {
		ClientRegistry.bindTileEntityRenderer(type.get(), renderer);
	}

	public static void register() {}
}
