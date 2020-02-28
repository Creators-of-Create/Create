package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.foundation.behaviour.base.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.modules.contraptions.components.actors.DrillTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.actors.HarvesterTileEntity;
import com.simibubi.create.modules.contraptions.components.actors.HarvesterTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.clock.CuckooClockRenderer;
import com.simibubi.create.modules.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.BearingTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.pulley.PulleyRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.pulley.PulleyTileEntity;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.crank.HandCrankTileEntity;
import com.simibubi.create.modules.contraptions.components.crank.HandCrankTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelControllerTileEntity;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.modules.contraptions.components.fan.EncasedFanTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.fan.NozzleTileEntity;
import com.simibubi.create.modules.contraptions.components.flywheel.FlywheelRenderer;
import com.simibubi.create.modules.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.EngineRenderer;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.FurnaceEngineTileEntity;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.motor.MotorTileEntity;
import com.simibubi.create.modules.contraptions.components.motor.MotorTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.modules.contraptions.components.saw.SawTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.turntable.TurntableTileEntity;
import com.simibubi.create.modules.contraptions.components.waterwheel.WaterWheelTileEntity;
import com.simibubi.create.modules.contraptions.processing.BasinTileEntity;
import com.simibubi.create.modules.contraptions.processing.BasinTileEntityRenderer;
import com.simibubi.create.modules.contraptions.redstone.AnalogLeverTileEntity;
import com.simibubi.create.modules.contraptions.redstone.AnalogLeverTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.advanced.SpeedControllerRenderer;
import com.simibubi.create.modules.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.AdjustablePulleyTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.ClutchTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.encased.SplitShaftTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.modules.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearboxTileEntity;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearboxTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity;
import com.simibubi.create.modules.logistics.block.RedstoneLinkTileEntity;
import com.simibubi.create.modules.logistics.block.StockswitchTileEntity;
import com.simibubi.create.modules.logistics.block.belts.BeltObserverTileEntity;
import com.simibubi.create.modules.logistics.block.belts.BeltObserverTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.belts.FunnelTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexPulsepeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.extractor.LinkedExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateTileEntity;
import com.simibubi.create.modules.logistics.block.transposer.LinkedTransposerTileEntity;
import com.simibubi.create.modules.logistics.block.transposer.TransposerTileEntity;
import com.simibubi.create.modules.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.modules.schematics.block.SchematicannonRenderer;
import com.simibubi.create.modules.schematics.block.SchematicannonTileEntity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public enum AllTileEntities {

	// Schematics
	SCHEMATICANNON(SchematicannonTileEntity::new, AllBlocks.SCHEMATICANNON),
	SCHEMATICTABLE(SchematicTableTileEntity::new, AllBlocks.SCHEMATIC_TABLE),

	// Kinetics
	SHAFT(ShaftTileEntity::new, AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL, AllBlocks.ENCASED_SHAFT),
	MOTOR(MotorTileEntity::new, AllBlocks.CREATIVE_MOTOR),
	GEARBOX(GearboxTileEntity::new, AllBlocks.GEARBOX),
	TURNTABLE(TurntableTileEntity::new, AllBlocks.TURNTABLE),
	ENCASED_SHAFT(EncasedShaftTileEntity::new, AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT),
	ADJUSTABLE_PULLEY(AdjustablePulleyTileEntity::new, AllBlocks.ADJUSTABLE_PULLEY),
	ENCASED_FAN(EncasedFanTileEntity::new, AllBlocks.ENCASED_FAN),
	NOZZLE(NozzleTileEntity::new, AllBlocks.NOZZLE),
	CLUTCH(ClutchTileEntity::new, AllBlocks.CLUTCH),
	GEARSHIFT(GearshiftTileEntity::new, AllBlocks.GEARSHIFT),
	HAND_CRANK(HandCrankTileEntity::new, AllBlocks.HAND_CRANK),
	CUCKOO_CLOCK(CuckooClockTileEntity::new, AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK),

	BELT(BeltTileEntity::new, AllBlocks.BELT),
	BELT_TUNNEL(BeltTunnelTileEntity::new, AllBlocks.BELT_TUNNEL),
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON),
	MECHANICAL_BEARING(MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING),
	CLOCKWORK_BEARING(ClockworkBearingTileEntity::new, AllBlocks.CLOCKWORK_BEARING),
	ROPE_PULLEY(PulleyTileEntity::new, AllBlocks.ROPE_PULLEY),
	CHASSIS(
			ChassisTileEntity::new,
			AllBlocks.ROTATION_CHASSIS,
			AllBlocks.TRANSLATION_CHASSIS,
			AllBlocks.TRANSLATION_CHASSIS_SECONDARY),
	DRILL(DrillTileEntity::new, AllBlocks.DRILL),
	SAW(SawTileEntity::new, AllBlocks.SAW),
	HARVESTER(HarvesterTileEntity::new, AllBlocks.HARVESTER),
	FLYWHEEL(FlywheelTileEntity::new, AllBlocks.FLYWHEEL),
	FURNACE_ENGINE(FurnaceEngineTileEntity::new, AllBlocks.FURNACE_ENGINE),
	CRUSHING_WHEEL(CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER),
	WATER_WHEEL(WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL),
	MECHANICAL_PRESS(MechanicalPressTileEntity::new, AllBlocks.MECHANICAL_PRESS),
	MECHANICAL_MIXER(MechanicalMixerTileEntity::new, AllBlocks.MECHANICAL_MIXER),
	DEPLOYER(DeployerTileEntity::new, AllBlocks.DEPLOYER),
	BASIN(BasinTileEntity::new, AllBlocks.BASIN),
	MECHANICAL_CRAFTER(MechanicalCrafterTileEntity::new, AllBlocks.MECHANICAL_CRAFTER),
	ROTATION_SPEED_CONTROLLER(SpeedControllerTileEntity::new, AllBlocks.ROTATION_SPEED_CONTROLLER),
	SPEED_GAUGE(SpeedGaugeTileEntity::new, AllBlocks.SPEED_GAUGE),
	STRESS_GAUGE(StressGaugeTileEntity::new, AllBlocks.STRESS_GAUGE),
	ANALOG_LEVER(AnalogLeverTileEntity::new, AllBlocks.ANALOG_LEVER),

	// Logistics
	REDSTONE_BRIDGE(RedstoneLinkTileEntity::new, AllBlocks.REDSTONE_BRIDGE),
	STOCKSWITCH(StockswitchTileEntity::new, AllBlocks.STOCKSWITCH),
	FLEXCRATE(FlexcrateTileEntity::new, AllBlocks.FLEXCRATE),
	EXTRACTOR(ExtractorTileEntity::new, AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR),
	LINKED_EXTRACTOR(LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR, AllBlocks.VERTICAL_LINKED_EXTRACTOR),
	TRANSPOSER(TransposerTileEntity::new, AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER),
	LINKED_TRANSPOSER(
			LinkedTransposerTileEntity::new,
			AllBlocks.LINKED_TRANSPOSER,
			AllBlocks.VERTICAL_LINKED_TRANSPOSER),
	BELT_FUNNEL(FunnelTileEntity::new, AllBlocks.BELT_FUNNEL, AllBlocks.VERTICAL_FUNNEL),
	ENTITY_DETECTOR(BeltObserverTileEntity::new, AllBlocks.ENTITY_DETECTOR),
	FLEXPEATER(FlexpeaterTileEntity::new, AllBlocks.FLEXPEATER),
	FLEXPULSEPEATER(FlexPulsepeaterTileEntity::new, AllBlocks.FLEXPULSEPEATER),

	// Curiosities
	WINDOW_IN_A_BLOCK(WindowInABlockTileEntity::new, AllBlocks.WINDOW_IN_A_BLOCK),

	;

	private Supplier<? extends TileEntity> supplier;
	public TileEntityType<?> type;
	private AllBlocks[] blocks;

	private AllTileEntities(Supplier<? extends TileEntity> supplier, AllBlocks... blocks) {
		this.supplier = supplier;
		this.blocks = blocks;
	}

	public boolean typeOf(TileEntity te) {
		return te.getType().equals(type);
	}

	public static void register(RegistryEvent.Register<TileEntityType<?>> event) {
		for (AllTileEntities tileEntity : values()) {
			Block[] blocks = new Block[tileEntity.blocks.length];
			for (int i = 0; i < blocks.length; i++)
				blocks[i] = tileEntity.blocks[i].block;

			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, Lang.asId(tileEntity.name()));
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, blocks).build(null)
					.setRegistryName(resourceLocation);
			event.getRegistry().register(tileEntity.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SchematicannonTileEntity.class, new SchematicannonRenderer());

		bind(ShaftTileEntity.class, new KineticTileEntityRenderer());
		bind(TurntableTileEntity.class, new KineticTileEntityRenderer());
		bind(MotorTileEntity.class, new MotorTileEntityRenderer());
		bind(EncasedShaftTileEntity.class, new EncasedShaftTileEntityRenderer());
		bind(AdjustablePulleyTileEntity.class, new EncasedShaftTileEntityRenderer());
		bind(DrillTileEntity.class, new DrillTileEntityRenderer());
		bind(SawTileEntity.class, new SawTileEntityRenderer());
		bind(EncasedFanTileEntity.class, new EncasedFanTileEntityRenderer());
		bind(GearboxTileEntity.class, new GearboxTileEntityRenderer());
		bind(GearshiftTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(ClutchTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(BeltTileEntity.class, new BeltTileEntityRenderer());
		bind(WaterWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(HandCrankTileEntity.class, new HandCrankTileEntityRenderer());
		bind(CuckooClockTileEntity.class, new CuckooClockRenderer());
		bind(AnalogLeverTileEntity.class, new AnalogLeverTileEntityRenderer());

		bind(MechanicalPistonTileEntity.class, new MechanicalPistonTileEntityRenderer());
		bind(MechanicalBearingTileEntity.class, new BearingTileEntityRenderer());
		bind(ClockworkBearingTileEntity.class, new BearingTileEntityRenderer());
		bind(PulleyTileEntity.class, new PulleyRenderer());
		bind(HarvesterTileEntity.class, new HarvesterTileEntityRenderer());

		bind(CrushingWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(MechanicalPressTileEntity.class, new MechanicalPressTileEntityRenderer());
		bind(MechanicalMixerTileEntity.class, new MechanicalMixerTileEntityRenderer());
		bind(MechanicalCrafterTileEntity.class, new MechanicalCrafterTileEntityRenderer());
		bind(SpeedGaugeTileEntity.class, new GaugeTileEntityRenderer(GaugeBlock.Type.SPEED));
		bind(StressGaugeTileEntity.class, new GaugeTileEntityRenderer(GaugeBlock.Type.STRESS));
		bind(BasinTileEntity.class, new BasinTileEntityRenderer());
		bind(DeployerTileEntity.class, new DeployerTileEntityRenderer());
		bind(FlywheelTileEntity.class, new FlywheelRenderer());
		bind(FurnaceEngineTileEntity.class, new EngineRenderer<>());
		bind(SpeedControllerTileEntity.class, new SpeedControllerRenderer());

		bind(RedstoneLinkTileEntity.class, new SmartTileEntityRenderer<>());
		bind(ExtractorTileEntity.class, new SmartTileEntityRenderer<>());
		bind(LinkedExtractorTileEntity.class, new SmartTileEntityRenderer<>());
		bind(TransposerTileEntity.class, new SmartTileEntityRenderer<>());
		bind(LinkedTransposerTileEntity.class, new SmartTileEntityRenderer<>());
		bind(FunnelTileEntity.class, new SmartTileEntityRenderer<>());
		bind(BeltTunnelTileEntity.class, new BeltTunnelTileEntityRenderer());
		bind(BeltObserverTileEntity.class, new BeltObserverTileEntityRenderer());
		bind(FlexpeaterTileEntity.class, new FlexpeaterTileEntityRenderer());

//		bind(LogisticalControllerTileEntity.class, new LogisticalControllerTileEntityRenderer());
//		bind(LogisticiansTableTileEntity.class, new LogisticiansTableTileEntityRenderer());
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
	}

}
