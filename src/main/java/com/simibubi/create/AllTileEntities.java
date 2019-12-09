package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.generators.MotorTileEntity;
import com.simibubi.create.modules.contraptions.generators.MotorTileEntityRenderer;
import com.simibubi.create.modules.contraptions.generators.WaterWheelTileEntity;
import com.simibubi.create.modules.contraptions.receivers.BasinTileEntity;
import com.simibubi.create.modules.contraptions.receivers.BasinTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelControllerTileEntity;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelTileEntity;
import com.simibubi.create.modules.contraptions.receivers.DrillTileEntity;
import com.simibubi.create.modules.contraptions.receivers.DrillTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanTileEntity;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.HarvesterTileEntity;
import com.simibubi.create.modules.contraptions.receivers.HarvesterTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerTileEntity;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressTileEntity;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.SawTileEntity;
import com.simibubi.create.modules.contraptions.receivers.SawTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.TurntableTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.bearing.MechanicalBearingTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.piston.MechanicalPistonTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.piston.MechanicalPistonTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.modules.contraptions.receivers.crafter.MechanicalCrafterTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.ClutchTileEntity;
import com.simibubi.create.modules.contraptions.relays.EncasedShaftTileEntity;
import com.simibubi.create.modules.contraptions.relays.EncasedShaftTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.GearboxTileEntity;
import com.simibubi.create.modules.contraptions.relays.GearboxTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.GearshiftTileEntity;
import com.simibubi.create.modules.contraptions.relays.ShaftTileEntity;
import com.simibubi.create.modules.contraptions.relays.SplitShaftTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.modules.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity;
import com.simibubi.create.modules.logistics.block.LinkedTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.RedstoneBridgeTileEntity;
import com.simibubi.create.modules.logistics.block.StockswitchTileEntity;
import com.simibubi.create.modules.logistics.block.belts.BeltFunnelTileEntity;
import com.simibubi.create.modules.logistics.block.belts.BeltFunnelTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.belts.EntityDetectorTileEntity;
import com.simibubi.create.modules.logistics.block.belts.EntityDetectorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.belts.ExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.belts.ExtractorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.belts.LinkedExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.belts.LinkedExtractorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerTileEntityRenderer;
import com.simibubi.create.modules.logistics.management.controller.CalculationTileEntity;
import com.simibubi.create.modules.logistics.management.controller.RequestTileEntity;
import com.simibubi.create.modules.logistics.management.controller.StorageTileEntity;
import com.simibubi.create.modules.logistics.management.controller.SupplyTileEntity;
import com.simibubi.create.modules.logistics.management.controller.TransactionsTileEntity;
import com.simibubi.create.modules.logistics.management.index.LogisticalIndexTileEntity;
import com.simibubi.create.modules.logistics.transport.villager.LogisticiansTableTileEntity;
import com.simibubi.create.modules.logistics.transport.villager.LogisticiansTableTileEntityRenderer;
import com.simibubi.create.modules.logistics.transport.villager.PackageFunnelTileEntity;
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
	MOTOR(MotorTileEntity::new, AllBlocks.MOTOR),
	GEARBOX(GearboxTileEntity::new, AllBlocks.GEARBOX),
	TURNTABLE(TurntableTileEntity::new, AllBlocks.TURNTABLE),
	ENCASED_SHAFT(EncasedShaftTileEntity::new, AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT),
	ENCASED_FAN(EncasedFanTileEntity::new, AllBlocks.ENCASED_FAN),
	CLUTCH(ClutchTileEntity::new, AllBlocks.CLUTCH),
	GEARSHIFT(GearshiftTileEntity::new, AllBlocks.GEARSHIFT),
	BELT(BeltTileEntity::new, AllBlocks.BELT),
	BELT_TUNNEL(BeltTunnelTileEntity::new, AllBlocks.BELT_TUNNEL),
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON),
	MECHANICAL_BEARING(MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING),
	CHASSIS(ChassisTileEntity::new, AllBlocks.ROTATION_CHASSIS, AllBlocks.TRANSLATION_CHASSIS,
			AllBlocks.TRANSLATION_CHASSIS_SECONDARY),
	DRILL(DrillTileEntity::new, AllBlocks.DRILL),
	SAW(SawTileEntity::new, AllBlocks.SAW),
	HARVESTER(HarvesterTileEntity::new, AllBlocks.HARVESTER),
	CRUSHING_WHEEL(CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER),
	WATER_WHEEL(WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL),
	MECHANICAL_PRESS(MechanicalPressTileEntity::new, AllBlocks.MECHANICAL_PRESS),
	MECHANICAL_MIXER(MechanicalMixerTileEntity::new, AllBlocks.MECHANICAL_MIXER),
	BASIN(BasinTileEntity::new, AllBlocks.BASIN),
	MECHANICAL_CRAFTER(MechanicalCrafterTileEntity::new, AllBlocks.MECHANICAL_CRAFTER),
	SPEED_GAUGE(SpeedGaugeTileEntity::new, AllBlocks.SPEED_GAUGE),
	STRESS_GAUGE(StressGaugeTileEntity::new, AllBlocks.STRESS_GAUGE),

	// Logistics
	REDSTONE_BRIDGE(RedstoneBridgeTileEntity::new, AllBlocks.REDSTONE_BRIDGE),
	STOCKSWITCH(StockswitchTileEntity::new, AllBlocks.STOCKSWITCH),
	FLEXCRATE(FlexcrateTileEntity::new, AllBlocks.FLEXCRATE),
	EXTRACTOR(ExtractorTileEntity::new, AllBlocks.EXTRACTOR),
	LINKED_EXTRACTOR(LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR),
	BELT_FUNNEL(BeltFunnelTileEntity::new, AllBlocks.BELT_FUNNEL),
	ENTITY_DETECTOR(EntityDetectorTileEntity::new, AllBlocks.ENTITY_DETECTOR),
	FLEXPEATER(FlexpeaterTileEntity::new, AllBlocks.FLEXPEATER),
	LOGISTICAL_CASING(LogisticalCasingTileEntity::new, AllBlocks.LOGISTICAL_CASING),
	LOGISTICAL_SUPPLY_CONTROLLER(SupplyTileEntity::new, AllBlocks.LOGISTICAL_CONTROLLER),
	LOGISTICAL_REQUEST_CONTROLLER(RequestTileEntity::new, AllBlocks.LOGISTICAL_CONTROLLER),
	LOGISTICAL_STORAGE_CONTROLLER(StorageTileEntity::new, AllBlocks.LOGISTICAL_CONTROLLER),
	LOGISTICAL_CALCULATION_CONTROLLER(CalculationTileEntity::new, AllBlocks.LOGISTICAL_CONTROLLER),
	LOGISTICAL_TRANSATIONS_CONTROLLER(TransactionsTileEntity::new, AllBlocks.LOGISTICAL_CONTROLLER),
	LOGISTICAL_INDEX(LogisticalIndexTileEntity::new, AllBlocks.LOGISTICAL_INDEX),
	LOGISTICIANS_TABLE(LogisticiansTableTileEntity::new, AllBlocks.LOGISTICIANS_TABLE),
	PACKAGE_FUNNEL(PackageFunnelTileEntity::new, AllBlocks.PACKAGE_FUNNEL),

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
		bind(DrillTileEntity.class, new DrillTileEntityRenderer());
		bind(SawTileEntity.class, new SawTileEntityRenderer());
		bind(EncasedFanTileEntity.class, new EncasedFanTileEntityRenderer());
		bind(GearboxTileEntity.class, new GearboxTileEntityRenderer());
		bind(GearshiftTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(ClutchTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(BeltTileEntity.class, new BeltTileEntityRenderer());
		bind(MechanicalPistonTileEntity.class, new MechanicalPistonTileEntityRenderer());
		bind(MechanicalBearingTileEntity.class, new MechanicalBearingTileEntityRenderer());
		bind(CrushingWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(WaterWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(RedstoneBridgeTileEntity.class, new LinkedTileEntityRenderer());
		bind(LinkedExtractorTileEntity.class, new LinkedExtractorTileEntityRenderer());
		bind(ExtractorTileEntity.class, new ExtractorTileEntityRenderer());
		bind(BeltFunnelTileEntity.class, new BeltFunnelTileEntityRenderer());
		bind(BeltTunnelTileEntity.class, new BeltTunnelTileEntityRenderer());
		bind(EntityDetectorTileEntity.class, new EntityDetectorTileEntityRenderer());
		bind(MechanicalPressTileEntity.class, new MechanicalPressTileEntityRenderer());
		bind(FlexpeaterTileEntity.class, new FlexpeaterTileEntityRenderer());
		bind(LogisticalControllerTileEntity.class, new LogisticalControllerTileEntityRenderer());
		bind(LogisticiansTableTileEntity.class, new LogisticiansTableTileEntityRenderer());
		bind(HarvesterTileEntity.class, new HarvesterTileEntityRenderer());
		bind(MechanicalMixerTileEntity.class, new MechanicalMixerTileEntityRenderer());
		bind(MechanicalCrafterTileEntity.class, new MechanicalCrafterTileEntityRenderer());
		bind(BasinTileEntity.class, new BasinTileEntityRenderer());
		bind(SpeedGaugeTileEntity.class, new GaugeTileEntityRenderer(GaugeBlock.Type.SPEED));
		bind(StressGaugeTileEntity.class, new GaugeTileEntityRenderer(GaugeBlock.Type.STRESS));
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
	}

}
