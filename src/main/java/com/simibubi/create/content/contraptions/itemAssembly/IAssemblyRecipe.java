package com.simibubi.create.content.contraptions.itemAssembly;

import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAssemblyRecipe {

	default boolean supportsAssembly() {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDescriptionForAssembly();

	public void addAssemblyIngredients(List<Ingredient> list);

	default void addAssemblyFluidIngredients(List<FluidIngredient> list) {}
	
	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory();

}
