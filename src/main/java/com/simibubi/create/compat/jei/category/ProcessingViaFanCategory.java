package com.simibubi.create.compat.jei.category;

import java.util.Arrays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

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
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

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

		GlStateManager.pushMatrix();
		GlStateManager.color3f(1, 1, 1);
		GlStateManager.enableDepthTest();

		GlStateManager.translated(28, 18, 0);
		GlStateManager.rotated(10.5, -1f, 0, 0);
		GlStateManager.rotated(15.5, 0, 1, 0);
		GlStateManager.scaled(.6f, .6f, .6f);
		ScreenElementRenderer.renderBlock(this::renderFanCasing);
		
		GlStateManager.pushMatrix();
		float angle = AnimatedKinetics.getCurrentAngle() * 12;
		float t = 25;
		GlStateManager.translatef(t, -t, t);
		GlStateManager.rotated(angle, 0, 0, 1);
		GlStateManager.translatef(-t, t, -t);
		
		GlStateManager.translatef(t, 0, 175);
		GlStateManager.rotated(90, 0, 1, 0);
		GlStateManager.translatef(-t, 0, -175);
		
		ScreenElementRenderer.renderModel(this::renderFanInner);
		GlStateManager.popMatrix();

		GlStateManager.translated(-10, 0, 95);
		GlStateManager.rotated(7, 0, 1, 0);
		renderAttachedBlock();

		GlStateManager.popMatrix();

	}

	protected BlockState renderFanCasing() {
		return AllBlocks.ENCASED_FAN.get().getDefaultState().with(BlockStateProperties.FACING, Direction.WEST);
	}

	protected IBakedModel renderFanInner() {
		return AllBlockPartials.ENCASED_FAN_INNER.get();
	}

	public abstract void renderAttachedBlock();

}
