package com.simibubi.create;

import static com.simibubi.create.AllMovementBehaviours.addMovementBehaviour;
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
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem;
import com.simibubi.create.content.contraptions.components.actors.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.DrillBlock;
import com.simibubi.create.content.contraptions.components.actors.DrillMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.content.contraptions.components.actors.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.PloughBlock;
import com.simibubi.create.content.contraptions.components.actors.PloughMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.components.actors.SawMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.actors.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockBlock;
import com.simibubi.create.content.contraptions.components.crafter.CrafterCTBehaviour;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crank.HandCrankBlock;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
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
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorBlock;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorGenerator;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.saw.SawGenerator;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedBearingMovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock.ChassisCTBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailGenerator;
import com.simibubi.create.content.contraptions.components.tracks.ReinforcedRailBlock;
import com.simibubi.create.content.contraptions.components.turntable.TurntableBlock;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.contraptions.fluids.PipeAttachmentModel;
import com.simibubi.create.content.contraptions.fluids.PumpBlock;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyBlock;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainBlock;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.BracketBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.BracketBlockItem;
import com.simibubi.create.content.contraptions.fluids.pipes.BracketGenerator;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidValveBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeGenerator;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankGenerator;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankItem;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankModel;
import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.content.contraptions.processing.BasinGenerator;
import com.simibubi.create.content.contraptions.processing.BasinMovementBehaviour;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.create.content.contraptions.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftGenerator;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltGenerator;
import com.simibubi.create.content.contraptions.relays.belt.BeltModel;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticBlockModel;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.AdjustablePulleyBlock;
import com.simibubi.create.content.contraptions.relays.encased.ClutchBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltGenerator;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCTBehaviour;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeGenerator;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock;
import com.simibubi.create.content.curiosities.bell.HauntedBellBlock;
import com.simibubi.create.content.curiosities.bell.HauntedBellMovementBehaviour;
import com.simibubi.create.content.curiosities.bell.PeculiarBellBlock;
import com.simibubi.create.content.curiosities.toolbox.ToolboxBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelCTBehaviour;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.simibubi.create.content.logistics.block.chute.ChuteGenerator;
import com.simibubi.create.content.logistics.block.chute.ChuteItem;
import com.simibubi.create.content.logistics.block.chute.SmartChuteBlock;
import com.simibubi.create.content.logistics.block.depot.DepotBlock;
import com.simibubi.create.content.logistics.block.depot.EjectorBlock;
import com.simibubi.create.content.logistics.block.depot.EjectorItem;
import com.simibubi.create.content.logistics.block.diodes.AbstractDiodeGenerator;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterGenerator;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchGenerator;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterGenerator;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchGenerator;
import com.simibubi.create.content.logistics.block.funnel.AndesiteFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelGenerator;
import com.simibubi.create.content.logistics.block.funnel.BrassFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelGenerator;
import com.simibubi.create.content.logistics.block.funnel.FunnelItem;
import com.simibubi.create.content.logistics.block.funnel.FunnelMovementBehaviour;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmItem;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverBlock;
import com.simibubi.create.content.logistics.block.redstone.ContactMovementBehaviour;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverBlock;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlock;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeGenerator;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkGenerator;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchBlock;
import com.simibubi.create.content.logistics.item.LecternControllerBlock;
import com.simibubi.create.content.schematics.block.SchematicTableBlock;
import com.simibubi.create.content.schematics.block.SchematicannonBlock;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.ColorHandlers;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.worldgen.OxidizingBlock;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.conditions.ILootCondition.IBuilder;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

public class AllBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.BASE_CREATIVE_TAB);

	// Schematics

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				IBuilder survivesExplosion = SurvivesExplosion.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantRange.exactly(1))
					.add(ItemLootEntry.lootTableItem(AllBlocks.SCHEMATICANNON.get()
						.asItem())
						.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
							.copy("Options", "BlockEntityTag.Options")))));
			})
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
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::stone)
		.transform(BlockStressDefaults.setNoImpact())
		.properties(p -> p.sound(SoundType.WOOD))
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.WOOD))
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
			.item(CogwheelBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<EncasedShaftBlock> ANDESITE_ENCASED_SHAFT =
		REGISTRATE.block("andesite_encased_shaft", EncasedShaftBlock::andesite)
			.transform(BuilderTransformers.encasedShaft("andesite", AllSpriteShifts.ANDESITE_CASING))
			.register();

	public static final BlockEntry<EncasedShaftBlock> BRASS_ENCASED_SHAFT =
		REGISTRATE.block("brass_encased_shaft", EncasedShaftBlock::brass)
			.transform(BuilderTransformers.encasedShaft("brass", AllSpriteShifts.BRASS_CASING))
			.register();

	public static final BlockEntry<GearboxBlock> GEARBOX = REGISTRATE.block("gearbox", GearboxBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(AbstractBlock.Properties::noOcclusion)
		.transform(BlockStressDefaults.setNoImpact())
		.onRegister(CreateRegistrate.connectedTextures(new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
		.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
			(s, f) -> f.getAxis() == s.getValue(GearboxBlock.AXIS))))
		.blockstate((c, p) -> axisBlock(c, p, $ -> AssetLookup.partialBaseModel(c, p), true))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ClutchBlock> CLUTCH = REGISTRATE.block("clutch", ClutchBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(AbstractBlock.Properties::noOcclusion)
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(AbstractBlock.Properties::noOcclusion)
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedBeltBlock> ENCASED_CHAIN_DRIVE =
		REGISTRATE.block("encased_chain_drive", EncasedBeltBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<AdjustablePulleyBlock> ADJUSTABLE_CHAIN_GEARSHIFT =
		REGISTRATE.block("adjustable_chain_gearshift", AdjustablePulleyBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> {
				String powered = state.getValue(AdjustablePulleyBlock.POWERED) ? "_powered" : "";
				return p.models()
					.withExistingParent(c.getName() + "_" + suffix + powered,
						p.modLoc("block/encased_chain_drive/" + suffix))
					.texture("side", p.modLoc("block/" + c.getName() + powered));
			}).generate(c, p))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/encased_chain_drive/item"))
				.texture("side", p.modLoc("block/" + c.getName())))
			.build()
			.register();

	public static final BlockEntry<BeltBlock> BELT = REGISTRATE.block("belt", BeltBlock::new)
		.initialProperties(SharedProperties.beltMaterial, MaterialColor.COLOR_GRAY)
		.properties(p -> p.sound(SoundType.WOOL))
		.properties(p -> p.strength(0.8F))
		.blockstate(new BeltGenerator()::generate)
		.transform(BlockStressDefaults.setImpact(1.0))
		.onRegister(CreateRegistrate.blockModel(() -> BeltModel::new))
		.register();

	public static final BlockEntry<CreativeMotorBlock> CREATIVE_MOTOR =
		REGISTRATE.block("creative_motor", CreativeMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new CreativeMotorGenerator()::generate)
			.transform(BlockStressDefaults.setCapacity(16384.0))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelBlock> WATER_WHEEL = REGISTRATE.block("water_wheel", WaterWheelBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(AbstractBlock.Properties::noOcclusion)
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(false))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(BlockStressDefaults.setCapacity(16.0))
		.simpleItem()
		.register();

	public static final BlockEntry<EncasedFanBlock> ENCASED_FAN = REGISTRATE.block("encased_fan", EncasedFanBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(BlockStressDefaults.setCapacity(16.0))
		.transform(BlockStressDefaults.setImpact(2.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<NozzleBlock> NOZZLE = REGISTRATE.block("nozzle", NozzleBlock::new)
		.initialProperties(SharedProperties::stone)
		.tag(AllBlockTags.BRITTLE.tag)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::cutoutMipped)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TurntableBlock> TURNTABLE = REGISTRATE.block("turntable", TurntableBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
		.transform(BlockStressDefaults.setImpact(4.0))
		.simpleItem()
		.register();

	public static final BlockEntry<HandCrankBlock> HAND_CRANK = REGISTRATE.block("hand_crank", HandCrankBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(BlockStressDefaults.setCapacity(8.0))
		.tag(AllBlockTags.BRITTLE.tag)
		.onRegister(ItemUseOverrides::addBlock)
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
			.onRegisterAfter(Item.class, c -> TooltipHelper.referTo(c, CUCKOO_CLOCK))
			.register();

	public static final BlockEntry<MillstoneBlock> MILLSTONE = REGISTRATE.block("millstone", MillstoneBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CrushingWheelBlock> CRUSHING_WHEEL =
		REGISTRATE.block("crushing_wheel", CrushingWheelBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(8.0))
			.simpleItem()
			.register();

	public static final BlockEntry<CrushingWheelControllerBlock> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE.block("crushing_wheel_controller", CrushingWheelControllerBlock::new)
			.initialProperties(() -> Blocks.AIR)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStatesExcept(state -> ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.mcLoc("block/air")))
					.build(), CrushingWheelControllerBlock.FACING))
			.register();

	public static final BlockEntry<MechanicalPressBlock> MECHANICAL_PRESS =
		REGISTRATE.block("mechanical_press", MechanicalPressBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setImpact(8.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<MechanicalMixerBlock> MECHANICAL_MIXER =
		REGISTRATE.block("mechanical_mixer", MechanicalMixerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(4.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BasinBlock> BASIN = REGISTRATE.block("basin", BasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.blockstate(new BasinGenerator()::generate)
		.onRegister(addMovementBehaviour(new BasinMovementBehaviour()))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<BlazeBurnerBlock> BLAZE_BURNER =
		REGISTRATE.block("blaze_burner", BlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel(BlazeBurnerBlock::getLight))
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_HEATERS.tag)
			.loot((lt, block) -> lt.add(block, BlazeBurnerBlock.buildLootTable()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item(BlazeBurnerBlockItem::withBlaze)
			.model(AssetLookup.<BlazeBurnerBlockItem>customBlockItemModel("blaze_burner", "block_with_blaze"))
			.build()
			.register();

	public static final BlockEntry<LitBlazeBurnerBlock> LIT_BLAZE_BURNER =
		REGISTRATE.block("lit_blaze_burner", LitBlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel(LitBlazeBurnerBlock::getLight))
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_HEATERS.tag)
			.loot((lt, block) -> lt.dropOther(block, AllItems.EMPTY_BLAZE_BURNER.get()))
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStates(state -> ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.modLoc("block/blaze_burner/"
							+ (state.getValue(LitBlazeBurnerBlock.FLAME_TYPE) == LitBlazeBurnerBlock.FlameType.SOUL
								? "block_with_soul_fire"
								: "block_with_fire"))))
					.build()))
			.register();

	public static final BlockEntry<DepotBlock> DEPOT = REGISTRATE.block("depot", DepotBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<EjectorBlock> WEIGHTED_EJECTOR =
		REGISTRATE.block("weighted_ejector", EjectorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p), 180))
			.transform(BlockStressDefaults.setImpact(2.0))
			.item(EjectorItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ChuteBlock> CHUTE = REGISTRATE.block("chute", ChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate(new ChuteGenerator()::generate)
		.item(ChuteItem::new)
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<SmartChuteBlock> SMART_CHUTE = REGISTRATE.block("smart_chute", SmartChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<GaugeBlock> SPEEDOMETER = REGISTRATE.block("speedometer", GaugeBlock::speed)
		.initialProperties(SharedProperties::wooden)
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<GaugeBlock> STRESSOMETER = REGISTRATE.block("stressometer", GaugeBlock::stress)
		.initialProperties(SharedProperties::wooden)
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<BracketBlock> WOODEN_BRACKET = REGISTRATE.block("wooden_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("wooden")::generate)
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("wooden"))
		.register();

	public static final BlockEntry<BracketBlock> METAL_BRACKET = REGISTRATE.block("metal_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("metal")::generate)
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("metal"))
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
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.encasedPipe())
			.onRegister(CreateRegistrate.connectedTextures(new EncasedCTBehaviour(AllSpriteShifts.COPPER_CASING)))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.COPPER_CASING,
				(s, f) -> !s.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<GlassFluidPipeBlock> GLASS_FLUID_PIPE =
		REGISTRATE.block("glass_fluid_pipe", GlassFluidPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.addLayer(() -> RenderType::cutoutMipped)
			.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> p.models()
				.getExistingFile(
					p.modLoc("block/fluid_pipe/window" + (s.getValue(GlassFluidPipeBlock.ALT) ? "_alt" : "")))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<PumpBlock> MECHANICAL_PUMP = REGISTRATE.block("mechanical_pump", PumpBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SmartFluidPipeBlock> SMART_FLUID_PIPE =
		REGISTRATE.block("smart_fluid_pipe", SmartFluidPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new SmartFluidPipeGenerator()::generate)
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FluidValveBlock> FLUID_VALVE = REGISTRATE.block("fluid_valve", FluidValveBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((c, p) -> BlockStateGen.directionalAxisBlock(c, p,
			(state, vertical) -> AssetLookup.partialBaseModel(c, p, vertical ? "vertical" : "horizontal",
				state.getValue(FluidValveBlock.ENABLED) ? "open" : "closed")))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ValveHandleBlock> COPPER_VALVE_HANDLE =
		REGISTRATE.block("copper_valve_handle", ValveHandleBlock::copper)
			.transform(BuilderTransformers.valveHandle(null))
			.register();

	public static final DyedBlockList<ValveHandleBlock> DYED_VALVE_HANDLES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_valve_handle", p -> ValveHandleBlock.dyed(p, colour))
			.transform(BuilderTransformers.valveHandle(colour))
			.recipe((c, p) -> ShapelessRecipeBuilder.shapeless(c.get())
				.requires(colour.getTag())
				.requires(AllItemTags.VALVE_HANDLES.tag)
				.unlockedBy("has_valve", RegistrateRecipeProvider.hasItem(AllItemTags.VALVE_HANDLES.tag))
				.save(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_valve_handle")))
			.register();
	});

	public static final BlockEntry<FluidTankBlock> FLUID_TANK = REGISTRATE.block("fluid_tank", FluidTankBlock::regular)
		.initialProperties(SharedProperties::softMetal)
		.properties(AbstractBlock.Properties::noOcclusion)
		.blockstate(new FluidTankGenerator()::generate)
		.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::standard))
		.addLayer(() -> RenderType::cutoutMipped)
		.item(FluidTankItem::new)
		.model(AssetLookup.<FluidTankItem>customBlockItemModel("_", "block_single_window"))
		.build()
		.register();

	public static final BlockEntry<FluidTankBlock> CREATIVE_FLUID_TANK =
		REGISTRATE.block("creative_fluid_tank", FluidTankBlock::creative)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(new FluidTankGenerator("creative_")::generate)
			.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::creative))
			.addLayer(() -> RenderType::cutoutMipped)
			.item(FluidTankItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/fluid_tank/block_single_window"))
				.texture("5", p.modLoc("block/creative_fluid_tank_window_single"))
				.texture("1", p.modLoc("block/creative_fluid_tank"))
				.texture("0", p.modLoc("block/creative_casing")))
			.build()
			.register();

	public static final BlockEntry<HosePulleyBlock> HOSE_PULLEY = REGISTRATE.block("hose_pulley", HosePulleyBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.horizontalBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ItemDrainBlock> ITEM_DRAIN = REGISTRATE.block("item_drain", ItemDrainBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.standardModel(c, p)))
		.simpleItem()
		.register();

	public static final BlockEntry<SpoutBlock> SPOUT = REGISTRATE.block("spout", SpoutBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
		.addLayer(() -> RenderType::cutoutMipped)
		.item(AssemblyOperatorBlockItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_FLUID_INTERFACE =
		REGISTRATE.block("portable_fluid_interface", PortableStorageInterfaceBlock::forFluids)
			.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(addMovementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.transform(customItemModel())
			.register();

	// Contraptions

	public static final BlockEntry<MechanicalPistonBlock> MECHANICAL_PISTON =
		REGISTRATE.block("mechanical_piston", MechanicalPistonBlock::normal)
			.transform(BuilderTransformers.mechanicalPiston(PistonType.DEFAULT))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalPistonBlock> STICKY_MECHANICAL_PISTON =
		REGISTRATE.block("sticky_mechanical_piston", MechanicalPistonBlock::sticky)
			.transform(BuilderTransformers.mechanicalPiston(PistonType.STICKY))
			.tag(AllBlockTags.SAFE_NBT.tag)
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
			.loot((p, b) -> p.dropOther(b, PISTON_EXTENSION_POLE.get()))
			.blockstate((c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, state -> p.models()
				.getExistingFile(p.modLoc("block/mechanical_piston/" + state.getValue(MechanicalPistonHeadBlock.TYPE)
					.getSerializedName() + "/head"))))
			.register();

	public static final BlockEntry<GantryCarriageBlock> GANTRY_CARRIAGE =
		REGISTRATE.block("gantry_carriage", GantryCarriageBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.directionalAxisBlockProvider())
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<GantryShaftBlock> GANTRY_SHAFT =
		REGISTRATE.block("gantry_shaft", GantryShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.directionalBlock(c.get(), s -> {
				boolean isPowered = s.getValue(GantryShaftBlock.POWERED);
				boolean isFlipped = s.getValue(GantryShaftBlock.FACING)
					.getAxisDirection() == AxisDirection.NEGATIVE;
				String partName = s.getValue(GantryShaftBlock.PART)
					.getSerializedName();
				String flipped = isFlipped ? "_flipped" : "";
				String powered = isPowered ? "_powered" : "";
				ModelFile existing = AssetLookup.partialBaseModel(c, p, partName);
				if (!isPowered && !isFlipped)
					return existing;
				return p.models()
					.withExistingParent("block/" + c.getName() + "_" + partName + powered + flipped,
						existing.getLocation())
					.texture("2", p.modLoc("block/" + c.getName() + powered + flipped));
			}))
			.transform(BlockStressDefaults.setNoImpact())
			.item()
			.transform(customItemModel("_", "block_single"))
			.register();

	public static final BlockEntry<WindmillBearingBlock> WINDMILL_BEARING =
		REGISTRATE.block("windmill_bearing", WindmillBearingBlock::new)
			.transform(BuilderTransformers.bearing("windmill", "gearbox", true))
			.transform(BlockStressDefaults.setCapacity(512.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalBearingBlock> MECHANICAL_BEARING =
		REGISTRATE.block("mechanical_bearing", MechanicalBearingBlock::new)
			.transform(BuilderTransformers.bearing("mechanical", "gearbox", false))
			.transform(BlockStressDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(new StabilizedBearingMovementBehaviour()))
			.register();

	public static final BlockEntry<ClockworkBearingBlock> CLOCKWORK_BEARING =
		REGISTRATE.block("clockwork_bearing", ClockworkBearingBlock::new)
			.transform(BuilderTransformers.bearing("clockwork", "brass_gearbox", false))
			.transform(BlockStressDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PulleyBlock> ROPE_PULLEY = REGISTRATE.block("rope_pulley", PulleyBlock::new)
		.initialProperties(SharedProperties::stone)
		.tag(AllBlockTags.SAFE_NBT.tag)
		.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PulleyBlock.RopeBlock> ROPE = REGISTRATE.block("rope", PulleyBlock.RopeBlock::new)
		.initialProperties(SharedProperties.beltMaterial, MaterialColor.COLOR_BROWN)
		.tag(AllBlockTags.BRITTLE.tag)
		.properties(p -> p.sound(SoundType.WOOL))
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
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.cartAssembler())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(BlockTags.RAILS, AllBlockTags.SAFE_NBT.tag)
			.item(CartAssemblerBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ReinforcedRailBlock> REINFORCED_RAIL =
		REGISTRATE.block("reinforced_rail", ReinforcedRailBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.reinforcedRail())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(BlockTags.RAILS)
			.item()
			.model((c, p) -> p.blockItem(() -> c.getEntry()
				.getBlock(), "/block"))
			.build()
			.register();

	public static final BlockEntry<ControllerRailBlock> CONTROLLER_RAIL =
		REGISTRATE.block("controller_rail", ControllerRailBlock::new)
			.initialProperties(() -> Blocks.POWERED_RAIL)
			.blockstate(new ControllerRailGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.color(() -> ColorHandlers::getRedstonePower)
			.tag(BlockTags.RAILS)
			.item()
			.model((c, p) -> p.generated(c, Create.asResource("block/" + c.getName())))
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
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.lang("Linear Chassis")
			.simpleItem()
			.register();

	public static final BlockEntry<LinearChassisBlock> SECONDARY_LINEAR_CHASSIS =
		REGISTRATE.block("secondary_linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.simpleItem()
			.register();

	public static final BlockEntry<RadialChassisBlock> RADIAL_CHASSIS =
		REGISTRATE.block("radial_chassis", RadialChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.radialChassis())
			.item()
			.model((c, p) -> {
				String path = "block/" + c.getName();
				p.cubeColumn(c.getName(), p.modLoc(path + "_side"), p.modLoc(path + "_end"));
			})
			.build()
			.register();

	public static final BlockEntry<StickerBlock> STICKER = REGISTRATE.block("sticker", StickerBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(AbstractBlock.Properties::noOcclusion)
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<DrillBlock> MECHANICAL_DRILL = REGISTRATE.block("mechanical_drill", DrillBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new DrillMovementBehaviour()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SawBlock> MECHANICAL_SAW = REGISTRATE.block("mechanical_saw", SawBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(new SawGenerator()::generate)
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new SawMovementBehaviour()))
		.addLayer(() -> RenderType::cutoutMipped)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<DeployerBlock> DEPLOYER = REGISTRATE.block("deployer", DeployerBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalAxisBlockProvider())
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new DeployerMovementBehaviour()))
		.item(AssemblyOperatorBlockItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE.block("portable_storage_interface", PortableStorageInterfaceBlock::forItems)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(addMovementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<RedstoneContactBlock> REDSTONE_CONTACT =
		REGISTRATE.block("redstone_contact", RedstoneContactBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new ContactMovementBehaviour()))
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<HarvesterBlock> MECHANICAL_HARVESTER =
		REGISTRATE.block("mechanical_harvester", HarvesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new HarvesterMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PloughBlock> MECHANICAL_PLOUGH =
		REGISTRATE.block("mechanical_plough", PloughBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new PloughMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(false))
			.simpleItem()
			.register();

	public static final DyedBlockList<SeatBlock> SEATS = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
		return REGISTRATE.block(colourName + "_seat", p -> new SeatBlock(p, colour, colour == DyeColor.RED))
			.initialProperties(SharedProperties::wooden)
			.onRegister(addMovementBehaviour(movementBehaviour))
			.blockstate((c, p) -> {
				p.simpleBlock(c.get(), p.models()
					.withExistingParent(colourName + "_seat", p.modLoc("block/seat"))
					.texture("1", p.modLoc("block/seat/top_" + colourName))
					.texture("2", p.modLoc("block/seat/side_" + colourName)));
			})
			.recipe((c, p) -> {
				ShapelessRecipeBuilder.shapeless(c.get())
					.requires(DyeHelper.getWoolOfDye(colour))
					.requires(ItemTags.WOODEN_SLABS)
					.unlockedBy("has_wool", RegistrateRecipeProvider.hasItem(ItemTags.WOOL))
					.save(p, Create.asResource("crafting/kinetics/" + c.getName()));
				ShapelessRecipeBuilder.shapeless(c.get())
					.requires(colour.getTag())
					.requires(AllItemTags.SEATS.tag)
					.unlockedBy("has_seat", RegistrateRecipeProvider.hasItem(AllItemTags.SEATS.tag))
					.save(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_seat"));
			})
			.onRegisterAfter(Item.class, v -> TooltipHelper.referTo(v, "block.create.brown_seat"))
			.tag(AllBlockTags.SEATS.tag)
			.item()
			.tag(AllItemTags.SEATS.tag)
			.build()
			.register();
	});

	public static final BlockEntry<SailBlock> SAIL_FRAME = REGISTRATE.block("sail_frame", p -> SailBlock.frame(p))
		.initialProperties(SharedProperties::wooden)
		.properties(AbstractBlock.Properties::noOcclusion)
		.blockstate(BlockStateGen.directionalBlockProvider(false))
		.tag(AllBlockTags.WINDMILL_SAILS.tag)
		.tag(AllBlockTags.FAN_TRANSPARENT.tag)
		.simpleItem()
		.register();

	public static final BlockEntry<SailBlock> SAIL =
		REGISTRATE.block("white_sail", p -> SailBlock.withCanvas(p, DyeColor.WHITE))
			.initialProperties(SharedProperties::wooden)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.directionalBlockProvider(false))
			.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.simpleItem()
			.register();

	public static final DyedBlockList<SailBlock> DYED_SAILS = new DyedBlockList<>(colour -> {
		if (colour == DyeColor.WHITE) {
			return SAIL;
		}
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_sail", p -> SailBlock.withCanvas(p, colour))
			.properties(AbstractBlock.Properties::noOcclusion)
			.initialProperties(SharedProperties::wooden)
			.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
				.withExistingParent(colourName + "_sail", p.modLoc("block/white_sail"))
				.texture("0", p.modLoc("block/sail/canvas_" + colourName))))
			.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.tag(AllBlockTags.SAILS.tag)
			.loot((p, b) -> p.dropOther(b, SAIL.get()))
			.register();
	});

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
			.properties(p -> p.lightLevel($ -> 12))
			.lang("Radiant Casing")
			.register();

	public static final BlockEntry<MechanicalCrafterBlock> MECHANICAL_CRAFTER =
		REGISTRATE.block("mechanical_crafter", MechanicalCrafterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(AbstractBlock.Properties::noOcclusion)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setImpact(2.0))
			.onRegister(CreateRegistrate.connectedTextures(new CrafterCTBehaviour()))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SequencedGearshiftBlock> SEQUENCED_GEARSHIFT =
		REGISTRATE.block("sequenced_gearshift", SequencedGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.properties(AbstractBlock.Properties::noOcclusion)
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate(new SequencedGearshiftGenerator()::generate)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FlywheelBlock> FLYWHEEL = REGISTRATE.block("flywheel", FlywheelBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(AbstractBlock.Properties::noOcclusion)
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(new FlywheelGenerator()::generate)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<FurnaceEngineBlock> FURNACE_ENGINE =
		REGISTRATE.block("furnace_engine", FurnaceEngineBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setCapacity(1024.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SpeedControllerBlock> ROTATION_SPEED_CONTROLLER =
		REGISTRATE.block("rotation_speed_controller", SpeedControllerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(BlockStressDefaults.setNoImpact())
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
				.rotationX(s.getValue(ArmBlock.CEILING) ? 180 : 0)
				.build()))
		.transform(BlockStressDefaults.setImpact(2.0))
		.item(ArmItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<AndesiteFunnelBlock> ANDESITE_FUNNEL =
		REGISTRATE.block("andesite_funnel", AndesiteFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(FunnelMovementBehaviour.andesite()))
			.blockstate(new FunnelGenerator("andesite", false)::generate)
			.item(FunnelItem::new)
			.model(FunnelGenerator.itemModel("andesite"))
			.build()
			.register();

	public static final BlockEntry<BeltFunnelBlock> ANDESITE_BELT_FUNNEL =
		REGISTRATE.block("andesite_belt_funnel", p -> new BeltFunnelBlock(AllBlocks.ANDESITE_FUNNEL, p))
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("andesite", new ResourceLocation("block/polished_andesite"))::generate)
			.loot((p, b) -> p.dropOther(b, ANDESITE_FUNNEL.get()))
			.register();

	public static final BlockEntry<BrassFunnelBlock> BRASS_FUNNEL =
		REGISTRATE.block("brass_funnel", BrassFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(FunnelMovementBehaviour.brass()))
			.blockstate(new FunnelGenerator("brass", true)::generate)
			.item(FunnelItem::new)
			.model(FunnelGenerator.itemModel("brass"))
			.build()
			.register();

	public static final BlockEntry<BeltFunnelBlock> BRASS_BELT_FUNNEL =
		REGISTRATE.block("brass_belt_funnel", p -> new BeltFunnelBlock(AllBlocks.BRASS_FUNNEL, p))
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("brass", Create.asResource("block/brass_block"))::generate)
			.loot((p, b) -> p.dropOther(b, BRASS_FUNNEL.get()))
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

	public static final BlockEntry<ContentObserverBlock> CONTENT_OBSERVER =
		REGISTRATE.block("content_observer", ContentObserverBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel("_", "block"))
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
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<NixieTubeBlock> ORANGE_NIXIE_TUBE =
		REGISTRATE.block("nixie_tube", p -> new NixieTubeBlock(p, DyeColor.ORANGE))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel($ -> 5))
			.blockstate(new NixieTubeGenerator()::generate)
			.addLayer(() -> RenderType::translucent)
			.item()
			.transform(customItemModel())
			.register();

	public static final DyedBlockList<NixieTubeBlock> NIXIE_TUBES = new DyedBlockList<>(colour -> {
		if (colour == DyeColor.ORANGE)
			return ORANGE_NIXIE_TUBE;
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_nixie_tube", p -> new NixieTubeBlock(p, colour))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel($ -> 5))
			.blockstate(new NixieTubeGenerator()::generate)
			.loot((p, b) -> p.dropOther(b, ORANGE_NIXIE_TUBE.get()))
			.addLayer(() -> RenderType::translucent)
			.register();
	});

	public static final BlockEntry<RedstoneLinkBlock> REDSTONE_LINK =
		REGISTRATE.block("redstone_link", RedstoneLinkBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.BRITTLE.tag, AllBlockTags.SAFE_NBT.tag)
			.blockstate(new RedstoneLinkGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel("_", "transmitter"))
			.register();

	public static final BlockEntry<AnalogLeverBlock> ANALOG_LEVER =
		REGISTRATE.block("analog_lever", AnalogLeverBlock::new)
			.initialProperties(() -> Blocks.LEVER)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(ItemUseOverrides::addBlock)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PulseRepeaterBlock> PULSE_REPEATER =
		REGISTRATE.block("pulse_repeater", PulseRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new PulseRepeaterGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel("diodes", "pulse_repeater"))
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_REPEATER =
		REGISTRATE.block("adjustable_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_PULSE_REPEATER =
		REGISTRATE.block("adjustable_pulse_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<PoweredLatchBlock> POWERED_LATCH =
		REGISTRATE.block("powered_latch", PoweredLatchBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new PoweredLatchGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.simpleItem()
			.register();

	public static final BlockEntry<ToggleLatchBlock> POWERED_TOGGLE_LATCH =
		REGISTRATE.block("powered_toggle_latch", ToggleLatchBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.blockstate(new ToggleLatchGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel("diodes", "latch_off"))
			.register();

	public static final BlockEntry<LecternControllerBlock> LECTERN_CONTROLLER =
		REGISTRATE.block("lectern_controller", LecternControllerBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
				.getExistingFile(p.mcLoc("block/lectern"))))
			.loot((lt, block) -> lt.dropOther(block, Blocks.LECTERN))
			.register();

	// Curiosities

	static {
		REGISTRATE.startSection(AllSections.CURIOSITIES);
	}

	public static final BlockEntry<CopperBacktankBlock> COPPER_BACKTANK =
		REGISTRATE.block("copper_backtank", CopperBacktankBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(4.0))
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				IBuilder survivesExplosion = SurvivesExplosion.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantRange.exactly(1))
					.add(ItemLootEntry.lootTableItem(AllItems.COPPER_BACKTANK.get())
						.apply(CopyName.copyName(CopyName.Source.BLOCK_ENTITY))
						.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
							.copy("Air", "Air"))
						.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
							.copy("Enchantments", "Enchantments")))));
			})
			.register();

	public static final BlockEntry<PeculiarBellBlock> PECULIAR_BELL =
		REGISTRATE.block("peculiar_bell", PeculiarBellBlock::new)
			.transform(BuilderTransformers.bell())
			.onRegister(addMovementBehaviour(new BellMovementBehaviour()))
			.register();

	public static final BlockEntry<HauntedBellBlock> HAUNTED_BELL =
		REGISTRATE.block("haunted_bell", HauntedBellBlock::new)
			.transform(BuilderTransformers.bell())
			.onRegister(addMovementBehaviour(new HauntedBellMovementBehaviour()))
			.register();

	public static final DyedBlockList<ToolboxBlock> TOOLBOXES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_toolbox", p -> new ToolboxBlock(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.sound(SoundType.WOOD))
			.addLayer(() -> RenderType::cutoutMipped)
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				IBuilder survivesExplosion = SurvivesExplosion.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantRange.exactly(1))
					.add(ItemLootEntry.lootTableItem(block)
						.apply(CopyName.copyName(CopyName.Source.BLOCK_ENTITY))
						.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
							.copy("UniqueId", "UniqueId"))
						.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
							.copy("Inventory", "Inventory")))));
			})
			.blockstate((c, p) -> {
				p.horizontalBlock(c.get(), p.models()
					.withExistingParent(colourName + "_toolbox", p.modLoc("block/toolbox/block"))
					.texture("0", p.modLoc("block/toolbox/" + colourName)));
			})
			.onRegisterAfter(Item.class, v -> TooltipHelper.referTo(v, "block.create.toolbox"))
			.tag(AllBlockTags.TOOLBOXES.tag)
			.item()
			.model((c, p) -> p.withExistingParent(colourName + "_toolbox", p.modLoc("block/toolbox/item"))
				.texture("0", p.modLoc("block/toolbox/" + colourName)))
			.tag(AllItemTags.TOOLBOXES.tag)
			.build()
			.register();
	});

	// Materials

	static {
		REGISTRATE.startSection(AllSections.MATERIALS);
	}

	public static final BlockEntry<OxidizingBlock> COPPER_ORE =
		REGISTRATE.block("copper_ore", p -> new OxidizingBlock(p, 1))
			.initialProperties(() -> Blocks.IRON_ORE)
			.transform(oxidizedBlockstate())
			.tag(Tags.Blocks.ORES)
			.transform(tagBlockAndItem("ores/copper"))
			.tag(Tags.Items.ORES)
			.transform(oxidizedItemModel())
			.register();

	public static final BlockEntry<Block> ZINC_ORE = REGISTRATE.block("zinc_ore", Block::new)
		.initialProperties(() -> Blocks.GOLD_BLOCK)
		.properties(p -> p.harvestLevel(2)
			.harvestTool(ToolType.PICKAXE)
			.sound(SoundType.STONE))
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem("ores/zinc"))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<OxidizingBlock> COPPER_BLOCK =
		REGISTRATE.block("copper_block", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.tag(Tags.Blocks.STORAGE_BLOCKS)
			.tag(BlockTags.BEACON_BASE_BLOCKS)
			.transform(tagBlockAndItem("storage_blocks/copper"))
			.tag(Tags.Items.STORAGE_BLOCKS)
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.lang("Block of Copper")
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

	public static final BlockEntry<Block> ZINC_BLOCK = REGISTRATE.block("zinc_block", p -> new Block(p))
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem("storage_blocks/zinc"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Zinc")
		.register();

	public static final BlockEntry<Block> BRASS_BLOCK = REGISTRATE.block("brass_block", p -> new Block(p))
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.cubeAll(c.getName(), p.modLoc("block/brass_storage_block"))))
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem("storage_blocks/brass"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Brass")
		.register();

	// Load this class

	public static void register() {}

}
