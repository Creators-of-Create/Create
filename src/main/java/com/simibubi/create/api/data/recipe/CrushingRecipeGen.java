package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.data.recipe.CommonMetal;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

/**
 * The base class for Crushing recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * or the helper methods contained in this class to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateCrushingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class CrushingRecipeGen extends StandardProcessingRecipeGen<CrushingRecipe> {
	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type, Supplier<ItemLike> crushed,
											   Supplier<ItemLike> nugget, float chance) {
		return mineralRecycling(type, b -> b.duration(250)
			.output(chance, crushed.get(), 1)
			.output(chance, nugget.get(), 1));
	}

	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type, UnaryOperator<StandardProcessingRecipe.Builder<CrushingRecipe>> transform) {
		create(Lang.asId(type.name()) + "_recycling", b -> transform.apply(b.require(type.materialTag)));
		return create(type.getBaseBlock()::get, transform);
	}

	protected GeneratedRecipe stoneOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
									   int duration) {
		return ore(Blocks.COBBLESTONE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe deepslateOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
										   int duration) {
		return ore(Blocks.COBBLED_DEEPSLATE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe netherOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
										int duration) {
		return ore(Blocks.NETHERRACK, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe ore(ItemLike stoneType, Supplier<ItemLike> ore, Supplier<ItemLike> raw,
								  float expectedAmount, int duration) {
		return create(ore, b -> {
			b.duration(duration)
				.output(raw.get(), Mth.floor(expectedAmount));
			float extra = expectedAmount - Mth.floor(expectedAmount);
			if (extra > 0)
				b.output(extra, raw.get(), 1);
			b.output(.75f, AllItems.EXP_NUGGET.get(), raw.get() == AllItems.CRUSHED_GOLD.get() ? 2 : 1);
			return b.output(.125f, stoneType);
		});
	}

	protected GeneratedRecipe moddedOre(CommonMetal metal, Supplier<ItemLike> result) {
		TagKey<Item> tag = metal.ores.items();
		return create(metal + "_ore", b -> {
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition(tag.location())))
				.require(tag)
				.output(result.get(), 1)
				.output(.75f, result.get(), 1)
				.output(.75f, AllItems.EXP_NUGGET.get());
		});
	}

	protected GeneratedRecipe rawOre(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, int xpMult) {
		return rawOre(metalName, input, result, false, xpMult);
	}

	protected GeneratedRecipe rawOreBlock(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, int xpMult) {
		return rawOre(metalName, input, result, true, xpMult);
	}

	protected GeneratedRecipe rawOre(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, boolean block, int xpMult) {
		return create("raw_" + metalName + (block ? "_block" : ""), b -> {
			int amount = block ? 9 : 1;
			return b.duration(400)
				.require(input.get())
				.output(result.get(), amount)
				.output(.75f, AllItems.EXP_NUGGET.get(), amount * xpMult);
		});
	}

	protected GeneratedRecipe moddedRawOre(CommonMetal metal, Supplier<ItemLike> result) {
		return moddedRawOre(metal, result, false);
	}

	protected GeneratedRecipe moddedRawOreBlock(CommonMetal metal, Supplier<ItemLike> result) {
		return moddedRawOre(metal, result, true);
	}

	protected GeneratedRecipe moddedRawOre(CommonMetal metal, Supplier<ItemLike> result, boolean block) {
		return create("raw_" + metal + (block ? "_block" : ""), b -> {
			int amount = block ? 9 : 1;
			TagKey<Item> material = block ? metal.rawStorageBlocks.items() : metal.rawOres;
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition(material.location())))
				.require(material)
				.output(result.get(), amount)
				.output(.75f, AllItems.EXP_NUGGET.get(), amount);
		});
	}

	public CrushingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CRUSHING;
	}
}
