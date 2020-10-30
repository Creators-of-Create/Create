package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;

public abstract class ProcessingViaFanCategory<T extends IRecipe<?>> extends CreateRecipeCategory<T> {

	public ProcessingViaFanCategory(IDrawable icon) {
		super(icon, emptyBackground(177, 70));
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setDisplayName(Lang.translate("recipe." + name + ".fan").formatted(TextFormatting.RESET));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, @Nullable IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 20, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
			.get(0)
			.getMatchingStacks()));

		itemStacks.init(1, false, 139, 47);
		itemStacks.set(1, recipe.getRecipeOutput());
	}

	protected void renderWidgets(MatrixStack matrixStack, T recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 20, 47);
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 139, 47);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 47, 29);
		AllGuiTextures.JEI_LIGHT.draw(matrixStack, 66, 39);
		AllGuiTextures.JEI_LONG_ARROW.draw(matrixStack, 53, 51);
	}

	@Override
	public void draw(@Nullable T recipe, @Nullable MatrixStack matrixStack, double mouseX, double mouseY) {
		if (matrixStack == null)
			return;
		renderWidgets(matrixStack, recipe, mouseX, mouseY);
		matrixStack.push();
		matrixStack.translate(56, 33, 0);
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-12.5f));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 24;

		GuiGameElement.of(AllBlockPartials.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		renderAttachedBlock(matrixStack);
		matrixStack.pop();
	}

	public abstract void renderAttachedBlock(MatrixStack matrixStack);

}
