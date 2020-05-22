package com.simibubi.create.compat.jei.category;

import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.GuiGameElement;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.crafting.IRecipe;

public abstract class ProcessingViaFanCategory<T extends IRecipe<?>> extends CreateRecipeCategory<T> {

	public ProcessingViaFanCategory(String name, IDrawable icon) {
		super(name, icon, emptyBackground(177, 70));
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 20, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
			.get(0)
			.getMatchingStacks()));

		itemStacks.init(1, false, 139, 47);
		itemStacks.set(1, recipe.getRecipeOutput());
	}

	protected void renderWidgets(T recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(20, 47);
		ScreenResources.JEI_SLOT.draw(139, 47);
		ScreenResources.JEI_SHADOW.draw(47, 29);
		ScreenResources.JEI_LIGHT.draw(66, 39);
		ScreenResources.JEI_LONG_ARROW.draw(53, 51);
	}

	@Override
	public void draw(T recipe, double mouseX, double mouseY) {
		renderWidgets(recipe, mouseX, mouseY);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(56, 33, 0);
		RenderSystem.rotatef(-12.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = 24;

		GuiGameElement.of(AllBlockPartials.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(scale)
			.render();

		GuiGameElement.of(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render();

		renderAttachedBlock();
		RenderSystem.popMatrix();
	}

	public abstract void renderAttachedBlock();

}
