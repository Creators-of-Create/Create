package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class CrushingRecipe extends AbstractCrushingRecipe {

	public CrushingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.CRUSHING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 7;
	}

}
