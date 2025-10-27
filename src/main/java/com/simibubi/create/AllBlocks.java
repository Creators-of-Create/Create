package com.simibubi.create;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.display.DisplayTarget.displayTarget;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.api.contraption.storage.item.MountedItemStorageType.mountedItemStorage;
import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.simibubi.create.foundation.data.TagGen.tagBlockAndItem;

import java.util.Map;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.simibubi.create.api.stress.BlockStressValues;
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
import com.simibubi.create.content.decoration.CardboardBlock;
import com.simibubi.create.content.decoration.MetalLadderBlock;
import com.simibubi.create.content.decoration.MetalScaffoldingBlock;
import com.simibubi.create.content.decoration.RoofBlockCTBehaviour;
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
import com.simibubi.create.content.fluids.tank.FluidTankMovementBehavior;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltGenerator;
import com.simibubi.create.content.kinetics.belt.BeltModel;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
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
import com.simibubi.create.content.logistics.depot.MountedDepotInteractionBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelModel;
import com.simibubi.create.content.logistics.funnel.AndesiteFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelGenerator;
import com.simibubi.create.content.logistics.funnel.BrassFunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelGenerator;
import com.simibubi.create.content.logistics.funnel.FunnelItem;
import com.simibubi.create.content.logistics.funnel.FunnelMovementBehaviour;
import com.simibubi.create.content.logistics.itemHatch.ItemHatchBlock;
import com.simibubi.create.content.logistics.packagePort.PackagePortItem;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlock;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlock;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkGenerator;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlock;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlock;
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
import com.simibubi.create.content.processing.burner.BlazeBurnerMovementBehaviour;
import com.simibubi.create.content.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.content.redstone.RoseQuartzLampBlock;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlock;
import com.simibubi.create.content.redstone.contact.ContactMovementBehaviour;
import com.simibubi.create.content.redstone.contact.RedstoneContactBlock;
import com.simibubi.create.content.redstone.contact.RedstoneContactItem;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlock;
import com.simibubi.create.content.redstone.diodes.AbstractDiodeGenerator;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlock;
import com.simibubi.create.content.redstone.diodes.BrassDiodeGenerator;
import com.simibubi.create.content.redstone.diodes.PoweredLatchBlock;
import com.simibubi.create.content.redstone.diodes.PoweredLatchGenerator;
import com.simibubi.create.content.redstone.diodes.ToggleLatchBlock;
import com.simibubi.create.content.redstone.diodes.ToggleLatchGenerator;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockItem;
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
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.MetalBarsGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.UncontainableBlockItem;
import com.simibubi.create.foundation.mixin.accessor.BlockLootSubProviderAccessor;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.createmod.catnip.data.Couple;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
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
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.DeferredSoundType;

@SuppressWarnings("removal")
public class AllBlocks {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

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
						.apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
							.include(AllDataComponents.SCHEMATICANNON_OPTIONS)))));
			})
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.properties(p -> p.mapColor(MapColor.PODZOL)
				.forceSolidOn())
			.transform(axeOrPickaxe())
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
				.getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	// Kinetics

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.METAL).forceSolidOff())
		.transform(CStress.setNoImpact())
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.sound(SoundType.WOOD)
			.mapColor(MapColor.DIRT))
		.transform(CStress.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.WOOD)
				.mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.transform(CStress.setNoImpact())
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
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.PODZOL))
		.transform(CStress.setNoImpact())
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
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.PODZOL))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(CStress.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.PODZOL))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(CStress.setNoImpact())
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ChainDriveBlock> ENCASED_CHAIN_DRIVE =
		REGISTRATE.block("encased_chain_drive", ChainDriveBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.PODZOL))
			.transform(CStress.setNoImpact())
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ChainGearshiftBlock> ADJUSTABLE_CHAIN_GEARSHIFT =
		REGISTRATE.block("adjustable_chain_gearshift", ChainGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.NETHER))
			.transform(CStress.setNoImpact())
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
		.transform(CStress.setNoImpact())
		.transform(displaySource(AllDisplaySources.ITEM_NAMES))
		.onRegister(CreateRegistrate.blockModel(() -> BeltModel::new))
		.clientExtension(() -> () -> new BeltBlock.RenderProperties())
		.register();

	public static final BlockEntry<ChainConveyorBlock> CHAIN_CONVEYOR =
		REGISTRATE.block("chain_conveyor", ChainConveyorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.transform(CStress.setImpact(1))
			.transform(CStress.setImpact(1))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<CreativeMotorBlock> CREATIVE_MOTOR =
		REGISTRATE.block("creative_motor", CreativeMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_PURPLE)
				.forceSolidOn())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(pickaxeOnly())
			.blockstate(new CreativeMotorGenerator()::generate)
			.transform(CStress.setCapacity(16384.0))
			.onRegister(BlockStressValues.setGeneratorSpeed(256, true))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelBlock> WATER_WHEEL = REGISTRATE.block("water_wheel", WaterWheelBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.DIRT))
		.transform(axeOrPickaxe())
		.blockstate(
			(c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, s -> AssetLookup.partialBaseModel(c, p)))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(CStress.setCapacity(32))
		.onRegister(BlockStressValues.setGeneratorSpeed(8))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<LargeWaterWheelBlock> LARGE_WATER_WHEEL =
		REGISTRATE.block("large_water_wheel", LargeWaterWheelBlock::new)
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> axisBlock(c, p,
				s -> s.getValue(LargeWaterWheelBlock.EXTENSION) ? AssetLookup.partialBaseModel(c, p, "extension")
					: AssetLookup.partialBaseModel(c, p)))
			.transform(CStress.setCapacity(128.0))
			.onRegister(BlockStressValues.setGeneratorSpeed(4))
			.item(LargeWaterWheelBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelStructuralBlock> WATER_WHEEL_STRUCTURAL =
		REGISTRATE.block("water_wheel_structure", WaterWheelStructuralBlock::new)
			.initialProperties(SharedProperties::wooden)
			.clientExtension(() -> () -> new WaterWheelStructuralBlock.RenderProperties())
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStatesExcept(BlockStateGen.mapToAir(p), WaterWheelStructuralBlock.FACING))
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.DIRT))
			.transform(axeOrPickaxe())
			.lang("Large Water Wheel")
			.register();

	public static final BlockEntry<EncasedFanBlock> ENCASED_FAN = REGISTRATE.block("encased_fan", EncasedFanBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(axeOrPickaxe())
		.transform(CStress.setImpact(2.0))
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
		.transform(CStress.setImpact(4.0))
		.simpleItem()
		.register();

	public static final BlockEntry<HandCrankBlock> HAND_CRANK = REGISTRATE.block("hand_crank", HandCrankBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(CStress.setCapacity(8.0))
		.onRegister(BlockStressValues.setGeneratorSpeed(32))
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
			.transform(displaySource(AllDisplaySources.TIME_OF_DAY))
			.transform(displaySource(AllDisplaySources.STOPWATCH))
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
		.transform(CStress.setImpact(4.0))
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
			.transform(CStress.setImpact(8.0))
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
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(CStress.setImpact(8.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<MechanicalMixerBlock> MECHANICAL_MIXER =
		REGISTRATE.block("mechanical_mixer", MechanicalMixerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.STONE))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(CStress.setImpact(4.0))
			.item(AssemblyOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BasinBlock> BASIN = REGISTRATE.block("basin", BasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK))
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
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
				.lightLevel(BlazeBurnerBlock::getLight))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag,
				AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
			.loot((lt, block) -> lt.add(block, BlazeBurnerBlock.buildLootTable()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(movementBehaviour(new BlazeBurnerMovementBehaviour()))
			.onRegister(interactionBehaviour(new ConductorBlockInteractionBehavior.BlazeBurner()))
			.item(BlazeBurnerBlockItem::withBlaze)
			.model(AssetLookup.customBlockItemModel("blaze_burner", "block_with_blaze"))
			.build()
			.register();

	public static final BlockEntry<LitBlazeBurnerBlock> LIT_BLAZE_BURNER =
		REGISTRATE.block("lit_blaze_burner", LitBlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY)
				.lightLevel(LitBlazeBurnerBlock::getLight))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag,
				AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
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
		.transform(displaySource(AllDisplaySources.ITEM_NAMES))
		.onRegister(interactionBehaviour(new MountedDepotInteractionBehaviour()))
		.transform(mountedItemStorage(AllMountedStorageTypes.DEPOT))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<EjectorBlock> WEIGHTED_EJECTOR =
		REGISTRATE.block("weighted_ejector", EjectorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.COLOR_GRAY))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p), 180))
			.transform(CStress.setImpact(2.0))
			.transform(displaySource(AllDisplaySources.ITEM_NAMES))
			.item(EjectorItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ChuteBlock> CHUTE = REGISTRATE.block("chute", ChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isSuffocating((state, level, pos) -> false))
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutoutMipped)
		.clientExtension(() -> () -> new ReducedDestroyEffects())
		.blockstate(new ChuteGenerator()::generate)
		.item(ChuteItem::new)
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<SmartChuteBlock> SMART_CHUTE = REGISTRATE.block("smart_chute", SmartChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isSuffocating((state, level, pos) -> false)
			.isRedstoneConductor((state, level, pos) -> false))
		.addLayer(() -> RenderType::cutoutMipped)
		.clientExtension(() -> () -> new ReducedDestroyEffects())
		.transform(pickaxeOnly())
		.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<GaugeBlock> SPEEDOMETER = REGISTRATE.block("speedometer", GaugeBlock::speed)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.transform(CStress.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.transform(displaySource(AllDisplaySources.KINETIC_SPEED))
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<GaugeBlock> STRESSOMETER = REGISTRATE.block("stressometer", GaugeBlock::stress)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.transform(axeOrPickaxe())
		.transform(CStress.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.transform(displaySource(AllDisplaySources.KINETIC_STRESS))
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<BracketBlock> WOODEN_BRACKET = REGISTRATE.block("wooden_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("wooden")::generate)
		.properties(p -> p.sound(SoundType.SCAFFOLDING))
		.transform(axeOrPickaxe())
		.item(BracketBlockItem::new)
		.tag(AllItemTags.INVALID_FOR_TRACK_PAVING.tag)
		.transform(BracketGenerator.itemModel("wooden"))
		.register();

	public static final BlockEntry<BracketBlock> METAL_BRACKET = REGISTRATE.block("metal_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("metal")::generate)
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.item(BracketBlockItem::new)
		.tag(AllItemTags.INVALID_FOR_TRACK_PAVING.tag)
		.transform(BracketGenerator.itemModel("metal"))
		.register();

	// Fluids

	public static final BlockEntry<FluidPipeBlock> FLUID_PIPE = REGISTRATE.block("fluid_pipe", FluidPipeBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.forceSolidOff())
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.pipe())
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedPipeBlock> ENCASED_FLUID_PIPE =
		REGISTRATE.block("encased_fluid_pipe", p -> new EncasedPipeBlock(p, AllBlocks.COPPER_CASING::get))
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.encasedPipe())
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.COPPER_CASING)))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.COPPER_CASING,
				(s, f) -> !s.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.transform(EncasingRegistry.addVariantTo(AllBlocks.FLUID_PIPE))
			.register();

	public static final BlockEntry<GlassFluidPipeBlock> GLASS_FLUID_PIPE =
		REGISTRATE.block("glass_fluid_pipe", GlassFluidPipeBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion())
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
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
			.loot((p, b) -> p.dropOther(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<PumpBlock> MECHANICAL_PUMP = REGISTRATE.block("mechanical_pump", PumpBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.mapColor(MapColor.STONE))
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.transform(CStress.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SmartFluidPipeBlock> SMART_FLUID_PIPE =
		REGISTRATE.block("smart_fluid_pipe", SmartFluidPipeBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(pickaxeOnly())
			.blockstate(new SmartFluidPipeGenerator()::generate)
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FluidValveBlock> FLUID_VALVE = REGISTRATE.block("fluid_valve", FluidValveBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> BlockStateGen.directionalAxisBlock(c, p,
			(state, vertical) -> AssetLookup.partialBaseModel(c, p, vertical ? "vertical" : "horizontal",
				state.getValue(FluidValveBlock.ENABLED) ? "open" : "closed")))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ValveHandleBlock> COPPER_VALVE_HANDLE =
		REGISTRATE.block("copper_valve_handle", ValveHandleBlock::copper)
			.transform(pickaxeOnly())
			.transform(BuilderTransformers.valveHandle(null))
			.transform(CStress.setCapacity(8.0))
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
		.properties(p -> p.noOcclusion()
			.isRedstoneConductor((p1, p2, p3) -> true))
		.transform(pickaxeOnly())
		.blockstate(new FluidTankGenerator()::generate)
		.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::standard))
		.transform(displaySource(AllDisplaySources.BOILER))
		.transform(mountedFluidStorage(AllMountedStorageTypes.FLUID_TANK))
		.onRegister(movementBehaviour(new FluidTankMovementBehavior()))
		.addLayer(() -> RenderType::cutoutMipped)
		.item(FluidTankItem::new)
		.model(AssetLookup.customBlockItemModel("_", "block_single_window"))
		.build()
		.register();

	public static final BlockEntry<FluidTankBlock> CREATIVE_FLUID_TANK =
		REGISTRATE.block("creative_fluid_tank", FluidTankBlock::creative)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.COLOR_PURPLE))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new FluidTankGenerator("creative_")::generate)
			.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::creative))
			.transform(mountedFluidStorage(AllMountedStorageTypes.CREATIVE_FLUID_TANK))
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
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(pickaxeOnly())
		.blockstate(BlockStateGen.horizontalBlockProvider(true))
		.transform(CStress.setImpact(4.0))
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
			.transform(CStress.setCapacity(1024.0))
			.onRegister(BlockStressValues.setGeneratorSpeed(64, true))
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
			.properties(p -> p.mapColor(MapColor.GOLD)
				.forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate(BlockStateGen.whistleExtender())
			.register();

	public static final BlockEntry<PoweredShaftBlock> POWERED_SHAFT =
		REGISTRATE.block("powered_shaft", PoweredShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL)
				.forceSolidOn())
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
			.properties(p -> p.sound(SoundType.SCAFFOLDING)
				.mapColor(MapColor.DIRT)
				.forceSolidOn())
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
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.directionalAxisBlockProvider())
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<GantryShaftBlock> GANTRY_SHAFT =
		REGISTRATE.block("gantry_shaft", GantryShaftBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.NETHER)
				.forceSolidOn())
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
			.transform(CStress.setNoImpact())
			.item()
			.transform(customItemModel("_", "block_single"))
			.register();

	public static final BlockEntry<WindmillBearingBlock> WINDMILL_BEARING =
		REGISTRATE.block("windmill_bearing", WindmillBearingBlock::new)
			.transform(axeOrPickaxe())
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(BuilderTransformers.bearing("windmill", "gearbox"))
			.transform(CStress.setCapacity(512.0))
			.onRegister(BlockStressValues.setGeneratorSpeed(16, true))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalBearingBlock> MECHANICAL_BEARING =
		REGISTRATE.block("mechanical_bearing", MechanicalBearingBlock::new)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.bearing("mechanical", "gearbox"))
			.transform(CStress.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(movementBehaviour(new StabilizedBearingMovementBehaviour()))
			.register();

	public static final BlockEntry<ClockworkBearingBlock> CLOCKWORK_BEARING =
		REGISTRATE.block("clockwork_bearing", ClockworkBearingBlock::new)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.transform(axeOrPickaxe())
			.transform(BuilderTransformers.bearing("clockwork", "brass_gearbox"))
			.transform(CStress.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PulleyBlock> ROPE_PULLEY = REGISTRATE.block("rope_pulley", PulleyBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.noOcclusion())
		.addLayer(() -> RenderType::cutoutMipped)
		.transform(axeOrPickaxe())
		.tag(AllBlockTags.SAFE_NBT.tag)
		.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
		.transform(CStress.setImpact(4.0))
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
			.transform(CStress.setImpact(4.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<CartAssemblerBlock> CART_ASSEMBLER =
		REGISTRATE.block("cart_assembler", CartAssemblerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.COLOR_GRAY))
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
			.color(() -> () -> (state, world, pos, layer) -> RedStoneWireBlock
				.getColorForPower(pos != null && world != null ? state.getValue(BlockStateProperties.POWER) : 0))
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
		.transform(CStress.setImpact(4.0))
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
		.transform(CStress.setImpact(4.0))
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
		.transform(CStress.setImpact(4.0))
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
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
				.lightLevel(ElevatorContactBlock::getLight))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.directionalBlock(c.get(), state -> {
				Boolean calling = state.getValue(ElevatorContactBlock.CALLING);
				Boolean powering = state.getValue(ElevatorContactBlock.POWERING);
				return powering ? AssetLookup.partialBaseModel(c, p, "powered")
					: calling ? AssetLookup.partialBaseModel(c, p, "dim") : AssetLookup.partialBaseModel(c, p);
			}))
			.loot((p, b) -> p.dropOther(b, REDSTONE_CONTACT.get()))
			.transform(displaySource(AllDisplaySources.CURRENT_FLOOR))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<HarvesterBlock> MECHANICAL_HARVESTER =
		REGISTRATE.block("mechanical_harvester", HarvesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL)
				.forceSolidOn())
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
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn())
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
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
			.sound(SoundType.COPPER))
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
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(CStress.setImpact(2.0))
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
			.transform(CStress.setNoImpact())
			.blockstate(new SequencedGearshiftGenerator()::generate)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FlywheelBlock> FLYWHEEL = REGISTRATE.block("flywheel", FlywheelBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.TERRACOTTA_YELLOW))
		.transform(axeOrPickaxe())
		.transform(CStress.setNoImpact())
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
			.transform(CStress.setNoImpact())
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
		.transform(CStress.setImpact(2.0))
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
		.clientExtension(() -> () -> new TrackBlock.RenderProperties())
		.onRegister(CreateRegistrate.blockModel(() -> TrackModel::new))
		.blockstate(new TrackBlockStateGenerator()::generate)
		.tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
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
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
			.sound(SoundType.NETHERITE_BLOCK))
		.lang("Train Casing")
		.register();

	public static final BlockEntry<StationBlock> TRACK_STATION = REGISTRATE.block("track_station", StationBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.PODZOL)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
		.transform(displaySource(AllDisplaySources.STATION_SUMMARY))
		.transform(displaySource(AllDisplaySources.TRAIN_STATUS))
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
			.transform(displaySource(AllDisplaySources.OBSERVED_TRAIN_NAME))
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
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
			.sound(SoundType.NETHERITE_BLOCK))
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

	public static final BlockEntry<AndesiteFunnelBlock> ANDESITE_FUNNEL =
		REGISTRATE.block("andesite_funnel", AndesiteFunnelBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.STONE))
			.transform(pickaxeOnly())
			.tag(AllBlockTags.SAFE_NBT.tag)
			.clientExtension(() -> () -> new ReducedDestroyEffects())
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
			.clientExtension(() -> () -> new ReducedDestroyEffects())
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
			.clientExtension(() -> () -> new ReducedDestroyEffects())
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
			.clientExtension(() -> () -> new ReducedDestroyEffects())
			.blockstate(new BeltFunnelGenerator("brass")::generate)
			.loot((p, b) -> p.dropOther(b, BRASS_FUNNEL.get()))
			.register();

	public static final BlockEntry<BeltTunnelBlock> ANDESITE_TUNNEL =
		REGISTRATE.block("andesite_tunnel", BeltTunnelBlock::new)
			.properties(p -> p.mapColor(MapColor.STONE))
			.transform(BuilderTransformers.beltTunnel("andesite", ResourceLocation.withDefaultNamespace("block/polished_andesite")))
			.transform(displaySource(AllDisplaySources.ACCUMULATE_ITEMS))
			.transform(displaySource(AllDisplaySources.ITEM_THROUGHPUT))
			.register();

	public static final BlockEntry<BrassTunnelBlock> BRASS_TUNNEL =
		REGISTRATE.block("brass_tunnel", BrassTunnelBlock::new)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(BuilderTransformers.beltTunnel("brass", Create.asResource("block/brass_block")))
			.transform(displaySource(AllDisplaySources.ACCUMULATE_ITEMS))
			.transform(displaySource(AllDisplaySources.ITEM_THROUGHPUT))
			.onRegister(connectedTextures(BrassTunnelCTBehaviour::new))
			.register();

	public static final BlockEntry<SmartObserverBlock> SMART_OBSERVER =
		REGISTRATE.block("content_observer", SmartObserverBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
				.noOcclusion())
			.properties(p -> p.isRedstoneConductor(($1, $2, $3) -> false))
			.transform(axeOrPickaxe())
			.blockstate(new SmartObserverGenerator()::generate)
			.transform(displaySource(AllDisplaySources.COUNT_ITEMS))
			.transform(displaySource(AllDisplaySources.LIST_ITEMS))
			.transform(displaySource(AllDisplaySources.COUNT_FLUIDS))
			.transform(displaySource(AllDisplaySources.LIST_FLUIDS))
			.transform(displaySource(AllDisplaySources.READ_PACKAGE_ADDRESS))
			.lang("Smart Observer")
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<ThresholdSwitchBlock> THRESHOLD_SWITCH =
		REGISTRATE.block("stockpile_switch", ThresholdSwitchBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
				.noOcclusion())
			.properties(p -> p.isRedstoneConductor(($1, $2, $3) -> false))
			.transform(axeOrPickaxe())
			.blockstate(new ThresholdSwitchGenerator()::generate)
			.transform(displaySource(AllDisplaySources.FILL_LEVEL))
			.lang("Threshold Switch")
			.item()
			.transform(customItemModel("threshold_switch", "block_wall"))
			.register();

	public static final BlockEntry<CreativeCrateBlock> CREATIVE_CRATE =
		REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.transform(BuilderTransformers.crate("creative"))
			.properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
			.transform(mountedItemStorage(AllMountedStorageTypes.CREATIVE_CRATE))
			.register();

	public static final BlockEntry<ItemVaultBlock> ITEM_VAULT = REGISTRATE.block("item_vault", ItemVaultBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
			.sound(SoundType.NETHERITE_BLOCK)
			.explosionResistance(1200))
		.transform(pickaxeOnly())
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.standardModel(c, p))
				.rotationY(s.getValue(ItemVaultBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0)
				.build()))
		.onRegister(connectedTextures(ItemVaultCTBehaviour::new))
		.transform(mountedItemStorage(AllMountedStorageTypes.VAULT))
		.item(ItemVaultItem::new)
		.build()
		.register();

	public static final BlockEntry<ItemHatchBlock> ITEM_HATCH = REGISTRATE.block("item_hatch", ItemHatchBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutoutMipped)
		.blockstate((c, p) -> p.horizontalBlock(c.get(),
			s -> AssetLookup.partialBaseModel(c, p, s.getValue(ItemHatchBlock.OPEN) ? "open" : "closed")))
		.item()
		.transform(customItemModel("_", "block_closed"))
		.register();

	public static final BlockEntry<PackagerBlock> PACKAGER = REGISTRATE.block("packager", PackagerBlock::new)
		.transform(BuilderTransformers.packager())
		.register();

	public static final BlockEntry<RepackagerBlock> REPACKAGER = REGISTRATE.block("repackager", RepackagerBlock::new)
		.transform(BuilderTransformers.packager())
		.lang("Re-Packager")
		.register();

	public static final BlockEntry<FrogportBlock> PACKAGE_FROGPORT =
		REGISTRATE.block("package_frogport", FrogportBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.noOcclusion())
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
				.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item(PackagePortItem::new)
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final DyedBlockList<PostboxBlock> PACKAGE_POSTBOXES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_postbox", p -> new PostboxBlock(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.mapColor(colour))
			.transform(axeOnly())
			.blockstate((c, p) -> {
				p.horizontalBlock(c.get(), s -> {
					String suffix = s.getValue(PostboxBlock.OPEN) ? "open" : "closed";
					return p.models()
						.withExistingParent(colourName + "_postbox_" + suffix,
							p.modLoc("block/package_postbox/block_" + suffix))
						.texture("0", p.modLoc("block/post_box/post_box_" + colourName))
						.texture("1", p.modLoc("block/post_box/post_box_" + colourName + "_" + suffix));
				});
			})
			.tag(AllBlockTags.POSTBOXES.tag)
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.package_postbox"))
			.item(PackagePortItem::new)
			.recipe((c, p) -> {
				ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get())
					.define('D', colour.getTag())
					.define('B', Items.BARREL)
					.define('A', AllItems.ANDESITE_ALLOY)
					.pattern("D")
					.pattern("B")
					.pattern("A")
					.unlockedBy("has_barrel", RegistrateRecipeProvider.has(Items.BARREL))
					.save(p, Create.asResource("crafting/logistics/" + c.getName()));
				ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
					.requires(colour.getTag())
					.requires(AllItemTags.POSTBOXES.tag)
					.unlockedBy("has_postbox", RegistrateRecipeProvider.has(AllItemTags.POSTBOXES.tag))
					.save(p, Create.asResource("crafting/logistics/" + c.getName() + "_from_other_postbox"));
			})
			.model((c, p) -> p.withExistingParent(colourName + "_postbox", p.modLoc("block/package_postbox/item"))
				.texture("0", p.modLoc("block/post_box/post_box_" + colourName))
				.texture("1", p.modLoc("block/post_box/post_box_" + colourName + "_closed")))
			.tag(AllItemTags.POSTBOXES.tag)
			.build()
			.register();
	});

	public static final BlockEntry<PackagerLinkBlock> STOCK_LINK =
		REGISTRATE.block("stock_link", PackagerLinkBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
				.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.blockstate(new PackagerLinkGenerator()::generate)
			.item(LogisticallyLinkedBlockItem::new)
			.transform(customItemModel("_", "block_vertical"))
			.register();

	public static final BlockEntry<StockTickerBlock> STOCK_TICKER =
		REGISTRATE.block("stock_ticker", StockTickerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.sound(SoundType.GLASS))
			.transform(axeOrPickaxe())
			.addLayer(() -> RenderType::cutoutMipped)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p)))
			.item(LogisticallyLinkedBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<RedstoneRequesterBlock> REDSTONE_REQUESTER =
		REGISTRATE.block("redstone_requester", RedstoneRequesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.properties(p -> p.noOcclusion())
			.transform(pickaxeOnly())
			.blockstate((c, p) -> BlockStateGen.horizontalAxisBlock(c, p, AssetLookup.forPowered(c, p)))
			.item(RedstoneRequesterBlockItem::new)
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<FactoryPanelBlock> FACTORY_GAUGE =
		REGISTRATE.block("factory_gauge", FactoryPanelBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion())
			.properties(p -> p.forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(CreateRegistrate.blockModel(() -> FactoryPanelModel::new))
			.transform(displaySource(AllDisplaySources.GAUGE_STATUS))
			.item(FactoryPanelBlockItem::new)
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final DyedBlockList<TableClothBlock> TABLE_CLOTHS = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_table_cloth", p -> new TableClothBlock(p, colour))
			.transform(BuilderTransformers.tableCloth(colourName, () -> Blocks.BLACK_CARPET, true))
			.properties(p -> p.mapColor(colour))
			.recipe((c, p) -> {
				ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get(), 2)
					.requires(DyeHelper.getWoolOfDye(colour))
					.requires(AllItems.ANDESITE_ALLOY)
					.unlockedBy("has_wool", RegistrateRecipeProvider.has(ItemTags.WOOL))
					.save(p, Create.asResource("crafting/logistics/" + c.getName()));
				ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
					.requires(colour.getTag())
					.requires(AllItemTags.DYED_TABLE_CLOTHS.tag)
					.unlockedBy("has_postbox", RegistrateRecipeProvider.has(AllItemTags.DYED_TABLE_CLOTHS.tag))
					.save(p, Create.asResource("crafting/logistics/" + c.getName() + "_from_other_table_cloth"));
			})
			.register();
	});

	public static final BlockEntry<TableClothBlock> ANDESITE_TABLE_CLOTH =
		REGISTRATE.block("andesite_table_cloth", p -> new TableClothBlock(p, "andesite"))
			.transform(BuilderTransformers.tableCloth("andesite", SharedProperties::stone, false))
			.properties(p -> p.mapColor(MapColor.STONE)
				.requiresCorrectToolForDrops())
			.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.ANDESITE_ALLOY.get()),
				RecipeCategory.DECORATIONS, c::get, 2))
			.transform(pickaxeOnly())
			.lang("Andesite Table Cover")
			.register();

	public static final BlockEntry<TableClothBlock> BRASS_TABLE_CLOTH =
		REGISTRATE.block("brass_table_cloth", p -> new TableClothBlock(p, "brass"))
			.transform(BuilderTransformers.tableCloth("brass", SharedProperties::softMetal, false))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
				.requiresCorrectToolForDrops())
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(CommonMetal.BRASS.ingots),
				RecipeCategory.DECORATIONS, c::get, 2))
			.transform(pickaxeOnly())
			.lang("Brass Table Cover")
			.register();

	public static final BlockEntry<TableClothBlock> COPPER_TABLE_CLOTH =
		REGISTRATE.block("copper_table_cloth", p -> new TableClothBlock(p, "copper"))
			.transform(BuilderTransformers.tableCloth("copper", SharedProperties::copperMetal, false))
			.properties(p -> p.requiresCorrectToolForDrops())
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(CommonMetal.COPPER.ingots),
				RecipeCategory.DECORATIONS, c::get, 2))
			.transform(pickaxeOnly())
			.lang("Copper Table Cover")
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
			.transform(CStress.setNoImpact())
			.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.transform(displayTarget(AllDisplayTargets.DISPLAY_BOARD))
			.lang("Display Board")
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<NixieTubeBlock> ORANGE_NIXIE_TUBE =
		REGISTRATE.block("nixie_tube", p -> new NixieTubeBlock(p, DyeColor.ORANGE))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel($ -> 5)
				.mapColor(DyeColor.ORANGE)
				.forceSolidOn())
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
			.properties(p -> p.lightLevel($ -> 5)
				.mapColor(colour)
				.forceSolidOn())
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
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
				.forceSolidOn())
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
		.tag(AllBlockTags.SAFE_NBT.tag)
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

	public static final BlockEntry<BrassDiodeBlock> PULSE_TIMER = REGISTRATE.block("pulse_timer", BrassDiodeBlock::new)
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
			.properties(p -> p.mapColor(MapColor.GOLD)
				.forceSolidOn())
			.transform(BuilderTransformers.bell())
			.onRegister(movementBehaviour(new BellMovementBehaviour()))
			.register();

	public static final BlockEntry<HauntedBellBlock> HAUNTED_BELL =
		REGISTRATE.block("haunted_bell", HauntedBellBlock::new)
			.properties(p -> p.mapColor(MapColor.SAND)
				.forceSolidOn())
			.transform(BuilderTransformers.bell())
			.onRegister(movementBehaviour(new HauntedBellMovementBehaviour()))
			.register();

	public static final BlockEntry<DeskBellBlock> DESK_BELL = REGISTRATE.block("desk_bell", DeskBellBlock::new)
		.properties(p -> p.mapColor(MapColor.SAND))
		.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.onRegister(movementBehaviour(new BellMovementBehaviour()))
		.register();

	public static final DyedBlockList<ToolboxBlock> TOOLBOXES = new DyedBlockList<>(colour -> {
		String colourName = colour.getSerializedName();
		return REGISTRATE.block(colourName + "_toolbox", p -> new ToolboxBlock(p, colour))
			.initialProperties(SharedProperties::wooden)
			.properties(p -> p.sound(SoundType.WOOD)
				.mapColor(colour)
				.forceSolidOn())
			.addLayer(() -> RenderType::cutoutMipped)
			.loot((lt, block) -> {
				lt.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
						.when(ExplosionCondition.survivesExplosion())
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(block)
								.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
								.apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
										.include(AllDataComponents.TOOLBOX_UUID)
										.include(AllDataComponents.TOOLBOX_INVENTORY)
								)
						)
				));
			})
			.blockstate((c, p) -> {
				p.horizontalBlock(c.get(), p.models()
					.withExistingParent(colourName + "_toolbox", p.modLoc("block/toolbox/block"))
					.texture("0", p.modLoc("block/toolbox/" + colourName)));
			})
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.toolbox"))
			.transform(mountedItemStorage(AllMountedStorageTypes.TOOLBOX))
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
		.tag(AllBlockTags.SAFE_NBT.tag)
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
				() -> DataIngredient.tag(CommonMetal.BRASS.ingots), MapColor.TERRACOTTA_YELLOW))
			.register();

	public static final BlockEntry<MetalLadderBlock> COPPER_LADDER =
		REGISTRATE.block("copper_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("copper",
				() -> DataIngredient.tag(CommonMetal.COPPER.ingots), MapColor.COLOR_ORANGE))
			.register();

	public static final BlockEntry<IronBarsBlock> ANDESITE_BARS = MetalBarsGen.createBars("andesite", true,
		() -> DataIngredient.items(AllItems.ANDESITE_ALLOY.get()), MapColor.STONE);
	public static final BlockEntry<IronBarsBlock> BRASS_BARS = MetalBarsGen.createBars("brass", true,
		() -> DataIngredient.tag(CommonMetal.BRASS.ingots), MapColor.TERRACOTTA_YELLOW);
	public static final BlockEntry<IronBarsBlock> COPPER_BARS = MetalBarsGen.createBars("copper", true,
		() -> DataIngredient.tag(CommonMetal.COPPER.ingots), MapColor.COLOR_ORANGE);

	public static final BlockEntry<MetalScaffoldingBlock> ANDESITE_SCAFFOLD = REGISTRATE
		.block("andesite_scaffolding", MetalScaffoldingBlock::new)
		.transform(BuilderTransformers.scaffold("andesite", () -> DataIngredient.items(AllItems.ANDESITE_ALLOY.get()),
			MapColor.STONE, AllSpriteShifts.ANDESITE_SCAFFOLD, AllSpriteShifts.ANDESITE_SCAFFOLD_INSIDE,
			AllSpriteShifts.ANDESITE_CASING))
		.register();

	public static final BlockEntry<MetalScaffoldingBlock> BRASS_SCAFFOLD =
		REGISTRATE.block("brass_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("brass",
				() -> DataIngredient.tag(CommonMetal.BRASS.ingots), MapColor.TERRACOTTA_YELLOW,
				AllSpriteShifts.BRASS_SCAFFOLD, AllSpriteShifts.BRASS_SCAFFOLD_INSIDE, AllSpriteShifts.BRASS_CASING))
			.register();

	public static final BlockEntry<MetalScaffoldingBlock> COPPER_SCAFFOLD =
		REGISTRATE.block("copper_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("copper",
				() -> DataIngredient.tag(CommonMetal.COPPER.ingots), MapColor.COLOR_ORANGE,
				AllSpriteShifts.COPPER_SCAFFOLD, AllSpriteShifts.COPPER_SCAFFOLD_INSIDE, AllSpriteShifts.COPPER_CASING))
			.register();

	public static final BlockEntry<GirderBlock> METAL_GIRDER = REGISTRATE.block("metal_girder", GirderBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate(GirderBlockStateGenerator::blockState)
		.onRegister(CreateRegistrate.blockModel(() -> ConnectedGirderModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GirderEncasedShaftBlock> METAL_GIRDER_ENCASED_SHAFT =
		REGISTRATE.block("metal_girder_encased_shaft", GirderEncasedShaftBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
				.sound(SoundType.NETHERITE_BLOCK))
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
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(CommonMetal.ZINC.ingots),
				RecipeCategory.BUILDING_BLOCKS, c::get, 4))
			.transform(customItemModel("copycat_base", "step"))
			.register();

	public static final BlockEntry<CopycatPanelBlock> COPYCAT_PANEL =
		REGISTRATE.block("copycat_panel", CopycatPanelBlock::new)
			.transform(BuilderTransformers.copycat())
			.onRegister(CreateRegistrate.blockModel(() -> CopycatPanelModel::new))
			.item()
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(CommonMetal.ZINC.ingots),
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
			.transform(displaySource(AllDisplaySources.ENTITY_NAME))
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
		REGISTRATE.block("andesite_door", p -> SlidingDoorBlock.stone(p, true))
			.transform(BuilderTransformers.slidingDoor("andesite"))
			.properties(p -> p.mapColor(MapColor.STONE)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> BRASS_DOOR =
		REGISTRATE.block("brass_door", p -> SlidingDoorBlock.stone(p, false))
			.transform(BuilderTransformers.slidingDoor("brass"))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> COPPER_DOOR =
		REGISTRATE.block("copper_door", p -> SlidingDoorBlock.stone(p, true))
			.transform(BuilderTransformers.slidingDoor("copper"))
			.properties(p -> p.mapColor(MapColor.COLOR_ORANGE)
				.noOcclusion())
			.register();

	public static final BlockEntry<SlidingDoorBlock> TRAIN_DOOR =
		REGISTRATE.block("train_door", p -> SlidingDoorBlock.metal(p, false))
			.transform(BuilderTransformers.slidingDoor("train"))
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
				.noOcclusion())
			.register();

	public static final BlockEntry<TrainTrapdoorBlock> TRAIN_TRAPDOOR =
		REGISTRATE.block("train_trapdoor", TrainTrapdoorBlock::metal)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN))
			.transform(BuilderTransformers.trapdoor(true))
			.register();

	public static final BlockEntry<SlidingDoorBlock> FRAMED_GLASS_DOOR =
		REGISTRATE.block("framed_glass_door", p -> SlidingDoorBlock.glass(p, false))
			.transform(BuilderTransformers.slidingDoor("glass"))
			.properties(p -> p.mapColor(MapColor.NONE)
				.noOcclusion())
			.register();

	public static final BlockEntry<TrainTrapdoorBlock> FRAMED_GLASS_TRAPDOOR =
		REGISTRATE.block("framed_glass_trapdoor", TrainTrapdoorBlock::glass)
			.initialProperties(SharedProperties::softMetal)
			.transform(BuilderTransformers.trapdoor(false))
			.properties(p -> p.mapColor(MapColor.NONE)
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
		.loot((lt, b) ->  {
			HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = lt.getRegistries().lookupOrThrow(Registries.ENCHANTMENT);

			lt.add(b,
				lt.createSilkTouchDispatchTable(b,
				lt.applyExplosionDecay(b, LootItem.lootTableItem(AllItems.RAW_ZINC.get())
					.apply(ApplyBonusCount.addOreBonusCount(enchantmentRegistryLookup.getOrThrow(Enchantments.FORTUNE))))));
		})
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem(Map.of(
			CommonMetal.ZINC.ores.blocks(), CommonMetal.ZINC.ores.items(),
			Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE
		)))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<Block> DEEPSLATE_ZINC_ORE = REGISTRATE.block("deepslate_zinc_ore", Block::new)
		.initialProperties(() -> Blocks.DEEPSLATE_GOLD_ORE)
		.properties(p -> p.mapColor(MapColor.STONE)
			.requiresCorrectToolForDrops()
			.sound(SoundType.DEEPSLATE))
		.transform(pickaxeOnly())
		.loot((lt, b) -> {
			HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = lt.getRegistries().lookupOrThrow(Registries.ENCHANTMENT);

			lt.add(b,
					lt.createSilkTouchDispatchTable(b,
					lt.applyExplosionDecay(b, LootItem.lootTableItem(AllItems.RAW_ZINC.get())
							.apply(ApplyBonusCount.addOreBonusCount(enchantmentRegistryLookup.getOrThrow(Enchantments.FORTUNE))))));
		})
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem(Map.of(
			CommonMetal.ZINC.ores.blocks(), CommonMetal.ZINC.ores.items(),
			Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE
		)))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<Block> RAW_ZINC_BLOCK = REGISTRATE.block("raw_zinc_block", Block::new)
		.initialProperties(() -> Blocks.RAW_GOLD_BLOCK)
		.properties(p -> p.mapColor(MapColor.GLOW_LICHEN)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.lang("Block of Raw Zinc")
		.transform(tagBlockAndItem(CommonMetal.ZINC.rawStorageBlocks))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.register();

	public static final BlockEntry<Block> ZINC_BLOCK = REGISTRATE.block("zinc_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.properties(p -> p.mapColor(MapColor.GLOW_LICHEN)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem(CommonMetal.ZINC.storageBlocks))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Zinc")
		.register();

	public static final BlockEntry<Block> ANDESITE_ALLOY_BLOCK = REGISTRATE.block("andesite_alloy_block", Block::new)
		.initialProperties(() -> Blocks.ANDESITE)
		.properties(p -> p.mapColor(MapColor.STONE)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("andesite_block"))
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.transform(tagBlockAndItem(AllBlockTags.ANDESITE_ALLOY_STORAGE_BLOCKS.tag, AllItemTags.ANDESITE_ALLOY_STORAGE_BLOCKS.tag))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Andesite Alloy")
		.register();

	public static final BlockEntry<Block> INDUSTRIAL_IRON_BLOCK = REGISTRATE.block("industrial_iron_block", Block::new)
		.transform(BuilderTransformers.palettesIronBlock())
		.lang("Block of Industrial Iron")
		.register();

	public static final BlockEntry<Block> WEATHERED_IRON_BLOCK = REGISTRATE.block("weathered_iron_block", Block::new)
		.transform(BuilderTransformers.palettesIronBlock())
		.lang("Block of Weathered Iron")
		.register();

	public static final BlockEntry<Block> BRASS_BLOCK = REGISTRATE.block("brass_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("brass_block"))
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(BlockTags.BEACON_BASE_BLOCKS)
		.transform(tagBlockAndItem(CommonMetal.BRASS.storageBlocks))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.lang("Block of Brass")
		.register();

	public static final BlockEntry<CardboardBlock> CARDBOARD_BLOCK =
		REGISTRATE.block("cardboard_block", CardboardBlock::new)
			.initialProperties(() -> Blocks.MUSHROOM_STEM)
			.properties(p -> p.mapColor(MapColor.COLOR_BROWN)
				.sound(SoundType.CHISELED_BOOKSHELF)
				.ignitedByLava())
			.transform(axeOnly())
			.blockstate(BlockStateGen.horizontalAxisBlockProvider(false))
			.tag(Tags.Blocks.STORAGE_BLOCKS)
			.tag(AllBlockTags.CARDBOARD_STORAGE_BLOCKS.tag)
			.item()
			.burnTime(4000)
			.tag(AllItemTags.CARDBOARD_STORAGE_BLOCKS.tag)
			.tag(Tags.Items.STORAGE_BLOCKS)
			.build()
			.lang("Block of Cardboard")
			.register();

	public static final BlockEntry<CardboardBlock> BOUND_CARDBOARD_BLOCK =
		REGISTRATE.block("bound_cardboard_block", CardboardBlock::new)
			.initialProperties(() -> Blocks.MUSHROOM_STEM)
			.properties(p -> p.mapColor(MapColor.COLOR_BROWN)
				.sound(SoundType.CHISELED_BOOKSHELF)
				.ignitedByLava())
			.transform(axeOnly())
			.blockstate(BlockStateGen.horizontalAxisBlockProvider(false))
			.loot((r, b) -> r.add(b, LootTable.lootTable()
				.withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(b)
						.when(((BlockLootSubProviderAccessor) r).create$hasSilkTouch())
						.otherwise(r.applyExplosionCondition(b, LootItem.lootTableItem(Items.STRING)))))
				.withPool(r.applyExplosionCondition(b, LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(AllBlocks.CARDBOARD_BLOCK.asItem()))
					.when(((BlockLootSubProviderAccessor) r).create$hasSilkTouch().invert())))))
			.item()
			.burnTime(4000)
			.build()
			.lang("Bound Block of Cardboard")
			.register();

	public static final BlockEntry<ExperienceBlock> EXPERIENCE_BLOCK =
		REGISTRATE.block("experience_block", ExperienceBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.PLANT)
				.sound(new DeferredSoundType(1, .5f, () -> SoundEvents.AMETHYST_BLOCK_BREAK,
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
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK)
			.requiresCorrectToolForDrops())
		.transform(pickaxeOnly())
		.blockstate(simpleCubeAll("palettes/rose_quartz_tiles"))
		.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.POLISHED_ROSE_QUARTZ.get()),
			RecipeCategory.BUILDING_BLOCKS, c::get, 2))
		.simpleItem()
		.register();

	public static final BlockEntry<Block> SMALL_ROSE_QUARTZ_TILES =
		REGISTRATE.block("small_rose_quartz_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PINK)
				.requiresCorrectToolForDrops())
			.transform(pickaxeOnly())
			.blockstate(simpleCubeAll("palettes/small_rose_quartz_tiles"))
			.recipe((c, p) -> p.stonecutting(DataIngredient.items(AllItems.POLISHED_ROSE_QUARTZ.get()),
				RecipeCategory.BUILDING_BLOCKS, c::get, 2))
			.simpleItem()
			.register();

	public static final CopperBlockSet COPPER_SHINGLES = new CopperBlockSet(REGISTRATE, "copper_shingles",
		"copper_roof_top", CopperBlockSet.DEFAULT_VARIANTS, (c, p) -> {
		p.stonecutting(DataIngredient.tag(CommonMetal.COPPER.ingots), RecipeCategory.BUILDING_BLOCKS,
			c::get, 2);
	}, (ws, block) -> connectedTextures(() -> new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_SHINGLES.get(ws)))
		.accept(block));

	public static final CopperBlockSet COPPER_TILES =
		new CopperBlockSet(REGISTRATE, "copper_tiles", "copper_roof_top", CopperBlockSet.DEFAULT_VARIANTS, (c, p) -> {
			p.stonecutting(DataIngredient.tag(CommonMetal.COPPER.ingots), RecipeCategory.BUILDING_BLOCKS,
				c::get, 2);
		}, (ws, block) -> connectedTextures(() -> new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_TILES.get(ws)))
			.accept(block));

	// Load this class

	public static void register() {
	}

}
