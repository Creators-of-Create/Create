package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.CompatMetals.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.LEAD;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.SILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.TIN;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.URANIUM;

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
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
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

	private Marker MATERIALS = enterFolder("materials");

	GeneratedRecipe

	RAW_ZINC = create(AllItems.RAW_ZINC).returns(9)
		.unlockedBy(AllBlocks.RAW_ZINC_BLOCK::get)
		.viaShapeless(b -> b.requires(AllBlocks.RAW_ZINC_BLOCK.get())),

		RAW_ZINC_BLOCK = create(AllBlocks.RAW_ZINC_BLOCK).unlockedBy(AllItems.RAW_ZINC::get)
			.viaShaped(b -> b.define('C', AllItems.RAW_ZINC.get())
				.pattern("CCC")
				.pattern("CCC")
				.pattern("CCC")),

		COPPER_NUGGET = create(AllItems.COPPER_NUGGET).returns(9)
			.unlockedBy(() -> Items.COPPER_INGOT)
			.viaShapeless(b -> b.requires(I.copper())),

		COPPER_INGOT = create(() -> Items.COPPER_INGOT).unlockedBy(AllItems.COPPER_NUGGET::get)
			.viaShaped(b -> b.define('C', I.copperNugget())
				.pattern("CCC")
				.pattern("CCC")
				.pattern("CCC")),

		ANDESITE_ALLOY_FROM_BLOCK = create(AllItems.ANDESITE_ALLOY).withSuffix("_from_block")
			.returns(9)
			.unlockedBy(I::andesite)
			.viaShapeless(b -> b.requires(AllBlocks.ANDESITE_ALLOY_BLOCK.get())),

		ANDESITE_ALLOY_BLOCK = create(AllBlocks.ANDESITE_ALLOY_BLOCK).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('C', I.andesite())
				.pattern("CCC")
				.pattern("CCC")
				.pattern("CCC")),

		EXPERIENCE_FROM_BLOCK = create(AllItems.EXP_NUGGET).withSuffix("_from_block")
			.returns(9)
			.unlockedBy(AllItems.EXP_NUGGET::get)
			.viaShapeless(b -> b.requires(AllBlocks.EXPERIENCE_BLOCK.get())),

		EXPERIENCE_BLOCK = create(AllBlocks.EXPERIENCE_BLOCK).unlockedBy(AllItems.EXP_NUGGET::get)
			.viaShaped(b -> b.define('C', AllItems.EXP_NUGGET.get())
				.pattern("CCC")
				.pattern("CCC")
				.pattern("CCC")),

		BRASS_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.BRASS_NUGGET, AllItems.BRASS_INGOT, AllBlocks.BRASS_BLOCK),
				ImmutableList.of(I::brassNugget, I::brass, I::brassBlock)),

		ZINC_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.ZINC_NUGGET, AllItems.ZINC_INGOT, AllBlocks.ZINC_BLOCK),
				ImmutableList.of(I::zincNugget, I::zinc, I::zincBlock)),

		ROSE_QUARTZ_CYCLE =
			conversionCycle(ImmutableList.of(AllBlocks.ROSE_QUARTZ_TILES, AllBlocks.SMALL_ROSE_QUARTZ_TILES)),

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

		ELECTRON_TUBE = create(AllItems.ELECTRON_TUBE).unlockedBy(AllItems.ROSE_QUARTZ::get)
			.viaShaped(b -> b.define('L', AllItems.POLISHED_ROSE_QUARTZ.get())
				.define('N', I.ironSheet())
				.pattern("L")
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

	private Marker CURIOSITIES = enterFolder("curiosities");

	GeneratedRecipe

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
				.pattern("P")),

		CAKE = create(() -> Items.CAKE).unlockedByTag(() -> AllTags.forgeItemTag("dough"))
			.viaShaped(b -> b.define('E', Tags.Items.EGGS)
				.define('S', Items.SUGAR)
				.define('P', AllTags.forgeItemTag("dough"))
				.define('M', () -> Items.MILK_BUCKET)
				.pattern(" M ")
				.pattern("SES")
				.pattern(" P "))

	;

	private Marker KINETICS = enterFolder("kinetics");

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

		COGWHEEL = create(AllBlocks.COGWHEEL).unlockedBy(I::andesite)
			.viaShapeless(b -> b.requires(I.shaft())
				.requires(I.planks())),

		LARGE_COGWHEEL = create(AllBlocks.LARGE_COGWHEEL).unlockedBy(I::andesite)
			.viaShapeless(b -> b.requires(I.shaft())
				.requires(I.planks())
				.requires(I.planks())),

		LARGE_COGWHEEL_FROM_LITTLE = create(AllBlocks.LARGE_COGWHEEL).withSuffix("_from_little")
			.unlockedBy(I::andesite)
			.viaShapeless(b -> b.requires(I.cog())
				.requires(I.planks())),

		WATER_WHEEL = create(AllBlocks.WATER_WHEEL).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.planks())
				.define('C', I.shaft())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		LARGE_WATER_WHEEL = create(AllBlocks.LARGE_WATER_WHEEL).unlockedBy(AllBlocks.WATER_WHEEL::get)
			.viaShaped(b -> b.define('S', I.planks())
				.define('C', AllBlocks.WATER_WHEEL.get())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		SHAFT = create(AllBlocks.SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.pattern("A")
				.pattern("A")),

		MECHANICAL_PRESS = create(AllBlocks.MECHANICAL_PRESS).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('S', I.shaft())
				.define('I', AllTags.forgeItemTag("storage_blocks/iron"))
				.pattern("S")
				.pattern("C")
				.pattern("I")),

		MILLSTONE = create(AllBlocks.MILLSTONE).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.cog())
				.define('S', I.andesiteCasing())
				.define('I', I.stone())
				.pattern("C")
				.pattern("S")
				.pattern("I")),

		MECHANICAL_PISTON = create(AllBlocks.MECHANICAL_PISTON).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('B', ItemTags.WOODEN_SLABS)
				.define('C', I.andesiteCasing())
				.define('I', AllBlocks.PISTON_EXTENSION_POLE.get())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

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
			.viaShaped(b -> b.define('B', ItemTags.WOODEN_SLABS)
				.define('C', I.andesiteCasing())
				.define('I', I.cog())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		GANTRY_SHAFT = create(AllBlocks.GANTRY_SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.andesite())
				.define('R', I.redstone())
				.pattern("A")
				.pattern("R")
				.pattern("A")),

		PLACARD = create(AllBlocks.PLACARD).returns(1)
			.unlockedByTag(() -> I.brass())
			.viaShapeless(b -> b.requires(Items.ITEM_FRAME)
				.requires(I.brassSheet())),

		TRAIN_DOOR = create(AllBlocks.TRAIN_DOOR).returns(1)
			.unlockedBy(() -> I.railwayCasing())
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_DOORS)
				.requires(I.railwayCasing())),

		ANDESITE_DOOR = create(AllBlocks.ANDESITE_DOOR).returns(1)
			.unlockedBy(() -> I.andesiteCasing())
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_DOORS)
				.requires(I.andesiteCasing())),

		BRASS_DOOR = create(AllBlocks.BRASS_DOOR).returns(1)
			.unlockedBy(() -> I.brassCasing())
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_DOORS)
				.requires(I.brassCasing())),

		COPPER_DOOR = create(AllBlocks.COPPER_DOOR).returns(1)
			.unlockedBy(() -> I.copperCasing())
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_DOORS)
				.requires(I.copperCasing())),

		TRAIN_TRAPDOOR = create(AllBlocks.TRAIN_TRAPDOOR).returns(1)
			.unlockedBy(() -> I.railwayCasing())
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_TRAPDOORS)
				.requires(I.railwayCasing())),

		FRAMED_GLASS_DOOR = create(AllBlocks.FRAMED_GLASS_DOOR).returns(1)
			.unlockedBy(AllPaletteBlocks.FRAMED_GLASS::get)
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_DOORS)
				.requires(AllPaletteBlocks.FRAMED_GLASS.get())),

		FRAMED_GLASS_TRAPDOOR = create(AllBlocks.FRAMED_GLASS_TRAPDOOR).returns(1)
			.unlockedBy(AllPaletteBlocks.FRAMED_GLASS::get)
			.viaShapeless(b -> b.requires(ItemTags.WOODEN_TRAPDOORS)
				.requires(AllPaletteBlocks.FRAMED_GLASS.get())),

		ANALOG_LEVER = create(AllBlocks.ANALOG_LEVER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', I.andesiteCasing())
				.define('P', Tags.Items.RODS_WOODEN)
				.pattern("P")
				.pattern("S")),

		ROSE_QUARTZ_LAMP = create(AllBlocks.ROSE_QUARTZ_LAMP).unlockedByTag(I::zinc)
			.viaShapeless(b -> b.requires(AllItems.POLISHED_ROSE_QUARTZ.get())
				.requires(I.redstone())
				.requires(I.zinc())),

		BELT_CONNECTOR = create(AllItems.BELT_CONNECTOR).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('D', Items.DRIED_KELP)
				.pattern("DDD")
				.pattern("DDD")),

		ADJUSTABLE_PULLEY = create(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT).unlockedBy(I::electronTube)
			.viaShapeless(b -> b.requires(AllBlocks.ENCASED_CHAIN_DRIVE.get())
				.requires(I.electronTube())),

		CART_ASSEMBLER = create(AllBlocks.CART_ASSEMBLER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('L', ItemTags.LOGS)
				.define('R', I.redstone())
				.define('C', I.andesite())
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
			.viaShaped(b -> b.define('A', I.andesiteCasing())
				.define('S', I.shaft())
				.define('P', AllItems.PROPELLER.get())
				.pattern("S")
				.pattern("A")
				.pattern("P")),

		CUCKOO_CLOCK = create(AllBlocks.CUCKOO_CLOCK).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('S', ItemTags.PLANKS)
				.define('A', Items.CLOCK)
				.define('C', I.andesiteCasing())
				.pattern("S")
				.pattern("C")
				.pattern("A")),

		MECHANICAL_CRAFTER = create(AllBlocks.MECHANICAL_CRAFTER).returns(3)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.electronTube())
				.define('R', Blocks.CRAFTING_TABLE)
				.define('C', I.brassCasing())
				.pattern("B")
				.pattern("C")
				.pattern("R")),

		WINDMILL_BEARING = create(AllBlocks.WINDMILL_BEARING).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('B', ItemTags.WOODEN_SLABS)
				.define('C', I.stone())
				.define('I', I.shaft())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		MECHANICAL_BEARING = create(AllBlocks.MECHANICAL_BEARING).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('B', ItemTags.WOODEN_SLABS)
				.define('C', I.andesiteCasing())
				.define('I', I.shaft())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		CLOCKWORK_BEARING = create(AllBlocks.CLOCKWORK_BEARING).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('S', I.electronTube())
				.define('B', I.woodSlab())
				.define('C', I.brassCasing())
				.pattern("B")
				.pattern("C")
				.pattern("S")),

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

		METAL_GIRDER = create(AllBlocks.METAL_GIRDER).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('P', I.ironSheet())
				.define('C', I.andesite())
				.pattern("PPP")
				.pattern("CCC")),

		DISPLAY_BOARD = create(AllBlocks.DISPLAY_BOARD).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('A', I.electronTube())
				.define('P', I.andesite())
				.pattern("PAP")),

		STEAM_WHISTLE = create(AllBlocks.STEAM_WHISTLE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.goldSheet())
				.define('C', I.copper())
				.pattern("P")
				.pattern("C")),

		STEAM_ENGINE = create(AllBlocks.STEAM_ENGINE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.goldSheet())
				.define('C', I.copperBlock())
				.define('A', I.andesite())
				.pattern("P")
				.pattern("A")
				.pattern("C")),

		FLUID_PIPE = create(AllBlocks.FLUID_PIPE).returns(4)
			.unlockedBy(I::copper)
			.viaShaped(b -> b.define('S', I.copperSheet())
				.define('C', I.copper())
				.pattern("SCS")),

		FLUID_PIPE_2 = create(AllBlocks.FLUID_PIPE).withSuffix("_vertical")
			.returns(4)
			.unlockedBy(I::copper)
			.viaShaped(b -> b.define('S', I.copperSheet())
				.define('C', I.copper())
				.pattern("S")
				.pattern("C")
				.pattern("S")),

		MECHANICAL_PUMP = create(AllBlocks.MECHANICAL_PUMP).unlockedBy(I::copper)
			.viaShapeless(b -> b.requires(I.cog())
				.requires(AllBlocks.FLUID_PIPE.get())),

		SMART_FLUID_PIPE = create(AllBlocks.SMART_FLUID_PIPE).unlockedBy(I::copper)
			.viaShaped(b -> b.define('P', I.electronTube())
				.define('S', AllBlocks.FLUID_PIPE.get())
				.define('I', I.brassSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		FLUID_VALVE = create(AllBlocks.FLUID_VALVE).unlockedBy(I::copper)
			.viaShapeless(b -> b.requires(I.ironSheet())
				.requires(AllBlocks.FLUID_PIPE.get())),

		SPOUT = create(AllBlocks.SPOUT).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('T', I.copperCasing())
				.define('P', Items.DRIED_KELP)
				.pattern("T")
				.pattern("P")),

		ITEM_DRAIN = create(AllBlocks.ITEM_DRAIN).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.define('P', Blocks.IRON_BARS)
				.define('S', I.copperCasing())
				.pattern("P")
				.pattern("S")),

		FLUID_TANK = create(AllBlocks.FLUID_TANK).unlockedByTag(() -> Tags.Items.BARRELS_WOODEN)
			.viaShaped(b -> b.define('B', I.copperSheet())
				.define('C', Tags.Items.BARRELS_WOODEN)
				.pattern("B")
				.pattern("C")
				.pattern("B")),

		ITEM_VAULT = create(AllBlocks.ITEM_VAULT).unlockedByTag(() -> Tags.Items.BARRELS_WOODEN)
			.viaShaped(b -> b.define('B', I.ironSheet())
				.define('C', Tags.Items.BARRELS_WOODEN)
				.pattern("B")
				.pattern("C")
				.pattern("B")),

		TRAIN_SIGNAL = create(AllBlocks.TRACK_SIGNAL).unlockedBy(I::railwayCasing)
			.returns(4)
			.viaShapeless(b -> b.requires(I.railwayCasing())
				.requires(I.electronTube())),

		TRAIN_OBSERVER = create(AllBlocks.TRACK_OBSERVER).unlockedBy(I::railwayCasing)
			.returns(2)
			.viaShapeless(b -> b.requires(I.railwayCasing())
				.requires(ItemTags.WOODEN_PRESSURE_PLATES)),

		TRAIN_OBSERVER_2 = create(AllBlocks.TRACK_OBSERVER).withSuffix("_from_other_plates")
			.unlockedBy(I::railwayCasing)
			.returns(2)
			.viaShapeless(b -> b.requires(I.railwayCasing())
				.requires(Ingredient.of(Items.STONE_PRESSURE_PLATE, Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,
					Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE))),

		TRAIN_SCHEDULE = create(AllItems.SCHEDULE).unlockedByTag(I::sturdySheet)
			.returns(4)
			.viaShapeless(b -> b.requires(I.sturdySheet())
				.requires(Items.PAPER)),

		TRAIN_STATION = create(AllBlocks.TRACK_STATION).unlockedBy(I::railwayCasing)
			.returns(2)
			.viaShapeless(b -> b.requires(I.railwayCasing())
				.requires(Items.COMPASS)),

		TRAIN_CONTROLS = create(AllBlocks.TRAIN_CONTROLS).unlockedBy(I::railwayCasing)
			.viaShaped(b -> b.define('I', I.precisionMechanism())
				.define('B', Items.LEVER)
				.define('C', I.railwayCasing())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		DEPLOYER = create(AllBlocks.DEPLOYER).unlockedBy(I::electronTube)
			.viaShaped(b -> b.define('I', AllItems.BRASS_HAND.get())
				.define('B', I.electronTube())
				.define('C', I.andesiteCasing())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		PORTABLE_STORAGE_INTERFACE = create(AllBlocks.PORTABLE_STORAGE_INTERFACE).unlockedBy(I::andesiteCasing)
			.viaShapeless(b -> b.requires(I.andesiteCasing())
				.requires(AllBlocks.CHUTE.get())),

		PORTABLE_FLUID_INTERFACE = create(AllBlocks.PORTABLE_FLUID_INTERFACE).unlockedBy(I::copperCasing)
			.viaShapeless(b -> b.requires(I.copperCasing())
				.requires(AllBlocks.CHUTE.get())),

		ROPE_PULLEY = create(AllBlocks.ROPE_PULLEY).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('B', I.andesiteCasing())
				.define('C', ItemTags.WOOL)
				.define('I', I.ironSheet())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		HOSE_PULLEY = create(AllBlocks.HOSE_PULLEY).unlockedBy(I::copper)
			.viaShaped(b -> b.define('B', I.copperCasing())
				.define('C', Items.DRIED_KELP_BLOCK)
				.define('I', I.copperSheet())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		ELEVATOR_PULLEY = create(AllBlocks.ELEVATOR_PULLEY).unlockedByTag(I::brass)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.define('C', Items.DRIED_KELP_BLOCK)
				.define('I', I.ironSheet())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		CONTRAPTION_CONTROLS = create(AllBlocks.CONTRAPTION_CONTROLS).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('B', ItemTags.BUTTONS)
				.define('C', I.andesiteCasing())
				.define('I', I.electronTube())
				.pattern("B")
				.pattern("C")
				.pattern("I")),

		EMPTY_BLAZE_BURNER = create(AllItems.EMPTY_BLAZE_BURNER).unlockedByTag(I::iron)
			.viaShaped(b -> b.define('A', Tags.Items.NETHERRACK)
				.define('I', I.ironSheet())
				.pattern(" I ")
				.pattern("IAI")
				.pattern(" I ")),

		CHUTE = create(AllBlocks.CHUTE).unlockedBy(I::andesite)
			.returns(4)
			.viaShaped(b -> b.define('A', I.ironSheet())
				.define('I', I.iron())
				.pattern("A")
				.pattern("I")
				.pattern("A")),

		SMART_CHUTE = create(AllBlocks.SMART_CHUTE).unlockedBy(AllBlocks.CHUTE::get)
			.viaShaped(b -> b.define('P', I.electronTube())
				.define('S', AllBlocks.CHUTE.get())
				.define('I', I.brassSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		DEPOT = create(AllBlocks.DEPOT).unlockedBy(I::andesiteCasing)
			.viaShapeless(b -> b.requires(I.andesite())
				.requires(I.andesiteCasing())),

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
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('S', I.cog())
				.define('I', AllItems.WHISK.get())
				.pattern("S")
				.pattern("C")
				.pattern("I")),

		CLUTCH = create(AllBlocks.CLUTCH).unlockedBy(I::andesiteCasing)
			.viaShapeless(b -> b.requires(I.andesiteCasing())
				.requires(I.shaft())
				.requires(I.redstone())),

		GEARSHIFT = create(AllBlocks.GEARSHIFT).unlockedBy(I::andesiteCasing)
			.viaShapeless(b -> b.requires(I.andesiteCasing())
				.requires(I.cog())
				.requires(I.redstone())),

		SAIL = create(AllBlocks.SAIL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.define('W', ItemTags.WOOL)
				.define('S', Tags.Items.RODS_WOODEN)
				.define('A', I.andesite())
				.pattern("WS")
				.pattern("SA")),

		SAIL_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.SAIL_FRAME, AllBlocks.SAIL)),

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

		ENCASED_CHAIN_DRIVE = create(AllBlocks.ENCASED_CHAIN_DRIVE).unlockedBy(I::andesiteCasing)
			.viaShapeless(b -> b.requires(I.andesiteCasing())
				.requires(I.ironNugget())
				.requires(I.ironNugget())
				.requires(I.ironNugget())),

		FLYWHEEL = create(AllBlocks.FLYWHEEL).unlockedByTag(I::brass)
			.viaShaped(b -> b.define('C', I.brass())
				.define('A', I.shaft())
				.pattern("CCC")
				.pattern("CAC")
				.pattern("CCC")),

		SPEEDOMETER = create(AllBlocks.SPEEDOMETER).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('C', Items.COMPASS)
				.define('A', I.andesiteCasing())
				.pattern("C")
				.pattern("A")),

		GAUGE_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.SPEEDOMETER, AllBlocks.STRESSOMETER)),

		ROTATION_SPEED_CONTROLLER = create(AllBlocks.ROTATION_SPEED_CONTROLLER).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.precisionMechanism())
				.define('C', I.brassCasing())
				.pattern("B")
				.pattern("C")),

		NIXIE_TUBE = create(AllBlocks.ORANGE_NIXIE_TUBE).returns(4)
			.unlockedBy(I::brassCasing)
			.viaShapeless(b -> b.requires(I.electronTube())
				.requires(I.electronTube())),

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

		MECHANICAL_ROLLER = create(AllBlocks.MECHANICAL_ROLLER).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.electronTube())
				.define('I', AllBlocks.CRUSHING_WHEEL.get())
				.pattern("A")
				.pattern("C")
				.pattern("I")),

		MECHANICAL_DRILL = create(AllBlocks.MECHANICAL_DRILL).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.define('C', I.andesiteCasing())
				.define('A', I.andesite())
				.define('I', I.iron())
				.pattern(" A ")
				.pattern("AIA")
				.pattern(" C ")),

		SEQUENCED_GEARSHIFT = create(AllBlocks.SEQUENCED_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShapeless(b -> b.requires(I.brassCasing())
				.requires(I.cog())
				.requires(I.electronTube()))

	;

	private Marker LOGISTICS = enterFolder("logistics");

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
				.pattern("A")
				.pattern("K")),

		BRASS_FUNNEL = create(AllBlocks.BRASS_FUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.define('A', I.brass())
				.define('K', Items.DRIED_KELP)
				.define('E', I.electronTube())
				.pattern("E")
				.pattern("A")
				.pattern("K")),

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

		SMART_OBSERVER = create(AllBlocks.SMART_OBSERVER).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.define('R', I.electronTube())
				.define('I', Blocks.OBSERVER)
				.pattern("R")
				.pattern("B")
				.pattern("I")),

		THRESHOLD_SWITCH = create(AllBlocks.THRESHOLD_SWITCH).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('B', I.brassCasing())
				.define('R', I.electronTube())
				.define('I', Blocks.COMPARATOR)
				.pattern("R")
				.pattern("B")
				.pattern("I")),

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
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('C', Blocks.REDSTONE_TORCH)
				.define('S', I.brassCasing())
				.pattern("C")
				.pattern("S")),

		DISPLAY_LINK = create(AllBlocks.DISPLAY_LINK).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.define('C', Blocks.REDSTONE_TORCH)
				.define('A', I.copperSheet())
				.define('S', I.brassCasing())
				.pattern("C")
				.pattern("S")
				.pattern("A"))

	;

	private Marker SCHEMATICS = enterFolder("schematics");

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
				.define('S', Blocks.SMOOTH_STONE)
				.define('I', Blocks.IRON_BLOCK)
				.pattern(" I ")
				.pattern("LIL")
				.pattern("SDS")),

		EMPTY_SCHEMATIC = create(AllItems.EMPTY_SCHEMATIC).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(Items.PAPER)
				.requires(Tags.Items.DYES_LIGHT_BLUE)),

		SCHEMATIC_AND_QUILL = create(AllItems.SCHEMATIC_AND_QUILL).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.requires(AllItems.EMPTY_SCHEMATIC.get())
				.requires(Tags.Items.FEATHERS))

	;

	private Marker PALETTES = enterFolder("palettes");

	GeneratedRecipe

	SCORCHIA = create(AllPaletteStoneTypes.SCORCHIA.getBaseBlock()::get).returns(8)
		.unlockedBy(AllPaletteStoneTypes.SCORIA.getBaseBlock()::get)
		.viaShaped(b -> b.define('#', AllPaletteStoneTypes.SCORIA.getBaseBlock()
			.get())
			.define('D', Tags.Items.DYES_BLACK)
			.pattern("###")
			.pattern("#D#")
			.pattern("###"))

	;

	private Marker APPLIANCES = enterFolder("appliances");

	GeneratedRecipe

	DOUGH = create(AllItems.DOUGH).unlockedByTag(I::wheatFlour)
		.viaShapeless(b -> b.requires(I.wheatFlour())
			.requires(Items.WATER_BUCKET)),

		CLIPBOARD = create(AllBlocks.CLIPBOARD).unlockedBy(I::andesite)
			.viaShaped(b -> b.define('G', I.planks())
				.define('P', Items.PAPER)
				.define('A', I.andesite())
				.pattern("A")
				.pattern("P")
				.pattern("G")),

		CLIPBOARD_CLEAR = clearData(AllBlocks.CLIPBOARD), SCHEDULE_CLEAR = clearData(AllItems.SCHEDULE),
		FILTER_CLEAR = clearData(AllItems.FILTER), ATTRIBUTE_FILTER_CLEAR = clearData(AllItems.ATTRIBUTE_FILTER),

		DIVING_HELMET = create(AllItems.COPPER_DIVING_HELMET).unlockedBy(I::copper)
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

		DIVING_BOOTS = create(AllItems.COPPER_DIVING_BOOTS).unlockedBy(I::copper)
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
				.requires(Items.BONE_MEAL)),

		NETHERITE_DIVING_HELMET =
			create(AllItems.NETHERITE_DIVING_HELMET).viaSmithing(AllItems.COPPER_DIVING_HELMET::get, I::netherite),
		NETHERITE_BACKTANK =
			create(AllItems.NETHERITE_BACKTANK).viaSmithing(AllItems.COPPER_BACKTANK::get, I::netherite),
		NETHERITE_DIVING_BOOTS =
			create(AllItems.NETHERITE_DIVING_BOOTS).viaSmithing(AllItems.COPPER_DIVING_BOOTS::get, I::netherite),

		NETHERITE_DIVING_HELMET_2 = create(AllItems.NETHERITE_DIVING_HELMET).withSuffix("_from_netherite")
			.viaSmithing(() -> Items.NETHERITE_HELMET, () -> Ingredient.of(AllItems.COPPER_DIVING_HELMET.get())),
		NETHERITE_BACKTANK_2 = create(AllItems.NETHERITE_BACKTANK).withSuffix("_from_netherite")
			.viaSmithing(() -> Items.NETHERITE_CHESTPLATE, () -> Ingredient.of(AllItems.COPPER_BACKTANK.get())),
		NETHERITE_DIVING_BOOTS_2 = create(AllItems.NETHERITE_DIVING_BOOTS).withSuffix("_from_netherite")
			.viaSmithing(() -> Items.NETHERITE_BOOTS, () -> Ingredient.of(AllItems.COPPER_DIVING_BOOTS.get()))

	;

	private Marker COOKING = enterFolder("/");

	GeneratedRecipe

	DOUGH_TO_BREAD = create(() -> Items.BREAD).viaCooking(AllItems.DOUGH::get)
		.inSmoker(),

		SOUL_SAND = create(AllPaletteStoneTypes.SCORIA.getBaseBlock()::get).viaCooking(() -> Blocks.SOUL_SAND)
			.inFurnace(),

		FRAMED_GLASS = recycleGlass(AllPaletteBlocks.FRAMED_GLASS),
		TILED_GLASS = recycleGlass(AllPaletteBlocks.TILED_GLASS),
		VERTICAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.VERTICAL_FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS),
		FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.FRAMED_GLASS_PANE),
		TILED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.TILED_GLASS_PANE),
		VERTICAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.VERTICAL_FRAMED_GLASS_PANE),
		HORIZONTAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS_PANE),

		CRUSHED_IRON = blastCrushedMetal(() -> Items.IRON_INGOT, AllItems.CRUSHED_IRON::get),
		CRUSHED_GOLD = blastCrushedMetal(() -> Items.GOLD_INGOT, AllItems.CRUSHED_GOLD::get),
		CRUSHED_COPPER = blastCrushedMetal(() -> Items.COPPER_INGOT, AllItems.CRUSHED_COPPER::get),
		CRUSHED_ZINC = blastCrushedMetal(AllItems.ZINC_INGOT::get, AllItems.CRUSHED_ZINC::get),

		CRUSHED_OSMIUM = blastModdedCrushedMetal(AllItems.CRUSHED_OSMIUM, OSMIUM),
		CRUSHED_PLATINUM = blastModdedCrushedMetal(AllItems.CRUSHED_PLATINUM, PLATINUM),
		CRUSHED_SILVER = blastModdedCrushedMetal(AllItems.CRUSHED_SILVER, SILVER),
		CRUSHED_TIN = blastModdedCrushedMetal(AllItems.CRUSHED_TIN, TIN),
		CRUSHED_LEAD = blastModdedCrushedMetal(AllItems.CRUSHED_LEAD, LEAD),
		CRUSHED_QUICKSILVER = blastModdedCrushedMetal(AllItems.CRUSHED_QUICKSILVER, QUICKSILVER),
		CRUSHED_BAUXITE = blastModdedCrushedMetal(AllItems.CRUSHED_BAUXITE, ALUMINUM),
		CRUSHED_URANIUM = blastModdedCrushedMetal(AllItems.CRUSHED_URANIUM, URANIUM),
		CRUSHED_NICKEL = blastModdedCrushedMetal(AllItems.CRUSHED_NICKEL, NICKEL),

		ZINC_ORE = create(AllItems.ZINC_INGOT::get).withSuffix("_from_ore")
			.viaCookingTag(() -> AllTags.forgeItemTag("ores/zinc"))
			.rewardXP(1)
			.inBlastFurnace(),

		RAW_ZINC_ORE = create(AllItems.ZINC_INGOT::get).withSuffix("_from_raw_ore")
			.viaCooking(AllItems.RAW_ZINC::get)
			.rewardXP(.7f)
			.inBlastFurnace()

	;

	/*
	 * End of recipe list
	 */

	String currentFolder = "";

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

	GeneratedRecipe createSpecial(Supplier<? extends SimpleCraftingRecipeSerializer<?>> serializer, String recipeType,
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

	GeneratedRecipe blastModdedCrushedMetal(ItemEntry<? extends Item> ingredient, CompatMetals metal) {
		String metalName = metal.getName();
		for (Mods mod : metal.getMods()) {
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
		List<Supplier<TagKey<Item>>> ingredients) {
		GeneratedRecipe result = null;
		for (int i = 0; i + 1 < variants.size(); i++) {
			ItemProviderEntry<? extends ItemLike> currentEntry = variants.get(i);
			ItemProviderEntry<? extends ItemLike> nextEntry = variants.get(i + 1);
			Supplier<TagKey<Item>> currentIngredient = ingredients.get(i);
			Supplier<TagKey<Item>> nextIngredient = ingredients.get(i + 1);

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

	GeneratedRecipe clearData(ItemProviderEntry<? extends ItemLike> item) {
		return create(item).withSuffix("_clear")
			.unlockedBy(item::get)
			.viaShapeless(b -> b.requires(item.get()));
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

		GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
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

		// FIXME 5.1 refactor - recipe categories as markers instead of sections?
		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
			return register(consumer -> {
				ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
			return register(consumer -> {
				ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaSmithing(Supplier<? extends Item> base, Supplier<Ingredient> upgradeMaterial) {
			return register(consumer -> {
				UpgradeRecipeBuilder b = UpgradeRecipeBuilder.smithing(Ingredient.of(base.get()), upgradeMaterial.get(),
					RecipeCategory.COMBAT, result.get()
						.asItem());
				b.unlocks("has_item", inventoryTrigger(ItemPredicate.Builder.item()
					.of(base.get())
					.build()));
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
			return compatDatagenOutput == null ? RegisteredObjects.getKeyOrThrow(result.get()
				.asItem()) : compatDatagenOutput;
		}

		GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
			return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingTag(Supplier<TagKey<Item>> tag) {
			return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
			return new GeneratedCookingRecipeBuilder(ingredient);
		}

		class GeneratedCookingRecipeBuilder {

			private Supplier<Ingredient> ingredient;
			private float exp;
			private int cookingTime;

			private final RecipeSerializer<? extends AbstractCookingRecipe> FURNACE = RecipeSerializer.SMELTING_RECIPE,
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

			private GeneratedRecipe create(RecipeSerializer<? extends AbstractCookingRecipe> serializer,
				UnaryOperator<SimpleCookingRecipeBuilder> builder, float cookingTimeModifier) {
				return register(consumer -> {
					boolean isOtherMod = compatDatagenOutput != null;

					SimpleCookingRecipeBuilder b = builder.apply(SimpleCookingRecipeBuilder.generic(ingredient.get(),
						RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(), exp,
						(int) (cookingTime * cookingTimeModifier), serializer));
					
					if (unlockedBy != null)
						b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
					
					b.save(result -> {
						consumer.accept(
							isOtherMod ? new ModdedCookingRecipeResult(result, compatDatagenOutput, recipeConditions)
								: result);
					}, createSimpleLocation(RegisteredObjects.getKeyOrThrow(serializer)
						.getPath()));
				});
			}
		}
	}

	@Override
	public String getName() {
		return "Create's Standard Recipes";
	}

	public StandardRecipeGen(PackOutput p_i48262_1_) {
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
