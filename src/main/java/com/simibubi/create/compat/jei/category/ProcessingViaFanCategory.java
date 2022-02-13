package com.simibubi.create.compat.jei.category;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ProcessingViaFanCategory<T extends Recipe<?>> extends CreateRecipeCategory<T> {

	protected static final int SCALE = 24;

	public ProcessingViaFanCategory(IDrawable icon) {
		this(178, icon);
	}

	public ProcessingViaFanCategory(int width, IDrawable icon) {
		super(icon, emptyBackground(width, 72));
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setHoverName(Lang.translate("recipe." + name + ".fan").withStyle(style -> style.withItalic(false)));
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(0, true, 20, 47);
		itemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

		itemStacks.init(1, false, 140, 47);
		itemStacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
	}

	@Override
	public void draw(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		renderWidgets(matrixStack, recipe, mouseX, mouseY);

		matrixStack.pushPose();
		translateFan(matrixStack);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));

		AnimatedKinetics.defaultBlockElement(AllBlockPartials.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(SCALE)
			.render(matrixStack);

		AnimatedKinetics.defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(SCALE)
			.render(matrixStack);

		renderAttachedBlock(matrixStack);
		matrixStack.popPose();
	}

	protected void renderWidgets(PoseStack matrixStack, T recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 46, 29);
		getBlockShadow().render(matrixStack, 65, 39);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 54, 51);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 20, 47);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 140, 47);
	}

	protected AllGuiTextures getBlockShadow() {
		return AllGuiTextures.JEI_SHADOW;
	}

	protected void translateFan(PoseStack matrixStack) {
		matrixStack.translate(56, 33, 0);
	}

	protected abstract void renderAttachedBlock(PoseStack matrixStack);

	public static abstract class MultiOutput<T extends ProcessingRecipe<?>> extends ProcessingViaFanCategory<T> {

		public MultiOutput(IDrawable icon) {
			super(icon);
		}

		public MultiOutput(int width, IDrawable icon) {
			super(width, icon);
		}

		@Override
		public void setIngredients(T recipe, IIngredients ingredients) {
			ingredients.setInputIngredients(recipe.getIngredients());
			ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
			IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

			List<ProcessingOutput> results = recipe.getRollableResults();
			int xOffsetAmount = 1 - Math.min(3, results.size());

			itemStacks.init(0, true, 5 * xOffsetAmount + 20, 47);
			itemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

			int xOffsetOutput = 9 * xOffsetAmount;
			boolean excessive = results.size() > 9;
			for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
				int xOffset = (outputIndex % 3) * 19 + xOffsetOutput;
				int yOffset = (outputIndex / 3) * -19 + (excessive ? 8 : 0);

				itemStacks.init(outputIndex + 1, false, 140 + xOffset, 47 + yOffset);
				itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
			}

			addStochasticTooltip(itemStacks, results);
		}

		@Override
		protected void renderWidgets(PoseStack matrixStack, T recipe, double mouseX, double mouseY) {
			int size = recipe.getRollableResultsAsItemStacks()
				.size();
			int xOffsetAmount = 1 - Math.min(3, size);

			AllGuiTextures.JEI_SHADOW.render(matrixStack, 46, 29);
			getBlockShadow().render(matrixStack, 65, 39);
			AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 7 * xOffsetAmount + 54, 51);
			AllGuiTextures.JEI_SLOT.render(matrixStack, 5 * xOffsetAmount + 20, 47);

			int xOffsetOutput = 9 * xOffsetAmount;
			boolean excessive = size > 9;
			for (int i = 0; i < size; i++) {
				int xOffset = (i % 3) * 19 + xOffsetOutput;
				int yOffset = (i / 3) * -19 + (excessive ? 8 : 0);
				getRenderedSlot(recipe, i).render(matrixStack, 140 + xOffset, 47 + yOffset);
			}
		}

	}

}
