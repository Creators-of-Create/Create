package com.simibubi.create.content.contraptions.processing.fan;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.RECIPE_WRAPPER;
import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public abstract class AbstractRecipeFanType<T extends ProcessingRecipe<RecipeWrapper>> extends AbstractFanProcessingType {

	@FunctionalInterface
	public interface FanRecipeFinder<T extends ProcessingRecipe<RecipeWrapper>> {

		Optional<T> find(RecipeWrapper wrapper, Level world);

	}

	private final FanRecipeFinder<T> finder;

	public AbstractRecipeFanType(int priority, ResourceLocation name, FanRecipeFinder<T> finder) {
		super(priority, name);
		this.finder = finder;
	}

	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<T> recipe = finder.find(RECIPE_WRAPPER, world);
		if (recipe.isPresent())
			return applyRecipeOn(stack, recipe.get());
		return null;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		return InWorldProcessing.isWashable(stack, level);
	}

}
