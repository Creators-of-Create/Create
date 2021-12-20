package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import me.alphamode.forgetags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipesProvider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public abstract class CreateRecipeProvider extends FabricRecipesProvider {

	protected final List<GeneratedRecipe> all = new ArrayList<>();

	public CreateRecipeProvider(FabricDataGenerator generator) {
		super(generator);
	}

	@Override
	protected void generateRecipes(Consumer<FinishedRecipe> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
		Create.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(Consumer<FinishedRecipe> consumer);
	}

	protected static class Marker {
	}

	protected static class I {

		static Tag.Named<Item> redstone() {
			return Tags.Items.DUSTS_REDSTONE;
		}

		static Tag.Named<Item> planks() {
			return ItemTags.PLANKS;
		}

		static Tag.Named<Item> woodSlab() {
			return ItemTags.WOODEN_SLABS;
		}

		static Tag.Named<Item> gold() {
			return AllTags.forgeItemTag("ingots/gold");
		}

		static Tag.Named<Item> goldSheet() {
			return AllTags.forgeItemTag("plates/gold");
		}

		static Tag.Named<Item> stone() {
			return Tags.Items.STONE;
		}

		static ItemLike andesite() {
			return AllItems.ANDESITE_ALLOY.get();
		}

		static ItemLike shaft() {
			return AllBlocks.SHAFT.get();
		}

		static ItemLike cog() {
			return AllBlocks.COGWHEEL.get();
		}

		static ItemLike largeCog() {
			return AllBlocks.LARGE_COGWHEEL.get();
		}

		static ItemLike andesiteCasing() {
			return AllBlocks.ANDESITE_CASING.get();
		}

		static Tag.Named<Item> brass() {
			return AllTags.forgeItemTag("ingots/brass");
		}

		static Tag.Named<Item> brassSheet() {
			return AllTags.forgeItemTag("plates/brass");
		}

		static Tag.Named<Item> iron() {
			return Tags.Items.INGOTS_IRON;
		}

		static Tag.Named<Item> ironNugget() {
			return AllTags.forgeItemTag("nuggets/iron");
		}

		static Tag.Named<Item> zinc() {
			return AllTags.forgeItemTag("ingots/zinc");
		}

		static Tag.Named<Item> ironSheet() {
			return AllTags.forgeItemTag("plates/iron");
		}

		static ItemLike brassCasing() {
			return AllBlocks.BRASS_CASING.get();
		}

		static ItemLike electronTube() {
			return AllItems.ELECTRON_TUBE.get();
		}

		static ItemLike precisionMechanism() {
			return AllItems.PRECISION_MECHANISM.get();
		}

		static ItemLike copperBlock() {
			return Items.COPPER_BLOCK;
		}

		static Tag.Named<Item> brassBlock() {
			return AllTags.forgeItemTag("storage_blocks/brass");
		}

		static Tag.Named<Item> zincBlock() {
			return AllTags.forgeItemTag("storage_blocks/zinc");
		}

		static ItemLike copper() {
			return Items.COPPER_INGOT;
		}

		static Tag.Named<Item> copperSheet() {
			return AllTags.forgeItemTag("plates/copper");
		}

		static Tag.Named<Item> copperNugget() {
			return AllTags.forgeItemTag("nuggets/copper");
		}

		static Tag.Named<Item> brassNugget() {
			return AllTags.forgeItemTag("nuggets/brass");
		}

		static Tag.Named<Item> zincNugget() {
			return AllTags.forgeItemTag("nuggets/zinc");
		}

		static ItemLike copperCasing() {
			return AllBlocks.COPPER_CASING.get();
		}

		static ItemLike refinedRadiance() {
			return AllItems.REFINED_RADIANCE.get();
		}

		static ItemLike shadowSteel() {
			return AllItems.SHADOW_STEEL.get();
		}

	}
}
