package com.simibubi.create.foundation.data.recipe;

import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class HauntingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	BRASS_BELL = convert(AllBlocks.HAUNTED_BELL::get, () -> Ingredient.of(AllBlocks.PECULIAR_BELL.get())),
		SOUL_SAND = convert(() -> Blocks.SOUL_SAND, () -> Ingredient.of(ItemTags.SAND)),
		SOUL_DIRT = convert(() -> Blocks.SOUL_SOIL, () -> Ingredient.of(ItemTags.DIRT)),
		BLACK_STONE = convert(() -> Blocks.BLACKSTONE, () -> Ingredient.of(Tags.Items.COBBLESTONE)),
		CRIMSON_FUNGUS = convert(Items.CRIMSON_FUNGUS, Items.RED_MUSHROOM),
		WARPED_FUNGUS = convert(Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM),
		CRIMSON_NYLIUM = convert(Blocks.CRIMSON_NYLIUM, Blocks.MYCELIUM),
		WARPED_NYLIUM = convert(Blocks.WARPED_NYLIUM, Blocks.PODZOL);

	public GeneratedRecipe convert(ItemLike result, ItemLike input) {
		return convert(() -> result, () -> Ingredient.of(input));
	}

	public GeneratedRecipe convert(Supplier<ItemLike> result, Supplier<Ingredient> input) {
		ProcessingRecipeSerializer<HauntingRecipe> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> new ProcessingRecipeBuilder<>(serializer.getFactory(),
			new ResourceLocation(Create.ID, result.get()
				.asItem()
				.getRegistryName()
				.getPath())).withItemIngredients(input.get())
					.output(result.get())
					.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	public HauntingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.HAUNTING;
	}

}
