package com.simibubi.create.api.data.recipe;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.Create;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * The base class for Mechanical Crafting recipe generation.
 * Addons should extend this and use the {@link #create(Supplier)} method to
 * make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class MechanicalCraftingRecipeGen extends BaseRecipeProvider {

	public MechanicalCraftingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	protected GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
		return new GeneratedRecipeBuilder(result);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
		Create.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
	}

	protected class GeneratedRecipeBuilder {

		private String suffix;
		private Supplier<ItemLike> result;
		private int amount;

		public GeneratedRecipeBuilder(Supplier<ItemLike> result) {
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		public GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		public GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		public GeneratedRecipe recipe(UnaryOperator<MechanicalCraftingRecipeBuilder> builder) {
			return register(consumer -> {
				MechanicalCraftingRecipeBuilder b =
					builder.apply(MechanicalCraftingRecipeBuilder.shapedRecipe(result.get(), amount));
				ResourceLocation location = asResource("mechanical_crafting/" + CatnipServices.REGISTRIES.getKeyOrThrow(result.get()
								.asItem())
					.getPath() + suffix);
				b.build(consumer, location);
			});
		}
	}

	@Override
	public String getName() {
		return modid + "'s mechanical crafting recipes";
	}

}
