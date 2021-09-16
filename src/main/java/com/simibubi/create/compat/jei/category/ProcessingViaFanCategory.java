package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import com.mojang.math.Vector3f;

public abstract class ProcessingViaFanCategory<T extends Recipe<?>> extends CreateRecipeCategory<T> {

	public ProcessingViaFanCategory(IDrawable icon) {
		this(177, icon);
	}

	protected ProcessingViaFanCategory(int width, IDrawable icon) {
		super(icon, emptyBackground(width, 71));
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setHoverName(Lang.translate("recipe." + name + ".fan").withStyle(style -> style.withItalic(false)));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, @Nullable IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 20, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
			.get(0)
			.getItems()));

		itemStacks.init(1, false, 139, 47);
		itemStacks.set(1, recipe.getResultItem());
	}

	protected void renderWidgets(PoseStack matrixStack, T recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 20, 47);
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 139, 47);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 47, 29);
		AllGuiTextures.JEI_LIGHT.draw(matrixStack, 66, 39);
		AllGuiTextures.JEI_LONG_ARROW.draw(matrixStack, 53, 51);
	}

	@Override
	public void draw(@Nullable T recipe, @Nullable PoseStack matrixStack, double mouseX, double mouseY) {
		if (matrixStack == null)
			return;
		renderWidgets(matrixStack, recipe, mouseX, mouseY);

		matrixStack.pushPose();
		translateFan(matrixStack);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 24;

		AnimatedKinetics.defaultBlockElement(AllBlockPartials.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(scale)
			.render(matrixStack);

		AnimatedKinetics.defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		renderAttachedBlock(matrixStack);
		matrixStack.popPose();
	}

	protected void translateFan(PoseStack matrixStack) {
		matrixStack.translate(56, 33, 0);
	}

	public abstract void renderAttachedBlock(PoseStack matrixStack);

}
