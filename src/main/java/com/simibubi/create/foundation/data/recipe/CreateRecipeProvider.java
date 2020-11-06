package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

public abstract class CreateRecipeProvider extends RecipeProvider {

	final List<GeneratedRecipe> all = new ArrayList<>();

	public CreateRecipeProvider(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
		Create.logger.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
	}

	@FunctionalInterface
	interface GeneratedRecipe {
		void register(Consumer<IFinishedRecipe> consumer);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	protected static class Marker {
	}

	protected static class I {

		static ITag.INamedTag<Item> redstone() {
			return Tags.Items.DUSTS_REDSTONE;
		}
		
		static ITag.INamedTag<Item> planks() {
			return ItemTags.PLANKS;
		}

		static ITag.INamedTag<Item> gold() {
			return AllTags.forgeItemTag("ingots/gold");
		}

		static ITag.INamedTag<Item> goldSheet() {
			return AllTags.forgeItemTag("plates/gold");
		}

		static ITag.INamedTag<Item> stone() {
			return Tags.Items.STONE;
		}

		static IItemProvider andesite() {
			return AllItems.ANDESITE_ALLOY.get();
		}

		static IItemProvider shaft() {
			return AllBlocks.SHAFT.get();
		}

		static IItemProvider cog() {
			return AllBlocks.COGWHEEL.get();
		}

		static IItemProvider andesiteCasing() {
			return AllBlocks.ANDESITE_CASING.get();
		}

		static ITag.INamedTag<Item> brass() {
			return AllTags.forgeItemTag("ingots/brass");
		}

		static ITag.INamedTag<Item> brassSheet() {
			return AllTags.forgeItemTag("plates/brass");
		}

		static ITag.INamedTag<Item> iron() {
			return Tags.Items.INGOTS_IRON;
		}

		static ITag.INamedTag<Item> zinc() {
			return AllTags.forgeItemTag("ingots/zinc");
		}

		static ITag.INamedTag<Item> ironSheet() {
			return AllTags.forgeItemTag("plates/iron");
		}

		static IItemProvider brassCasing() {
			return AllBlocks.BRASS_CASING.get();
		}

		static IItemProvider electronTube() {
			return AllItems.ELECTRON_TUBE.get();
		}

		static IItemProvider circuit() {
			return AllItems.INTEGRATED_CIRCUIT.get();
		}

		static ITag.INamedTag<Item> copperBlock() {
			return AllTags.forgeItemTag("storage_blocks/copper");
		}

		static ITag.INamedTag<Item> brassBlock() {
			return AllTags.forgeItemTag("storage_blocks/brass");
		}

		static ITag.INamedTag<Item> zincBlock() {
			return AllTags.forgeItemTag("storage_blocks/zinc");
		}

		static ITag.INamedTag<Item> copper() {
			return AllTags.forgeItemTag("ingots/copper");
		}

		static ITag.INamedTag<Item> copperSheet() {
			return AllTags.forgeItemTag("plates/copper");
		}

		static ITag.INamedTag<Item> copperNugget() {
			return AllTags.forgeItemTag("nuggets/copper");
		}

		static ITag.INamedTag<Item> brassNugget() {
			return AllTags.forgeItemTag("nuggets/brass");
		}

		static ITag.INamedTag<Item> zincNugget() {
			return AllTags.forgeItemTag("nuggets/zinc");
		}

		static IItemProvider copperCasing() {
			return AllBlocks.COPPER_CASING.get();
		}

		static IItemProvider refinedRadiance() {
			return AllItems.REFINED_RADIANCE.get();
		}

		static IItemProvider shadowSteel() {
			return AllItems.SHADOW_STEEL.get();
		}

	}
}
