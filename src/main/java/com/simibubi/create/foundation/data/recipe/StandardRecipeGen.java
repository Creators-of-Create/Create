package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

@SuppressWarnings("unused")
public class StandardRecipeGen extends CreateRecipeProvider {

	/*
	 * Recipes are added through fields, so one can navigate to the right one easily
	 * 
	 * (Ctrl-o) in Eclipse
	 */

	private Marker MATERIALS = enterSection(AllSections.MATERIALS);

	GeneratedRecipe

	COPPER_COMPACTING =
		metalCompacting(ImmutableList.of(AllItems.COPPER_NUGGET, AllItems.COPPER_INGOT, AllBlocks.COPPER_BLOCK),
			ImmutableList.of(I::copperNugget, I::copper, I::copperBlock)),

		BRASS_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.BRASS_NUGGET, AllItems.BRASS_INGOT, AllBlocks.BRASS_BLOCK),
				ImmutableList.of(I::brassNugget, I::brass, I::brassBlock)),

		ZINC_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.ZINC_NUGGET, AllItems.ZINC_INGOT, AllBlocks.ZINC_BLOCK),
				ImmutableList.of(I::zincNugget, I::zinc, I::zincBlock)),

		ANDESITE_ALLOY = create(AllItems.ANDESITE_ALLOY).unlockedByTag(I::iron)
			.viaShaped(b -> b.key('A', Blocks.ANDESITE)
				.key('B', Tags.Items.NUGGETS_IRON)
				.patternLine("BA")
				.patternLine("AB")),

		ANDESITE_ALLOY_FROM_ZINC = create(AllItems.ANDESITE_ALLOY).withSuffix("_from_zinc")
			.unlockedByTag(I::zinc)
			.viaShaped(b -> b.key('A', Blocks.ANDESITE)
				.key('B', I.zincNugget())
				.patternLine("BA")
				.patternLine("AB")),

		ANDESITE_CASING = create(AllBlocks.ANDESITE_CASING).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', ItemTags.PLANKS)
				.key('C', I.andesite())
				.key('S', ItemTags.LOGS)
				.patternLine("AAA")
				.patternLine("CSC")
				.patternLine("AAA")),

		BRASS_CASING = create(AllBlocks.BRASS_CASING).returns(4)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.key('A', ItemTags.PLANKS)
				.key('C', I.brassSheet())
				.key('S', ItemTags.LOGS)
				.patternLine("AAA")
				.patternLine("CSC")
				.patternLine("AAA")),

		COPPER_CASING = create(AllBlocks.COPPER_CASING).returns(4)
			.unlockedByTag(I::copper)
			.viaShaped(b -> b.key('A', ItemTags.PLANKS)
				.key('C', I.copperSheet())
				.key('S', ItemTags.LOGS)
				.patternLine("AAA")
				.patternLine("CSC")
				.patternLine("AAA")),

		RADIANT_CASING = create(AllBlocks.REFINED_RADIANCE_CASING).returns(4)
			.unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.key('A', ItemTags.PLANKS)
				.key('C', I.refinedRadiance())
				.key('S', Tags.Items.GLASS_COLORLESS)
				.patternLine("AAA")
				.patternLine("CSC")
				.patternLine("AAA")),

		SHADOW_CASING = create(AllBlocks.SHADOW_STEEL_CASING).returns(4)
			.unlockedBy(I::shadowSteel)
			.viaShaped(b -> b.key('A', ItemTags.PLANKS)
				.key('C', I.shadowSteel())
				.key('S', Tags.Items.OBSIDIAN)
				.patternLine("AAA")
				.patternLine("CSC")
				.patternLine("AAA")),

		ELECTRON_TUBE = create(AllItems.ELECTRON_TUBE).unlockedBy(AllItems.ROSE_QUARTZ::get)
			.viaShaped(b -> b.key('L', AllItems.POLISHED_ROSE_QUARTZ.get())
				.key('R', Items.REDSTONE_TORCH)
				.key('N', Tags.Items.NUGGETS_IRON)
				.patternLine("L")
				.patternLine("R")
				.patternLine("N")),

		ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ).unlockedBy(() -> Items.REDSTONE)
			.viaShapeless(b -> b.addIngredient(Tags.Items.GEMS_QUARTZ)
				.addIngredient(Ingredient.fromTag(I.redstone()), 8)),

		SAND_PAPER = create(AllItems.SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.SAND_COLORLESS)),

		RED_SAND_PAPER = create(AllItems.RED_SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.SAND_RED))

	;

	private Marker CURIOSITIES = enterSection(AllSections.CURIOSITIES);

	GeneratedRecipe DEFORESTER = create(AllItems.DEFORESTER).unlockedBy(I::refinedRadiance)
		.viaShaped(b -> b.key('E', I.refinedRadiance())
			.key('G', I.cog())
			.key('O', Tags.Items.OBSIDIAN)
			.patternLine("EG")
			.patternLine("EO")
			.patternLine(" O")),

		WAND_OF_SYMMETRY = create(AllItems.WAND_OF_SYMMETRY).unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.key('E', I.refinedRadiance())
				.key('G', Tags.Items.GLASS_PANES_WHITE)
				.key('O', Tags.Items.OBSIDIAN)
				.key('L', I.brass())
				.patternLine(" GE")
				.patternLine("LEG")
				.patternLine("OL ")),

		MINECART_COUPLING = create(AllItems.MINECART_COUPLING).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('E', I.andesite())
				.key('O', I.ironSheet())
				.patternLine("  E")
				.patternLine(" O ")
				.patternLine("E  ")),

		BLOCKZAPPER = create(AllItems.BLOCKZAPPER).unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.key('E', I.refinedRadiance())
				.key('A', I.andesite())
				.key('O', Tags.Items.OBSIDIAN)
				.patternLine("  E")
				.patternLine(" O ")
				.patternLine("OA "))

	;

	private Marker KINETICS = enterSection(AllSections.KINETICS);

	GeneratedRecipe BASIN = create(AllBlocks.BASIN).unlockedBy(I::andesite)
		.viaShaped(b -> b.key('A', I.andesite())
			.patternLine("A A")
			.patternLine("AAA")),

		GOGGLES = create(AllItems.GOGGLES).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('G', Tags.Items.GLASS)
				.key('P', I.goldSheet())
				.key('S', Tags.Items.STRING)
				.patternLine(" S ")
				.patternLine("GPG")),

		WRENCH = create(AllItems.WRENCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('G', I.goldSheet())
				.key('P', I.cog())
				.key('S', Tags.Items.RODS_WOODEN)
				.patternLine("GG")
				.patternLine("GP")
				.patternLine(" S")),

		FILTER = create(AllItems.FILTER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.WOOL)
				.key('A', Tags.Items.NUGGETS_IRON)
				.patternLine("ASA")),

		ATTRIBUTE_FILTER = create(AllItems.ATTRIBUTE_FILTER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.WOOL)
				.key('A', I.copperNugget())
				.patternLine("ASA")),

		BRASS_HAND = create(AllItems.BRASS_HAND).unlockedByTag(I::brass)
			.viaShaped(b -> b.key('A', I.andesite())
				.key('B', I.brassSheet())
				.patternLine(" A ")
				.patternLine("BBB")
				.patternLine(" B ")),

		SUPER_GLUE = create(AllItems.SUPER_GLUE).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.key('A', Tags.Items.SLIMEBALLS)
				.key('S', I.ironSheet())
				.key('N', Tags.Items.NUGGETS_IRON)
				.patternLine("AS")
				.patternLine("NA")),

		CRAFTER_SLOT_COVER = create(AllItems.CRAFTER_SLOT_COVER).unlockedBy(AllBlocks.MECHANICAL_CRAFTER::get)
			.viaShaped(b -> b.key('A', I.brassNugget())
				.patternLine("AAA")),

		COGWHEEL = create(AllBlocks.COGWHEEL).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.WOODEN_BUTTONS)
				.key('C', I.andesite())
				.patternLine("SSS")
				.patternLine("SCS")
				.patternLine("SSS")),

		LARGE_COGWHEEL = create(AllBlocks.LARGE_COGWHEEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.WOODEN_BUTTONS)
				.key('C', I.andesite())
				.key('D', ItemTags.PLANKS)
				.patternLine("SDS")
				.patternLine("DCD")
				.patternLine("SDS")),

		WATER_WHEEL = create(AllBlocks.WATER_WHEEL).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.WOODEN_SLABS)
				.key('C', AllBlocks.LARGE_COGWHEEL.get())
				.patternLine("SSS")
				.patternLine("SCS")
				.patternLine("SSS")),

		SHAFT = create(AllBlocks.SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', I.andesite())
				.patternLine("A")
				.patternLine("A")),

		MECHANICAL_PRESS = create(AllBlocks.MECHANICAL_PRESS).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('B', I.andesite())
				.key('S', I.cog())
				.key('C', I.andesiteCasing())
				.key('I', AllTags.forgeItemTag("storage_blocks/iron"))
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		MILLSTONE = create(AllBlocks.MILLSTONE).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('B', ItemTags.PLANKS)
				.key('S', I.andesite())
				.key('C', I.cog())
				.key('I', I.stone())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		MECHANICAL_PISTON = create(AllBlocks.MECHANICAL_PISTON).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('B', ItemTags.PLANKS)
				.key('S', I.cog())
				.key('C', I.andesiteCasing())
				.key('I', AllBlocks.PISTON_EXTENSION_POLE.get())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		STICKY_MECHANICAL_PISTON = create(AllBlocks.STICKY_MECHANICAL_PISTON).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', Tags.Items.SLIMEBALLS)
				.key('P', AllBlocks.MECHANICAL_PISTON.get())
				.patternLine("S")
				.patternLine("P")),

		TURNTABLE = create(AllBlocks.TURNTABLE).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.shaft())
				.key('P', ItemTags.WOODEN_SLABS)
				.patternLine("P")
				.patternLine("S")),

		PISTON_EXTENSION_POLE = create(AllBlocks.PISTON_EXTENSION_POLE).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', I.andesite())
				.key('P', ItemTags.PLANKS)
				.patternLine("P")
				.patternLine("A")
				.patternLine("P")),

		ANALOG_LEVER = create(AllBlocks.ANALOG_LEVER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.andesiteCasing())
				.key('P', Tags.Items.RODS_WOODEN)
				.patternLine("P")
				.patternLine("S")),

		BELT_CONNECTOR = create(AllItems.BELT_CONNECTOR).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('D', Items.DRIED_KELP)
				.patternLine("DDD")
				.patternLine("DDD")),

		ADJUSTABLE_PULLEY = create(AllBlocks.ADJUSTABLE_PULLEY).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('A', I.electronTube())
				.key('B', AllBlocks.ENCASED_BELT.get())
				.key('C', AllBlocks.LARGE_COGWHEEL.get())
				.patternLine("A")
				.patternLine("B")
				.patternLine("C")),

		CART_ASSEMBLER = create(AllBlocks.CART_ASSEMBLER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('L', ItemTags.LOGS)
				.key('R', I.redstone())
				.key('C', I.andesite())
				.patternLine(" L ")
				.patternLine("CRC")
				.patternLine("L L")),

		HAND_CRANK = create(AllBlocks.HAND_CRANK).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.andesite())
				.key('C', ItemTags.PLANKS)
				.key('B', I.brass())
				.patternLine(" B ")
				.patternLine("CCC")
				.patternLine("  S")),

		NOZZLE = create(AllBlocks.NOZZLE).unlockedBy(AllBlocks.ENCASED_FAN::get)
			.viaShaped(b -> b.key('S', I.andesite())
				.key('C', ItemTags.WOOL)
				.patternLine(" S ")
				.patternLine(" C ")
				.patternLine("SSS")),

		PROPELLER = create(AllItems.PROPELLER).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.key('S', I.ironSheet())
				.key('C', I.andesite())
				.patternLine(" S ")
				.patternLine("SCS")
				.patternLine(" S ")),

		WHISK = create(AllItems.WHISK).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.key('S', I.ironSheet())
				.key('C', I.andesite())
				.patternLine(" C ")
				.patternLine("SCS")
				.patternLine("SSS")),

		ENCASED_FAN = create(AllBlocks.ENCASED_FAN).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.key('S', I.shaft())
				.key('A', I.andesite())
				.key('R', ItemTags.PLANKS)
				.key('B', Items.IRON_BARS)
				.key('P', AllItems.PROPELLER.get())
				.patternLine(" S ")
				.patternLine("RAR")
				.patternLine("BPB")),

		CUCKOO_CLOCK = create(AllBlocks.CUCKOO_CLOCK).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', ItemTags.PLANKS)
				.key('A', Items.CLOCK)
				.key('B', ItemTags.LOGS)
				.key('P', I.cog())
				.patternLine(" S ")
				.patternLine("SAS")
				.patternLine("BPB")),

		MECHANICAL_CRAFTER = create(AllBlocks.MECHANICAL_CRAFTER).returns(3)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('B', I.electronTube())
				.key('R', Blocks.CRAFTING_TABLE)
				.key('C', I.brassCasing())
				.key('S', I.cog())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" R ")),

		MECHANICAL_BEARING = create(AllBlocks.MECHANICAL_BEARING).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('I', I.shaft())
				.key('S', I.andesite())
				.key('B', AllBlocks.TURNTABLE.get())
				.key('C', I.andesiteCasing())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		CLOCKWORK_BEARING = create(AllBlocks.CLOCKWORK_BEARING).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('I', I.shaft())
				.key('S', I.electronTube())
				.key('B', AllBlocks.TURNTABLE.get())
				.key('C', I.brassCasing())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		FLUID_PIPE = create(AllBlocks.FLUID_PIPE).returns(16)
			.unlockedByTag(I::copper)
			.viaShaped(b -> b.key('S', I.copperSheet())
				.key('C', I.copper())
				.patternLine("SCS")),

		MECHANICAL_PUMP = create(AllBlocks.MECHANICAL_PUMP).unlockedByTag(I::copper)
			.viaShaped(b -> b.key('P', I.cog())
				.key('S', AllBlocks.FLUID_PIPE.get())
				.patternLine("P")
				.patternLine("S")),

		SPOUT = create(AllBlocks.SPOUT).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.key('T', AllBlocks.FLUID_TANK.get())
				.key('P', Items.DRIED_KELP)
				.key('S', I.copperNugget())
				.patternLine("T")
				.patternLine("P")
				.patternLine("S")),

		FLUID_TANK = create(AllBlocks.FLUID_TANK).returns(2)
			.unlockedBy(I::copperCasing)
			.viaShaped(b -> b.key('B', I.copperCasing())
				.key('S', I.copperNugget())
				.key('C', Tags.Items.GLASS)
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" B ")),

		DEPLOYER = create(AllBlocks.DEPLOYER).unlockedBy(I::electronTube)
			.viaShaped(b -> b.key('I', AllItems.BRASS_HAND.get())
				.key('B', I.electronTube())
				.key('S', I.cog())
				.key('C', I.andesiteCasing())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		ROPE_PULLEY = create(AllBlocks.ROPE_PULLEY).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.shaft())
				.key('B', I.andesiteCasing())
				.key('C', ItemTags.WOOL)
				.key('I', I.ironSheet())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		EMPTY_BLAZE_BURNER = create(AllItems.EMPTY_BLAZE_BURNER).unlockedByTag(I::iron)
			.viaShaped(b -> b.key('A', Blocks.IRON_BARS)
				.key('I', I.ironSheet())
				.patternLine("II")
				.patternLine("AA")),

		CHUTE = create(AllBlocks.CHUTE).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', I.ironSheet())
				.key('I', I.andesite())
				.patternLine("II")
				.patternLine("AA")),

		DEPOT = create(AllBlocks.DEPOT).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('A', I.andesite())
				.key('I', I.andesiteCasing())
				.patternLine("A")
				.patternLine("I")),

		MECHANICAL_MIXER = create(AllBlocks.MECHANICAL_MIXER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.cog())
				.key('B', I.andesite())
				.key('C', I.andesiteCasing())
				.key('I', AllItems.WHISK.get())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I ")),

		CLUTCH = create(AllBlocks.CLUTCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.shaft())
				.key('B', I.redstone())
				.key('C', I.andesiteCasing())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" B ")),

		GEARSHIFT = create(AllBlocks.GEARSHIFT).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('S', I.cog())
				.key('B', I.redstone())
				.key('C', I.andesiteCasing())
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" B ")),

		RADIAL_CHASIS = create(AllBlocks.RADIAL_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('P', I.andesite())
				.key('L', ItemTags.LOGS)
				.patternLine(" L ")
				.patternLine("PLP")
				.patternLine(" L ")),

		LINEAR_CHASIS = create(AllBlocks.LINEAR_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('P', I.andesite())
				.key('L', ItemTags.LOGS)
				.patternLine(" P ")
				.patternLine("LLL")
				.patternLine(" P ")),

		LINEAR_CHASSIS_CYCLE =
			conversionCycle(ImmutableList.of(AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)),

		MINECART = create(() -> Items.MINECART).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.addIngredient(AllItems.MINECART_CONTRAPTION.get())),

		FURNACE_MINECART = create(() -> Items.FURNACE_MINECART).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.addIngredient(AllItems.FURNACE_MINECART_CONTRAPTION.get())),

		GEARBOX = create(AllBlocks.GEARBOX).unlockedBy(I::cog)
			.viaShaped(b -> b.key('C', I.cog())
				.key('B', I.andesiteCasing())
				.patternLine(" C ")
				.patternLine("CBC")
				.patternLine(" C ")),

		GEARBOX_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.GEARBOX, AllItems.VERTICAL_GEARBOX)),

		MYSTERIOUS_CUCKOO_CLOCK = create(AllBlocks.MYSTERIOUS_CUCKOO_CLOCK).unlockedBy(AllBlocks.CUCKOO_CLOCK::get)
			.viaShaped(b -> b.key('C', Tags.Items.GUNPOWDER)
				.key('B', AllBlocks.CUCKOO_CLOCK.get())
				.patternLine(" C ")
				.patternLine("CBC")
				.patternLine(" C ")),

		ENCASED_BELT = create(AllBlocks.ENCASED_BELT).returns(4)
			.unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('C', I.andesiteCasing())
				.key('B', Items.DRIED_KELP)
				.patternLine(" C ")
				.patternLine("CBC")
				.patternLine(" C ")),

		SPEEDOMETER = create(AllBlocks.SPEEDOMETER).unlockedBy(I::andesite)
			.viaShaped(b -> b.key('C', Items.COMPASS)
				.key('A', I.andesiteCasing())
				.key('S', I.shaft())
				.patternLine(" C ")
				.patternLine("SAS")),

		GAUGE_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.SPEEDOMETER, AllBlocks.STRESSOMETER)),

		ROTATION_SPEED_CONTROLLER = create(AllBlocks.ROTATION_SPEED_CONTROLLER).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('B', I.circuit())
				.key('C', I.brassCasing())
				.key('S', I.shaft())
				.patternLine(" B ")
				.patternLine("SCS")),

		MECHANICAL_SAW = create(AllBlocks.MECHANICAL_SAW).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('C', I.andesiteCasing())
				.key('A', I.ironSheet())
				.key('I', I.iron())
				.patternLine(" A ")
				.patternLine("AIA")
				.patternLine(" C ")),

		MECHANICAL_HARVESTER = create(AllBlocks.MECHANICAL_HARVESTER).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('C', I.andesiteCasing())
				.key('A', I.andesite())
				.key('I', I.ironSheet())
				.patternLine("AIA")
				.patternLine("AIA")
				.patternLine(" C ")),

		MECHANICAL_PLOUGH = create(AllBlocks.MECHANICAL_PLOUGH).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('C', I.andesiteCasing())
				.key('A', I.andesite())
				.key('I', I.ironSheet())
				.patternLine("III")
				.patternLine("AAA")
				.patternLine(" C ")),

		MECHANICAL_DRILL = create(AllBlocks.MECHANICAL_DRILL).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.key('C', I.andesiteCasing())
				.key('A', I.andesite())
				.key('I', I.iron())
				.patternLine(" A ")
				.patternLine("AIA")
				.patternLine(" C ")),

		SEQUENCED_GEARSHIFT = create(AllBlocks.SEQUENCED_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('B', I.electronTube())
				.key('S', I.cog())
				.key('C', I.brassCasing())
				.key('I', Items.CLOCK)
				.patternLine(" B ")
				.patternLine("SCS")
				.patternLine(" I "))

	;

	private Marker LOGISTICS = enterSection(AllSections.LOGISTICS);

	GeneratedRecipe

	REDSTONE_CONTACT = create(AllBlocks.REDSTONE_CONTACT).returns(2)
		.unlockedBy(I::brassCasing)
		.viaShaped(b -> b.key('W', I.redstone())
			.key('D', I.brassCasing())
			.key('S', I.iron())
			.patternLine("WDW")
			.patternLine(" S ")
			.patternLine("WDW")),

		ANDESITE_FUNNEL = create(AllBlocks.ANDESITE_FUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', I.andesite())
				.key('K', Items.DRIED_KELP)
				.patternLine("AKA")
				.patternLine(" K ")),

		BRASS_FUNNEL = create(AllBlocks.BRASS_FUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.key('A', I.brass())
				.key('K', Items.DRIED_KELP)
				.key('E', I.electronTube())
				.patternLine("AEA")
				.patternLine(" K ")),

		ANDESITE_TUNNEL = create(AllBlocks.ANDESITE_TUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.key('A', I.andesite())
				.key('K', Items.DRIED_KELP)
				.patternLine("AA")
				.patternLine("KK")),

		BRASS_TUNNEL = create(AllBlocks.BRASS_TUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.key('A', I.brass())
				.key('K', Items.DRIED_KELP)
				.key('E', I.electronTube())
				.patternLine("E ")
				.patternLine("AA")
				.patternLine("KK")),

		ADJUSTABLE_CRATE = create(AllBlocks.ADJUSTABLE_CRATE).returns(4)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('B', I.brassCasing())
				.patternLine("BBB")
				.patternLine("B B")
				.patternLine("BBB")),

		BELT_OBSERVER = create(AllBlocks.BELT_OBSERVER).unlockedBy(AllItems.BELT_CONNECTOR::get)
			.viaShaped(b -> b.key('B', I.brassCasing())
				.key('R', I.redstone())
				.key('I', I.iron())
				.key('C', Blocks.OBSERVER)
				.patternLine("RCI")
				.patternLine(" B ")),

		STOCKPILE_SWITCH = create(AllBlocks.STOCKPILE_SWITCH).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.key('B', I.brassCasing())
				.key('R', I.redstone())
				.key('I', I.iron())
				.key('C', Blocks.COMPARATOR)
				.patternLine("RCI")
				.patternLine(" B ")),

		ADJUSTABLE_REPEATER = create(AllBlocks.ADJUSTABLE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.key('T', Blocks.REDSTONE_TORCH)
				.key('C', Items.CLOCK)
				.key('R', I.redstone())
				.key('S', I.stone())
				.patternLine("RCT")
				.patternLine("SSS")),

		ADJUSTABLE_PULSE_REPEATER = create(AllBlocks.ADJUSTABLE_PULSE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.key('S', AllBlocks.PULSE_REPEATER.get())
				.key('P', AllBlocks.ADJUSTABLE_REPEATER.get())
				.patternLine("SP")),

		PULSE_REPEATER = create(AllBlocks.PULSE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.key('T', Blocks.REDSTONE_TORCH)
				.key('R', I.redstone())
				.key('S', I.stone())
				.patternLine("RRT")
				.patternLine("SSS")),

		POWERED_TOGGLE_LATCH = create(AllBlocks.POWERED_TOGGLE_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.key('T', Blocks.REDSTONE_TORCH)
				.key('C', Blocks.LEVER)
				.key('S', I.stone())
				.patternLine(" T ")
				.patternLine(" C ")
				.patternLine("SSS")),

		POWERED_LATCH = create(AllBlocks.POWERED_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.key('T', Blocks.REDSTONE_TORCH)
				.key('C', Blocks.LEVER)
				.key('R', I.redstone())
				.key('S', I.stone())
				.patternLine(" T ")
				.patternLine("RCR")
				.patternLine("SSS")),

		REDSTONE_LINK = create(AllBlocks.REDSTONE_LINK).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.key('C', Blocks.REDSTONE_TORCH)
				.key('S', I.brassSheet())
				.key('I', ItemTags.PLANKS)
				.patternLine("  C")
				.patternLine("SIS"))

	;

	private Marker SCHEMATICS = enterSection(AllSections.SCHEMATICS);

	GeneratedRecipe

	SCHEMATIC_TABLE = create(AllBlocks.SCHEMATIC_TABLE).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
		.viaShaped(b -> b.key('W', ItemTags.WOODEN_SLABS)
			.key('S', Blocks.SMOOTH_STONE)
			.patternLine("WWW")
			.patternLine(" S ")
			.patternLine(" S ")),

		SCHEMATICANNON = create(AllBlocks.SCHEMATICANNON).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
			.viaShaped(b -> b.key('L', ItemTags.LOGS)
				.key('D', Blocks.DISPENSER)
				.key('C', Blocks.CAULDRON)
				.key('S', Blocks.SMOOTH_STONE)
				.key('I', Blocks.IRON_BLOCK)
				.patternLine(" C ")
				.patternLine("LDL")
				.patternLine("SIS")),

		EMPTY_SCHEMATIC = create(AllItems.EMPTY_SCHEMATIC).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.DYES_LIGHT_BLUE)),

		SCHEMATIC_AND_QUILL = create(AllItems.SCHEMATIC_AND_QUILL).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(AllItems.EMPTY_SCHEMATIC.get())
				.addIngredient(Tags.Items.FEATHERS))

	;

	private Marker PALETTES = enterSection(AllSections.PALETTES);

	GeneratedRecipe

	DARK_SCORIA = create(AllPaletteBlocks.DARK_SCORIA).returns(8)
		.unlockedBy(() -> AllPaletteBlocks.SCORIA.get())
		.viaShaped(b -> b.key('#', AllPaletteBlocks.SCORIA.get())
			.key('D', Tags.Items.DYES_BLACK)
			.patternLine("###")
			.patternLine("#D#")
			.patternLine("###")),

		COPPER_SHINGLES = create(AllBlocks.COPPER_SHINGLES).returns(16)
			.unlockedByTag(I::copperSheet)
			.viaShaped(b -> b.key('#', I.copperSheet())
				.patternLine("##")
				.patternLine("##")),

		COPPER_SHINGLES_FROM_TILES = create(AllBlocks.COPPER_SHINGLES).withSuffix("_from_tiles")
			.unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.addIngredient(AllBlocks.COPPER_TILES.get())),

		COPPER_TILES = create(AllBlocks.COPPER_TILES).unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.addIngredient(AllBlocks.COPPER_SHINGLES.get()))

	;

	private Marker APPLIANCES = enterFolder("appliances");

	GeneratedRecipe

	DOUGH = create(AllItems.DOUGH).unlockedBy(AllItems.WHEAT_FLOUR::get)
		.viaShapeless(b -> b.addIngredient(AllItems.WHEAT_FLOUR.get())
			.addIngredient(Items.WATER_BUCKET)),

		SLIME_BALL = create(() -> Items.SLIME_BALL).unlockedBy(AllItems.DOUGH::get)
			.viaShapeless(b -> b.addIngredient(AllItems.DOUGH.get())
				.addIngredient(Tags.Items.DYES_LIME)),

		TREE_FERTILIZER = create(AllItems.TREE_FERTILIZER).returns(2)
			.unlockedBy(() -> Items.BONE_MEAL)
			.viaShapeless(b -> b.addIngredient(Ingredient.fromTag(ItemTags.SMALL_FLOWERS), 2)
				.addIngredient(Ingredient.fromItems(Items.HORN_CORAL, Items.BRAIN_CORAL, Items.TUBE_CORAL,
					Items.BUBBLE_CORAL, Items.FIRE_CORAL))
				.addIngredient(Items.BONE_MEAL))

	;

	private Marker COOKING = enterFolder("/");

	GeneratedRecipe

	DOUGH_TO_BREAD = create(() -> Items.BREAD).viaCooking(AllItems.DOUGH::get)
		.inSmoker(),

		LIMESAND = create(AllPaletteBlocks.LIMESTONE::get).viaCooking(AllPaletteBlocks.LIMESAND::get)
			.inFurnace(),
		SOUL_SAND = create(AllPaletteBlocks.SCORIA::get).viaCooking(() -> Blocks.SOUL_SAND)
			.inFurnace(),
		DIORITE = create(AllPaletteBlocks.DOLOMITE::get).viaCooking(() -> Blocks.DIORITE)
			.inFurnace(),
		GRANITE = create(AllPaletteBlocks.GABBRO::get).viaCooking(() -> Blocks.GRANITE)
			.inFurnace(),

		FRAMED_GLASS = recycleGlass(AllPaletteBlocks.FRAMED_GLASS),
		TILED_GLASS = recycleGlass(AllPaletteBlocks.TILED_GLASS),
		VERTICAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.VERTICAL_FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS),
		FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.FRAMED_GLASS_PANE),
		TILED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.TILED_GLASS_PANE),
		VERTICAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.VERTICAL_FRAMED_GLASS_PANE),
		HORIZONTAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS_PANE),

		COPPER_ORE = blastMetalOre(AllItems.COPPER_INGOT::get, AllTags.forgeItemTag("ores/copper")),
		ZINC_ORE = blastMetalOre(AllItems.ZINC_INGOT::get, AllTags.forgeItemTag("ores/zinc")),
		CRUSHED_IRON = blastCrushedMetal(() -> Items.IRON_INGOT, AllItems.CRUSHED_IRON::get),
		CRUSHED_GOLD = blastCrushedMetal(() -> Items.GOLD_INGOT, AllItems.CRUSHED_GOLD::get),
		CRUSHED_COPPER = blastCrushedMetal(AllItems.COPPER_INGOT::get, AllItems.CRUSHED_COPPER::get),
		CRUSHED_ZINC = blastCrushedMetal(AllItems.ZINC_INGOT::get, AllItems.CRUSHED_ZINC::get),
		CRUSHED_BRASS = blastCrushedMetal(AllItems.BRASS_INGOT::get, AllItems.CRUSHED_BRASS::get)

	;

	/*
	 * End of recipe list
	 */

	String currentFolder = "";

	Marker enterSection(AllSections section) {
		currentFolder = Lang.asId(section.name());
		return new Marker();
	}

	Marker enterFolder(String folder) {
		currentFolder = folder;
		return new Marker();
	}

	GeneratedRecipeBuilder create(Supplier<IItemProvider> result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}
	
	GeneratedRecipeBuilder create(ItemProviderEntry<? extends IItemProvider> result) {
		return create(result::get);
	}

	GeneratedRecipe blastCrushedMetal(Supplier<? extends IItemProvider> result,
		Supplier<? extends IItemProvider> ingredient) {
		return create(result::get).withSuffix("_from_crushed").viaCooking(ingredient::get)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe blastMetalOre(Supplier<? extends IItemProvider> result, Tag<Item> ore) {
		return create(result::get).withSuffix("_from_ore").viaCookingTag(() -> ore)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe recycleGlass(BlockEntry<? extends Block> ingredient) {
		return create(() -> Blocks.GLASS).withSuffix("_from_" + ingredient.getId()
			.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe recycleGlassPane(BlockEntry<? extends Block> ingredient) {
		return create(() -> Blocks.GLASS_PANE).withSuffix("_from_" + ingredient.getId()
			.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe metalCompacting(List<ItemProviderEntry<? extends IItemProvider>> variants,
		List<Supplier<Tag<Item>>> ingredients) {
		GeneratedRecipe result = null;
		for (int i = 0; i + 1 < variants.size(); i++) {
			ItemProviderEntry<? extends IItemProvider> currentEntry = variants.get(i);
			ItemProviderEntry<? extends IItemProvider> nextEntry = variants.get(i + 1);
			Supplier<Tag<Item>> currentIngredient = ingredients.get(i);
			Supplier<Tag<Item>> nextIngredient = ingredients.get(i + 1);

			result = create(nextEntry).withSuffix("_from_compacting")
				.unlockedBy(currentEntry::get)
				.viaShaped(b -> b.patternLine("###")
					.patternLine("###")
					.patternLine("###")
					.key('#', currentIngredient.get()));

			result = create(currentEntry).returns(9)
				.withSuffix("_from_decompacting")
				.unlockedBy(nextEntry::get)
				.viaShapeless(b -> b.addIngredient(nextIngredient.get()));
		}
		return result;
	}

	GeneratedRecipe conversionCycle(List<ItemProviderEntry<? extends IItemProvider>> cycle) {
		GeneratedRecipe result = null;
		for (int i = 0; i < cycle.size(); i++) {
			ItemProviderEntry<? extends IItemProvider> currentEntry = cycle.get(i);
			ItemProviderEntry<? extends IItemProvider> nextEntry = cycle.get((i + 1) % cycle.size());
			result = create(nextEntry).withSuffix("from_conversion")
				.unlockedBy(currentEntry::get)
				.viaShapeless(b -> b.addIngredient(currentEntry.get()));
		}
		return result;
	}

	class GeneratedRecipeBuilder {

		private String path;
		private String suffix;
		private Supplier<? extends IItemProvider> result;
		private Supplier<ItemPredicate> unlockedBy;
		private int amount;

		public GeneratedRecipeBuilder(String path, Supplier<? extends IItemProvider> result) {
			this.path = path;
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder unlockedBy(Supplier<? extends IItemProvider> item) {
			this.unlockedBy = () -> ItemPredicate.Builder.create()
				.item(item.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder unlockedByTag(Supplier<Tag<Item>> tag) {
			this.unlockedBy = () -> ItemPredicate.Builder.create()
				.tag(tag.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
			return register(consumer -> {
				ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shapedRecipe(result.get(), amount));
				if (unlockedBy != null)
					b.addCriterion("has_item", hasItem(unlockedBy.get()));
				b.build(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
			return register(consumer -> {
				ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapelessRecipe(result.get(), amount));
				if (unlockedBy != null)
					b.addCriterion("has_item", hasItem(unlockedBy.get()));
				b.build(consumer, createLocation("crafting"));
			});
		}

		private ResourceLocation createSimpleLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + result.get()
				.asItem()
				.getRegistryName()
				.getPath() + suffix);
		}

		private ResourceLocation createLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + path + "/" + result.get()
				.asItem()
				.getRegistryName()
				.getPath() + suffix);
		}

		GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends IItemProvider> item) {
			return unlockedBy(item).viaCookingIngredient(() -> Ingredient.fromItems(item.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingTag(Supplier<Tag<Item>> tag) {
			return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.fromTag(tag.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
			return new GeneratedCookingRecipeBuilder(ingredient);
		}

		class GeneratedCookingRecipeBuilder {

			private Supplier<Ingredient> ingredient;
			private float exp;
			private int cookingTime;

			private final CookingRecipeSerializer<?> FURNACE = IRecipeSerializer.SMELTING,
				SMOKER = IRecipeSerializer.SMOKING, BLAST = IRecipeSerializer.BLASTING,
				CAMPFIRE = IRecipeSerializer.CAMPFIRE_COOKING;

			GeneratedCookingRecipeBuilder(Supplier<Ingredient> ingredient) {
				this.ingredient = ingredient;
				cookingTime = 200;
				exp = 0;
			}

			GeneratedCookingRecipeBuilder forDuration(int duration) {
				cookingTime = duration;
				return this;
			}

			GeneratedCookingRecipeBuilder rewardXP(float xp) {
				exp = xp;
				return this;
			}

			GeneratedRecipe inFurnace() {
				return inFurnace(b -> b);
			}

			GeneratedRecipe inFurnace(UnaryOperator<CookingRecipeBuilder> builder) {
				return create(FURNACE, builder, 1);
			}

			GeneratedRecipe inSmoker() {
				return inSmoker(b -> b);
			}

			GeneratedRecipe inSmoker(UnaryOperator<CookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				create(CAMPFIRE, builder, 3);
				return create(SMOKER, builder, .5f);
			}

			GeneratedRecipe inBlastFurnace() {
				return inBlastFurnace(b -> b);
			}

			GeneratedRecipe inBlastFurnace(UnaryOperator<CookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				return create(BLAST, builder, .5f);
			}

			private GeneratedRecipe create(CookingRecipeSerializer<?> serializer,
				UnaryOperator<CookingRecipeBuilder> builder, float cookingTimeModifier) {
				return register(consumer -> {
					CookingRecipeBuilder b = builder.apply(CookingRecipeBuilder.cookingRecipe(ingredient.get(),
						result.get(), exp, (int) (cookingTime * cookingTimeModifier), serializer));
					if (unlockedBy != null)
						b.addCriterion("has_item", hasItem(unlockedBy.get()));
					b.build(consumer, createSimpleLocation(serializer.getRegistryName()
						.getPath()));
				});
			}

		}

	}

	@Override
	public String getName() {
		return "Create's Standard Recipes";
	}

	public StandardRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

}
