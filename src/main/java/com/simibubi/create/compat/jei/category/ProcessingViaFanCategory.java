package com.simibubi.create.compat.jei.category;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@ParametersAreNonnullByDefault
public abstract class ProcessingViaFanCategory<T extends Recipe<?>> extends CreateRecipeCategory<T> {

	protected static final int SCALE = 24;

	public ProcessingViaFanCategory(Info<T> info) {
		super(info);
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setHoverName(Lang.translateDirect("recipe." + name + ".fan").withStyle(style -> style.withItalic(false)));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 21, 48)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 141, 48)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(recipe.getResultItem());
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
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
	}

	protected AllGuiTextures getBlockShadow() {
		return AllGuiTextures.JEI_SHADOW;
	}

	protected void translateFan(PoseStack matrixStack) {
		matrixStack.translate(56, 33, 0);
	}

	protected abstract void renderAttachedBlock(PoseStack matrixStack);

	public static abstract class MultiOutput<T extends ProcessingRecipe<?>> extends ProcessingViaFanCategory<T> {

		public MultiOutput(Info<T> info) {
			super(info);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
			List<ProcessingOutput> results = recipe.getRollableResults();
			int xOffsetAmount = 1 - Math.min(3, results.size());

			builder
					.addSlot(RecipeIngredientRole.INPUT, 5 * xOffsetAmount + 21, 48)
					.setBackground(getRenderedSlot(), -1, -1)
					.addIngredients(recipe.getIngredients().get(0));

			int i = 0;
			boolean excessive = results.size() > 9;
			for (ProcessingOutput output : results) {
				int xOffset = (i % 3) * 19 + 9 * xOffsetAmount;
				int yOffset = (i / 3) * -19 + (excessive ? 8 : 0);

				builder
						.addSlot(RecipeIngredientRole.OUTPUT, 141 + xOffset, 48 + yOffset)
						.setBackground(getRenderedSlot(output), -1, -1)
						.addItemStack(output.getStack())
						.addTooltipCallback(addStochasticTooltip(output));
				i++;
			}
		}

		@Override
		protected void renderWidgets(PoseStack matrixStack, T recipe, double mouseX, double mouseY) {
			int size = recipe.getRollableResultsAsItemStacks().size();
			int xOffsetAmount = 1 - Math.min(3, size);

			AllGuiTextures.JEI_SHADOW.render(matrixStack, 46, 29);
			getBlockShadow().render(matrixStack, 65, 39);
			AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 7 * xOffsetAmount + 54, 51);

		}

	}

}
