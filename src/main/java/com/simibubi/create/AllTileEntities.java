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
import com.simibubi.create.modules.contraptions.receivers.EncasedFanTileEntity;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerTileEntity;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressTileEntity;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.TurntableTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalBearingTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalBearingTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntityRenderer;
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
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity;
import com.simibubi.create.modules.logistics.block.BeltFunnelTileEntity;
import com.simibubi.create.modules.logistics.block.EntityDetectorTileEntity;
import com.simibubi.create.modules.logistics.block.EntityDetectorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.ExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.ExtractorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.FlexcrateTileEntity;
import com.simibubi.create.modules.logistics.block.LinkedExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.LinkedExtractorTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.LinkedTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.RedstoneBridgeTileEntity;
import com.simibubi.create.modules.logistics.block.StockswitchTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntityRenderer;
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
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.IForgeRegistry;

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
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON),
	MECHANICAL_BEARING(MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING),
	CHASSIS(ChassisTileEntity::new, AllBlocks.ROTATION_CHASSIS, AllBlocks.TRANSLATION_CHASSIS),
	DRILL(DrillTileEntity::new, AllBlocks.DRILL),
	CRUSHING_WHEEL(CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER),
	WATER_WHEEL(WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL),
	MECHANICAL_PRESS(MechanicalPressTileEntity::new, AllBlocks.MECHANICAL_PRESS),
	MECHANICAL_MIXER(MechanicalMixerTileEntity::new, AllBlocks.MECHANICAL_MIXER),
	BASIN(BasinTileEntity::new, AllBlocks.BASIN),

	// Logistics
	REDSTONE_BRIDGE(RedstoneBridgeTileEntity::new, AllBlocks.REDSTONE_BRIDGE),
	STOCKSWITCH(StockswitchTileEntity::new, AllBlocks.STOCKSWITCH),
	FLEXCRATE(FlexcrateTileEntity::new, AllBlocks.FLEXCRATE),
	EXTRACTOR(ExtractorTileEntity::new, AllBlocks.EXTRACTOR),
	LINKED_EXTRACTOR(LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR),
	BELT_FUNNEL(BeltFunnelTileEntity::new, AllBlocks.BELT_FUNNEL),
	ENTITY_DETECTOR(EntityDetectorTileEntity::new, AllBlocks.ENTITY_DETECTOR),
	FLEXPEATER(FlexpeaterTileEntity::new, AllBlocks.FLEXPEATER),

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

	public static void registerTileEntities(IForgeRegistry<TileEntityType<?>> registry) {
		for (AllTileEntities tileEntity : values()) {
			Block[] blocks = new Block[tileEntity.blocks.length];
			for (int i = 0; i < blocks.length; i++)
				blocks[i] = tileEntity.blocks[i].block;

			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, Lang.asId(tileEntity.name()));
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, blocks).build(null)
					.setRegistryName(resourceLocation);
			registry.register(tileEntity.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SchematicannonTileEntity.class, new SchematicannonRenderer());
		bind(ShaftTileEntity.class, new KineticTileEntityRenderer());
		bind(TurntableTileEntity.class, new KineticTileEntityRenderer());
		bind(MotorTileEntity.class, new MotorTileEntityRenderer());
		bind(EncasedShaftTileEntity.class, new EncasedShaftTileEntityRenderer());
		bind(EncasedFanTileEntity.class, new EncasedFanTileEntityRenderer());
		bind(GearboxTileEntity.class, new GearboxTileEntityRenderer());
		bind(GearshiftTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(ClutchTileEntity.class, new SplitShaftTileEntityRenderer());
		bind(BeltTileEntity.class, new BeltTileEntityRenderer());
		bind(MechanicalPistonTileEntity.class, new MechanicalPistonTileEntityRenderer());
		bind(MechanicalBearingTileEntity.class, new MechanicalBearingTileEntityRenderer());
		bind(DrillTileEntity.class, new KineticTileEntityRenderer());
		bind(CrushingWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(WaterWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(RedstoneBridgeTileEntity.class, new LinkedTileEntityRenderer());
		bind(LinkedExtractorTileEntity.class, new LinkedExtractorTileEntityRenderer());
		bind(ExtractorTileEntity.class, new ExtractorTileEntityRenderer());
		bind(EntityDetectorTileEntity.class, new EntityDetectorTileEntityRenderer());
		bind(MechanicalPressTileEntity.class, new MechanicalPressTileEntityRenderer());
		bind(FlexpeaterTileEntity.class, new FlexpeaterTileEntityRenderer());
		bind(MechanicalMixerTileEntity.class, new MechanicalMixerTileEntityRenderer());
		bind(BasinTileEntity.class, new BasinTileEntityRenderer());
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
	}

}
