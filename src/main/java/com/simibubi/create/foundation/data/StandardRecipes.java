package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

@SuppressWarnings("unused")
public class StandardRecipes extends RecipeProvider {

	final List<GeneratedRecipe> all = new ArrayList<>();

	/*
	 * Recipes are added through fields, so one can navigate to the right one easily
	 * 
	 * (Ctrl-o) in Eclipse
	 */

	private Marker MATERIALS = enterSection(AllSections.MATERIALS);

	GeneratedRecipe ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ).unlockedBy(() -> Items.REDSTONE)
		.viaShapeless(b -> b.addIngredient(Tags.Items.GEMS_QUARTZ)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)
			.addIngredient(Tags.Items.DUSTS_REDSTONE)),

		SAND_PAPER = create(AllItems.SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.SAND_COLORLESS)),

		RED_SAND_PAPER = create(AllItems.RED_SAND_PAPER).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.SAND_RED))

	// TODO
	;

	private Marker CURIOSITIES = enterSection(AllSections.CURIOSITIES);
	// TODO

	private Marker KINETICS = enterSection(AllSections.KINETICS);
	
	GeneratedRecipe BASIN = create(AllBlocks.BASIN).unlockedBy(AllItems.ANDESITE_ALLOY::get)
		.viaShaped(b -> b.key('#', AllItems.ANDESITE_ALLOY.get())
			.patternLine("# #")
			.patternLine("###")),

		BRASS_HAND = create(AllBlocks.SCHEMATIC_TABLE).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
			.viaShaped(b -> b.key('#', AllItems.ANDESITE_ALLOY.get())
				.key('+', AllTags.forgeItemTag("plates/brass"))
				.patternLine(" # ")
				.patternLine("+++")
				.patternLine(" + "))
	// TODO
	;

	private Marker LOGISTICS = enterSection(AllSections.LOGISTICS);
	// TODO

	private Marker SCHEMATICS = enterSection(AllSections.SCHEMATICS);

	GeneratedRecipe SCHEMATIC_TABLE = create(AllBlocks.SCHEMATIC_TABLE).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
		.viaShaped(b -> b.key('#', ItemTags.WOODEN_SLABS)
			.key('+', Blocks.SMOOTH_STONE)
			.patternLine("###")
			.patternLine(" + ")
			.patternLine(" + ")),

		SCHEMATICANNON = create(AllBlocks.SCHEMATICANNON).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
			.viaShaped(b -> b.key('#', ItemTags.LOGS)
				.key('+', Blocks.DISPENSER)
				.key('.', Blocks.CAULDRON)
				.key('_', Blocks.SMOOTH_STONE)
				.key('-', Blocks.IRON_BLOCK)
				.patternLine(" . ")
				.patternLine("#+#")
				.patternLine("_-_")),

		EMPTY_SCHEMATIC = create(AllItems.EMPTY_SCHEMATIC).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(Items.PAPER)
				.addIngredient(Tags.Items.DYES_LIGHT_BLUE)),

		SCHEMATIC_AND_QUILL = create(AllItems.SCHEMATIC_AND_QUILL).unlockedBy(() -> Items.PAPER)
			.viaShapeless(b -> b.addIngredient(AllItems.EMPTY_SCHEMATIC.get())
				.addIngredient(Tags.Items.FEATHERS))

	;
	private Marker APPLIANCES = enterFolder("appliances");

	GeneratedRecipe

	DOUGH = create(AllItems.DOUGH).unlockedBy(AllItems.WHEAT_FLOUR::get)
		.viaShapeless(b -> b.addIngredient(AllItems.WHEAT_FLOUR.get())
			.addIngredient(Items.WATER_BUCKET)),

		SLIME_BALL = create(() -> Items.SLIME_BALL).unlockedBy(AllItems.DOUGH::get)
			.viaShapeless(b -> b.addIngredient(AllItems.DOUGH.get())
				.addIngredient(Tags.Items.DYES_LIME)),

		CAKE = create(() -> Items.CAKE).unlockedBy(AllItems.DOUGH::get)
			.viaShaped(b -> b.key('#', Items.SUGAR)
				.key('+', Tags.Items.EGGS)
				.key('.', Items.MILK_BUCKET)
				.key('-', AllItems.DOUGH.get())
				.patternLine(" . ")
				.patternLine("#+#")
				.patternLine(" - "))

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

	GeneratedRecipeBuilder create(ItemProviderEntry<? extends IItemProvider> result) {
		return create(result::get);
	}

	GeneratedRecipeBuilder create(Supplier<IItemProvider> result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	@FunctionalInterface
	interface GeneratedRecipe {
		void register(Consumer<IFinishedRecipe> consumer);
	}

	class Marker {
	}

	class GeneratedRecipeBuilder {

		private String path;
		private String suffix;
		private Supplier<IItemProvider> result;
		private Supplier<IItemProvider> unlockedBy;
		private int amount;

		public GeneratedRecipeBuilder(String path, Supplier<IItemProvider> result) {
			this.path = path;
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder unlockedBy(Supplier<IItemProvider> item) {
			this.unlockedBy = item;
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
			return register(consumer -> {
				ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shapedRecipe(result.get(), amount));
				if (unlockedBy != null)
					b.addCriterion("has_item", hasItem(unlockedBy.get()));
				b.build(consumer, createLocation("crafting_shaped"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
			return register(consumer -> {
				ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapelessRecipe(result.get(), amount));
				if (unlockedBy != null)
					b.addCriterion("has_item", hasItem(unlockedBy.get()));
				b.build(consumer, createLocation("crafting_shapeless"));
			});
		}

		private GeneratedRecipe register(GeneratedRecipe recipe) {
			all.add(recipe);
			return recipe;
		}

		private ResourceLocation createLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + path + "/" + result.get()
				.asItem()
				.getRegistryName()
				.getPath() + suffix);
		}

	}

	@Override
	public String getName() {
		return "Create's Standard Recipes";
	}

	public StandardRecipes(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
	}

}
