package com.simibubi.create;

import static com.simibubi.create.AllTags.tagBlockAndItem;
import static com.simibubi.create.content.AllSections.SCHEMATICS;
import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.BlockStateGen.oxidizedBlockstate;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.ModelGen.oxidizedItemModel;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.components.actors.DrillBlock;
import com.simibubi.create.content.contraptions.components.actors.DrillMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.content.contraptions.components.actors.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.PloughBlock;
import com.simibubi.create.content.contraptions.components.actors.PloughMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.components.actors.SawMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.actors.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.StorageInterfaceMovement;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockBlock;
import com.simibubi.create.content.contraptions.components.crafter.CrafterCTBehaviour;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crank.HandCrankBlock;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelBlock;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.content.contraptions.components.fan.NozzleBlock;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelGenerator;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneBlock;
import com.simibubi.create.content.contraptions.components.mixer.BasinOperatorBlockItem;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorBlock;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorGenerator;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.saw.SawGenerator;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock.ChassisCTBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.tracks.ReinforcedRailBlock;
import com.simibubi.create.content.contraptions.components.turntable.TurntableBlock;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.contraptions.fluids.PipeAttachmentModel;
import com.simibubi.create.content.contraptions.fluids.PumpBlock;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankGenerator;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankItem;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankModel;
import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftGenerator;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltColor;
import com.simibubi.create.content.contraptions.relays.belt.BeltGenerator;
import com.simibubi.create.content.contraptions.relays.belt.BeltModel;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyBlock;
import com.simibubi.create.content.contraptions.relays.encased.ClutchBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltGenerator;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeGenerator;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelCTBehaviour;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.simibubi.create.content.logistics.block.chute.ChuteGenerator;
import com.simibubi.create.content.logistics.block.chute.ChuteItem;
import com.simibubi.create.content.logistics.block.depot.DepotBlock;
import com.simibubi.create.content.logistics.block.diodes.AbstractDiodeGenerator;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterGenerator;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchGenerator;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterGenerator;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchGenerator;
import com.simibubi.create.content.logistics.block.extractor.ExtractorBlock;
import com.simibubi.create.content.logistics.block.extractor.ExtractorMovementBehaviour;
import com.simibubi.create.content.logistics.block.extractor.LinkedExtractorBlock;
import com.simibubi.create.content.logistics.block.extractor.VerticalExtractorGenerator;
import com.simibubi.create.content.logistics.block.funnel.AndesiteBeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.AndesiteChuteFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.AndesiteFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelGenerator;
import com.simibubi.create.content.logistics.block.funnel.BrassBeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BrassChuteFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BrassFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.ChuteFunnelGenerator;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmItem;
import com.simibubi.create.content.logistics.block.packager.PackagerBlock;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverBlock;
import com.simibubi.create.content.logistics.block.redstone.ContactMovementBehaviour;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlock;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeGenerator;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkGenerator;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchBlock;
import com.simibubi.create.content.logistics.block.transposer.LinkedTransposerBlock;
import com.simibubi.create.content.logistics.block.transposer.TransposerBlock;
import com.simibubi.create.content.logistics.block.transposer.VerticalTransposerGenerator;
import com.simibubi.create.content.palettes.MetalBlock;
import com.simibubi.create.content.schematics.block.SchematicTableBlock;
import com.simibubi.create.content.schematics.block.SchematicannonBlock;
import com.simibubi.create.foundation.config.StressConfigDefaults;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.worldgen.OxidizingBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.ToolType;

public class AllBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.baseCreativeTab);

	// Schematics

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
				.getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	// Kinetics

	static {
		REGISTRATE.startSection(AllSections.KINETICS);
	}

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
		.initialProperties(SharedProperties::stone)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::stone)
		.transform(StressConfigDefaults.setNoImpact())
		.properties(p -> p.sound(SoundType.WOOD))
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.WOOD))
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.item(CogwheelBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<EncasedShaftBlock> ENCASED_SHAFT =
		REGISTRATE.block("encased_shaft", EncasedShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate((c, p) -> axisBlock(c, p, blockState -> p.models()
				.getExistingFile(p.modLoc("block/encased_shaft/" + blockState.get(EncasedShaftBlock.CASING)
					.getName()))))
			.loot((p, b) -> p.registerDropping(b, SHAFT.get()))
			.register();

	public static final BlockEntry<GearboxBlock> GEARBOX = REGISTRATE.block("gearbox", GearboxBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(Block.Properties::nonOpaque)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(BlockStateGen.axisBlockProvider(true))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ClutchBlock> CLUTCH = REGISTRATE.block("clutch", ClutchBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(Block.Properties::nonOpaque)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(Block.Properties::nonOpaque)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedBeltBlock> ENCASED_BELT =
		REGISTRATE.block("encased_belt", EncasedBeltBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<AdjustablePulleyBlock> ADJUSTABLE_PULLEY =
		REGISTRATE.block("adjustable_pulley", AdjustablePulleyBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> {
				String powered = state.get(AdjustablePulleyBlock.POWERED) ? "_powered" : "";
				return p.models()
					.withExistingParent(c.getName() + "_" + suffix + powered, p.modLoc("block/encased_belt/" + suffix))
					.texture("side", p.modLoc("block/" + c.getName() + powered));
			}).generate(c, p))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/encased_belt/item"))
				.texture("side", p.modLoc("block/" + c.getName())))
			.build()
			.register();

	public static final BlockEntry<BeltBlock> BELT = REGISTRATE.block("belt", BeltBlock::new)
		.initialProperties(SharedProperties.beltMaterial, MaterialColor.GRAY)
		.properties(p -> p.sound(SoundType.CLOTH))
		.properties(p -> p.hardnessAndResistance(0.8F))
		.blockstate(new BeltGenerator()::generate)
		.transform(StressConfigDefaults.setImpact(1.0))
		.onRegister(CreateRegistrate.blockColors(() -> BeltColor::new))
		.onRegister(CreateRegistrate.blockModel(() -> BeltModel::new))
		.register();

	public static final BlockEntry<CreativeMotorBlock> CREATIVE_MOTOR =
		REGISTRATE.block("creative_motor", CreativeMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate(new CreativeMotorGenerator()::generate)
			.transform(StressConfigDefaults.setCapacity(16384.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelBlock> WATER_WHEEL = REGISTRATE.block("water_wheel", WaterWheelBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(Block.Properties::nonOpaque)
		.blockstate(BlockStateGen.horizontalWheelProvider(false))
		.addLayer(() -> RenderType::getCutoutMipped)
		.transform(StressConfigDefaults.setCapacity(16.0))
		.simpleItem()
		.register();

	public static final BlockEntry<EncasedFanBlock> ENCASED_FAN = REGISTRATE.block("encased_fan", EncasedFanBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::getCutoutMipped)
		.transform(StressConfigDefaults.setCapacity(16.0))
		.transform(StressConfigDefaults.setImpact(2.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<NozzleBlock> NOZZLE = REGISTRATE.block("nozzle", NozzleBlock::new)
		.initialProperties(SharedProperties::stone)
		.tag(AllBlockTags.BRITTLE.tag)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::getCutoutMipped)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TurntableBlock> TURNTABLE = REGISTRATE.block("turntable", TurntableBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
		.transform(StressConfigDefaults.setImpact(4.0))
		.simpleItem()
		.register();

	public static final BlockEntry<HandCrankBlock> HAND_CRANK = REGISTRATE.block("hand_crank", HandCrankBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(StressConfigDefaults.setCapacity(32.0))
		.tag(AllBlockTags.BRITTLE.tag)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CuckooClockBlock> CUCKOO_CLOCK =
		REGISTRATE.block("cuckoo_clock", CuckooClockBlock::regular)
			.transform(BuilderTransformers.cuckooClock())
			.register();

	public static final BlockEntry<CuckooClockBlock> MYSTERIOUS_CUCKOO_CLOCK =
		REGISTRATE.block("mysterious_cuckoo_clock", CuckooClockBlock::mysterious)
			.transform(BuilderTransformers.cuckooClock())
			.lang("Cuckoo Clock")
			.register();

	public static final BlockEntry<MillstoneBlock> MILLSTONE = REGISTRATE.block("millstone", MillstoneBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CrushingWheelBlock> CRUSHING_WHEEL =
		REGISTRATE.block("crushing_wheel", CrushingWheelBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.addLayer(() -> RenderType::getCutoutMipped)
			.transform(StressConfigDefaults.setImpact(8.0))
			.simpleItem()
			.register();

	public static final BlockEntry<CrushingWheelControllerBlock> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE.block("crushing_wheel_controller", CrushingWheelControllerBlock::new)
			.initialProperties(() -> Blocks.AIR)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStates(state -> ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.mcLoc("block/air")))
					.build()))
			.register();

	public static final BlockEntry<MechanicalPressBlock> MECHANICAL_PRESS =
		REGISTRATE.block("mechanical_press", MechanicalPressBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setImpact(8.0))
			.item(BasinOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<MechanicalMixerBlock> MECHANICAL_MIXER =
		REGISTRATE.block("mechanical_mixer", MechanicalMixerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::getCutoutMipped)
			.transform(StressConfigDefaults.setImpact(4.0))
			.item(BasinOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BasinBlock> BASIN = REGISTRATE.block("basin", BasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.standardModel(ctx, prov)))
		.simpleItem()
		.register();

	public static final BlockEntry<BlazeBurnerBlock> BLAZE_BURNER =
		REGISTRATE.block("blaze_burner", BlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightValue(12))
			.addLayer(() -> RenderType::getCutoutMipped)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_HEATERS.tag)
			.loot((lt, block) -> lt.registerLootTable(block, BlazeBurnerBlock.buildLootTable()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item(BlazeBurnerBlockItem::withBlaze)
			.model(AssetLookup.<BlazeBurnerBlockItem>customItemModel("blaze_burner", "block_with_blaze"))
			.build()
			.register();

	public static final BlockEntry<DepotBlock> DEPOT = REGISTRATE.block("depot", DepotBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<ChuteBlock> CHUTE = REGISTRATE.block("chute", ChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.addLayer(() -> RenderType::getCutoutMipped)
		.blockstate(new ChuteGenerator()::generate)
		.item(ChuteItem::new)
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<GaugeBlock> SPEEDOMETER = REGISTRATE.block("speedometer", GaugeBlock::speed)
		.initialProperties(SharedProperties::wooden)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<GaugeBlock> STRESSOMETER = REGISTRATE.block("stressometer", GaugeBlock::stress)
		.initialProperties(SharedProperties::wooden)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	// Fluids

	public static final BlockEntry<FluidPipeBlock> FLUID_PIPE = REGISTRATE.block("fluid_pipe", FluidPipeBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.pipe())
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedPipeBlock> ENCASED_FLUID_PIPE =
		REGISTRATE.block("encased_fluid_pipe", EncasedPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, state -> p.models()
				.cubeColumn(c.getName(), p.modLoc("block/copper_casing"), p.modLoc("block/encased_pipe"))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.registerDropping(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<GlassFluidPipeBlock> GLASS_FLUID_PIPE =
		REGISTRATE.block("glass_fluid_pipe", GlassFluidPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.addLayer(() -> RenderType::getCutoutMipped)
			.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> p.models()
				.getExistingFile(p.modLoc("block/fluid_pipe/window" + (s.get(GlassFluidPipeBlock.ALT) ? "_alt" : "")))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.registerDropping(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<PumpBlock> MECHANICAL_PUMP = REGISTRATE.block("mechanical_pump", PumpBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<FluidTankBlock> FLUID_TANK = REGISTRATE.block("fluid_tank", FluidTankBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(Block.Properties::nonOpaque)
		.blockstate(new FluidTankGenerator()::generate)
		.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::new))
		.addLayer(() -> RenderType::getCutoutMipped)
		.item(FluidTankItem::new)
		.model(AssetLookup.<FluidTankItem>customItemModel("_", "block_single_window"))
		.build()
		.register();

	public static final BlockEntry<SpoutBlock> SPOUT = REGISTRATE.block("spout", SpoutBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
		.addLayer(() -> RenderType::getCutoutMipped)
		.item()
		.transform(customItemModel())
		.register();

	// Contraptions

	public static final BlockEntry<MechanicalPistonBlock> MECHANICAL_PISTON =
		REGISTRATE.block("mechanical_piston", MechanicalPistonBlock::normal)
			.transform(BuilderTransformers.mechanicalPiston(PistonType.DEFAULT))
			.register();

	public static final BlockEntry<MechanicalPistonBlock> STICKY_MECHANICAL_PISTON =
		REGISTRATE.block("sticky_mechanical_piston", MechanicalPistonBlock::sticky)
			.transform(BuilderTransformers.mechanicalPiston(PistonType.STICKY))
			.register();

	public static final BlockEntry<PistonExtensionPoleBlock> PISTON_EXTENSION_POLE =
		REGISTRATE.block("piston_extension_pole", PistonExtensionPoleBlock::new)
			.initialProperties(() -> Blocks.PISTON_HEAD)
			.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(false))
			.simpleItem()
			.register();

	public static final BlockEntry<MechanicalPistonHeadBlock> MECHANICAL_PISTON_HEAD =
		REGISTRATE.block("mechanical_piston_head", MechanicalPistonHeadBlock::new)
			.initialProperties(() -> Blocks.PISTON_HEAD)
			.loot((p, b) -> p.registerDropping(b, PISTON_EXTENSION_POLE.get()))
			.blockstate((c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, state -> p.models()
				.getExistingFile(p.modLoc("block/mechanical_piston/" + state.get(MechanicalPistonHeadBlock.TYPE)
					.getName() + "/head"))))
			.register();

	public static final BlockEntry<MechanicalBearingBlock> MECHANICAL_BEARING =
		REGISTRATE.block("mechanical_bearing", MechanicalBearingBlock::new)
			.transform(BuilderTransformers.bearing("mechanical", "gearbox"))
			.transform(StressConfigDefaults.setCapacity(512.0))
			.transform(StressConfigDefaults.setImpact(4.0))
			.register();

	public static final BlockEntry<ClockworkBearingBlock> CLOCKWORK_BEARING =
		REGISTRATE.block("clockwork_bearing", ClockworkBearingBlock::new)
			.transform(BuilderTransformers.bearing("clockwork", "brass_gearbox"))
			.transform(StressConfigDefaults.setImpact(4.0))
			.register();

	public static final BlockEntry<PulleyBlock> ROPE_PULLEY = REGISTRATE.block("rope_pulley", PulleyBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PulleyBlock.RopeBlock> ROPE = REGISTRATE.block("rope", PulleyBlock.RopeBlock::new)
		.initialProperties(SharedProperties.beltMaterial, MaterialColor.BROWN)
		.tag(AllBlockTags.BRITTLE.tag)
		.properties(p -> p.sound(SoundType.CLOTH))
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
		.register();

	public static final BlockEntry<PulleyBlock.MagnetBlock> PULLEY_MAGNET =
		REGISTRATE.block("pulley_magnet", PulleyBlock.MagnetBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
			.register();

	public static final BlockEntry<CartAssemblerBlock> CART_ASSEMBLER =
		REGISTRATE.block("cart_assembler", CartAssemblerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.blockstate(BlockStateGen.cartAssembler())
			.addLayer(() -> RenderType::getCutoutMipped)
			.tag(BlockTags.RAILS)
			.item(CartAssemblerBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ReinforcedRailBlock> REINFORCED_RAIL =
		REGISTRATE.block("reinforced_rail", ReinforcedRailBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.blockstate(BlockStateGen.reinforcedRail())
			.addLayer(() -> RenderType::getCutoutMipped)
			.tag(BlockTags.RAILS)
			.item()
			.model((c, p) -> p.blockItem(() -> c.getEntry()
				.getBlock(), "/block"))
			.build()
			.register();

	public static final BlockEntry<MinecartAnchorBlock> MINECART_ANCHOR =
		REGISTRATE.block("minecart_anchor", MinecartAnchorBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/cart_assembler/" + c.getName()))))
			.register();

	public static final BlockEntry<LinearChassisBlock> LINEAR_CHASSIS =
		REGISTRATE.block("linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.lang("Linear Chassis")
			.simpleItem()
			.register();

	public static final BlockEntry<LinearChassisBlock> SECONDARY_LINEAR_CHASSIS =
		REGISTRATE.block("secondary_linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.simpleItem()
			.register();

	public static final BlockEntry<RadialChassisBlock> RADIAL_CHASSIS =
		REGISTRATE.block("radial_chassis", RadialChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.blockstate(BlockStateGen.radialChassis())
			.item()
			.model((c, p) -> {
				String path = "block/" + c.getName();
				p.cubeColumn(c.getName(), p.modLoc(path + "_side"), p.modLoc(path + "_end"));
			})
			.build()
			.register();

	public static final BlockEntry<DrillBlock> MECHANICAL_DRILL = REGISTRATE.block("mechanical_drill", DrillBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(AllMovementBehaviours.addMovementBehaviour(new DrillMovementBehaviour()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SawBlock> MECHANICAL_SAW = REGISTRATE.block("mechanical_saw", SawBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(new SawGenerator()::generate)
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(AllMovementBehaviours.addMovementBehaviour(new SawMovementBehaviour()))
		.addLayer(() -> RenderType::getCutoutMipped)
		.item()
		.model((c, p) -> p.blockItem(() -> c.getEntry()
			.getBlock(), "/horizontal"))
		.build()
		.register();

	public static final BlockEntry<DeployerBlock> DEPLOYER = REGISTRATE.block("deployer", DeployerBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalAxisBlockProvider())
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(AllMovementBehaviours.addMovementBehaviour(new DeployerMovementBehaviour()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE.block("portable_storage_interface", PortableStorageInterfaceBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(AllMovementBehaviours.addMovementBehaviour(new StorageInterfaceMovement()))
			.blockstate(BlockStateGen.directionalBlockProvider(false))
			.simpleItem()
			.register();

	public static final BlockEntry<HarvesterBlock> MECHANICAL_HARVESTER =
		REGISTRATE.block("mechanical_harvester", HarvesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(AllMovementBehaviours.addMovementBehaviour(new HarvesterMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PloughBlock> MECHANICAL_PLOUGH =
		REGISTRATE.block("mechanical_plough", PloughBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(AllMovementBehaviours.addMovementBehaviour(new PloughMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(false))
			.simpleItem()
			.register();

	static {
		for (DyeColor colour : DyeColor.values()) {
			String colourName = colour.getName();
			SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
			REGISTRATE.block(colourName + "_seat", p -> new SeatBlock(p, colour == DyeColor.RED))
				.initialProperties(SharedProperties::wooden)
				.onRegister(AllMovementBehaviours.addMovementBehaviour(movementBehaviour))
				.blockstate((c, p) -> {
					p.simpleBlock(c.get(), p.models()
						.withExistingParent(colourName + "_seat", p.modLoc("block/seat"))
						.texture("1", p.modLoc("block/seat/top_" + colourName))
						.texture("2", p.modLoc("block/seat/side_" + colourName)));
				})
				.recipe((c, p) -> {
					ShapedRecipeBuilder.shapedRecipe(c.get())
						.patternLine("#")
						.patternLine("-")
						.key('#', DyeHelper.getWoolOfDye(colour))
						.key('-', ItemTags.WOODEN_SLABS)
						.addCriterion("has_wool",
							new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.UNBOUNDED,
								MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED,
								new ItemPredicate[] { ItemPredicate.Builder.create()
									.tag(ItemTags.WOOL)
									.build() }))
						.build(p, Create.asResource("crafting/kinetics/" + c.getName()));
					ShapedRecipeBuilder.shapedRecipe(c.get())
						.patternLine("#")
						.patternLine("-")
						.key('#', DyeHelper.getTagOfDye(colour))
						.key('-', AllItemTags.SEATS.tag)
						.addCriterion("has_seat",
							new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.UNBOUNDED,
								MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED,
								new ItemPredicate[] { ItemPredicate.Builder.create()
									.tag(AllItemTags.SEATS.tag)
									.build() }))
						.build(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_seat"));
				})
				.tag(AllBlockTags.SEATS.tag)
				.item()
				.tag(AllItemTags.SEATS.tag)
				.build()
				.register();
		}
	}

	public static final BlockEntry<CasingBlock> ANDESITE_CASING = REGISTRATE.block("andesite_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.ANDESITE_CASING))
		.register();

	public static final BlockEntry<CasingBlock> BRASS_CASING = REGISTRATE.block("brass_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.BRASS_CASING))
		.register();

	public static final BlockEntry<CasingBlock> COPPER_CASING = REGISTRATE.block("copper_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.COPPER_CASING))
		.register();

	public static final BlockEntry<CasingBlock> SHADOW_STEEL_CASING =
		REGISTRATE.block("shadow_steel_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(AllSpriteShifts.SHADOW_STEEL_CASING))
			.lang("Shadow Casing")
			.register();

	public static final BlockEntry<CasingBlock> REFINED_RADIANCE_CASING =
		REGISTRATE.block("refined_radiance_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(AllSpriteShifts.REFINED_RADIANCE_CASING))
			.properties(p -> p.lightValue(12))
			.lang("Radiant Casing")
			.register();

	public static final BlockEntry<MechanicalCrafterBlock> MECHANICAL_CRAFTER =
		REGISTRATE.block("mechanical_crafter", MechanicalCrafterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(Block.Properties::nonOpaque)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setImpact(2.0))
			.onRegister(CreateRegistrate.connectedTextures(new CrafterCTBehaviour()))
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SequencedGearshiftBlock> SEQUENCED_GEARSHIFT =
		REGISTRATE.block("sequenced_gearshift", SequencedGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(new SequencedGearshiftGenerator()::generate)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FlywheelBlock> FLYWHEEL = REGISTRATE.block("flywheel", FlywheelBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(Block.Properties::nonOpaque)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new FlywheelGenerator()::generate)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<FurnaceEngineBlock> FURNACE_ENGINE =
		REGISTRATE.block("furnace_engine", FurnaceEngineBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setCapacity(1024.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SpeedControllerBlock> ROTATION_SPEED_CONTROLLER =
		REGISTRATE.block("rotation_speed_controller", SpeedControllerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
			.item()
			.transform(customItemModel())
			.register();

	// Logistics

	static {
		REGISTRATE.startSection(AllSections.LOGISTICS);
	}

	public static final BlockEntry<ArmBlock> MECHANICAL_ARM = REGISTRATE.block("mechanical_arm", ArmBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.partialBaseModel(c, p))
				.rotationX(s.get(ArmBlock.CEILING) ? 180 : 0)
				.build()))
		.transform(StressConfigDefaults.setImpact(8.0))
		.item(ArmItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<AndesiteFunnelBlock> ANDESITE_FUNNEL =
		REGISTRATE.block("andesite_funnel", AndesiteFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.transform(BuilderTransformers.funnel("andesite", Create.asResource("block/andesite_casing")))
			.register();

	public static final BlockEntry<AndesiteBeltFunnelBlock> ANDESITE_BELT_FUNNEL =
		REGISTRATE.block("andesite_belt_funnel", AndesiteBeltFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate(new BeltFunnelGenerator("andesite")::generate)
			.loot((p, b) -> p.registerDropping(b, ANDESITE_FUNNEL.get()))
			.register();

	public static final BlockEntry<AndesiteChuteFunnelBlock> ANDESITE_CHUTE_FUNNEL =
		REGISTRATE.block("andesite_chute_funnel", AndesiteChuteFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate(new ChuteFunnelGenerator("andesite")::generate)
			.loot((p, b) -> p.registerDropping(b, ANDESITE_FUNNEL.get()))
			.register();

	public static final BlockEntry<BrassFunnelBlock> BRASS_FUNNEL =
		REGISTRATE.block("brass_funnel", BrassFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.transform(BuilderTransformers.funnel("brass", Create.asResource("block/brass_casing")))
			.register();

	public static final BlockEntry<BrassBeltFunnelBlock> BRASS_BELT_FUNNEL =
		REGISTRATE.block("brass_belt_funnel", BrassBeltFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new BeltFunnelGenerator("brass")::generate)
			.loot((p, b) -> p.registerDropping(b, BRASS_FUNNEL.get()))
			.register();

	public static final BlockEntry<BrassChuteFunnelBlock> BRASS_CHUTE_FUNNEL =
		REGISTRATE.block("brass_chute_funnel", BrassChuteFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new ChuteFunnelGenerator("brass")::generate)
			.loot((p, b) -> p.registerDropping(b, BRASS_FUNNEL.get()))
			.register();

	public static final BlockEntry<BeltTunnelBlock> ANDESITE_TUNNEL =
		REGISTRATE.block("andesite_tunnel", BeltTunnelBlock::new)
			.transform(BuilderTransformers.beltTunnel("andesite", new ResourceLocation("block/polished_andesite")))
			.register();

	public static final BlockEntry<BrassTunnelBlock> BRASS_TUNNEL =
		REGISTRATE.block("brass_tunnel", BrassTunnelBlock::new)
			.transform(BuilderTransformers.beltTunnel("brass", Create.asResource("block/brass_block")))
			.onRegister(connectedTextures(new BrassTunnelCTBehaviour()))
			.register();

	public static final BlockEntry<RedstoneContactBlock> REDSTONE_CONTACT =
		REGISTRATE.block("redstone_contact", RedstoneContactBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(AllMovementBehaviours.addMovementBehaviour(new ContactMovementBehaviour()))
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<RedstoneLinkBlock> REDSTONE_LINK =
		REGISTRATE.block("redstone_link", RedstoneLinkBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(new RedstoneLinkGenerator()::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.transform(customItemModel("_", "transmitter"))
			.register();

	public static final BlockEntry<NixieTubeBlock> NIXIE_TUBE = REGISTRATE.block("nixie_tube", NixieTubeBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.lightValue(5))
		.blockstate(new NixieTubeGenerator()::generate)
		.addLayer(() -> RenderType::getTranslucent)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<StockpileSwitchBlock> STOCKPILE_SWITCH =
		REGISTRATE.block("stockpile_switch", StockpileSwitchBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.horizontalBlock(c.get(),
				AssetLookup.withIndicator(c, p, $ -> AssetLookup.standardModel(c, p), StockpileSwitchBlock.INDICATOR)))
			.simpleItem()
			.register();

	public static final BlockEntry<AdjustableCrateBlock> ADJUSTABLE_CRATE =
		REGISTRATE.block("adjustable_crate", AdjustableCrateBlock::new)
			.transform(BuilderTransformers.crate("brass"))
			.register();

	public static final BlockEntry<CreativeCrateBlock> CREATIVE_CRATE =
		REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.transform(BuilderTransformers.crate("creative"))
			.register();

	public static final BlockEntry<BeltObserverBlock> BELT_OBSERVER =
		REGISTRATE.block("belt_observer", BeltObserverBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate(BlockStateGen.beltObserver())
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PackagerBlock> PACKAGER = REGISTRATE.block("packager", PackagerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.transform(StressConfigDefaults.setImpact(4.0))
		.properties(Block.Properties::nonOpaque)
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.partialBaseModel(c, p))
				.rotationY(s.get(PackagerBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0)
				.build()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ExtractorBlock> EXTRACTOR = REGISTRATE.block("extractor", ExtractorBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.tag(AllBlockTags.BRITTLE.tag)
		.onRegister(AllMovementBehaviours.addMovementBehaviour(new ExtractorMovementBehaviour()))
		.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p, c.getName() + "/horizontal")))
		.item()
		.transform(customItemModel("_", "horizontal"))
		.register();

	public static final BlockEntry<ExtractorBlock.Vertical> VERTICAL_EXTRACTOR =
		REGISTRATE.block("vertical_extractor", ExtractorBlock.Vertical::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(new VerticalExtractorGenerator(false)::generate)
			.loot((p, b) -> p.registerDropping(b, EXTRACTOR.get()))
			.register();

	public static final BlockEntry<LinkedExtractorBlock> LINKED_EXTRACTOR = REGISTRATE
		.block("linked_extractor", LinkedExtractorBlock::new)
		.tag(AllBlockTags.BRITTLE.tag)
		.initialProperties(SharedProperties::softMetal)
		.addLayer(() -> RenderType::getCutoutMipped)
		.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p, "extractor/horizontal_linked")))
		.item()
		.transform(customItemModel("extractor", "horizontal_linked"))
		.register();

	public static final BlockEntry<LinkedExtractorBlock.Vertical> VERTICAL_LINKED_EXTRACTOR =
		REGISTRATE.block("vertical_linked_extractor", LinkedExtractorBlock.Vertical::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(new VerticalExtractorGenerator(true)::generate)
			.loot((p, b) -> p.registerDropping(b, LINKED_EXTRACTOR.get()))
			.addLayer(() -> RenderType::getCutoutMipped)
			.register();

	public static final BlockEntry<TransposerBlock> TRANSPOSER = REGISTRATE.block("transposer", TransposerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p, c.getName() + "/block"), 180))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<TransposerBlock.Vertical> VERTICAL_TRANSPOSER =
		REGISTRATE.block("vertical_transposer", TransposerBlock.Vertical::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new VerticalTransposerGenerator(false)::generate)
			.loot((p, b) -> p.registerDropping(b, TRANSPOSER.get()))
			.register();

	public static final BlockEntry<LinkedTransposerBlock> LINKED_TRANSPOSER =
		REGISTRATE.block("linked_transposer", LinkedTransposerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.addLayer(() -> RenderType::getCutoutMipped)
			.blockstate(
				(c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p, "transposer/horizontal_linked"), 180))
			.item()
			.transform(customItemModel("transposer", "horizontal_linked"))
			.register();

	public static final BlockEntry<LinkedTransposerBlock.Vertical> VERTICAL_LINKED_TRANSPOSER =
		REGISTRATE.block("vertical_linked_transposer", LinkedTransposerBlock.Vertical::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new VerticalTransposerGenerator(true)::generate)
			.loot((p, b) -> p.registerDropping(b, LINKED_TRANSPOSER.get()))
			.addLayer(() -> RenderType::getCutoutMipped)
			.register();

	public static final BlockEntry<AnalogLeverBlock> ANALOG_LEVER =
		REGISTRATE.block("analog_lever", AnalogLeverBlock::new)
			.initialProperties(() -> Blocks.LEVER)
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PulseRepeaterBlock> PULSE_REPEATER =
		REGISTRATE.block("pulse_repeater", PulseRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new PulseRepeaterGenerator()::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.transform(customItemModel("diodes", "pulse_repeater"))
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_REPEATER =
		REGISTRATE.block("adjustable_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_PULSE_REPEATER =
		REGISTRATE.block("adjustable_pulse_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<PoweredLatchBlock> POWERED_LATCH =
		REGISTRATE.block("powered_latch", PoweredLatchBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new PoweredLatchGenerator()::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.simpleItem()
			.register();

	public static final BlockEntry<ToggleLatchBlock> POWERED_TOGGLE_LATCH =
		REGISTRATE.block("powered_toggle_latch", ToggleLatchBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new ToggleLatchGenerator()::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.item()
			.transform(customItemModel("diodes", "latch_off"))
			.register();

	// Materials

	static {
		REGISTRATE.startSection(AllSections.MATERIALS);
	}

	public static final BlockEntry<OxidizingBlock> COPPER_ORE =
		REGISTRATE.block("copper_ore", p -> new OxidizingBlock(p, 1))
			.initialProperties(() -> Blocks.IRON_ORE)
			.transform(oxidizedBlockstate())
			.transform(tagBlockAndItem("ores/copper"))
			.transform(oxidizedItemModel())
			.register();

	public static final BlockEntry<Block> ZINC_ORE = REGISTRATE.block("zinc_ore", Block::new)
		.initialProperties(() -> Blocks.GOLD_BLOCK)
		.properties(p -> p.harvestLevel(2)
			.harvestTool(ToolType.PICKAXE)
			.sound(SoundType.STONE))
		.transform(tagBlockAndItem("ores/zinc"))
		.build()
		.register();

	public static final BlockEntry<OxidizingBlock> COPPER_BLOCK =
		REGISTRATE.block("copper_block", p -> new OxidizingBlock(p, 1 / 32f, true))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.transform(tagBlockAndItem("storage_blocks/copper"))
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<OxidizingBlock> COPPER_SHINGLES =
		REGISTRATE.block("copper_shingles", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.item()
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<OxidizingBlock> COPPER_TILES =
		REGISTRATE.block("copper_tiles", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.item()
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<MetalBlock> ZINC_BLOCK = REGISTRATE.block("zinc_block", p -> new MetalBlock(p, true))
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.transform(tagBlockAndItem("storage_blocks/zinc"))
		.build()
		.register();

	public static final BlockEntry<MetalBlock> BRASS_BLOCK =
		REGISTRATE.block("brass_block", p -> new MetalBlock(p, true))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.transform(tagBlockAndItem("storage_blocks/brass"))
			.build()
			.register();

	// Load this class

	public static void register() {}

}
