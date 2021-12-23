package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.SoulSmokingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SoulSmokingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

			SOUL_SAND = convert(Blocks.SOUL_SAND, Blocks.SAND, Blocks.RED_SAND),
			SOUL_DIRT = convert(Blocks.SOUL_SOIL, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT),
			BLACK_STONE = convert(Blocks.BLACKSTONE, Blocks.COBBLESTONE),
			CRIMSON_FUNGUS = convert(Items.CRIMSON_FUNGUS, Items.RED_MUSHROOM),
			WARPED_FUNGUS = convert(Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM),
			CRIMSON_NYLIUM = convert(Blocks.CRIMSON_NYLIUM, Blocks.MYCELIUM),
			WARPED_NYLIUM = convert(Blocks.WARPED_NYLIUM, Blocks.PODZOL);

	public GeneratedRecipe convert(ItemLike result, ItemLike... input) {
		ProcessingRecipeSerializer<SoulSmokingRecipe> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> new ProcessingRecipeBuilder<>(serializer.getFactory(),
				new ResourceLocation(Create.ID, result.asItem().getRegistryName().getPath()))
				.withItemIngredients(Ingredient.of(input)).output(result).build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	public SoulSmokingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SOUL_SMOKING;
	}

}
