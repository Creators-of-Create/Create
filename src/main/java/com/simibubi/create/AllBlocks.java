package com.simibubi.create;

import static com.simibubi.create.AllInteractionBehaviours.interactionBehaviour;
import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours.assignDataBehaviour;
import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.simibubi.create.foundation.data.TagGen.tagBlockAndItem;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovingInteraction;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.plough.PloughBlock;
import com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.actors.roller.RollerBlock;
import com.simibubi.create.content.contraptions.actors.roller.RollerBlockItem;
import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour;
import com.simibubi.create.content.contraptions.bearing.BlankSailBlockItem;
import com.simibubi.create.content.contraptions.bearing.ClockworkBearingBlock;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.bearing.SailBlock;
import com.simibubi.create.content.contraptions.bearing.StabilizedBearingMovementBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlock;
import com.simibubi.create.content.contraptions.behaviour.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.chassis.LinearChassisBlock.ChassisCTBehaviour;
import com.simibubi.create.content.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlock;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.decoration.MetalLadderBlock;
import com.simibubi.create.content.decoration.MetalScaffoldingBlock;
import com.simibubi.create.content.decoration.TrainTrapdoorBlock;
import com.simibubi.create.content.decoration.TrapdoorCTBehaviour;
import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.decoration.bracket.BracketBlockItem;
import com.simibubi.create.content.decoration.bracket.BracketGenerator;
import com.simibubi.create.content.decoration.copycat.CopycatBarsModel;
import com.simibubi.create.content.decoration.copycat.CopycatPanelBlock;
import com.simibubi.create.content.decoration.copycat.CopycatPanelModel;
import com.simibubi.create.content.decoration.copycat.CopycatStepBlock;
import com.simibubi.create.content.decoration.copycat.CopycatStepModel;
import com.simibubi.create.content.decoration.copycat.SpecialCopycatPanelBlockState;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.decoration.girder.ConnectedGirderModel;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderBlockStateGenerator;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.decoration.placard.PlacardBlock;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleGenerator;
import com.simibubi.create.content.equipment.armor.BacktankBlock;
import com.simibubi.create.content.equipment.bell.HauntedBellBlock;
import com.simibubi.create.content.equipment.bell.HauntedBellMovementBehaviour;
import com.simibubi.create.content.equipment.bell.PeculiarBellBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockItem;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.content.fluids.drain.ItemDrainBlock;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlock;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeGenerator;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.spout.SpoutBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankGenerator;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.content.fluids.tank.FluidTankModel;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltGenerator;
import com.simibubi.create.content.kinetics.belt.BeltModel;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveGenerator;
import com.simibubi.create.content.kinetics.chainDrive.ChainGearshiftBlock;
import com.simibubi.create.content.kinetics.clock.CuckooClockBlock;
import com.simibubi.create.content.kinetics.crafter.CrafterCTBehaviour;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.kinetics.crank.HandCrankBlock;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerMovingInteraction;
import com.simibubi.create.content.kinetics.drill.DrillBlock;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.fan.NozzleBlock;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlock;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.content.kinetics.gauge.GaugeGenerator;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlock;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorGenerator;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlock;
import com.simibubi.create.content.kinetics.saw.SawBlock;
import com.simibubi.create.content.kinetics.saw.SawGenerator;
import com.simibubi.create.content.kinetics.saw.SawMovementBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogCTBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.transmission.ClutchBlock;
import com.simibubi.create.content.kinetics.transmission.GearshiftBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftGenerator;
import com.simibubi.create.content.kinetics.turntable.TurntableBlock;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockItem;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import com.simibubi.create.content.logistics.chute.ChuteGenerator;
import com.simibubi.create.content.logistics.chute.ChuteItem;
import com.simibubi.create.content.logistics.chute.SmartChuteBlock;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlock;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.EjectorBlock;
import com.simibubi.create.content.logistics.depot.EjectorItem;
import com.simibubi.create.content.logistics.funnel.AndesiteFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelGenerator;
import com.simibubi.create.content.logistics.funnel.BrassFunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelGenerator;
import com.simibubi.create.content.logistics.funnel.FunnelItem;
import com.simibubi.create.content.logistics.funnel.FunnelMovementBehaviour;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelCTBehaviour;
import com.simibubi.create.content.logistics.vault.ItemVaultBlock;
import com.simibubi.create.content.logistics.vault.ItemVaultCTBehaviour;
import com.simibubi.create.content.logistics.vault.ItemVaultItem;
import com.simibubi.create.content.materials.ExperienceBlock;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinGenerator;
import com.simibubi.create.content.processing.basin.BasinMovementBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerInteractionBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerMovementBehaviour;
import com.simibubi.create.content.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.content.redstone.RoseQuartzLampBlock;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlock;
import com.simibubi.create.content.redstone.contact.ContactMovementBehaviour;
import com.simibubi.create.content.redstone.contact.RedstoneContactBlock;
import com.simibubi.create.content.redstone.contact.RedstoneContactItem;
import com.simibubi.create.content.redstone.diodes.AbstractDiodeGenerator;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlock;
import com.simibubi.create.content.redstone.diodes.BrassDiodeGenerator;
import com.simibubi.create.content.redstone.diodes.PoweredLatchBlock;
import com.simibubi.create.content.redstone.diodes.PoweredLatchGenerator;
import com.simibubi.create.content.redstone.diodes.ToggleLatchBlock;
import com.simibubi.create.content.redstone.diodes.ToggleLatchGenerator;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockItem;
import com.simibubi.create.content.redstone.displayLink.source.AccumulatedItemCountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.BoilerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.CurrentFloorDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.EntityNameDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FillLevelDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FluidAmountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FluidListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemCountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemNameDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemThroughputDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.KineticSpeedDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.KineticStressDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ObservedTrainNameSource;
import com.simibubi.create.content.redstone.displayLink.source.StationSummaryDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.StopWatchDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.TimeOfDayDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.TrainStatusDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayBoardTarget;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.content.redstone.link.RedstoneLinkGenerator;
import com.simibubi.create.content.redstone.link.controller.LecternControllerBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeGenerator;
import com.simibubi.create.content.redstone.rail.ControllerRailBlock;
import com.simibubi.create.content.redstone.rail.ControllerRailGenerator;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlock;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverGenerator;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchGenerator;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlock;
import com.simibubi.create.content.schematics.table.SchematicTableBlock;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.StandardBogeyBlock;
import com.simibubi.create.content.trains.display.FlapDisplayBlock;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.observer.TrackObserverBlock;
import com.simibubi.create.content.trains.signal.SignalBlock;
import com.simibubi.create.content.trains.station.StationBlock;
import com.simibubi.create.content.trains.track.FakeTrackBlock;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.simibubi.create.content.trains.track.TrackBlockStateGenerator;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackModel;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.simibubi.create.foundation.block.CopperBlockSet;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.MetalBarsGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.UncontainableBlockItem;
import com.simibubi.create.foundation.utility.ColorHandlers;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.ForgeSoundType;

public class AllBlocks {

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
	}

	// Schematics

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
			.transform(pickaxeOnly())
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(AllBlocks.SCHEMATICANNON.get()
						.asItem())
						.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
							.copy("Options", "BlockEntityTag.Options")))));
			})
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.properties(p -> p.mapColor(MapColor.PODZOL).forceSolidOn())
			.transform(axeOrPickaxe())
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
				.getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	// Kinetics

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
		.transform(BlockStressDefaults.setNoImpact())
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.DIRT))
		.transform(BlockStressDefaults.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
			.item(CogwheelBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<EncasedShaftBlock> ANDESITE_ENCASED_SHAFT =
		REGISTRATE.block("andesite_encased_shaft", p -> new EncasedShaftBlock(p, AllBlocks.ANDESITE_CASING::get))
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(BuilderTransformers.encasedShaft("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
			.transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
			.transform(axeOrPickaxe())
			.register();

	public static final BlockEntry<EncasedShaftBlock> BRASS_ENCASED_SHAFT =
		REGISTRATE.block("brass_encased_shaft", p -> new EncasedShaftBlock(p, AllBlocks.BRASS_CASING::get))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(BuilderTransformers.encasedShaft("brass", () -> AllSpriteShifts.BRASS_CASING))
			.transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
			.transform(axeOrPickaxe())
			.register();

	public static final BlockEntry<EncasedCogwheelBlock> ANDESITE_ENCASED_COGWHEEL = REGISTRATE
		.block("andesite_encased_cogwheel", p -> new EncasedCogwheelBlock(p, false, AllBlocks.ANDESITE_CASING::get))
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(BuilderTransformers.encasedCogwheel("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
		.transform(EncasingRegistry.addVariantTo(AllBlocks.COGWHEEL))
		.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCogCTBehaviour(AllSpriteShifts.ANDESITE_CASING,
			Couple.create(AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_SIDE,
				AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_OTHERSIDE))))
		.transform(axeOrPickaxe())
		.register();

	public static final BlockEntry<EncasedCogwheelBlock> BRASS_ENCASED_COGWHEEL =
		REGISTRATE.block("brass_encased_cogwheel", p -> new EncasedCogwheelBlock(p, false, AllBlocks.BRASS_CASING::get))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(BuilderTransformers.encasedCogwheel("brass", () -> AllSpriteShifts.BRASS_CASING))
			.transform(EncasingRegistry.addVariantTo(AllBlocks.COGWHEEL))
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCogCTBehaviour(AllSpriteShifts.BRASS_CASING,
				Couple.create(AllSpriteShifts.BRASS_ENCASED_COGWHEEL_SIDE,
					AllSpriteShifts.BRASS_ENCASED_COGWHEEL_OTHERSIDE))))
			.transform(axeOrPickaxe())
			.register();

	public static final BlockEntry<EncasedCogwheelBlock> ANDESITE_ENCASED_LARGE_COGWHEEL = REGISTRATE
		.block("andesite_encased_large_cogwheel",
			p -> new EncasedCogwheelBlock(p, true, AllBlocks.ANDESITE_CASING::get))
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(BuilderTransformers.encasedLargeCogwheel("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
		.transform(EncasingRegistry.addVariantTo(AllBlocks.LARGE_COGWHEEL))
		.transform(axeOrPickaxe())
		.register();

	public static final BlockEntry<EncasedCogwheelBlock> BRASS_ENCASED_LARGE_COGWHEEL = REGISTRATE
		.block("brass_encased_large_cogwheel", p -> new EncasedCogwheelBlock(p, true, AllBlocks.BRASS_CASING::get))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
		.transform(BuilderTransformers.encasedLargeCogwheel("brass", () -> AllSpriteShifts.BRASS_CASING))
		.transform(EncasingRegistry.addVariantTo(AllBlocks.LARGE_COGWHEEL))
		.transform(axeOrPickaxe())
		.register();

	public static final BlockEntry<GearboxBlock> GEARBOX = REGISTRATE.block("gearbox", GearboxBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
		.transform(BlockStressDefaults.setNoImpact())
		.transform(axeOrPickaxe())
		.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
		.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
			(s, f) -> f.getAxis() == s.getValue(GearboxBlock.AXIS))))
		.blockstate((c, p) -> axisBlock(c, p, $ -> AssetLookup.partialBaseModel(c, p), true))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ClutchBlock> CLUTCH = REGISTRATE.block("clutch", ClutchBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(BlockStressDefaults.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(BlockStressDefaults.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ChainDriveBlock> ENCASED_CHAIN_DRIVE =
		REGISTRATE.block("encased_chain_drive", ChainDriveBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
			.transform(BlockStressDefaults.setNoImpact())
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ChainGearshiftBlock> ADJUSTABLE_CHAIN_GEARSHIFT =
		REGISTRATE.block("adjustable_chain_gearshift", ChainGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.NETHER))
			.transform(BlockStressDefaults.setNoImpact())
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> {
				String powered = state.getValue(ChainGearshiftBlock.POWERED) ? "_powered" : "";
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
		.properties(p -> p.sound(SoundType.WOOL)
			.strength(0.8f)
			.mapColor(MapColor.COLOR_GRAY))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(axeOrPickaxe())
		.blockstate(new BeltGenerator()::generate)
		.transform(BlockStressDefaults.setImpact(0))
		.onRegister(assignDataBehaviour(new ItemNameDisplaySource(), "combine_item_names"))
		.onRegister(CreateRegistrate.blockModel(() -> BeltModel::new))
		.register();

	public static final BlockEntry<CreativeMotorBlock> CREATIVE_MOTOR =
		REGISTRATE.block("creative_motor", CreativeMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_PURPLE).forceSolidOn())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(pickaxeOnly())
			.blockstate(new CreativeMotorGenerator()::generate)
			.transform(BlockStressDefaults.setCapacity(16384.0))
			.transform(BlockStressDefaults.setGeneratorSpeed(() -> Couple.create(0, 256)))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelBlock> WATER_WHEEL = REGISTRATE.block("water_wheel", WaterWheelBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.noOcclusion().mapColor(MapColor.DIRT))
		.transform(axeOrPickaxe())
		.blockstate(
			(c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, s -> AssetLookup.partialBaseModel(c, p)))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(BlockStressDefaults.setCapacity(32.0))
		.transform(BlockStressDefaults.setGeneratorSpeed(WaterWheelBlock::getSpeedRange))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<LargeWaterWheelBlock> LARGE_WATER_WHEEL =
		REGISTRATE.block("large_water_wheel", LargeWaterWheelBlock::new)
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.noOcclusion().mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> axisBlock(c, p,
				s -> s.getValue(LargeWaterWheelBlock.EXTENSION) ? AssetLookup.partialBaseModel(c, p, "extension")
					: AssetLookup.partialBaseModel(c, p)))
			.transform(BlockStressDefaults.setCapacity(128.0))
			.transform(BlockStressDefaults.setGeneratorSpeed(LargeWaterWheelBlock::getSpeedRange))
			.item(LargeWaterWheelBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelStructuralBlock> WATER_WHEEL_STRUCTURAL =
		REGISTRATE.block("water_wheel_structure", WaterWheelStructuralBlock::new)
			.initialProperties(SharedProperties::wooden)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStatesExcept(BlockStateGen.mapToAir(p), WaterWheelStructuralBlock.FACING))
			.properties(p -> p.noOcclusion().mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.lang("Large Water Wheel")
			.register();

	public static final BlockEntry<EncasedFanBlock> ENCASED_FAN = REGISTRATE.block("encased_fan", EncasedFanBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(axeOrPickaxe())
		.transform(BlockStressDefaults.setImpact(2.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<NozzleBlock> NOZZLE = REGISTRATE.block("nozzle", NozzleBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY))
		.tag(AllBlockTags.BRITTLE.tag)
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::cutoutMipped)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TurntableBlock> TURNTABLE = REGISTRATE.block("turntable", TurntableBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
		.transform(BlockStressDefaults.setImpact(4.0))
		.simpleItem()
		.register();

	public static final BlockEntry<HandCrankBlock> HAND_CRANK = REGISTRATE.block("hand_crank", HandCrankBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(BlockStressDefaults.setCapacity(8.0))
		.transform(BlockStressDefaults.setGeneratorSpeed(HandCrankBlock::getSpeedRange))
		.tag(AllBlockTags.BRITTLE.tag)
		.onRegister(ItemUseOverrides::addBlock)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CuckooClockBlock> CUCKOO_CLOCK =
		REGISTRATE.block("cuckoo_clock", CuckooClockBlock::regular)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.cuckooClock())
			.onRegister(assignDataBehaviour(new TimeOfDayDisplaySource(), "time_of_day"))
			.onRegister(assignDataBehaviour(new StopWatchDisplaySource(), "stop_watch"))
			.register();

	public static final BlockEntry<CuckooClockBlock> MYSTERIOUS_CUCKOO_CLOCK =
		REGISTRATE.block("mysterious_cuckoo_clock", CuckooClockBlock::mysterious)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.cuckooClock())
			.lang("Cuckoo Clock")
			.onRegisterAfter(Registries.ITEM, c -> ItemDescription.referKey(c, CUCKOO_CLOCK))
			.register();

	public static final BlockEntry<MillstoneBlock> MILLSTONE = REGISTRATE.block("millstone", MillstoneBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.METAL))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CrushingWheelBlock> CRUSHING_WHEEL =
		REGISTRATE.block("crushing_wheel", CrushingWheelBlock::new)
			.properties(p -> p.mapColor(MapColor.METAL))
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.transform(pickaxeOnly())
			.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(8.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<CrushingWheelControllerBlock> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE.block("crushing_wheel_controller", CrushingWheelControllerBlock::new)
			.properties(p -> p.mapColor(MapColor.STONE)
				.noOcclusion()
				.noLootTable()
				.air()
				.noCollission()
				.pushReaction(PushReaction.BLOCK))
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStatesExcept(BlockStateGen.mapToAir(p), CrushingWheelControllerBlock.FACING))
			.register();

	public static final BlockEntry<MechanicalPressBlock> MECHANICAL_PRESS =
		REGISTRATE.block("mechanical_press", MechanicalPressBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setImpact(8.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<MechanicalMixerBlock> MECHANICAL_MIXER =
		REGISTRATE.block("mechanical_mixer", MechanicalMixerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.STONE))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(4.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BasinBlock> BASIN = REGISTRATE.block("basin", BasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate(new BasinGenerator()::generate)
		.addLayer(() -> RenderType::cutoutMipped)
		.onRegister(movementBehaviour(new BasinMovementBehaviour()))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<BlazeBurnerBlock> BLAZE_BURNER =
		REGISTRATE.block("blaze_burner", BlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBurnerBlock::getLight))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag, AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
			.loot((lt, block) -> lt.add(block, BlazeBurnerBlock.buildLootTable()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(movementBehaviour(new BlazeBurnerMovementBehaviour()))
			.onRegister(interactionBehaviour(new BlazeBurnerInteractionBehaviour()))
			.item(BlazeBurnerBlockItem::withBlaze)
			.model(AssetLookup.customBlockItemModel("blaze_burner", "block_with_blaze"))
			.build()
			.register();

	public static final BlockEntry<LitBlazeBurnerBlock> LIT_BLAZE_BURNER =
		REGISTRATE.block("lit_blaze_burner", LitBlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).lightLevel(LitBlazeBurnerBlock::getLight))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag, AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
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
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.onRegister(assignDataBehaviour(new ItemNameDisplaySource(), "combine_item_names"))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<EjectorBlock> WEIGHTED_EJECTOR =
		REGISTRATE.block("weighted_ejector", EjectorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_GRAY))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p), 180))
			.transform(BlockStressDefaults.setImpact(2.0))
			.onRegister(assignDataBehaviour(new ItemNameDisplaySource(), "combine_item_names"))
			.item(EjectorItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ChuteBlock> CHUTE = REGISTRATE.block("chute", ChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate(new ChuteGenerator()::generate)
		.item(ChuteItem::new)
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<SmartChuteBlock> SMART_CHUTE = REGISTRATE.block("smart_chute", SmartChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((level, pos, state) -> false))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(pickaxeOnly())
		.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<GaugeBlock> SPEEDOMETER = REGISTRATE.block("speedometer", GaugeBlock::speed)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.onRegister(assignDataBehaviour(new KineticSpeedDisplaySource(), "kinetic_speed"))
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<GaugeBlock> STRESSOMETER = REGISTRATE.block("stressometer", GaugeBlock::stress)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.onRegister(assignDataBehaviour(new KineticStressDisplaySource(), "kinetic_stress"))
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<BracketBlock> WOODEN_BRACKET = REGISTRATE.block("wooden_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("wooden")::generate)
		.properties(p -> p.sound(SoundType.SCAFFOLDING))
		.transform(axeOrPickaxe())
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("wooden"))
		.register();

	public static final BlockEntry<BracketBlock> METAL_BRACKET = REGISTRATE.block("metal_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("metal")::generate)
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("metal"))
		.register();

	// Fluids

	public static final BlockEntry<FluidPipeBlock> FLUID_PIPE = REGISTRATE.block("fluid_pipe", FluidPipeBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.forceSolidOn())
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.pipe())
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedPipeBlock> ENCASED_FLUID_PIPE =
		REGISTRATE.block("encased_fluid_pipe", p -> new EncasedPipeBlock(p, AllBlocks.COPPER_CASING::get))
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.encasedPipe())
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.COPPER_CASING)))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.COPPER_CASING,
				(s, f) -> !s.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.transform(EncasingRegistry.addVariantTo(AllBlocks.FLUID_PIPE))
			.register();

	public static final BlockEntry<GlassFluidPipeBlock> GLASS_FLUID_PIPE =
		REGISTRATE.block("glass_fluid_pipe", GlassFluidPipeBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.forceSolidOn())
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(pickaxeOnly())
			.blockstate((c, p) -> {
				p.getVariantBuilder(c.getEntry())
					.forAllStatesExcept(state -> {
						Axis axis = state.getValue(BlockStateProperties.AXIS);
						return ConfiguredModel.builder()
							.modelFile(p.models()
								.getExistingFile(p.modLoc("block/fluid_pipe/window")))
							.uvLock(false)
							.rotationX(axis == Axis.Y ? 0 : 90)
							.rotationY(axis == Axis.X ? 90 : 0)
							.build();
					}, BlockStateProperties.WATERLOGGED);
			})
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<PumpBlock> MECHANICAL_PUMP = REGISTRATE.block("mechanical_pump", PumpBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.mapColor(MapColor.STONE))
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SmartFluidPipeBlock> SMART_FLUID_PIPE =
		REGISTRATE.block("smart_fluid_pipe", SmartFluidPipeBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(pickaxeOnly())
			.blockstate(new SmartFluidPipeGenerator()::generate)
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FluidValveBlock> FLUID_VALVE = REGISTRATE.block("fluid_valve", FluidValveBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.transform(pickaxeOnly())
		.blockstate((c, p) -> BlockStateGen.directionalAxisBlock(c, p,
			(state, vertical) -> AssetLookup.partialBaseModel(c, p, vertical ? "vertical" : "horizontal",
				state.getValue(FluidValveBlock.ENABLED) ? "open" : "closed")))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ValveHandleBlock> COPPER_VALVE_HANDLE =
		REGISTRATE.block("copper_valve_handle", ValveHandleBlock::copper)
			.transform(pickaxeOnly())
			.transform(BuilderTransformers.valveHandle(null))
			.transform(BlockStressDefaults.setCapacity(8.0))
			.register();

	public static final DyedBlockList<ValveHandleBlock> DYED_VALVE_HANDLES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_valve_handle", p -> ValveHandleBlock.dyed(p, colour))
			.properties(p -> p.mapColor(colour.getMapColor()))
			.transform(pickaxeOnly())
			.transform(BuilderTransformers.valveHandle(colour))
			.recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get())
				.requires(colour.getTag())
				.requires(AllItemTags.VALVE_HANDLES.tag)
				.unlockedBy("has_valve", RegistrateRecipeProvider.has(AllItemTags.VALVE_HANDLES.tag))
				.save(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_valve_handle")))
			.register();
	});

	public static final BlockEntry<FluidTankBlock> FLUID_TANK = REGISTRATE.block("fluid_tank", FluidTankBlock::regular)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
		.transform(pickaxeOnly())
		.blockstate(new FluidTankGenerator()::generate)
		.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::standard))
		.onRegister(assignDataBehaviour(new BoilerDisplaySource(), "boiler_status"))
		.addLayer(() -> RenderType::cutoutMipped)
		.item(FluidTankItem::new)
		.model(AssetLookup.customBlockItemModel("_", "block_single_window"))
		.build()
		.register();

	public static final BlockEntry<FluidTankBlock> CREATIVE_FLUID_TANK =
		REGISTRATE.block("creative_fluid_tank", FluidTankBlock::creative)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_PURPLE))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new FluidTankGenerator("creative_")::generate)
			.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::creative))
			.addLayer(() -> RenderType::cutoutMipped)
			.item(FluidTankItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/fluid_tank/block_single_window"))
				.texture("5", p.modLoc("block/creative_fluid_tank_window_single"))
				.texture("1", p.modLoc("block/creative_fluid_tank"))
				.texture("particle", p.modLoc("block/creative_fluid_tank"))
				.texture("4", p.modLoc("block/creative_casing"))
				.texture("0", p.modLoc("block/creative_casing")))
			.build()
			.register();

	public static final BlockEntry<HosePulleyBlock> HOSE_PULLEY = REGISTRATE.block("hose_pulley", HosePulleyBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(BlockBehaviour.Properties::noOcclusion)
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.horizontalBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ItemDrainBlock> ITEM_DRAIN = REGISTRATE.block("item_drain", ItemDrainBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.standardModel(c, p)))
		.simpleItem()
		.register();

	public static final BlockEntry<SpoutBlock> SPOUT = REGISTRATE.block("spout", SpoutBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.transform(pickaxeOnly())
		.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
		.addLayer(() -> RenderType::cutoutMipped)
		.item(AssemblyOperatorBlockItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_FLUID_INTERFACE =
		REGISTRATE.block("portable_fluid_interface", PortableStorageInterfaceBlock::forFluids)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(movementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SteamEngineBlock> STEAM_ENGINE =
		REGISTRATE.block("steam_engine", SteamEngineBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.transform(pickaxeOnly())
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.transform(BlockStressDefaults.setCapacity(1024.0))
			.transform(BlockStressDefaults.setGeneratorSpeed(SteamEngineBlock::getSpeedRange))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WhistleBlock> STEAM_WHISTLE = REGISTRATE.block("steam_whistle", WhistleBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.mapColor(MapColor.GOLD))
		.transform(pickaxeOnly())
		.blockstate(new WhistleGenerator()::generate)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<WhistleExtenderBlock> STEAM_WHISTLE_EXTENSION =
		REGISTRATE.block("steam_whistle_extension", WhistleExtenderBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.mapColor(MapColor.GOLD).forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate(BlockStateGen.whistleExtender())
			.register();

	public static final BlockEntry<PoweredShaftBlock> POWERED_SHAFT =
		REGISTRATE.block("powered_shaft", PoweredShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.loot((lt, block) -> lt.dropOther(block, AllBlocks.SHAFT.get()))
			.register();

	// Contraptions

	public static final BlockEntry<MechanicalPistonBlock> MECHANICAL_PISTON =
		REGISTRATE.block("mechanical_piston", MechanicalPistonBlock::normal)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.mechanicalPiston(PistonType.DEFAULT))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalPistonBlock> STICKY_MECHANICAL_PISTON =
		REGISTRATE.block("sticky_mechanical_piston", MechanicalPistonBlock::sticky)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.mechanicalPiston(PistonType.STICKY))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PistonExtensionPoleBlock> PISTON_EXTENSION_POLE =
		REGISTRATE.block("piston_extension_pole", PistonExtensionPoleBlock::new)
			.initialProperties(() -> Blocks.PISTON_HEAD)
			.properties(p -> p.sound(SoundType.SCAFFOLDING).mapColor(MapColor.DIRT).forceSolidOn())
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(false))
			.simpleItem()
			.register();

	public static final BlockEntry<MechanicalPistonHeadBlock> MECHANICAL_PISTON_HEAD =
		REGISTRATE.block("mechanical_piston_head", MechanicalPistonHeadBlock::new)
			.initialProperties(() -> Blocks.PISTON_HEAD)
			.properties(p -> p.mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.loot((p, b) -> p.dropOther(b, PISTON_EXTENSION_POLE.get()))
			.blockstate((c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, state -> p.models()
				.getExistingFile(p.modLoc("block/mechanical_piston/" + state.getValue(MechanicalPistonHeadBlock.TYPE)
					.getSerializedName() + "/head"))))
			.register();

	public static final BlockEntry<GantryCarriageBlock> GANTRY_CARRIAGE =
		REGISTRATE.block("gantry_carriage", GantryCarriageBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.directionalAxisBlockProvider())
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<GantryShaftBlock> GANTRY_SHAFT =
		REGISTRATE.block("gantry_shaft", GantryShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.NETHER).forceSolidOn())
			.transform(axeOrPickaxe())
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
			.transform(axeOrPickaxe())
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(BuilderTransformers.bearing("windmill", "gearbox"))
			.transform(BlockStressDefaults.setCapacity(512.0))
			.transform(BlockStressDefaults.setGeneratorSpeed(WindmillBearingBlock::getSpeedRange))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalBearingBlock> MECHANICAL_BEARING =
		REGISTRATE.block("mechanical_bearing", MechanicalBearingBlock::new)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.bearing("mechanical", "gearbox"))
			.transform(BlockStressDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(movementBehaviour(new StabilizedBearingMovementBehaviour()))
			.register();

	public static final BlockEntry<ClockworkBearingBlock> CLOCKWORK_BEARING =
		REGISTRATE.block("clockwork_bearing", ClockworkBearingBlock::new)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.bearing("clockwork", "brass_gearbox"))
			.transform(BlockStressDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PulleyBlock> ROPE_PULLEY = REGISTRATE.block("rope_pulley", PulleyBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.tag(AllBlockTags.SAFE_NBT.tag)
		.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PulleyBlock.RopeBlock> ROPE = REGISTRATE.block("rope", PulleyBlock.RopeBlock::new)
		.properties(p -> p.sound(SoundType.WOOL)
			.mapColor(MapColor.COLOR_BROWN))
		.tag(AllBlockTags.BRITTLE.tag)
		.tag(BlockTags.CLIMBABLE)
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
		.register();

	public static final BlockEntry<PulleyBlock.MagnetBlock> PULLEY_MAGNET =
		REGISTRATE.block("pulley_magnet", PulleyBlock.MagnetBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.BRITTLE.tag)
			.tag(BlockTags.CLIMBABLE)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
			.register();

	public static final BlockEntry<ElevatorPulleyBlock> ELEVATOR_PULLEY =
		REGISTRATE.block("elevator_pulley", ElevatorPulleyBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setImpact(4.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<CartAssemblerBlock> CART_ASSEMBLER =
		REGISTRATE.block("cart_assembler", CartAssemblerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_GRAY))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.cartAssembler())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(BlockTags.RAILS, AllBlockTags.SAFE_NBT.tag)
			.item(CartAssemblerBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ControllerRailBlock> CONTROLLER_RAIL =
		REGISTRATE.block("controller_rail", ControllerRailBlock::new)
			.initialProperties(() -> Blocks.POWERED_RAIL)
			.transform(pickaxeOnly())
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
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(ChassisCTBehaviour::new))
			.lang("Linear Chassis")
			.simpleItem()
			.register();

	public static final BlockEntry<LinearChassisBlock> SECONDARY_LINEAR_CHASSIS =
		REGISTRATE.block("secondary_linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(ChassisCTBehaviour::new))
			.simpleItem()
			.register();

	public static final BlockEntry<RadialChassisBlock> RADIAL_CHASSIS =
		REGISTRATE.block("radial_chassis", RadialChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
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
		.transform(pickaxeOnly())
		.properties(BlockBehaviour.Properties::noOcclusion)
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ContraptionControlsBlock> CONTRAPTION_CONTROLS =
		REGISTRATE.block("contraption_controls", ContraptionControlsBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.horizontalBlock(c.get(), s -> AssetLookup.partialBaseModel(c, p)))
			.onRegister(movementBehaviour(new ContraptionControlsMovement()))
			.onRegister(interactionBehaviour(new ContraptionControlsMovingInteraction()))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<DrillBlock> MECHANICAL_DRILL = REGISTRATE.block("mechanical_drill", DrillBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(movementBehaviour(new DrillMovementBehaviour()))
		.item()
		.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SawBlock> MECHANICAL_SAW = REGISTRATE.block("mechanical_saw", SawBlock::new)
		.initialProperties(SharedProperties::stone)
		.addLayer(() -> RenderType::cutoutMipped)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate(new SawGenerator()::generate)
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(movementBehaviour(new SawMovementBehaviour()))
		.addLayer(() -> RenderType::cutoutMipped)
		.item()
		.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<DeployerBlock> DEPLOYER = REGISTRATE.block("deployer", DeployerBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.directionalAxisBlockProvider())
		.transform(BlockStressDefaults.setImpact(4.0))
		.onRegister(movementBehaviour(new DeployerMovementBehaviour()))
		.onRegister(interactionBehaviour(new DeployerMovingInteraction()))
		.item(AssemblyOperatorBlockItem::new)
		.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE.block("portable_storage_interface", PortableStorageInterfaceBlock::forItems)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(movementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<RedstoneContactBlock> REDSTONE_CONTACT =
		REGISTRATE.block("redstone_contact", RedstoneContactBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
			.transform(axeOrPickaxe())
			.onRegister(movementBehaviour(new ContactMovementBehaviour()))
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item(RedstoneContactItem::new)
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<ElevatorContactBlock> ELEVATOR_CONTACT =
		REGISTRATE.block("elevator_contact", ElevatorContactBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).lightLevel(ElevatorContactBlock::getLight))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.directionalBlock(c.get(), state -> {
				Boolean calling = state.getValue(ElevatorContactBlock.CALLING);
				Boolean powering = state.getValue(ElevatorContactBlock.POWERING);
				return powering ? AssetLookup.partialBaseModel(c, p, "powered")
					: calling ? AssetLookup.partialBaseModel(c, p, "dim") : AssetLookup.partialBaseModel(c, p);
			}))
			.loot((p, b) -> p.dropOther(b, REDSTONE_CONTACT.get()))
			.onRegister(assignDataBehaviour(new CurrentFloorDisplaySource(), "current_floor"))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<HarvesterBlock> MECHANICAL_HARVESTER =
		REGISTRATE.block("mechanical_harvester", HarvesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
			.transform(axeOrPickaxe())
			.onRegister(movementBehaviour(new HarvesterMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PloughBlock> MECHANICAL_PLOUGH =
		REGISTRATE.block("mechanical_plough", PloughBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY).forceSolidOn())
			.transform(axeOrPickaxe())
			.onRegister(movementBehaviour(new PloughMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(false))
			.item()
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.build()
			.register();

	public static final BlockEntry<RollerBlock> MECHANICAL_ROLLER =
		REGISTRATE.block("mechanical_roller", RollerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
				.noOcclusion())
			.transform(axeOrPickaxe())
			.onRegister(movementBehaviour(new RollerMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.addLayer(() -> RenderType::cutoutMipped)
			.item(RollerBlockItem::new)
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SailBlock> SAIL_FRAME = REGISTRATE.block("sail_frame", p -> SailBlock.frame(p))
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.DIRT)
			.sound(SoundType.SCAFFOLDING)
			.noOcclusion())
		.transform(axeOnly())
		.blockstate(BlockStateGen.directionalBlockProvider(false))
		.lang("Windmill Sail Frame")
		.tag(AllBlockTags.WINDMILL_SAILS.tag)
		.tag(AllBlockTags.FAN_TRANSPARENT.tag)
		.simpleItem()
		.register();

	public static final BlockEntry<SailBlock> SAIL =
		REGISTRATE.block("white_sail", p -> SailBlock.withCanvas(p, DyeColor.WHITE))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(MapColor.SNOW)
				.sound(SoundType.SCAFFOLDING)
				.noOcclusion())
			.transform(axeOnly())
			.blockstate(BlockStateGen.directionalBlockProvider(false))
			.lang("Windmill Sail")
			.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.item(BlankSailBlockItem::new)
			.build()
			.register();

	public static final DyedBlockList<SailBlock> DYED_SAILS = new DyedBlockList<>(colour -> {
		if (colour == DyeColor.WHITE) {
			return SAIL;
		}
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_sail", p -> SailBlock.withCanvas(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(colour.getMapColor())
				.sound(SoundType.SCAFFOLDING)
				.noOcclusion())
			.transform(axeOnly())
			.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
				.withExistingParent(colourName + "_sail", p.modLoc("block/white_sail"))
				.texture("0", p.modLoc("block/sail/canvas_" + colourName))))
			.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.loot((p, b) -> p.dropOther(b, SAIL.get()))
			.register();
	});

	public static final BlockEntry<CasingBlock> ANDESITE_CASING = REGISTRATE.block("andesite_casing", CasingBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(BuilderTransformers.casing(() -> AllSpriteShifts.ANDESITE_CASING))
		.register();

	public static final BlockEntry<CasingBlock> BRASS_CASING = REGISTRATE.block("brass_casing", CasingBlock::new)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
		.transform(BuilderTransformers.casing(() -> AllSpriteShifts.BRASS_CASING))
		.register();

	public static final BlockEntry<CasingBlock> COPPER_CASING = REGISTRATE.block("copper_casing", CasingBlock::new)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sound(SoundType.COPPER))
		.transform(BuilderTransformers.casing(() -> AllSpriteShifts.COPPER_CASING))
		.register();

	public static final BlockEntry<CasingBlock> SHADOW_STEEL_CASING =
		REGISTRATE.block("shadow_steel_casing", CasingBlock::new)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.transform(BuilderTransformers.casing(() -> AllSpriteShifts.SHADOW_STEEL_CASING))
			.lang("Shadow Casing")
			.register();

	public static final BlockEntry<CasingBlock> REFINED_RADIANCE_CASING =
		REGISTRATE.block("refined_radiance_casing", CasingBlock::new)
			.properties(p -> p.mapColor(MapColor.SNOW))
			.transform(BuilderTransformers.casing(() -> AllSpriteShifts.REFINED_RADIANCE_CASING))
			.properties(p -> p.lightLevel($ -> 12))
			.lang("Radiant Casing")
			.register();

	public static final BlockEntry<MechanicalCrafterBlock> MECHANICAL_CRAFTER =
		REGISTRATE.block("mechanical_crafter", MechanicalCrafterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(BlockStressDefaults.setImpact(2.0))
			.onRegister(CreateRegistrate.connectedTextures(CrafterCTBehaviour::new))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SequencedGearshiftBlock> SEQUENCED_GEARSHIFT =
		REGISTRATE.block("sequenced_gearshift", SequencedGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate(new SequencedGearshiftGenerator()::generate)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FlywheelBlock> FLYWHEEL = REGISTRATE.block("flywheel", FlywheelBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_YELLOW))
		.transform(axeOrPickaxe())
		.transform(BlockStressDefaults.setNoImpact())
		.blockstate(BlockStateGen.axisBlockProvider(true))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SpeedControllerBlock> ROTATION_SPEED_CONTROLLER =
		REGISTRATE.block("rotation_speed_controller", SpeedControllerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(BlockStressDefaults.setNoImpact())
			.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
			.item()
			.transform(customItemModel())
			.register();

	// Logistics

	public static final BlockEntry<ArmBlock> MECHANICAL_ARM = REGISTRATE.block("mechanical_arm", ArmBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.partialBaseModel(c, p))
				.rotationX(s.getValue(ArmBlock.CEILING) ? 180 : 0)
				.build()))
		.transform(BlockStressDefaults.setImpact(2.0))
		.item(ArmItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TrackBlock> TRACK = REGISTRATE.block("track", TrackMaterial.ANDESITE::createBlock)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.METAL)
			.strength(0.8F)
			.sound(SoundType.METAL)
			.noOcclusion()
			.forceSolidOn())
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(pickaxeOnly())
		.onRegister(CreateRegistrate.blockModel(() -> TrackModel::new))
		.blockstate(new TrackBlockStateGenerator()::generate)
		.tag(AllBlockTags.RELOCATION_NOT_SUPPORTED.tag)
		.tag(AllBlockTags.TRACKS.tag)
		.tag(AllBlockTags.GIRDABLE_TRACKS.tag)
		.lang("Train Track")
		.item(TrackBlockItem::new)
		.tag(AllItemTags.TRACKS.tag)
		.model((c, p) -> p.generated(c, Create.asResource("item/" + c.getName())))
		.build()
		.register();

	public static final BlockEntry<FakeTrackBlock> FAKE_TRACK = REGISTRATE.block("fake_track", FakeTrackBlock::new)
		.properties(p -> p.mapColor(MapColor.METAL)
			.noCollission()
			.noOcclusion()
			.replaceable())
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.withExistingParent(c.getName(), p.mcLoc("block/air"))))
		.lang("Track Marker for Maps")
		.register();

	public static final BlockEntry<CasingBlock> RAILWAY_CASING = REGISTRATE.block("railway_casing", CasingBlock::new)
		.transform(BuilderTransformers.layeredCasing(() -> AllSpriteShifts.RAILWAY_CASING_SIDE,
			() -> AllSpriteShifts.RAILWAY_CASING))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN).sound(SoundType.NETHERITE_BLOCK))
		.lang("Train Casing")
		.register();

	public static final BlockEntry<StationBlock> TRACK_STATION = REGISTRATE.block("track_station", StationBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
		.onRegister(assignDataBehaviour(new StationSummaryDisplaySource(), "station_summary"))
		.onRegister(assignDataBehaviour(new TrainStatusDisplaySource(), "train_status"))
		.lang("Train Station")
		.item(TrackTargetingBlockItem.ofType(EdgePointType.STATION))
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SignalBlock> TRACK_SIGNAL = REGISTRATE.block("track_signal", SignalBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.PODZOL)
			.noOcclusion()
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(state -> ConfiguredModel.builder()
				.modelFile(AssetLookup.partialBaseModel(c, p, state.getValue(SignalBlock.TYPE)
					.getSerializedName()))
				.build()))
		.lang("Train Signal")
		.item(TrackTargetingBlockItem.ofType(EdgePointType.SIGNAL))
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TrackObserverBlock> TRACK_OBSERVER =
		REGISTRATE.block("track_observer", TrackObserverBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.PODZOL)
				.noOcclusion()
				.sound(SoundType.NETHERITE_BLOCK))
			.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, AssetLookup.forPowered(c, p)))
			.transform(pickaxeOnly())
			.onRegister(assignDataBehaviour(new ObservedTrainNameSource(), "observed_train_name"))
			.lang("Train Observer")
			.item(TrackTargetingBlockItem.ofType(EdgePointType.OBSERVER))
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<StandardBogeyBlock> SMALL_BOGEY =
		REGISTRATE.block("small_bogey", p -> new StandardBogeyBlock(p, BogeySizes.SMALL))
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(BuilderTransformers.bogey())
			.register();

	public static final BlockEntry<StandardBogeyBlock> LARGE_BOGEY =
		REGISTRATE.block("large_bogey", p -> new StandardBogeyBlock(p, BogeySizes.LARGE))
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(BuilderTransformers.bogey())
			.register();

	public static final BlockEntry<ControlsBlock> TRAIN_CONTROLS = REGISTRATE.block("controls", ControlsBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHERITE_BLOCK))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.horizontalBlock(c.get(),
			s -> AssetLookup.partialBaseModel(c, p,
				s.getValue(ControlsBlock.VIRTUAL) ? "virtual" : s.getValue(ControlsBlock.OPEN) ? "open" : "closed")))
		.onRegister(movementBehaviour(new ControlsMovementBehaviour()))
		.onRegister(interactionBehaviour(new ControlsInteractionBehaviour()))
		.lang("Train Controls")
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ItemVaultBlock> ITEM_VAULT = REGISTRATE.block("item_vault", ItemVaultBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
			.explosionResistance(1200))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.standardModel(c, p))
				.rotationY(s.getValue(ItemVaultBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0)
				.build()))
		.onRegister(connectedTextures(ItemVaultCTBehaviour::new))
		.item(ItemVaultItem::new)
		.build()
		.register();

	public static final BlockEntry<AndesiteFunnelBlock> ANDESITE_FUNNEL =
		REGISTRATE.block("andesite_funnel", AndesiteFunnelBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.STONE))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(movementBehaviour(FunnelMovementBehaviour.andesite()))
			.blockstate(new FunnelGenerator("andesite", false)::generate)
			.item(FunnelItem::new)
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.model(FunnelGenerator.itemModel("andesite"))
			.build()
			.register();

	public static final BlockEntry<BeltFunnelBlock> ANDESITE_BELT_FUNNEL =
		REGISTRATE.block("andesite_belt_funnel", p -> new BeltFunnelBlock(AllBlocks.ANDESITE_FUNNEL, p))
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.STONE))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("andesite")::generate)
			.loot((p, b) -> p.dropOther(b, ANDESITE_FUNNEL.get()))
			.register();

	public static final BlockEntry<BrassFunnelBlock> BRASS_FUNNEL =
		REGISTRATE.block("brass_funnel", BrassFunnelBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(movementBehaviour(FunnelMovementBehaviour.brass()))
			.blockstate(new FunnelGenerator("brass", true)::generate)
			.item(FunnelItem::new)
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.model(FunnelGenerator.itemModel("brass"))
			.build()
			.register();

	public static final BlockEntry<BeltFunnelBlock> BRASS_BELT_FUNNEL =
		REGISTRATE.block("brass_belt_funnel", p -> new BeltFunnelBlock(AllBlocks.BRASS_FUNNEL, p))
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("brass")::generate)
			.loot((p, b) -> p.dropOther(b, BRASS_FUNNEL.get()))
			.register();

	public static final BlockEntry<BeltTunnelBlock> ANDESITE_TUNNEL =
		REGISTRATE.block("andesite_tunnel", BeltTunnelBlock::new)
			.properties(p -> p.mapColor(MapColor.STONE))
			.transform(BuilderTransformers.beltTunnel("andesite", new ResourceLocation("block/polished_andesite")))
			.onRegister(assignDataBehaviour(new AccumulatedItemCountDisplaySource(), "accumulate_items"))
			.onRegister(assignDataBehaviour(new ItemThroughputDisplaySource(), "item_throughput"))
			.register();

	public static final BlockEntry<BrassTunnelBlock> BRASS_TUNNEL =
		REGISTRATE.block("brass_tunnel", BrassTunnelBlock::new)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(BuilderTransformers.beltTunnel("brass", Create.asResource("block/brass_block")))
			.onRegister(assignDataBehaviour(new AccumulatedItemCountDisplaySource(), "accumulate_items"))
			.onRegister(assignDataBehaviour(new ItemThroughputDisplaySource(), "item_throughput"))
			.onRegister(connectedTextures(BrassTunnelCTBehaviour::new))
			.register();

	public static final BlockEntry<SmartObserverBlock> SMART_OBSERVER =
		REGISTRATE.block("content_observer", SmartObserverBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
			.transform(axeOrPickaxe())
			.blockstate(new SmartObserverGenerator()::generate)
			.onRegister(assignDataBehaviour(new ItemCountDisplaySource(), "count_items"))
			.onRegister(assignDataBehaviour(new ItemListDisplaySource(), "list_items"))
			.onRegister(assignDataBehaviour(new FluidAmountDisplaySource(), "count_fluids"))
			.onRegister(assignDataBehaviour(new FluidListDisplaySource(), "list_fluids"))
			.lang("Smart Observer")
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<ThresholdSwitchBlock> THRESHOLD_SWITCH =
		REGISTRATE.block("stockpile_switch", ThresholdSwitchBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
			.transform(axeOrPickaxe())
			.blockstate(new ThresholdSwitchGenerator()::generate)
			.onRegister(assignDataBehaviour(new FillLevelDisplaySource(), "fill_level"))
			.lang("Threshold Switch")
			.item()
			.transform(customItemModel("threshold_switch", "block_wall"))
			.register();

	public static final BlockEntry<CreativeCrateBlock> CREATIVE_CRATE =
		REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.transform(BuilderTransformers.crate("creative"))
			.properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
			.register();

	public static final BlockEntry<DisplayLinkBlock> DISPLAY_LINK =
		REGISTRATE.block("display_link", DisplayLinkBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.addLayer(() -> RenderType::translucent)
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item(DisplayLinkBlockItem::new)
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<FlapDisplayBlock> DISPLAY_BOARD =
		REGISTRATE.block("display_board", FlapDisplayBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(pickaxeOnly())
			.transform(BlockStressDefaults.setImpact(0))
			.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(assignDataBehaviour(new DisplayBoardTarget()))
			.lang("Display Board")
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<NixieTubeBlock> ORANGE_NIXIE_TUBE =
		REGISTRATE.block("nixie_tube", p -> new NixieTubeBlock(p, DyeColor.ORANGE))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel($ -> 5).mapColor(DyeColor.ORANGE).forceSolidOn())
			.transform(pickaxeOnly())
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
			.properties(p -> p.lightLevel($ -> 5).mapColor(colour).forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate(new NixieTubeGenerator()::generate)
			.loot((p, b) -> p.dropOther(b, ORANGE_NIXIE_TUBE.get()))
			.addLayer(() -> RenderType::translucent)
			.register();
	});

	public static final BlockEntry<RoseQuartzLampBlock> ROSE_QUARTZ_LAMP =
		REGISTRATE.block("rose_quartz_lamp", RoseQuartzLampBlock::new)
			.initialProperties(() -> Blocks.REDSTONE_LAMP)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK)
				.lightLevel(s -> s.getValue(RoseQuartzLampBlock.POWERING) ? 15 : 0))
			.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, s -> {
				boolean powered = s.getValue(RoseQuartzLampBlock.POWERING);
				String name = c.getName() + (powered ? "_powered" : "");
				return p.models()
					.cubeAll(name, p.modLoc("block/" + name));
			}))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	public static final BlockEntry<RedstoneLinkBlock> REDSTONE_LINK =
		REGISTRATE.block("redstone_link", RedstoneLinkBlock::new)
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).forceSolidOn())
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.BRITTLE.tag, AllBlockTags.SAFE_NBT.tag)
			.blockstate(new RedstoneLinkGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel("_", "transmitter"))
			.register();

	public static final BlockEntry<AnalogLeverBlock> ANALOG_LEVER =
		REGISTRATE.block("analog_lever", AnalogLeverBlock::new)
			.initialProperties(() -> Blocks.LEVER)
			.transform(axeOrPickaxe())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(ItemUseOverrides::addBlock)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PlacardBlock> PLACARD = REGISTRATE.block("placard", PlacardBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.forceSolidOn())
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.standardModel(c, p)))
		.simpleItem()
		.register();

	public static final BlockEntry<BrassDiodeBlock> PULSE_REPEATER =
		REGISTRATE.block("pulse_repeater", BrassDiodeBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BrassDiodeGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.model(AbstractDiodeGenerator::diodeItemModel)
			.build()
			.register();

	public static final BlockEntry<BrassDiodeBlock> PULSE_EXTENDER =
		REGISTRATE.block("pulse_extender", BrassDiodeBlock::new)
			.initialProperties(() -> Blocks.REPEATER)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BrassDiodeGenerator()::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.model(AbstractDiodeGenerator::diodeItemModel)
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
			.transform(axeOnly())
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
				.getExistingFile(p.mcLoc("block/lectern"))))
			.loot((lt, block) -> lt.dropOther(block, Blocks.LECTERN))
			.register();

	// Curiosities

	public static final BlockEntry<BacktankBlock> COPPER_BACKTANK =
		REGISTRATE.block("copper_backtank", BacktankBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.transform(BuilderTransformers.backtank(AllItems.COPPER_BACKTANK::get))
			.register();

	public static final BlockEntry<BacktankBlock> NETHERITE_BACKTANK =
		REGISTRATE.block("netherite_backtank", BacktankBlock::new)
			.initialProperties(SharedProperties::netheriteMetal)
			.transform(BuilderTransformers.backtank(AllItems.NETHERITE_BACKTANK::get))
			.register();

	public static final BlockEntry<PeculiarBellBlock> PECULIAR_BELL =
		REGISTRATE.block("peculiar_bell", PeculiarBellBlock::new)
			.properties(p -> p.mapColor(MapColor.GOLD).forceSolidOn())
			.transform(BuilderTransformers.bell())
			.onRegister(movementBehaviour(new BellMovementBehaviour()))
			.register();

	public static final BlockEntry<HauntedBellBlock> HAUNTED_BELL =
		REGISTRATE.block("haunted_bell", HauntedBellBlock::new)
			.properties(p -> p.mapColor(MapColor.SAND).forceSolidOn())
			.transform(BuilderTransformers.bell())
			.onRegister(movementBehaviour(new HauntedBellMovementBehaviour()))
			.register();

	public static final DyedBlockList<ToolboxBlock> TOOLBOXES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_toolbox", p -> new ToolboxBlock(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.sound(SoundType.WOOD).mapColor(colour).forceSolidOn())
			.addLayer(() -> RenderType::cutoutMipped)
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(block)
						.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
						.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
							.copy("UniqueId", "UniqueId"))
						.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
							.copy("Inventory", "Inventory")))));
			})
			.blockstate((c, p) -> {
				p.horizontalBlock(c.get(), p.models()
					.withExistingParent(colourName + "_toolbox", p.modLoc("block/toolbox/block"))
					.texture("0", p.modLoc("block/toolbox/" + colourName)));
			})
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.toolbox"))
			.tag(AllBlockTags.TOOLBOXES.tag)
			.item(UncontainableBlockItem::new)
			.model((c, p) -> p.withExistingParent(colourName + "_toolbox", p.modLoc("block/toolbox/item"))
				.texture("0", p.modLoc("block/toolbox/" + colourName)))
			.tag(AllItemTags.TOOLBOXES.tag)
			.build()
			.register();
	});

	public static final BlockEntry<ClipboardBlock> CLIPBOARD = REGISTRATE.block("clipboard", ClipboardBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.forceSolidOn())
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> p.horizontalFaceBlock(c.get(),
			s -> AssetLookup.partialBaseModel(c, p, s.getValue(ClipboardBlock.WRITTEN) ? "written" : "empty")))
		.loot((lt, b) -> lt.add(b, BlockLootSubProvider.noDrop()))
		.item(ClipboardBlockItem::new)
		.onRegister(ClipboardBlockItem::registerModelOverrides)
		.model((c, p) -> ClipboardOverrides.addOverrideModels(c, p))
		.build()
		.register();

	// Materials

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.PALETTES_CREATIVE_TAB);
	}

	public static final BlockEntry<MetalLadderBlock> ANDESITE_LADDER =
		REGISTRATE.block("andesite_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("andesite", () -> DataIngredient.items(AllItems.ANDESITE_ALLOY.get()),
				MapColor.STONE))
			.register();

	public static final BlockEntry<MetalLadderBlock> BRASS_LADDER =
		REGISTRATE.block("brass_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("brass",
				() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/brass")), MapColor.TERRACOTTA_YELLOW))
			.register();

	public static final BlockEntry<MetalLadderBlock> COPPER_LADDER =
		REGISTRATE.block("copper_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("copper",
				() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/copper")), MapColor.COLOR_ORANGE))
			.register();

	public static final BlockEntry<IronBarsBlock> ANDESITE_BARS = MetalBarsGen.createBars("andesite", true,
		() -> DataIngredient.items(AllItems.ANDESITE_ALLOY.get()), MapColor.STONE);
	public static final BlockEntry<IronBarsBlock> BRASS_BARS = MetalBarsGen.createBars("brass", true,
		() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/brass")), MapColor.TERRACOTTA_YELLOW);
	public static final BlockEntry<IronBarsBlock> COPPER_BARS = MetalBarsGen.createBars("copper", true,
		() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/copper")), MapColor.COLOR_ORANGE);

	public static final BlockEntry<MetalScaffoldingBlock> ANDESITE_SCAFFOLD = REGISTRATE
		.block("andesite_scaffolding", MetalScaffoldingBlock::new)
		.transform(BuilderTransformers.scaffold("andesite", () -> DataIngredient.items(AllItems.ANDESITE_ALLOY.get()),
			MapColor.STONE, AllSpriteShifts.ANDESITE_SCAFFOLD, AllSpriteShifts.ANDESITE_SCAFFOLD_INSIDE,
			AllSpriteShifts.ANDESITE_CASING))
		.register();

	public static final BlockEntry<MetalScaffoldingBlock> BRASS_SCAFFOLD =
		REGISTRATE.block("brass_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("brass",
				() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/brass")), MapColor.TERRACOTTA_YELLOW,
				AllSpriteShifts.BRASS_SCAFFOLD, AllSpriteShifts.BRASS_SCAFFOLD_INSIDE, AllSpriteShifts.BRASS_CASING))
			.register();

	public static final BlockEntry<MetalScaffoldingBlock> COPPER_SCAFFOLD =
		REGISTRATE.block("copper_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("copper",
				() -> DataIngredient.tag(AllTags.forgeItemTag("ingots/copper")), MapColor.COLOR_ORANGE,
				AllSpriteShifts.COPPER_SCAFFOLD, AllSpriteShifts.COPPER_SCAFFOLD_INSIDE, AllSpriteShifts.COPPER_CASING))
			.register();

	public static final BlockEntry<GirderBlock> METAL_GIRDER = REGISTRATE.block("metal_girder", GirderBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate(GirderBlockStateGenerator::blockState)
		.onRegister(CreateRegistrate.blockModel(() -> ConnectedGirderModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GirderEncasedShaftBlock> METAL_GIRDER_ENCASED_SHAFT =
		REGISTRATE.block("metal_girder_encased_shaft", GirderEncasedShaftBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.blockstate(GirderBlockStateGenerator::blockStateWithShaft)
			.loot((p, b) -> p.add(b, p.createSingleItemTable(METAL_GIRDER.get())
				.withPool(p.applyExplosionCondition(SHAFT.get(), LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(SHAFT.get()))))))
			.onRegister(CreateRegistrate.blockModel(() -> ConnectedGirderModel::new))
			.register();

	public static final BlockEntry<Block> COPYCAT_BASE = REGISTRATE.block("copycat_base", Block::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.GLOW_LICHEN))
		.addLayer(() -> RenderType::cutoutMipped)
		.tag(AllBlockTags.FAN_TRANSPARENT.tag)
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
		.register();

	public static final BlockEntry<CopycatStepBlock> COPYCAT_STEP =
		REGISTRATE.block("copycat_step", CopycatStepBlock::new)
			.properties(p -> p.forceSolidOn())
			.transform(BuilderTransformers.copycat())
			.onRegister(CreateRegistrate.blockModel(() -> CopycatStepModel::new))
			.item()
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(AllTags.forgeItemTag("ingots/zinc")),
				RecipeCategory.BUILDING_BLOCKS, c::get, 4))
			.transform(customItemModel("copycat_base", "step"))
			.register();

	public static final BlockEntry<CopycatPanelBlock> COPYCAT_PANEL =
		REGISTRATE.block("copycat_panel", CopycatPanelBlock::new)
			.transform(BuilderTransformers.copycat())
			.onRegister(CreateRegistrate.blockModel(() -> CopycatPanelModel::new))
			.item()
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(AllTags.forgeItemTag("ingots/zinc")),
				RecipeCategory.BUILDING_BLOCKS, c::get, 4))
			.transform(customItemModel("copycat_base", "panel"))
			.register();

	public static final BlockEntry<WrenchableDirectionalBlock> COPYCAT_BARS =
		REGISTRATE.block("copycat_bars", WrenchableDirectionalBlock::new)
			.blockstate(new SpecialCopycatPanelBlockState("bars")::generate)
			.onRegister(CreateRegistrate.blockModel(() -> CopycatBarsModel::new))
			.register();

	public static final DyedBlockList<SeatBlock> SEATS = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
		SeatInteractionBehaviour interactionBehaviour = new SeatInteractionBehaviour();
		return REGISTRATE.block(colourName + "_seat", p -> new SeatBlock(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(colour))
			.transform(axeOnly())
			.onRegister(movementBehaviour(movementBehaviour))
			.onRegister(interactionBehaviour(interactionBehaviour))
			.onRegister(assignDataBehaviour(new EntityNameDisplaySource(), "entity_name"))
			.blockstate((c, p) -> {
				p.simpleBlock(c.get(), p.models()
					.withExistingParent(colourName + "_seat", p.modLoc("block/seat"))
					.texture("1", p.modLoc("block/seat/top_" + colourName))
					.texture("2", p.modLoc("block/seat/side_" + colourName)));
			})
			.recipe((c, p) -> {
				ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
					.requires(DyeHelper.getWoolOfDye(colour))
					.requires(ItemTags.WOODEN_SLABS)
					.unlockedBy("has_wool", RegistrateRecipeProvider.has(ItemTags.WOOL))
					.save(p, Create.asResource("crafting/kinetics/" + c.getName()));
				ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
					.requires(colour.getTag())
					.requires(AllItemTags.SEATS.tag)
					.unlockedBy("has_seat", RegistrateRecipeProvider.has(AllItemTags.SEATS.tag))
					.save(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_seat"));
			})
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.seat"))
			.tag(AllBlockTags.SEATS.tag)
			.item()
			.tag(AllItemTags.SEATS.tag)
			.build()
			.register();
	});

	public static final BlockEntry<SlidingDoorBlock> ANDESITE_DOOR =
		REGISTRATE.block("andesite_door", p -> SlidingDoorBlock.metal(p, true))
			.transform(BuilderTransformers.slidingDoor("andesite"))
			.properties(p -> p.mapColor(MapColor.STONE)
				.sound(SoundType.STONE)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> BRASS_DOOR =
		REGISTRATE.block("brass_door", p -> SlidingDoorBlock.metal(p, false))
			.transform(BuilderTransformers.slidingDoor("brass"))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
				.sound(SoundType.STONE)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> COPPER_DOOR =
		REGISTRATE.block("copper_door", p -> SlidingDoorBlock.metal(p, true))
			.transform(BuilderTransformers.slidingDoor("copper"))
			.properties(p -> p.mapColor(MapColor.COLOR_ORANGE)
				.sound(SoundType.STONE)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> TRAIN_DOOR =
		REGISTRATE.block("train_door", p -> SlidingDoorBlock.metal(p, false))
			.transform(BuilderTransformers.slidingDoor("train"))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
				.sound(SoundType.NETHERITE_BLOCK)
				.noOcclusion())
			.register();

	public static final BlockEntry<TrainTrapdoorBlock> TRAIN_TRAPDOOR =
		REGISTRATE.block("train_trapdoor", TrainTrapdoorBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
				.sound(SoundType.NETHERITE_BLOCK))
			.transform(BuilderTransformers.trapdoor(true))
			.register();

	public static final BlockEntry<SlidingDoorBlock> FRAMED_GLASS_DOOR =
		REGISTRATE.block("framed_glass_door", p -> SlidingDoorBlock.glass(p, false))
			.transform(BuilderTransformers.slidingDoor("glass"))
			.properties(p -> p.mapColor(MapColor.NONE)
				.sound(SoundType.GLASS)
				.noOcclusion())
			.register();

	public static final BlockEntry<TrainTrapdoorBlock> FRAMED_GLASS_TRAPDOOR =
		REGISTRATE.block("framed_glass_trapdoor", TrainTrapdoorBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.transform(BuilderTransformers.trapdoor(false))
			.properties(p -> p.mapColor(MapColor.NONE)
				.sound(SoundType.GLASS)
				.noOcclusion())
			.onRegister(connectedTextures(TrapdoorCTBehaviour::new))
			.addLayer(() -> RenderType::cutoutMipped)
			.register();

	public static final BlockEntry<Block> ZINC_ORE = REGISTRATE.block("zinc_ore", Block::new)
		.initialProperties(() -> Blocks.GOLD_ORE)
		.properties(p -> p.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE))
		.transform(pickaxeOnly())
		.loot((lt, b) -> lt.add(b,
			RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
				lt.applyExplosionDecay(b, LootItem.lootTableItem(AllItems.RAW_ZINC.get())
					.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem("ores/zinc", "ores_in_ground/stone"))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<Block> DEEPSLATE_ZINC_ORE = REGISTRATE.block("deepslate_zinc_ore", Block::new)
		.initialProperties(() -> Blocks.DEEPSLATE_GOLD_ORE)
		.properties(p -> p.mapColor(MapColor.STONE)
			.requiresCorrectToolForDrops()
			.sound(SoundType.DEEPSLATE))
		.transform(pickaxeOnly())
		.loot((lt, b) -> lt.add(b,
			RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
				lt.applyExplosionDecay(b, LootItem.lootTableItem(AllItems.RAW_ZINC.get())
					.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem("ores/zinc", "ores_in_ground/deepslate"))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<Block> RAW_ZINC_BLOCK = REGISTRATE.block("raw_zinc_block", Block::new)
		.initialProperties(() -> Blocks.RAW_GOLD_BLOCK)
		.properties(p -> p.mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.lang("Block of Raw Zinc")
		.transform(tagBlockAndItem("storage_blocks/raw_zinc"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.register();

	public static final BlockEntry<Block> ZINC_BLOCK = REGISTRATE.block("zinc_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.properties(p -> p.mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem("storage_blocks/zinc"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Zinc")
		.register();

	public static final BlockEntry<Block> ANDESITE_ALLOY_BLOCK = REGISTRATE.block("andesite_alloy_block", Block::new)
		.initialProperties(() -> Blocks.ANDESITE)
		.properties(p -> p.mapColor(MapColor.STONE).requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("andesite_block"))
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.transform(tagBlockAndItem("storage_blocks/andesite_alloy"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Andesite Alloy")
		.register();

	public static final BlockEntry<Block> INDUSTRIAL_IRON_BLOCK = REGISTRATE.block("industrial_iron_block", Block::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.cubeColumn(c.getName(), p.modLoc("block/industrial_iron_block"),
				p.modLoc("block/industrial_iron_block_top"))))
		.tag(AllBlockTags.WRENCH_PICKUP.tag)
		.lang("Block of Industrial Iron")
		.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.INGOTS_IRON), RecipeCategory.BUILDING_BLOCKS,
			c::get, 2))
		.simpleItem()
		.register();

	public static final BlockEntry<Block> BRASS_BLOCK = REGISTRATE.block("brass_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("brass_block"))
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem("storage_blocks/brass"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Brass")
		.register();

	public static final BlockEntry<ExperienceBlock> EXPERIENCE_BLOCK =
		REGISTRATE.block("experience_block", ExperienceBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.PLANT)
				.sound(new ForgeSoundType(1, .5f, () -> SoundEvents.AMETHYST_BLOCK_BREAK,
					() -> SoundEvents.AMETHYST_BLOCK_STEP, () -> SoundEvents.AMETHYST_BLOCK_PLACE,
					() -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.AMETHYST_BLOCK_FALL))
				.requiresCorrectToolForDrops()
				.lightLevel(s -> 15))
			.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.standardModel(c, p)))
			.transform(pickaxeOnly())
			.lang("Block of Experience")
			.tag(Tags.Blocks.STORAGE_BLOCKS)
			.tag(BlockTags.BEACON_BASE_BLOCKS)
			.item()
			.properties(p -> p.rarity(Rarity.UNCOMMON))
			.tag(Tags.Items.STORAGE_BLOCKS)
			.build()
			.register();

	public static final BlockEntry<RotatedPillarBlock> ROSE_QUARTZ_BLOCK =
		REGISTRATE.block("rose_quartz_block", RotatedPillarBlock::new)
			.initialProperties(() -> Blocks.AMETHYST_BLOCK)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK)
				.requiresCorrectToolForDrops()
				.sound(SoundType.DEEPSLATE))
			.transform(pickaxeOnly())
			.blockstate((c, p) -> p.axisBlock(c.get(), p.modLoc("block/palettes/rose_quartz_side"),
				p.modLoc("block/palettes/rose_quartz_top")))
			.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.ROSE_QUARTZ.get()),
				RecipeCategory.BUILDING_BLOCKS, c::get, 2))
			.simpleItem()
			.lang("Block of Rose Quartz")
			.register();

	public static final BlockEntry<Block> ROSE_QUARTZ_TILES = REGISTRATE.block("rose_quartz_tiles", Block::new)
		.initialProperties(() -> Blocks.DEEPSLATE)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("palettes/rose_quartz_tiles"))
		.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.POLISHED_ROSE_QUARTZ.get()),
			RecipeCategory.BUILDING_BLOCKS, c::get, 2))
		.simpleItem()
		.register();

	public static final BlockEntry<Block> SMALL_ROSE_QUARTZ_TILES =
		REGISTRATE.block("small_rose_quartz_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops())
			.transform(pickaxeOnly())
			.blockstate(simpleCubeAll("palettes/small_rose_quartz_tiles"))
			.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.POLISHED_ROSE_QUARTZ.get()),
				RecipeCategory.BUILDING_BLOCKS, c::get, 2))
			.simpleItem()
			.register();

	public static final CopperBlockSet COPPER_SHINGLES = new CopperBlockSet(REGISTRATE, "copper_shingles",
		"copper_roof_top", CopperBlockSet.DEFAULT_VARIANTS, (c, p) -> {
			p.stonecutting(DataIngredient.tag(AllTags.forgeItemTag("ingots/copper")), RecipeCategory.BUILDING_BLOCKS,
				c::get, 2);
		});

	public static final CopperBlockSet COPPER_TILES =
		new CopperBlockSet(REGISTRATE, "copper_tiles", "copper_roof_top", CopperBlockSet.DEFAULT_VARIANTS, (c, p) -> {
			p.stonecutting(DataIngredient.tag(AllTags.forgeItemTag("ingots/copper")), RecipeCategory.BUILDING_BLOCKS,
				c::get, 2);
		});

	// Load this class

	public static void register() {}

}
