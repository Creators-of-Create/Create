package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.generators.MotorTileEntity;
import com.simibubi.create.modules.contraptions.generators.MotorTileEntityRenderer;
import com.simibubi.create.modules.contraptions.generators.WaterWheelTileEntity;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelControllerTileEntity;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelTileEntity;
import com.simibubi.create.modules.contraptions.receivers.DrillTileEntity;
import com.simibubi.create.modules.contraptions.receivers.TurntableTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.AxisTileEntity;
import com.simibubi.create.modules.contraptions.relays.AxisTunnelTileEntity;
import com.simibubi.create.modules.contraptions.relays.AxisTunnelTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.BeltTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.GearboxTileEntity;
import com.simibubi.create.modules.contraptions.relays.GearboxTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.GearshifterTileEntity;
import com.simibubi.create.modules.contraptions.relays.GearshifterTileEntityRenderer;
import com.simibubi.create.modules.logistics.FlexcrateTileEntity;
import com.simibubi.create.modules.logistics.RedstoneBridgeTileEntity;
import com.simibubi.create.modules.logistics.RedstoneBridgeTileEntityRenderer;
import com.simibubi.create.modules.logistics.StockswitchTileEntity;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public enum AllTileEntities {

	// Schematics
	SCHEMATICANNON(SchematicannonTileEntity::new, AllBlocks.SCHEMATICANNON),
	SCHEMATICTABLE(SchematicTableTileEntity::new, AllBlocks.SCHEMATIC_TABLE),

	// Kinetics
	AXIS(AxisTileEntity::new, AllBlocks.AXIS, AllBlocks.GEAR, AllBlocks.LARGE_GEAR, AllBlocks.AXIS_TUNNEL),
	MOTOR(MotorTileEntity::new, AllBlocks.MOTOR), GEARBOX(GearboxTileEntity::new, AllBlocks.GEARBOX),
	TURNTABLE(TurntableTileEntity::new, AllBlocks.TURNTABLE),
	AXIS_TUNNEL(AxisTunnelTileEntity::new, AllBlocks.AXIS_TUNNEL, AllBlocks.ENCASED_BELT),
	GEARSHIFTER(GearshifterTileEntity::new, AllBlocks.GEARSHIFTER), BELT(BeltTileEntity::new, AllBlocks.BELT),
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON),
	DRILL(DrillTileEntity::new, AllBlocks.DRILL),
	CRUSHING_WHEEL(CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER),
	WATER_WHEEL(WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL),

	// Logistics
	REDSTONE_BRIDGE(RedstoneBridgeTileEntity::new, AllBlocks.REDSTONE_BRIDGE),
	STOCKSWITCH(StockswitchTileEntity::new, AllBlocks.STOCKSWITCH),
	FLEXCRATE(FlexcrateTileEntity::new, AllBlocks.FLEXCRATE),
	
	;

	private Supplier<? extends TileEntity> supplier;
	public TileEntityType<?> type;
	private AllBlocks[] blocks;

	private AllTileEntities(Supplier<? extends TileEntity> supplier, AllBlocks... blocks) {
		this.supplier = supplier;
		this.blocks = blocks;
	}

	@SubscribeEvent
	public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {

		for (AllTileEntities tileEntity : values()) {
			Block[] blocks = new Block[tileEntity.blocks.length];
			for (int i = 0; i < blocks.length; i++)
				blocks[i] = tileEntity.blocks[i].block;

			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, tileEntity.name().toLowerCase());
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, blocks).build(null)
					.setRegistryName(resourceLocation);
			event.getRegistry().register(tileEntity.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SchematicannonTileEntity.class, new SchematicannonRenderer());
		bind(AxisTileEntity.class, new KineticTileEntityRenderer());
		bind(TurntableTileEntity.class, new KineticTileEntityRenderer());
		bind(MotorTileEntity.class, new MotorTileEntityRenderer());
		bind(AxisTunnelTileEntity.class, new AxisTunnelTileEntityRenderer());
		bind(GearboxTileEntity.class, new GearboxTileEntityRenderer());
		bind(GearshifterTileEntity.class, new GearshifterTileEntityRenderer());
		bind(BeltTileEntity.class, new BeltTileEntityRenderer());
		bind(MechanicalPistonTileEntity.class, new MechanicalPistonTileEntityRenderer());
		bind(DrillTileEntity.class, new KineticTileEntityRenderer());
		bind(CrushingWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(WaterWheelTileEntity.class, new KineticTileEntityRenderer());
		bind(RedstoneBridgeTileEntity.class, new RedstoneBridgeTileEntityRenderer());
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
	}

}
