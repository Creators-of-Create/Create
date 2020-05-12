package com.simibubi.create;

import java.util.function.Function;
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
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerTileEntity;
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
import com.simibubi.create.modules.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.modules.contraptions.components.millstone.MillstoneTileEntity;
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
import com.simibubi.create.modules.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntityRenderer;
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
import com.simibubi.create.modules.logistics.block.belts.observer.BeltObserverTileEntity;
import com.simibubi.create.modules.logistics.block.belts.observer.BeltObserverTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.modules.logistics.block.belts.tunnel.BeltTunnelTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.diodes.FlexPulsepeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntity;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.extractor.LinkedExtractorTileEntity;
import com.simibubi.create.modules.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.modules.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateTileEntity;
import com.simibubi.create.modules.logistics.block.transposer.LinkedTransposerTileEntity;
import com.simibubi.create.modules.logistics.block.transposer.TransposerTileEntity;
import com.simibubi.create.modules.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.modules.schematics.block.SchematicannonRenderer;
import com.simibubi.create.modules.schematics.block.SchematicannonTileEntity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public enum AllTileEntities {

	// Schematics
	SCHEMATICANNON(SchematicannonTileEntity::new, AllBlocksNew.SCHEMATICANNON),
	SCHEMATICTABLE(SchematicTableTileEntity::new, AllBlocksNew.SCHEMATIC_TABLE),

	// Kinetics
	SHAFT(ShaftTileEntity::new, AllBlocksNew.SHAFT, AllBlocksNew.COGWHEEL, AllBlocksNew.LARGE_COGWHEEL, AllBlocksNew.ENCASED_SHAFT),
	MOTOR(MotorTileEntity::new, AllBlocks.CREATIVE_MOTOR),
	GEARBOX(GearboxTileEntity::new, AllBlocksNew.GEARBOX),
	TURNTABLE(TurntableTileEntity::new, AllBlocks.TURNTABLE),
	ENCASED_SHAFT(EncasedShaftTileEntity::new, AllBlocksNew.ENCASED_SHAFT, AllBlocks.ENCASED_BELT),
	ADJUSTABLE_PULLEY(AdjustablePulleyTileEntity::new, AllBlocks.ADJUSTABLE_PULLEY),
	ENCASED_FAN(EncasedFanTileEntity::new, AllBlocks.ENCASED_FAN),
	NOZZLE(NozzleTileEntity::new, AllBlocks.NOZZLE),
	CLUTCH(ClutchTileEntity::new, AllBlocksNew.CLUTCH),
	GEARSHIFT(GearshiftTileEntity::new, AllBlocksNew.GEARSHIFT),
	HAND_CRANK(HandCrankTileEntity::new, AllBlocks.HAND_CRANK),
	CUCKOO_CLOCK(CuckooClockTileEntity::new, AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK),

	BELT(BeltTileEntity::new, AllBlocks.BELT),
	BELT_TUNNEL(BeltTunnelTileEntity::new, AllBlocks.BELT_TUNNEL),
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON),
	MECHANICAL_BEARING(MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING),
	CLOCKWORK_BEARING(ClockworkBearingTileEntity::new, AllBlocks.CLOCKWORK_BEARING),
	ROPE_PULLEY(PulleyTileEntity::new, AllBlocks.ROPE_PULLEY),
	CHASSIS(ChassisTileEntity::new, AllBlocks.ROTATION_CHASSIS, AllBlocks.TRANSLATION_CHASSIS,
			AllBlocks.TRANSLATION_CHASSIS_SECONDARY),
	DRILL(DrillTileEntity::new, AllBlocks.DRILL),
	SAW(SawTileEntity::new, AllBlocks.SAW),
	HARVESTER(HarvesterTileEntity::new, AllBlocks.HARVESTER),
	FLYWHEEL(FlywheelTileEntity::new, AllBlocks.FLYWHEEL),
	FURNACE_ENGINE(FurnaceEngineTileEntity::new, AllBlocks.FURNACE_ENGINE),
	
	MILLSTONE(MillstoneTileEntity::new, AllBlocks.MILLSTONE),
	CRUSHING_WHEEL(CrushingWheelTileEntity::new, AllBlocks.CRUSHING_WHEEL),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerTileEntity::new, AllBlocks.CRUSHING_WHEEL_CONTROLLER),
	WATER_WHEEL(WaterWheelTileEntity::new, AllBlocks.WATER_WHEEL),
	MECHANICAL_PRESS(MechanicalPressTileEntity::new, AllBlocks.MECHANICAL_PRESS),
	MECHANICAL_MIXER(MechanicalMixerTileEntity::new, AllBlocks.MECHANICAL_MIXER),
	DEPLOYER(DeployerTileEntity::new, AllBlocks.DEPLOYER),
	BASIN(BasinTileEntity::new, AllBlocks.BASIN),
	MECHANICAL_CRAFTER(MechanicalCrafterTileEntity::new, AllBlocks.MECHANICAL_CRAFTER),
	SEQUENCED_GEARSHIFT(SequencedGearshiftTileEntity::new, AllBlocks.SEQUENCED_GEARSHIFT),
	ROTATION_SPEED_CONTROLLER(SpeedControllerTileEntity::new, AllBlocks.ROTATION_SPEED_CONTROLLER),
	SPEED_GAUGE(SpeedGaugeTileEntity::new, AllBlocks.SPEED_GAUGE),
	STRESS_GAUGE(StressGaugeTileEntity::new, AllBlocks.STRESS_GAUGE),
	ANALOG_LEVER(AnalogLeverTileEntity::new, AllBlocks.ANALOG_LEVER),
	CART_ASSEMBLER(CartAssemblerTileEntity::new, AllBlocks.CART_ASSEMBLER),

	// Logistics
	REDSTONE_BRIDGE(RedstoneLinkTileEntity::new, AllBlocks.REDSTONE_BRIDGE),
	STOCKSWITCH(StockswitchTileEntity::new, AllBlocks.STOCKSWITCH),
	FLEXCRATE(FlexcrateTileEntity::new, AllBlocks.FLEXCRATE),
	CREATIVE_CRATE(CreativeCrateTileEntity::new, AllBlocks.CREATIVE_CRATE),
	EXTRACTOR(ExtractorTileEntity::new, AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR),
	LINKED_EXTRACTOR(LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR, AllBlocks.VERTICAL_LINKED_EXTRACTOR),
	TRANSPOSER(TransposerTileEntity::new, AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER),
	LINKED_TRANSPOSER(LinkedTransposerTileEntity::new, AllBlocks.LINKED_TRANSPOSER,
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
	private Supplier<? extends Block>[] blocks;

	@SafeVarargs
	private AllTileEntities(Supplier<? extends TileEntity> supplier, Supplier<? extends Block>... blocks) {
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
				blocks[i] = tileEntity.blocks[i].get();

			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, Lang.asId(tileEntity.name()));
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, blocks).build(null)
					.setRegistryName(resourceLocation);
			event.getRegistry().register(tileEntity.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SCHEMATICANNON, SchematicannonRenderer::new);

		bind(SHAFT, KineticTileEntityRenderer::new);
		bind(TURNTABLE, KineticTileEntityRenderer::new);
		bind(MOTOR, MotorTileEntityRenderer::new);
		bind(ENCASED_SHAFT, EncasedShaftTileEntityRenderer::new);
		bind(ADJUSTABLE_PULLEY, EncasedShaftTileEntityRenderer::new);
		bind(DRILL, DrillTileEntityRenderer::new);
		bind(SAW, SawTileEntityRenderer::new);
		bind(ENCASED_FAN, EncasedFanTileEntityRenderer::new);
		bind(GEARBOX, GearboxTileEntityRenderer::new);
		bind(GEARSHIFT, SplitShaftTileEntityRenderer::new);
		bind(CLUTCH, SplitShaftTileEntityRenderer::new);
		bind(SEQUENCED_GEARSHIFT, SplitShaftTileEntityRenderer::new);
		bind(BELT, BeltTileEntityRenderer::new);
		bind(WATER_WHEEL, KineticTileEntityRenderer::new);
		bind(HAND_CRANK, HandCrankTileEntityRenderer::new);
		bind(CUCKOO_CLOCK, CuckooClockRenderer::new);
		bind(ANALOG_LEVER, AnalogLeverTileEntityRenderer::new);

		bind(MECHANICAL_PISTON, MechanicalPistonTileEntityRenderer::new);
		bind(MECHANICAL_BEARING, BearingTileEntityRenderer::new);
		bind(CLOCKWORK_BEARING, BearingTileEntityRenderer::new);
		bind(ROPE_PULLEY, PulleyRenderer::new);
		bind(HARVESTER, HarvesterTileEntityRenderer::new);

		bind(MILLSTONE, MillstoneRenderer::new);
		bind(CRUSHING_WHEEL, KineticTileEntityRenderer::new);
		bind(MECHANICAL_PRESS, MechanicalPressTileEntityRenderer::new);
		bind(MECHANICAL_MIXER, MechanicalMixerTileEntityRenderer::new);
		bind(MECHANICAL_CRAFTER, MechanicalCrafterTileEntityRenderer::new);
		bind(SPEED_GAUGE, disp -> new GaugeTileEntityRenderer(disp, GaugeBlock.Type.SPEED));
		bind(STRESS_GAUGE, disp -> new GaugeTileEntityRenderer(disp, GaugeBlock.Type.STRESS));
		bind(BASIN, BasinTileEntityRenderer::new);
		bind(DEPLOYER, DeployerTileEntityRenderer::new);
		bind(FLYWHEEL, FlywheelRenderer::new);
		bind(FURNACE_ENGINE, EngineRenderer::new);
		bind(ROTATION_SPEED_CONTROLLER, SpeedControllerRenderer::new);

		bind(CREATIVE_CRATE, SmartTileEntityRenderer::new);
		bind(REDSTONE_BRIDGE, SmartTileEntityRenderer::new);
		bind(EXTRACTOR, SmartTileEntityRenderer::new);
		bind(LINKED_EXTRACTOR, SmartTileEntityRenderer::new);
		bind(TRANSPOSER, SmartTileEntityRenderer::new);
		bind(LINKED_TRANSPOSER, SmartTileEntityRenderer::new);
		bind(BELT_FUNNEL, SmartTileEntityRenderer::new);
		bind(BELT_TUNNEL, BeltTunnelTileEntityRenderer::new);
		bind(ENTITY_DETECTOR, BeltObserverTileEntityRenderer::new);
		bind(FLEXPEATER, FlexpeaterTileEntityRenderer::new);
	}

	@SuppressWarnings("unchecked") // TODO 1.15 this generic stuff is incompatible with the enum system - need strong types
	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(AllTileEntities type, Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<?>> renderer) {
		ClientRegistry.bindTileEntityRenderer((TileEntityType<T>) type.type, (Function<TileEntityRendererDispatcher, TileEntityRenderer<T>>) renderer);
	}

}
