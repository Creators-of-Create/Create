package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.CommonMetal.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.LEAD;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.SILVER;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.TIN;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.URANIUM;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.WashingRecipeGen;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Washing recipes
 *
 * @see WashingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateWashingRecipeGen extends WashingRecipeGen {

	GeneratedRecipe

		WOOL = create("wool", b -> b.require(ItemTags.WOOL)
		.output(Items.WHITE_WOOL)),

	STAINED_GLASS = create("stained_glass", b -> b.require(Tags.Items.GLASS_BLOCKS)
		.output(Items.GLASS)),
		STAINED_GLASS_PANE = create("stained_glass_pane", b -> b.require(Tags.Items.GLASS_PANES)
			.output(Items.GLASS_PANE)),

	GRAVEL = create(() -> Blocks.GRAVEL, b -> b.output(.25f, Items.FLINT)
		.output(.125f, Items.IRON_NUGGET)),
		SOUL_SAND = create(() -> Blocks.SOUL_SAND, b -> b.output(.125f, Items.QUARTZ, 4)
			.output(.02f, Items.GOLD_NUGGET)),
		RED_SAND = create(() -> Blocks.RED_SAND, b -> b.output(.125f, Items.GOLD_NUGGET, 3)
			.output(.05f, Items.DEAD_BUSH)),
		SAND = create(() -> Blocks.SAND, b -> b.output(.25f, Items.CLAY_BALL)),

	WEATHERED_IRON_BLOCK =
		create(AllBlocks.INDUSTRIAL_IRON_BLOCK::get, b -> b.output(AllBlocks.WEATHERED_IRON_BLOCK)),
		WEATHERED_IRON_WINDOW =
			create(AllPaletteBlocks.INDUSTRIAL_IRON_WINDOW::get, b -> b.output(AllPaletteBlocks.WEATHERED_IRON_WINDOW)),
		WEATHERED_IRON_WINDOW_PANE = create(AllPaletteBlocks.INDUSTRIAL_IRON_WINDOW_PANE::get,
			b -> b.output(AllPaletteBlocks.WEATHERED_IRON_WINDOW_PANE)),

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
	VH = simpleModded(Mods.VH, "ornate_chain", "ornate_chain_rusty");


	public CreateWashingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}

	public GeneratedRecipe moddedCrushedOre(ItemEntry<? extends Item> crushed, CommonMetal metal) {
		for (Mods mod : metal.mods) {
			String metalName = metal.getName(mod);
			ResourceLocation nugget = mod.nuggetOf(metalName);
			create(mod.getId() + "/" + crushed.getId()
					.getPath(),
				b -> b.withItemIngredients(Ingredient.of(crushed::get))
					.output(1, nugget, 9)
					.whenModLoaded(mod.getId()));
		}
		return null;
	}
}
