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

import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class WashingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	WOOL = create("wool", b -> b.require(ItemTags.WOOL)
		.output(Items.WHITE_WOOL)),

		STAINED_GLASS = create("stained_glass", b -> b.require(Tags.Items.STAINED_GLASS)
			.output(Items.GLASS)),
		STAINED_GLASS_PANE = create("stained_glass_pane", b -> b.require(Tags.Items.STAINED_GLASS_PANES)
			.output(Items.GLASS_PANE)),

		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.output(.25f, Items.FLINT)
			.output(.125f, Items.IRON_NUGGET)),
		SOUL_SAND = create(() -> Blocks.SOUL_SAND, b -> b.output(.125f, Items.QUARTZ, 4)
			.output(.02f, Items.GOLD_NUGGET)),
		RED_SAND = create(() -> Blocks.RED_SAND, b -> b.output(.125f, Items.GOLD_NUGGET, 3)
			.output(.05f, Items.DEAD_BUSH)),
		SAND = create(() -> Blocks.SAND, b -> b.output(.25f, Items.CLAY_BALL)),

		CRUSHED_COPPER = crushedOre(AllItems.CRUSHED_COPPER, AllItems.COPPER_NUGGET::get, () -> Items.CLAY_BALL, .5f),
		CRUSHED_ZINC = crushedOre(AllItems.CRUSHED_ZINC, AllItems.ZINC_NUGGET::get, () -> Items.GUNPOWDER, .25f),
		CRUSHED_GOLD = crushedOre(AllItems.CRUSHED_GOLD, () -> Items.GOLD_NUGGET, () -> Items.QUARTZ, .5f),
		CRUSHED_IRON = crushedOre(AllItems.CRUSHED_IRON, () -> Items.IRON_NUGGET, () -> Items.REDSTONE, .75f),

		CRUSHED_OSMIUM = moddedCrushedOre(AllItems.CRUSHED_OSMIUM, OSMIUM),
		CRUSHED_PLATINUM = moddedCrushedOre(AllItems.CRUSHED_PLATINUM, PLATINUM),
		CRUSHED_SILVER = moddedCrushedOre(AllItems.CRUSHED_SILVER, SILVER),
		CRUSHED_TIN = moddedCrushedOre(AllItems.CRUSHED_TIN, TIN),
		CRUSHED_LEAD = moddedCrushedOre(AllItems.CRUSHED_LEAD, LEAD),
		CRUSHED_QUICKSILVER = moddedCrushedOre(AllItems.CRUSHED_QUICKSILVER, QUICKSILVER),
		CRUSHED_BAUXITE = moddedCrushedOre(AllItems.CRUSHED_BAUXITE, ALUMINUM),
		CRUSHED_URANIUM = moddedCrushedOre(AllItems.CRUSHED_URANIUM, URANIUM),
		CRUSHED_NICKEL = moddedCrushedOre(AllItems.CRUSHED_NICKEL, NICKEL),

		ICE = convert(Blocks.ICE, Blocks.PACKED_ICE), MAGMA_BLOCK = convert(Blocks.MAGMA_BLOCK, Blocks.OBSIDIAN),

		WHITE_CONCRETE = convert(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE),
		ORANGE_CONCRETE = convert(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE),
		MAGENTA_CONCRETE = convert(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE),
		LIGHT_BLUE_CONCRETE = convert(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE),
		LIME_CONCRETE = convert(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE),
		YELLOW_CONCRETE = convert(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE),
		PINK_CONCRETE = convert(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE),
		LIGHT_GRAY_CONCRETE = convert(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE),
		GRAY_CONCRETE = convert(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE),
		PURPLE_CONCRETE = convert(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE),
		GREEN_CONCRETE = convert(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE),
		BROWN_CONCRETE = convert(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE),
		RED_CONCRETE = convert(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE),
		BLUE_CONCRETE = convert(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE),
		CYAN_CONCRETE = convert(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE),
		BLACK_CONCRETE = convert(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE),

		FLOUR = create("wheat_flour", b -> b.require(I.wheatFlour())
			.output(AllItems.DOUGH.get())),

		// Atmospheric
		ATMO_SAND = create("atmospheric/arid_sand", b -> b.require(Mods.ATM, "arid_sand")
				.output(.25f, Items.CLAY_BALL, 1)
				.output(0.05f, Mods.ATM, "aloe_kernels", 1)
				.whenModLoaded(Mods.ATM.getId())),

		ATMO_RED_SAND = create("atmospheric/red_arid_sand", b -> b.require(Mods.ATM, "red_arid_sand")
				.output(.125f, Items.CLAY_BALL, 4)
				.output(0.05f, Mods.ATM, "aloe_kernels", 1)
				.whenModLoaded(Mods.ATM.getId())),

		// Oh The Biomes You'll Go

		BYG = create("byg/cryptic_magma_block", b -> b.require(Mods.BYG, "cryptic_magma_block")
				.output(Blocks.OBSIDIAN).whenModLoaded(Mods.BYG.getId())),

		// Endergetic

		ENDER_END = simpleModded(Mods.ENDER, "end_corrock", "petrified_end_corrock"),
		ENDER_END_BLOCK = simpleModded(Mods.ENDER, "end_corrock_block", "petrified_end_corrock_block"),
		ENDER_END_CROWN = simpleModded(Mods.ENDER, "end_corrock_crown", "petrified_end_corrock_crown"),
		ENDER_NETHER = simpleModded(Mods.ENDER, "nether_corrock", "petrified_nether_corrock"),
		ENDER_NETHER_BLOCK = simpleModded(Mods.ENDER, "nether_corrock_block", "petrified_nether_corrock_block"),
		ENDER_NETHER_CROWN = simpleModded(Mods.ENDER, "nether_corrock_crown", "petrified_nether_corrock_crown"),
		ENDER_OVERWORLD = simpleModded(Mods.ENDER, "overworld_corrock", "petrified_overworld_corrock"),
		ENDER_OVERWORLD_BLOCK = simpleModded(Mods.ENDER, "overworld_corrock_block", "petrified_overworld_corrock_block"),
		ENDER_OVERWORLD_CROWN = simpleModded(Mods.ENDER, "overworld_corrock_crown", "petrified_overworld_corrock_crown"),

		// Quark
		Q = simpleModded(Mods.Q, "iron_plate", "rusty_iron_plate"),

		// Supplementaries
		SUP = simpleModded(Mods.SUP, "blackboard", "blackboard"),

		//Vault Hunters
		VH = simpleModded(Mods.VH, "ornate_chain", "ornate_chain_rusty")
	;

	public GeneratedRecipe convert(Block block, Block result) {
		return create(() -> block, b -> b.output(result));
	}

	public GeneratedRecipe crushedOre(ItemEntry<Item> crushed, Supplier<ItemLike> nugget, Supplier<ItemLike> secondary,
		float secondaryChance) {
		return create(crushed::get, b -> b.output(nugget.get(), 9)
			.output(secondaryChance, secondary.get(), 1));
	}

	public GeneratedRecipe moddedCrushedOre(ItemEntry<? extends Item> crushed, CompatMetals metal) {
		String metalName = metal.getName();
		for (Mods mod : metal.getMods()) {
			for (Map.Entry<String, String> entry : mod.nameReplacements.entrySet()) {
				metalName = metalName.replace(entry.getKey(), entry.getValue());
			}

			ResourceLocation nugget = mod.nuggetOf(metalName);
			create(mod.getId() + "/" + crushed.getId().getPath(),
				b -> b.withItemIngredients(Ingredient.of(crushed::get))
					.output(1, nugget, 9)
					.whenModLoaded(mod.getId()));
		}
		return null;
	}

	public GeneratedRecipe simpleModded(Mods mod, String input, String output) {
		return create(mod.getId() + "/" + output, b -> b.require(mod, input)
				.output(mod, output).whenModLoaded(mod.getId()));
	}

	public WashingRecipeGen(DataGenerator dataGenerator) {
		super(dataGenerator);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SPLASHING;
	}

}
