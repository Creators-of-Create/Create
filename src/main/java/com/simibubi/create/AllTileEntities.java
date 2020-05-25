package com.simibubi.create;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.content.contraptions.components.actors.DrillRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterRenderer;
import com.simibubi.create.content.contraptions.components.actors.HarvesterTileEntity;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterRenderer;
import com.simibubi.create.content.contraptions.components.crank.HandCrankTileEntity;
import com.simibubi.create.content.contraptions.components.crank.HandCrankRenderer;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerTileEntity;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerRenderer;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanRenderer;
import com.simibubi.create.content.contraptions.components.fan.NozzleTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineTileEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneTileEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerRenderer;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorRenderer;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressRenderer;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.components.turntable.TurntableTileEntity;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerRenderer;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ClutchTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxRenderer;
import com.simibubi.create.content.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverTileEntity;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverRenderer;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelRenderer;
import com.simibubi.create.content.logistics.block.diodes.AdjustablePulseRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterRenderer;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.extractor.ExtractorTileEntity;
import com.simibubi.create.content.logistics.block.extractor.LinkedExtractorTileEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateTileEntity;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverTileEntity;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverRenderer;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkTileEntity;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.content.logistics.block.transposer.LinkedTransposerTileEntity;
import com.simibubi.create.content.logistics.block.transposer.TransposerTileEntity;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonRenderer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.Lang;

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
	SCHEMATICANNON(SchematicannonTileEntity::new, AllBlocks.SCHEMATICANNON),
	SCHEMATIC_TABLE(SchematicTableTileEntity::new, AllBlocks.SCHEMATIC_TABLE),

	// Kinetics
	SIMPLE_KINETIC(SimpleKineticTileEntity::new, AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL,
		AllBlocks.ENCASED_SHAFT),
	MOTOR(CreativeMotorTileEntity::new, AllBlocks.CREATIVE_MOTOR),
	GEARBOX(GearboxTileEntity::new, AllBlocks.GEARBOX),
	ENCASED_SHAFT(EncasedShaftTileEntity::new, AllBlocks.ENCASED_SHAFT, AllBlocks.ENCASED_BELT),
	ADJUSTABLE_PULLEY(AdjustablePulleyTileEntity::new, AllBlocks.ADJUSTABLE_PULLEY),
	ENCASED_FAN(EncasedFanTileEntity::new, AllBlocks.ENCASED_FAN),
	NOZZLE(NozzleTileEntity::new, AllBlocks.NOZZLE),
	CLUTCH(ClutchTileEntity::new, AllBlocks.CLUTCH),
	GEARSHIFT(GearshiftTileEntity::new, AllBlocks.GEARSHIFT),
	TURNTABLE(TurntableTileEntity::new, AllBlocks.TURNTABLE),
	HAND_CRANK(HandCrankTileEntity::new, AllBlocks.HAND_CRANK),
	CUCKOO_CLOCK(CuckooClockTileEntity::new, AllBlocks.CUCKOO_CLOCK, AllBlocks.MYSTERIOUS_CUCKOO_CLOCK),

	BELT(BeltTileEntity::new, AllBlocks.BELT),
	BELT_TUNNEL(BeltTunnelTileEntity::new, AllBlocks.BELT_TUNNEL),
	MECHANICAL_PISTON(MechanicalPistonTileEntity::new, AllBlocks.MECHANICAL_PISTON,
		AllBlocks.STICKY_MECHANICAL_PISTON),
	MECHANICAL_BEARING(MechanicalBearingTileEntity::new, AllBlocks.MECHANICAL_BEARING),
	CLOCKWORK_BEARING(ClockworkBearingTileEntity::new, AllBlocks.CLOCKWORK_BEARING),
	ROPE_PULLEY(PulleyTileEntity::new, AllBlocks.ROPE_PULLEY),
	CHASSIS(ChassisTileEntity::new, AllBlocks.RADIAL_CHASSIS, AllBlocks.LINEAR_CHASSIS,
		AllBlocks.LINEAR_CHASSIS_SECONDARY),
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
	SPEEDOMETER(SpeedGaugeTileEntity::new, AllBlocks.SPEEDOMETER),
	STRESSOMETER(StressGaugeTileEntity::new, AllBlocks.STRESSOMETER),
	ANALOG_LEVER(AnalogLeverTileEntity::new, AllBlocks.ANALOG_LEVER),
	CART_ASSEMBLER(CartAssemblerTileEntity::new, AllBlocks.CART_ASSEMBLER),

	// Logistics
	REDSTONE_LINK(RedstoneLinkTileEntity::new, AllBlocks.REDSTONE_LINK),
	STOCKPILE_SWITCH(StockpileSwitchTileEntity::new, AllBlocks.STOCKPILE_SWITCH),
	ADJUSTABLE_CRATE(AdjustableCrateTileEntity::new, AllBlocks.ADJUSTABLE_CRATE),
	CREATIVE_CRATE(CreativeCrateTileEntity::new, AllBlocks.CREATIVE_CRATE),
	EXTRACTOR(ExtractorTileEntity::new, AllBlocks.EXTRACTOR, AllBlocks.VERTICAL_EXTRACTOR),
	LINKED_EXTRACTOR(LinkedExtractorTileEntity::new, AllBlocks.LINKED_EXTRACTOR,
		AllBlocks.VERTICAL_LINKED_EXTRACTOR),
	TRANSPOSER(TransposerTileEntity::new, AllBlocks.TRANSPOSER, AllBlocks.VERTICAL_TRANSPOSER),
	LINKED_TRANSPOSER(LinkedTransposerTileEntity::new, AllBlocks.LINKED_TRANSPOSER,
		AllBlocks.VERTICAL_LINKED_TRANSPOSER),
	FUNNEL(FunnelTileEntity::new, AllBlocks.FUNNEL, AllBlocks.VERTICAL_FUNNEL),
	BELT_OBSERVER(BeltObserverTileEntity::new, AllBlocks.BELT_OBSERVER),
	ADJUSTABLE_REPEATER(AdjustableRepeaterTileEntity::new, AllBlocks.ADJUSTABLE_REPEATER),
	ADJUSTABLE_PULSE_REPEATER(AdjustablePulseRepeaterTileEntity::new, AllBlocks.ADJUSTABLE_PULSE_REPEATER),

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
		return te.getType()
			.equals(type);
	}

	public static void register(RegistryEvent.Register<TileEntityType<?>> event) {
		for (AllTileEntities tileEntity : values()) {
			Block[] blocks = new Block[tileEntity.blocks.length];
			for (int i = 0; i < blocks.length; i++)
				blocks[i] = tileEntity.blocks[i].get();

			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, Lang.asId(tileEntity.name()));
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, blocks)
				.build(null)
				.setRegistryName(resourceLocation);
			event.getRegistry()
				.register(tileEntity.type);
		}
	}

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
		bind(SPEEDOMETER, disp -> new GaugeRenderer(disp, GaugeBlock.Type.SPEED));
		bind(STRESSOMETER, disp -> new GaugeRenderer(disp, GaugeBlock.Type.STRESS));
		bind(BASIN, BasinRenderer::new);
		bind(DEPLOYER, DeployerRenderer::new);
		bind(FLYWHEEL, FlywheelRenderer::new);
		bind(FURNACE_ENGINE, EngineRenderer::new);
		bind(ROTATION_SPEED_CONTROLLER, SpeedControllerRenderer::new);

		bind(CREATIVE_CRATE, SmartTileEntityRenderer::new);
		bind(REDSTONE_LINK, SmartTileEntityRenderer::new);
		bind(EXTRACTOR, SmartTileEntityRenderer::new);
		bind(LINKED_EXTRACTOR, SmartTileEntityRenderer::new);
		bind(TRANSPOSER, SmartTileEntityRenderer::new);
		bind(LINKED_TRANSPOSER, SmartTileEntityRenderer::new);
		bind(FUNNEL, SmartTileEntityRenderer::new);
		bind(BELT_TUNNEL, BeltTunnelRenderer::new);
		bind(BELT_OBSERVER, BeltObserverRenderer::new);
		bind(ADJUSTABLE_REPEATER, AdjustableRepeaterRenderer::new);
		bind(ADJUSTABLE_PULSE_REPEATER, AdjustableRepeaterRenderer::new);
	}

	@SuppressWarnings("unchecked") // TODO 1.15 this generic stuff is incompatible with the enum system - need
									// strong types
	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(AllTileEntities type,
		Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<?>> renderer) {
		ClientRegistry.bindTileEntityRenderer((TileEntityType<T>) type.type,
			(Function<TileEntityRendererDispatcher, TileEntityRenderer<T>>) renderer);
	}

}
