package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.Mods.EID;
import static com.simibubi.create.foundation.data.recipe.Mods.IE;
import static com.simibubi.create.foundation.data.recipe.Mods.INF;
import static com.simibubi.create.foundation.data.recipe.Mods.MEK;
import static com.simibubi.create.foundation.data.recipe.Mods.MW;
import static com.simibubi.create.foundation.data.recipe.Mods.SM;
import static com.simibubi.create.foundation.data.recipe.Mods.TH;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

@SuppressWarnings("unused")
public class StandardRecipeGen extends CreateRecipeProvider {

	/*
	 * Recipes are added through fields, so one can navigate to the right one easily
	 *
	 * (Ctrl-o) in Eclipse
	 */

	private Marker MATERIALS = enterSection(AllSections.MATERIALS);

	GeneratedRecipe

	COPPER_NUGGET = create(AllItems.COPPER_NUGGET).returns(9)
		.unlockedBy(() -> Items.COPPER_INGOT)
		.viaShapeless(b -> b.requires(I.copper())),

		COPPER_INGOT = create(() -> Items.COPPER_INGOT).unlockedBy(AllItems.COPPER_NUGGET::get)
			.viaShaped(b -> b.define('C', I.copperNugget())
				.pattern("CCC")
				.pattern("CCC")
				.pattern("CCC")),

		BRASS_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.BRASS_NUGGET, AllItems.BRASS_INGOT, AllBlocks.BRASS_BLOCK),
				ImmutableList.of(I::brassNugget, I::brass, I::brassBlock)),

		ZINC_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.ZINC_NUGGET, AllItems.ZINC_INGOT, AllBlocks.ZINC_BLOCK),
				ImmutableList.of(I::zincNugget, I::zinc, I::zincBlock)),

		ANDESITE_ALLOY = create(AllItems.ANDESITE_ALLOY).unlockedByTag(I::iron)
			.viaShaped(b -> b.define('A', Blocks.ANDESITE)
				.define('B', Tags.Items.NUGGETS_IRON)
				.pattern("BA")
				.pattern("AB")),

		ANDESITE_ALLOY_FROM_ZINC = create(AllItems.ANDESITE_ALLOY).withSuffix("_from_zinc")
			.unlockedByTag(I::zinc)
			.viaShaped(b -> b.define('A', Blocks.ANDESITE)
				.define('B', I.zincNugget())
				.pattern("BA")
				.pattern("AB")),

		ANDESITE_CASING = create(AllBlocks.ANDESITE_CASING).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', ItemTags.PLANKS)
				.define('C', I.andesite())
				.define('S', ItemTags.LOGS)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		BRASS_CASING = create(AllBlocks.BRASS_CASING).returns(4)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.define('A', ItemTags.PLANKS)
				.define('C', I.brassSheet())
				.define('S', ItemTags.LOGS)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		COPPER_CASING = create(AllBlocks.COPPER_CASING).returns(4)
			.unlockedBy(I::copper)
			.viaShaped(b -> b.define('A', ItemTags.PLANKS)
				.define('C', I.copperSheet())
				.define('S', ItemTags.LOGS)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		RADIANT_CASING = create(AllBlocks.REFINED_RADIANCE_CASING).returns(4)
			.unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.define('A', ItemTags.PLANKS)
				.define('C', I.refinedRadiance())
				.define('S', Tags.Items.GLASS_COLORLESS)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		SHADOW_CASING = create(AllBlocks.SHADOW_STEEL_CASING).returns(4)
			.unlockedBy(I::shadowSteel)
			.viaShaped(b -> b.define('A', ItemTags.PLANKS)
				.define('C', I.shadowSteel())
				.define('S', Tags.Items.OBSIDIAN)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		ELECTRON_TUBE = create(AllItems.ELECTRON_TUBE).unlockedBy(AllItems.ROSE_QUARTZ::get)
			.viaShaped(b -> b.define('L', AllItems.POLISHED_ROSE_QUARTZ.get())
				.define('R', Items.REDSTONE_TORCH)
				.define('N', Tags.Items.NUGGETS_IRON)
				.pattern("L")
				.pattern("R")
				.pattern("N")),

		ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ).unlockedBy(() -> Items.REDSTONE)
			.viaShapeless(b -> b.requires(Tags.Items.GEMS_QUARTZ)
				.requires(Ingredient.of(I.redstone()), 8)),

		SAND_PAPER = create(AllItems.SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(Items.PAPER)
				.requires(Tags.Items.SAND_COLORLESS)),

		RED_SAND_PAPER = create(AllItems.RED_SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(Items.PAPER)
				.requires(Tags.Items.SAND_RED))

	;

	private Marker CURIOSITIES = enterSection(AllSections.CURIOSITIES);

	GeneratedRecipe WAND_OF_SYMMETRY = create(AllItems.WAND_OF_SYMMETRY).unlockedBy(I::refinedRadiance)
		.viaShaped(b -> b.define('E', I.refinedRadiance())
			.define('G', Tags.Items.GLASS_PANES_WHITE)
			.define('O', Tags.Items.OBSIDIAN)
			.define('L', I.brass())
			.pattern(" GE")
			.pattern("LEG")
			.pattern("OL ")),

		TOOLBOX = create(AllBlocks.TOOLBOXES.get(DyeColor.BROWN)).unlockedByTag(I::goldSheet)
			.viaShaped(b -> b.define('S', I.goldSheet())
				.define('C', I.cog())
				.define('W', Tags.Items.CHESTS_WOODEN)
				.define('L', Tags.Items.LEATHER)
				.pattern(" C ")
				.pattern("SWS")
				.pattern(" L ")),

		TOOLBOX_DYEING = createSpecial(AllRecipeTypes.TOOLBOX_DYEING::getSerializer, "crafting", "toolbox_dyeing"),

		MINECART_COUPLING = create(AllItems.MINECART_COUPLING).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('E', I.andesite())
				.define('O', I.ironSheet())
				.pattern("  E")
				.pattern(" O ")
				.pattern("E  ")),

		PECULIAR_BELL = create(AllBlocks.PECULIAR_BELL).unlockedByTag(I::brass)
			.viaShaped(b -> b.define('I', I.brassBlock())
				.define('P', I.brassSheet())
				.pattern("I")
				.pattern("P"))

	;

	private Marker KINETICS = enterSection(AllSections.KINETICS);

	GeneratedRecipe BASIN = create(AllBlocks.BASIN).unlockedBy(I::andesite)
		.viaShaped(b -> b.define('A', I.andesite())
			.pattern("A A")
			.pattern("AAA")),

		GOGGLES = create(AllItems.GOGGLES).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('G', Tags.Items.GLASS)
				.define('P', I.goldSheet())
				.define('S', Tags.Items.STRING)
				.pattern(" S ")
				.pattern("GPG")),

		WRENCH = create(AllItems.WRENCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('G', I.goldSheet())
				.define('P', I.cog())
				.define('S', Tags.Items.RODS_WOODEN)
				.pattern("GG")
				.pattern("GP")
				.pattern(" S")),

		FILTER = create(AllItems.FILTER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.WOOL)
				.define('A', Tags.Items.NUGGETS_IRON)
				.pattern("ASA")),

		ATTRIBUTE_FILTER = create(AllItems.ATTRIBUTE_FILTER).unlockedByTag(I::brass)
			.viaShaped(b -> b.define('S', ItemTags.WOOL)
				.define('A', I.brassNugget())
				.pattern("ASA")),

		BRASS_HAND = create(AllItems.BRASS_HAND).unlockedByTag(I::brass)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('B', I.brassSheet())
				.pattern(" A ")
				.pattern("BBB")
				.pattern(" B ")),

		SUPER_GLUE = create(AllItems.SUPER_GLUE).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.define('A', Tags.Items.SLIMEBALLS)
				.define('S', I.ironSheet())
				.define('N', Tags.Items.NUGGETS_IRON)
				.pattern("AS")
				.pattern("NA")),

		CRAFTER_SLOT_COVER = create(AllItems.CRAFTER_SLOT_COVER).unlockedBy(AllBlocks.MECHANICAL_CRAFTER::get)
			.viaShaped(b -> b.define('A', I.brassNugget())
				.pattern("AAA")),

		COGWHEEL = create(AllBlocks.COGWHEEL).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.WOODEN_BUTTONS)
				.define('C', I.andesite())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		LARGE_COGWHEEL = create(AllBlocks.LARGE_COGWHEEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.WOODEN_BUTTONS)
				.define('C', I.andesite())
				.define('D', ItemTags.PLANKS)
				.pattern("SDS")
				.pattern("DCD")
				.pattern("SDS")),

		WATER_WHEEL = create(AllBlocks.WATER_WHEEL).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.WOODEN_SLABS)
				.define('C', AllBlocks.LARGE_COGWHEEL.get())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		SHAFT = create(AllBlocks.SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.pattern("A")
				.pattern("A")),

		MECHANICAL_PRESS = create(AllBlocks.MECHANICAL_PRESS).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('B', I.andesite())
				.define('S', I.cog())
				.define('C', I.andesiteCasing())
				.define('I', AllTags.forgeItemTag("storage_blocks/iron"))
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		MILLSTONE = create(AllBlocks.MILLSTONE).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('B', ItemTags.PLANKS)
				.define('S', I.andesite())
				.define('C', I.cog())
				.define('I', I.stone())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		MECHANICAL_PISTON = create(AllBlocks.MECHANICAL_PISTON).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('B', ItemTags.PLANKS)
				.define('S', I.cog())
				.define('C', I.andesiteCasing())
				.define('I', AllBlocks.PISTON_EXTENSION_POLE.get())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		STICKY_MECHANICAL_PISTON = create(AllBlocks.STICKY_MECHANICAL_PISTON).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', Tags.Items.SLIMEBALLS)
				.define('P', AllBlocks.MECHANICAL_PISTON.get())
				.pattern("S")
				.pattern("P")),

		TURNTABLE = create(AllBlocks.TURNTABLE).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('P', ItemTags.WOODEN_SLABS)
				.pattern("P")
				.pattern("S")),

		PISTON_EXTENSION_POLE = create(AllBlocks.PISTON_EXTENSION_POLE).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('P', ItemTags.PLANKS)
				.pattern("P")
				.pattern("A")
				.pattern("P")),

		GANTRY_PINION = create(AllBlocks.GANTRY_CARRIAGE).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('B', ItemTags.PLANKS)
				.define('S', I.cog())
				.define('C', I.andesiteCasing())
				.define('I', I.shaft())
				.pattern(" B ")
				.pattern("ICI")
				.pattern(" S ")),

		GANTRY_SHAFT = create(AllBlocks.GANTRY_SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('R', I.redstone())
				.pattern("A")
				.pattern("R")
				.pattern("A")),

		ANALOG_LEVER = create(AllBlocks.ANALOG_LEVER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.andesiteCasing())
				.define('P', Tags.Items.RODS_WOODEN)
				.pattern("P")
				.pattern("S")),

		BELT_CONNECTOR = create(AllItems.BELT_CONNECTOR).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('D', Items.DRIED_KELP)
				.pattern("DDD")
				.pattern("DDD")),

		ADJUSTABLE_PULLEY = create(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('A', I.electronTube())
				.define('B', AllBlocks.ENCASED_CHAIN_DRIVE.get())
				.define('C', AllBlocks.LARGE_COGWHEEL.get())
				.pattern("A")
				.pattern("B")
				.pattern("C")),

		CART_ASSEMBLER = create(AllBlocks.CART_ASSEMBLER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('L', ItemTags.LOGS)
				.define('R', I.redstone())
				.define('C', I.andesite())
				.pattern(" L ")
				.pattern("CRC")
				.pattern("L L")),

		CONTROLLER_RAIL = create(AllBlocks.CONTROLLER_RAIL).returns(6)
			.unlockedBy(() -> Items.POWERED_RAIL)
			.viaShaped(b -> b.define('A', I.gold())
				.define('E', I.electronTube())
				.define('S', Tags.Items.RODS_WOODEN)
				.pattern("A A")
				.pattern("ASA")
				.pattern("AEA")),

		HAND_CRANK = create(AllBlocks.HAND_CRANK).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('C', ItemTags.PLANKS)
				.define('S', I.shaft())
				.pattern(" S ")
				.pattern("CCC")
				.pattern("  A")),

		COPPER_VALVE_HANDLE = create(AllBlocks.COPPER_VALVE_HANDLE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('S', I.andesite())
				.define('C', I.copperSheet())
				.pattern("CCC")
				.pattern(" S ")),

		COPPER_VALVE_HANDLE_FROM_OTHER_HANDLES = create(AllBlocks.COPPER_VALVE_HANDLE).withSuffix("_from_others")
			.unlockedBy(I::copper)
			.viaShapeless(b -> b.requires(AllItemTags.VALVE_HANDLES.tag)),

		NOZZLE = create(AllBlocks.NOZZLE).unlockedBy(AllBlocks.ENCASED_FAN::get)
			.viaShaped(b -> b.define('S', I.andesite())
				.define('C', ItemTags.WOOL)
				.pattern(" S ")
				.pattern(" C ")
				.pattern("SSS")),

		PROPELLER = create(AllItems.PROPELLER).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.define('S', I.ironSheet())
				.define('C', I.andesite())
				.pattern(" S ")
				.pattern("SCS")
				.pattern(" S ")),

		WHISK = create(AllItems.WHISK).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.define('S', I.ironSheet())
				.define('C', I.andesite())
				.pattern(" C ")
				.pattern("SCS")
				.pattern("SSS")),

		ENCASED_FAN = create(AllBlocks.ENCASED_FAN).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('A', I.andesiteCasing())
				.define('R', I.cog())
				.define('P', AllItems.PROPELLER.get())
				.pattern(" S ")
				.pattern("RAR")
				.pattern(" P ")),

		CUCKOO_CLOCK = create(AllBlocks.CUCKOO_CLOCK).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.PLANKS)
				.define('A', Items.CLOCK)
				.define('B', ItemTags.LOGS)
				.define('P', I.cog())
				.pattern(" S ")
				.pattern("SAS")
				.pattern("BPB")),

		MECHANICAL_CRAFTER = create(AllBlocks.MECHANICAL_CRAFTER).returns(3)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.electronTube())
				.define('R', Blocks.CRAFTING_TABLE)
				.define('C', I.brassCasing())
				.define('S', I.cog())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" R ")),

		WINDMILL_BEARING = create(AllBlocks.WINDMILL_BEARING).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('I', I.shaft())
				.define('B', AllBlocks.TURNTABLE.get())
				.define('C', I.stone())
				.pattern(" B ")
				.pattern(" C ")
				.pattern(" I ")),

		MECHANICAL_BEARING = create(AllBlocks.MECHANICAL_BEARING).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('I', I.shaft())
				.define('S', I.andesite())
				.define('B', AllBlocks.TURNTABLE.get())
				.define('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		CLOCKWORK_BEARING = create(AllBlocks.CLOCKWORK_BEARING).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('I', I.shaft())
				.define('S', I.electronTube())
				.define('B', AllBlocks.TURNTABLE.get())
				.define('C', I.brassCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		WOODEN_BRACKET = create(AllBlocks.WOODEN_BRACKET).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', Tags.Items.RODS_WOODEN)
				.define('P', I.planks())
				.define('C', I.andesite())
				.pattern("SSS")
				.pattern("PCP")),

		METAL_BRACKET = create(AllBlocks.METAL_BRACKET).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', Tags.Items.NUGGETS_IRON)
				.define('P', I.iron())
				.define('C', I.andesite())
				.pattern("SSS")
				.pattern("PCP")),

		FLUID_PIPE = create(AllBlocks.FLUID_PIPE).returns(8)
			.unlockedBy(I::copper)
			.viaShaped(b -> b.define('S', I.copperSheet())
				.define('C', I.copper())
				.pattern("SCS")),

		MECHANICAL_PUMP = create(AllBlocks.MECHANICAL_PUMP).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.cog())
				.define('S', AllBlocks.FLUID_PIPE.get())
				.pattern("P")
				.pattern("S")),

		SMART_FLUID_PIPE = create(AllBlocks.SMART_FLUID_PIPE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.electronTube())
				.define('S', AllBlocks.FLUID_PIPE.get())
				.define('I', I.brassSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		FLUID_VALVE = create(AllBlocks.FLUID_VALVE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.shaft())
				.define('S', AllBlocks.FLUID_PIPE.get())
				.define('I', I.ironSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		SPOUT = create(AllBlocks.SPOUT).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('T', AllBlocks.FLUID_TANK.get())
				.define('P', Items.DRIED_KELP)
				.define('S', I.copperNugget())
				.pattern("T")
				.pattern("P")
				.pattern("S")),

		ITEM_DRAIN = create(AllBlocks.ITEM_DRAIN).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('P', Blocks.IRON_BARS)
				.define('S', I.copperCasing())
				.pattern("P")
				.pattern("S")),

		FLUID_TANK = create(AllBlocks.FLUID_TANK).returns(2)
			.unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('B', I.copperCasing())
				.define('S', I.copperNugget())
				.define('C', Tags.Items.GLASS)
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		DEPLOYER = create(AllBlocks.DEPLOYER).unlockedBy(I::electronTube)
			.viaShaped(b -> b.define('I', AllItems.BRASS_HAND.get())
				.define('B', I.electronTube())
				.define('S', I.cog())
				.define('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		PORTABLE_STORAGE_INTERFACE = create(AllBlocks.PORTABLE_STORAGE_INTERFACE).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('I', I.brassCasing())
				.define('B', AllBlocks.ANDESITE_FUNNEL.get())
				.pattern(" B ")
				.pattern(" I ")),

		PORTABLE_FLUID_INTERFACE = create(AllBlocks.PORTABLE_FLUID_INTERFACE).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('I', I.copperCasing())
				.define('B', AllBlocks.ANDESITE_FUNNEL.get())
				.pattern(" B ")
				.pattern(" I ")),

		ROPE_PULLEY = create(AllBlocks.ROPE_PULLEY).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('B', I.andesiteCasing())
				.define('C', ItemTags.WOOL)
				.define('I', I.ironSheet())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		HOSE_PULLEY = create(AllBlocks.HOSE_PULLEY).unlockedBy(I::copper)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('P', AllBlocks.FLUID_PIPE.get())
				.define('B', I.copperCasing())
				.define('C', Items.DRIED_KELP)
				.define('I', I.copperSheet())
				.pattern(" B ")
				.pattern("SCP")
				.pattern(" I ")),

		EMPTY_BLAZE_BURNER = create(AllItems.EMPTY_BLAZE_BURNER).unlockedByTag(I::iron)
			.viaShaped(b -> b.define('A', Blocks.IRON_BARS)
				.define('I', I.ironSheet())
				.pattern("II")
				.pattern("AA")),

		CHUTE = create(AllBlocks.CHUTE).unlockedBy(I::andesite)
			.returns(4)
			.viaShaped(b -> b.define('A', I.ironSheet())
				.define('I', I.andesite())
				.pattern("II")
				.pattern("AA")),

		SMART_CHUTE = create(AllBlocks.SMART_CHUTE).unlockedBy(AllBlocks.CHUTE::get)
			.viaShaped(b -> b.define('P', I.electronTube())
				.define('S', AllBlocks.CHUTE.get())
				.define('I', I.brassSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		DEPOT = create(AllBlocks.DEPOT).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('I', I.andesiteCasing())
				.pattern("A")
				.pattern("I")),

		WEIGHTED_EJECTOR = create(AllBlocks.WEIGHTED_EJECTOR).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('A', I.goldSheet())
				.define('D', AllBlocks.DEPOT.get())
				.define('I', I.cog())
				.pattern("A")
				.pattern("D")
				.pattern("I")),

		MECHANICAL_ARM = create(AllBlocks.MECHANICAL_ARM::get).unlockedBy(I::brassCasing)
			.returns(1)
			.viaShaped(b -> b.define('L', I.brassSheet())
				.define('I', I.precisionMechanism())
				.define('A', I.andesite())
				.define('C', I.brassCasing())
				.pattern("LLA")
				.pattern("L  ")
				.pattern("IC ")),

		MECHANICAL_MIXER = create(AllBlocks.MECHANICAL_MIXER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.cog())
				.define('B', I.andesite())
				.define('C', I.andesiteCasing())
				.define('I', AllItems.WHISK.get())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		CLUTCH = create(AllBlocks.CLUTCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('B', I.redstone())
				.define('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		GEARSHIFT = create(AllBlocks.GEARSHIFT).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.cog())
				.define('B', I.redstone())
				.define('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		SAIL_FRAME = create(AllBlocks.SAIL_FRAME).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('S', Tags.Items.RODS_WOODEN)
				.pattern("SSS")
				.pattern("SAS")
				.pattern("SSS")),

		SAIL = create(AllBlocks.SAIL).returns(8)
			.unlockedBy(AllBlocks.SAIL_FRAME::get)
			.viaShaped(b -> b.define('F', AllBlocks.SAIL_FRAME.get())
				.define('W', ItemTags.WOOL)
				.pattern("FFF")
				.pattern("FWF")
				.pattern("FFF")),

		RADIAL_CHASIS = create(AllBlocks.RADIAL_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('P', I.andesite())
				.define('L', ItemTags.LOGS)
				.pattern(" L ")
				.pattern("PLP")
				.pattern(" L ")),

		LINEAR_CHASIS = create(AllBlocks.LINEAR_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('P', I.andesite())
				.define('L', ItemTags.LOGS)
				.pattern(" P ")
				.pattern("LLL")
				.pattern(" P ")),

		LINEAR_CHASSIS_CYCLE =
			conversionCycle(ImmutableList.of(AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)),

		STICKER = create(AllBlocks.STICKER).returns(1)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('I', I.andesite())
				.define('C', Tags.Items.COBBLESTONE)
				.define('R', I.redstone())
				.define('S', Tags.Items.SLIMEBALLS)
				.pattern("ISI")
				.pattern("CRC")),

		MINECART = create(() -> Items.MINECART).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.requires(AllItems.MINECART_CONTRAPTION.get())),

		FURNACE_MINECART = create(() -> Items.FURNACE_MINECART).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.requires(AllItems.FURNACE_MINECART_CONTRAPTION.get())),

		GEARBOX = create(AllBlocks.GEARBOX).unlockedBy(I::cog)
			.viaShaped(b -> b.define('C', I.cog())
				.define('B', I.andesiteCasing())
				.pattern(" C ")
				.pattern("CBC")
				.pattern(" C ")),

		GEARBOX_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.GEARBOX, AllItems.VERTICAL_GEARBOX)),

		MYSTERIOUS_CUCKOO_CLOCK = create(AllBlocks.MYSTERIOUS_CUCKOO_CLOCK).unlockedBy(AllBlocks.CUCKOO_CLOCK::get)
			.viaShaped(b -> b.define('C', Tags.Items.GUNPOWDER)
				.define('B', AllBlocks.CUCKOO_CLOCK.get())
				.pattern(" C ")
				.pattern("CBC")
				.pattern(" C ")),

		ENCASED_CHAIN_DRIVE = create(AllBlocks.ENCASED_CHAIN_DRIVE).returns(2)
			.unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('S', I.shaft())
				.define('B', Tags.Items.NUGGETS_IRON)
				.define('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		SPEEDOMETER = create(AllBlocks.SPEEDOMETER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('C', Items.COMPASS)
				.define('A', I.andesiteCasing())
				.define('S', I.shaft())
				.pattern(" C ")
				.pattern("SAS")),

		GAUGE_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.SPEEDOMETER, AllBlocks.STRESSOMETER)),

		ROTATION_SPEED_CONTROLLER = create(AllBlocks.ROTATION_SPEED_CONTROLLER).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.precisionMechanism())
				.define('C', I.brassCasing())
				.define('S', I.shaft())
				.pattern(" B ")
				.pattern("SCS")),

		NIXIE_TUBE = create(AllBlocks.ORANGE_NIXIE_TUBE).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('E', I.electronTube())
				.define('B', I.brassCasing())
				.pattern("EBE")),

		MECHANICAL_SAW = create(AllBlocks.MECHANICAL_SAW).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.ironSheet())
				.define('I', I.iron())
				.pattern(" A ")
				.pattern("AIA")
				.pattern(" C ")),

		MECHANICAL_HARVESTER = create(AllBlocks.MECHANICAL_HARVESTER).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.andesite())
				.define('I', I.ironSheet())
				.pattern("AIA")
				.pattern("AIA")
				.pattern(" C ")),

		MECHANICAL_PLOUGH = create(AllBlocks.MECHANICAL_PLOUGH).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.andesite())
				.define('I', I.ironSheet())
				.pattern("III")
				.pattern("AAA")
				.pattern(" C ")),

		MECHANICAL_DRILL = create(AllBlocks.MECHANICAL_DRILL).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.andesite())
				.define('I', I.iron())
				.pattern(" A ")
				.pattern("AIA")
				.pattern(" C ")),

		SEQUENCED_GEARSHIFT = create(AllBlocks.SEQUENCED_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.electronTube())
				.define('S', I.cog())
				.define('C', I.brassCasing())
				.define('I', Items.CLOCK)
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I "))

	;

	private Marker LOGISTICS = enterSection(AllSections.LOGISTICS);

	GeneratedRecipe

	REDSTONE_CONTACT = create(AllBlocks.REDSTONE_CONTACT).returns(2)
		.unlockedBy(I::brassCasing)
		.viaShaped(b -> b.define('W', I.redstone())
			.define('C', Blocks.COBBLESTONE)
			.define('S', I.ironSheet())
			.pattern(" S ")
			.pattern("CWC")
			.pattern("CCC")),

		ANDESITE_FUNNEL = create(AllBlocks.ANDESITE_FUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('K', Items.DRIED_KELP)
				.pattern("AKA")
				.pattern(" K ")),

		BRASS_FUNNEL = create(AllBlocks.BRASS_FUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.define('A', I.brass())
				.define('K', Items.DRIED_KELP)
				.define('E', I.electronTube())
				.pattern("AEA")
				.pattern(" K ")),

		ANDESITE_TUNNEL = create(AllBlocks.ANDESITE_TUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('K', Items.DRIED_KELP)
				.pattern("AA")
				.pattern("KK")),

		BRASS_TUNNEL = create(AllBlocks.BRASS_TUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.define('A', I.brass())
				.define('K', Items.DRIED_KELP)
				.define('E', I.electronTube())
				.pattern("E ")
				.pattern("AA")
				.pattern("KK")),

		ADJUSTABLE_CRATE = create(AllBlocks.ADJUSTABLE_CRATE).returns(4)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.pattern("BBB")
				.pattern("B B")
				.pattern("BBB")),

		BELT_OBSERVER = create(AllBlocks.CONTENT_OBSERVER).unlockedBy(AllItems.BELT_CONNECTOR::get)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.define('R', I.redstone())
				.define('I', I.iron())
				.define('C', Blocks.OBSERVER)
				.pattern("RCI")
				.pattern(" B ")),

		STOCKPILE_SWITCH = create(AllBlocks.STOCKPILE_SWITCH).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.define('R', I.redstone())
				.define('I', I.iron())
				.define('C', Blocks.COMPARATOR)
				.pattern("RCI")
				.pattern(" B ")),

		PULSE_EXTENDER = create(AllBlocks.PULSE_EXTENDER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.define('T', Blocks.REDSTONE_TORCH)
				.define('C', I.brassSheet())
				.define('R', I.redstone())
				.define('S', I.stone())
				.pattern("  T")
				.pattern("RCT")
				.pattern("SSS")),

		PULSE_REPEATER = create(AllBlocks.PULSE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.define('T', Blocks.REDSTONE_TORCH)
				.define('C', I.brassSheet())
				.define('R', I.redstone())
				.define('S', I.stone())
				.pattern("RCT")
				.pattern("SSS")),

		POWERED_TOGGLE_LATCH = create(AllBlocks.POWERED_TOGGLE_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.define('T', Blocks.REDSTONE_TORCH)
				.define('C', Blocks.LEVER)
				.define('S', I.stone())
				.pattern(" T ")
				.pattern(" C ")
				.pattern("SSS")),

		POWERED_LATCH = create(AllBlocks.POWERED_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.define('T', Blocks.REDSTONE_TORCH)
				.define('C', Blocks.LEVER)
				.define('R', I.redstone())
				.define('S', I.stone())
				.pattern(" T ")
				.pattern("RCR")
				.pattern("SSS")),

		REDSTONE_LINK = create(AllBlocks.REDSTONE_LINK).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.define('C', Blocks.REDSTONE_TORCH)
				.define('S', I.brassSheet())
				.define('I', ItemTags.PLANKS)
				.pattern("  C")
				.pattern("SIS"))

	;

	private Marker SCHEMATICS = enterSection(AllSections.SCHEMATICS);

	GeneratedRecipe

	SCHEMATIC_TABLE = create(AllBlocks.SCHEMATIC_TABLE).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
		.viaShaped(b -> b.define('W', ItemTags.WOODEN_SLABS)
			.define('S', Blocks.SMOOTH_STONE)
			.pattern("WWW")
			.pattern(" S ")
			.pattern(" S ")),

		SCHEMATICANNON = create(AllBlocks.SCHEMATICANNON).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
			.viaShaped(b -> b.define('L', ItemTags.LOGS)
				.define('D', Blocks.DISPENSER)
				.define('C', Blocks.CAULDRON)
				.define('S', Blocks.SMOOTH_STONE)
				.define('I', Blocks.IRON_BLOCK)
				.pattern(" C ")
				.pattern("LDL")
				.pattern("SIS")),

		EMPTY_SCHEMATIC = create(AllItems.EMPTY_SCHEMATIC).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(Items.PAPER)
				.requires(Tags.Items.DYES_LIGHT_BLUE)),

		SCHEMATIC_AND_QUILL = create(AllItems.SCHEMATIC_AND_QUILL).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(AllItems.EMPTY_SCHEMATIC.get())
				.requires(Tags.Items.FEATHERS))

	;

	private Marker PALETTES = enterSection(AllSections.PALETTES);

	GeneratedRecipe

	DARK_SCORIA = create(AllPaletteBlocks.DARK_SCORIA).returns(8)
		.unlockedBy(() -> AllPaletteBlocks.SCORIA.get())
		.viaShaped(b -> b.define('#', AllPaletteBlocks.SCORIA.get())
			.define('D', Tags.Items.DYES_BLACK)
			.pattern("###")
			.pattern("#D#")
			.pattern("###")),

		COPPER_SHINGLES = create(AllBlocks.COPPER_SHINGLES).returns(16)
			.unlockedByTag(I::copperSheet)
			.viaShaped(b -> b.define('#', I.copperSheet())
				.pattern("##")
				.pattern("##")),

		COPPER_SHINGLES_FROM_TILES = create(AllBlocks.COPPER_SHINGLES).withSuffix("_from_plating")
			.unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.requires(AllBlocks.COPPER_PLATING.get())),

		COPPER_TILES = create(AllBlocks.COPPER_TILES).unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.requires(AllBlocks.COPPER_SHINGLES.get())),

		COPPER_PLATING = create(AllBlocks.COPPER_PLATING).unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.requires(AllBlocks.COPPER_TILES.get()))

	;

	private Marker APPLIANCES = enterFolder("appliances");

	GeneratedRecipe

	DOUGH = create(AllItems.DOUGH).unlockedBy(AllItems.WHEAT_FLOUR::get)
		.viaShapeless(b -> b.requires(AllItems.WHEAT_FLOUR.get())
			.requires(Items.WATER_BUCKET)),

		DIVING_HELMET = create(AllItems.DIVING_HELMET).unlockedBy(I::copper)
			.viaShaped(b -> b.define('G', Tags.Items.GLASS)
				.define('P', I.copper())
				.pattern("PPP")
				.pattern("PGP")),

		COPPER_BACKTANK = create(AllItems.COPPER_BACKTANK).unlockedBy(I::copper)
			.viaShaped(b -> b.define('G', I.shaft())
				.define('A', I.andesite())
				.define('B', I.copperBlock())
				.define('P', I.copper())
				.pattern("AGA")
				.pattern("PBP")
				.pattern(" P ")),

		DIVING_BOOTS = create(AllItems.DIVING_BOOTS).unlockedBy(I::copper)
			.viaShaped(b -> b.define('G', I.andesite())
				.define('P', I.copper())
				.pattern("P P")
				.pattern("P P")
				.pattern("G G")),

		LINKED_CONTROLLER = create(AllItems.LINKED_CONTROLLER).unlockedBy(AllBlocks.REDSTONE_LINK::get)
			.viaShaped(b -> b.define('S', ItemTags.WOODEN_BUTTONS)
				.define('P', AllBlocks.REDSTONE_LINK.get())
				.pattern("SSS")
				.pattern(" P ")
				.pattern("SSS")),

		CRAFTING_BLUEPRINT = create(AllItems.CRAFTING_BLUEPRINT).unlockedBy(() -> Items.CRAFTING_TABLE)
			.viaShapeless(b -> b.requires(Items.PAINTING)
				.requires(Items.CRAFTING_TABLE)),

		SLIME_BALL = create(() -> Items.SLIME_BALL).unlockedBy(AllItems.DOUGH::get)
			.viaShapeless(b -> b.requires(AllItems.DOUGH.get())
				.requires(Tags.Items.DYES_LIME)),

		TREE_FERTILIZER = create(AllItems.TREE_FERTILIZER).returns(2)
			.unlockedBy(() -> Items.BONE_MEAL)
			.viaShapeless(b -> b.requires(Ingredient.of(ItemTags.SMALL_FLOWERS), 2)
				.requires(Ingredient.of(Items.HORN_CORAL, Items.BRAIN_CORAL, Items.TUBE_CORAL, Items.BUBBLE_CORAL,
					Items.FIRE_CORAL))
				.requires(Items.BONE_MEAL))

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
		NAT_SCORIA = create(AllPaletteBlocks.SCORIA::get).withSuffix("_from_natural")
			.viaCooking(AllPaletteBlocks.NATURAL_SCORIA::get)
			.inFurnace(),

		FRAMED_GLASS = recycleGlass(AllPaletteBlocks.FRAMED_GLASS),
		TILED_GLASS = recycleGlass(AllPaletteBlocks.TILED_GLASS),
		VERTICAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.VERTICAL_FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS),
		FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.FRAMED_GLASS_PANE),
		TILED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.TILED_GLASS_PANE),
		VERTICAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.VERTICAL_FRAMED_GLASS_PANE),
		HORIZONTAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS_PANE),

		ZINC_ORE = blastMetalOre(AllItems.ZINC_INGOT::get, AllTags.forgeItemTag("ores/zinc")),
		CRUSHED_IRON = blastCrushedMetal(() -> Items.IRON_INGOT, AllItems.CRUSHED_IRON::get),
		CRUSHED_GOLD = blastCrushedMetal(() -> Items.GOLD_INGOT, AllItems.CRUSHED_GOLD::get),
		CRUSHED_COPPER = blastCrushedMetal(() -> Items.COPPER_INGOT, AllItems.CRUSHED_COPPER::get),
		CRUSHED_ZINC = blastCrushedMetal(AllItems.ZINC_INGOT::get, AllItems.CRUSHED_ZINC::get),
		CRUSHED_BRASS = blastCrushedMetal(AllItems.BRASS_INGOT::get, AllItems.CRUSHED_BRASS::get),

		CRUSHED_OSMIUM = blastModdedCrushedMetal(AllItems.CRUSHED_OSMIUM, "osmium", MEK),
		CRUSHED_PLATINUM = blastModdedCrushedMetal(AllItems.CRUSHED_PLATINUM, "platinum", SM),
		CRUSHED_SILVER = blastModdedCrushedMetal(AllItems.CRUSHED_SILVER, "silver", MW, TH, IE, SM, INF),
		CRUSHED_TIN = blastModdedCrushedMetal(AllItems.CRUSHED_TIN, "tin", MEK, TH, MW, SM),
		CRUSHED_LEAD = blastModdedCrushedMetal(AllItems.CRUSHED_LEAD, "lead", MEK, MW, TH, IE, SM, EID),
		CRUSHED_QUICKSILVER = blastModdedCrushedMetal(AllItems.CRUSHED_QUICKSILVER, "quicksilver", MW),
		CRUSHED_BAUXITE = blastModdedCrushedMetal(AllItems.CRUSHED_BAUXITE, "aluminum", IE, SM),
		CRUSHED_URANIUM = blastModdedCrushedMetal(AllItems.CRUSHED_URANIUM, "uranium", MEK, IE, SM),
		CRUSHED_NICKEL = blastModdedCrushedMetal(AllItems.CRUSHED_NICKEL, "nickel", TH, IE, SM)

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

	GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(ResourceLocation result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike> result) {
		return create(result::get);
	}

	GeneratedRecipe createSpecial(Supplier<? extends SimpleRecipeSerializer<?>> serializer, String recipeType,
		String path) {
		ResourceLocation location = Create.asResource(recipeType + "/" + currentFolder + "/" + path);
		return register(consumer -> {
			SpecialRecipeBuilder b = SpecialRecipeBuilder.special(serializer.get());
			b.save(consumer, location.toString());
		});
	}

	GeneratedRecipe blastCrushedMetal(Supplier<? extends ItemLike> result, Supplier<? extends ItemLike> ingredient) {
		return create(result::get).withSuffix("_from_crushed")
			.viaCooking(ingredient::get)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe blastModdedCrushedMetal(ItemEntry<? extends Item> ingredient, String metalName, Mods... mods) {
		for (Mods mod : mods) {
			ResourceLocation ingot = mod.ingotOf(metalName);
			String modId = mod.getId();
			create(ingot).withSuffix("_compat_" + modId)
				.whenModLoaded(modId)
				.viaCooking(ingredient::get)
				.rewardXP(.1f)
				.inBlastFurnace();
		}
		return null;
	}

	GeneratedRecipe blastMetalOre(Supplier<? extends ItemLike> result, Tag.Named<Item> ore) {
		return create(result::get).withSuffix("_from_ore")
			.viaCookingTag(() -> ore)
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

	GeneratedRecipe metalCompacting(List<ItemProviderEntry<? extends ItemLike>> variants,
		List<Supplier<Tag<Item>>> ingredients) {
		GeneratedRecipe result = null;
		for (int i = 0; i + 1 < variants.size(); i++) {
			ItemProviderEntry<? extends ItemLike> currentEntry = variants.get(i);
			ItemProviderEntry<? extends ItemLike> nextEntry = variants.get(i + 1);
			Supplier<Tag<Item>> currentIngredient = ingredients.get(i);
			Supplier<Tag<Item>> nextIngredient = ingredients.get(i + 1);

			result = create(nextEntry).withSuffix("_from_compacting")
				.unlockedBy(currentEntry::get)
				.viaShaped(b -> b.pattern("###")
					.pattern("###")
					.pattern("###")
					.define('#', currentIngredient.get()));

			result = create(currentEntry).returns(9)
				.withSuffix("_from_decompacting")
				.unlockedBy(nextEntry::get)
				.viaShapeless(b -> b.requires(nextIngredient.get()));
		}
		return result;
	}

	GeneratedRecipe conversionCycle(List<ItemProviderEntry<? extends ItemLike>> cycle) {
		GeneratedRecipe result = null;
		for (int i = 0; i < cycle.size(); i++) {
			ItemProviderEntry<? extends ItemLike> currentEntry = cycle.get(i);
			ItemProviderEntry<? extends ItemLike> nextEntry = cycle.get((i + 1) % cycle.size());
			result = create(nextEntry).withSuffix("from_conversion")
				.unlockedBy(currentEntry::get)
				.viaShapeless(b -> b.requires(currentEntry.get()));
		}
		return result;
	}

	class GeneratedRecipeBuilder {

		private String path;
		private String suffix;
		private Supplier<? extends ItemLike> result;
		private ResourceLocation compatDatagenOutput;
		List<ICondition> recipeConditions;

		private Supplier<ItemPredicate> unlockedBy;
		private int amount;

		private GeneratedRecipeBuilder(String path) {
			this.path = path;
			this.recipeConditions = new ArrayList<>();
			this.suffix = "";
			this.amount = 1;
		}

		public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
			this(path);
			this.result = result;
		}

		public GeneratedRecipeBuilder(String path, ResourceLocation result) {
			this(path);
			this.compatDatagenOutput = result;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
			this.unlockedBy = () -> ItemPredicate.Builder.item()
				.of(item.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder unlockedByTag(Supplier<Tag<Item>> tag) {
			this.unlockedBy = () -> ItemPredicate.Builder.item()
				.of(tag.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder whenModLoaded(String modid) {
			return withCondition(new ModLoadedCondition(modid));
		}

		GeneratedRecipeBuilder whenModMissing(String modid) {
			return withCondition(new NotCondition(new ModLoadedCondition(modid)));
		}

		GeneratedRecipeBuilder withCondition(ICondition condition) {
			recipeConditions.add(condition);
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
			return register(consumer -> {
				ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
			return register(consumer -> {
				ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		private ResourceLocation createSimpleLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
		}

		private ResourceLocation createLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
		}

		private ResourceLocation getRegistryName() {
			return compatDatagenOutput == null ? result.get()
				.asItem()
				.getRegistryName() : compatDatagenOutput;
		}

		GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
			return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingTag(Supplier<Tag<Item>> tag) {
			return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
			return new GeneratedCookingRecipeBuilder(ingredient);
		}

		class GeneratedCookingRecipeBuilder {

			private Supplier<Ingredient> ingredient;
			private float exp;
			private int cookingTime;

			private final SimpleCookingSerializer<?> FURNACE = RecipeSerializer.SMELTING_RECIPE,
				SMOKER = RecipeSerializer.SMOKING_RECIPE, BLAST = RecipeSerializer.BLASTING_RECIPE,
				CAMPFIRE = RecipeSerializer.CAMPFIRE_COOKING_RECIPE;

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

			GeneratedRecipe inFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				return create(FURNACE, builder, 1);
			}

			GeneratedRecipe inSmoker() {
				return inSmoker(b -> b);
			}

			GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				create(CAMPFIRE, builder, 3);
				return create(SMOKER, builder, .5f);
			}

			GeneratedRecipe inBlastFurnace() {
				return inBlastFurnace(b -> b);
			}

			GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				return create(BLAST, builder, .5f);
			}

			private GeneratedRecipe create(SimpleCookingSerializer<?> serializer,
				UnaryOperator<SimpleCookingRecipeBuilder> builder, float cookingTimeModifier) {
				return register(consumer -> {
					boolean isOtherMod = compatDatagenOutput != null;

					SimpleCookingRecipeBuilder b = builder.apply(
						SimpleCookingRecipeBuilder.cooking(ingredient.get(), isOtherMod ? Items.DIRT : result.get(),
							exp, (int) (cookingTime * cookingTimeModifier), serializer));
					if (unlockedBy != null)
						b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
					b.save(result -> {
						consumer.accept(
							isOtherMod ? new ModdedCookingRecipeResult(result, compatDatagenOutput, recipeConditions)
								: result);
					}, createSimpleLocation(serializer.getRegistryName()
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

	private static class ModdedCookingRecipeResult implements FinishedRecipe {

		private FinishedRecipe wrapped;
		private ResourceLocation outputOverride;
		private List<ICondition> conditions;

		public ModdedCookingRecipeResult(FinishedRecipe wrapped, ResourceLocation outputOverride,
			List<ICondition> conditions) {
			this.wrapped = wrapped;
			this.outputOverride = outputOverride;
			this.conditions = conditions;
		}

		@Override
		public ResourceLocation getId() {
			return wrapped.getId();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return wrapped.getType();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return wrapped.serializeAdvancement();
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return wrapped.getAdvancementId();
		}

		@Override
		public void serializeRecipeData(JsonObject object) {
			wrapped.serializeRecipeData(object);
			object.addProperty("result", outputOverride.toString());

			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			object.add("conditions", conds);
		}

	}

}
