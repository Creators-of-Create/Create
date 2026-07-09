package com.simibubi.create.compat.jei.category.sequencedAssembly;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class SequencedAssemblySubCategory {

	private final int width;

	public SequencedAssemblySubCategory(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {}

	public abstract void draw(SequencedRecipe<?> recipe, GuiGraphicsExtractor graphics, double mouseX, double mouseY, int index);

	public static class AssemblyPressing extends SequencedAssemblySubCategory {

		AnimatedPress press;

		public AssemblyPressing() {
			super(25);
			press = new AnimatedPress(false);
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, GuiGraphicsExtractor graphics, double mouseX, double mouseY, int index) {
			var ms = graphics.pose();
			press.offset = index;
			ms.pushMatrix();
			ms.translate(-5, 50);
			ms.scale(.6f, .6f);
			press.draw(graphics, getWidth() / 2, 0);
			ms.popMatrix();
		}

	}

	public static class AssemblySpouting extends SequencedAssemblySubCategory {

		AnimatedSpout spout;

		public AssemblySpouting() {
			super(25);
			spout = new AnimatedSpout();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
			SizedFluidIngredient fluidIngredient = recipe.getRecipe()
					.getFluidIngredients()
					.get(0);

			CreateRecipeCategory.addFluidSlot(builder, x + 4, 15, fluidIngredient);
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, GuiGraphicsExtractor graphics, double mouseX, double mouseY, int index) {
			var ms = graphics.pose();
			spout.offset = index;
			ms.pushMatrix();
			ms.translate(-7, 50);
			ms.scale(.75f, .75f);
			spout.withFluids(recipe.getRecipe()
					.getFluidIngredients()
					.get(0)
					.ingredient()
					.fluids()
					.stream()
					.map(holder -> new FluidStack(holder.value(), recipe.getRecipe()
						.getFluidIngredients()
						.get(0)
						.amount()))
					.toList())
				.draw(graphics, getWidth() / 2, 0);
			ms.popMatrix();
		}

	}

	public static class AssemblyDeploying extends SequencedAssemblySubCategory {

		AnimatedDeployer deployer;

		public AssemblyDeploying() {
			super(25);
			deployer = new AnimatedDeployer();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
			IRecipeSlotBuilder slot = builder
					.addSlot(RecipeIngredientRole.INPUT, x + 4, 15)
					.setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
					.addIngredients(recipe.getRecipe().getIngredients().get(1));

			if (recipe.getAsAssemblyRecipe() instanceof DeployerApplicationRecipe deployerRecipe && deployerRecipe.shouldKeepHeldItem()) {
				slot.addRichTooltipCallback(
						(recipeSlotView, tooltip) -> tooltip.add(CreateLang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD))
				);
			}
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, GuiGraphicsExtractor graphics, double mouseX, double mouseY, int index) {
			var ms = graphics.pose();
			deployer.offset = index;
			ms.pushMatrix();
			ms.translate(-7, 50);
			ms.scale(.75f, .75f);
			deployer.draw(graphics, getWidth() / 2, 0);
			ms.popMatrix();
		}

	}

	public static class AssemblyCutting extends SequencedAssemblySubCategory {

		AnimatedSaw saw;

		public AssemblyCutting() {
			super(25);
			saw = new AnimatedSaw();
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, GuiGraphicsExtractor graphics, double mouseX, double mouseY, int index) {
			var ms = graphics.pose();
			ms.pushMatrix();
			ms.translate(0, 51.5f);
			ms.scale(.6f, .6f);
			saw.draw(graphics, getWidth() / 2, 30);
			ms.popMatrix();
		}

	}

}
