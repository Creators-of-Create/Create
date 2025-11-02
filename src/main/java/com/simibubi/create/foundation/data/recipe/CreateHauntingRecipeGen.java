package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.HauntingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Haunting recipes
 * @see HauntingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateHauntingRecipeGen extends HauntingRecipeGen {

	GeneratedRecipe

	BRASS_BELL = convert(() -> Ingredient.of(AllBlocks.PECULIAR_BELL.get()), AllBlocks.HAUNTED_BELL::get),

	HAUNT_STONE = convert(Items.STONE, Items.INFESTED_STONE),
	HAUNT_DEEPSLATE = convert(Items.DEEPSLATE, Items.INFESTED_DEEPSLATE),
	HAUNT_STONE_BRICKS = convert(Items.STONE_BRICKS, Items.INFESTED_STONE_BRICKS),
	HAUNT_MOSSY_STONE_BRICKS = convert(Items.MOSSY_STONE_BRICKS, Items.INFESTED_MOSSY_STONE_BRICKS),
	HAUNT_CRACKED_STONE_BRICKS = convert(Items.CRACKED_STONE_BRICKS, Items.INFESTED_CRACKED_STONE_BRICKS),
	HAUNT_CHISELED_STONE_BRICKS = convert(Items.CHISELED_STONE_BRICKS, Items.INFESTED_CHISELED_STONE_BRICKS),

	SOUL_TORCH = convert(Items.TORCH, Items.SOUL_TORCH),
	SOUL_CAMPFIRE = convert(Items.CAMPFIRE, Items.SOUL_CAMPFIRE),
	SOUL_LANTERN = convert(Items.LANTERN, Items.SOUL_LANTERN),

	POISON_POTATO = convert(Items.POTATO, Items.POISONOUS_POTATO),
	GLOW_INK = convert(Items.INK_SAC, Items.GLOW_INK_SAC),
	GLOW_BERRIES = convert(Items.SWEET_BERRIES, Items.GLOW_BERRIES),
	NETHER_BRICK = convert(Items.BRICK, Items.NETHER_BRICK),

	PRISMARINE = create(Create.asResource("lapis_recycling"), b -> b.require(Tags.Items.GEMS_LAPIS)
		.output(.75f, Items.PRISMARINE_SHARD)
		.output(.125f, Items.PRISMARINE_CRYSTALS)),

	SOUL_SAND = convert(() -> Ingredient.of(ItemTags.SAND), () -> Blocks.SOUL_SAND),
	SOUL_DIRT = convert(() -> Ingredient.of(ItemTags.DIRT), () -> Blocks.SOUL_SOIL),
		BLACK_STONE = convert(() -> Ingredient.of(Tags.Items.COBBLESTONES), () -> Blocks.BLACKSTONE),
	CRIMSON_FUNGUS = convert(Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS),
	WARPED_FUNGUS = convert(Items.BROWN_MUSHROOM, Items.WARPED_FUNGUS),

	// Farmer's Delight
	FD = moddedConversion(Mods.FD, "tomato", "rotten_tomato"),

	// Haunted Harvest
	HH = create(Mods.HH.recipeId("rotten_apple"), b -> b.require(Items.APPLE)
		.output(Mods.HH, "rotten_apple")
		.whenModLoaded(Mods.HH.getId()))

	;

	public CreateHauntingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
