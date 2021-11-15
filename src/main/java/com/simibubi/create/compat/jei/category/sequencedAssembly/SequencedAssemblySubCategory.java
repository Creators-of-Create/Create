package com.simibubi.create.compat.jei.category.sequencedAssembly;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.IAssemblyRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import net.minecraft.ChatFormatting;

public abstract class SequencedAssemblySubCategory {

	private int width;

	public SequencedAssemblySubCategory(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int addItemIngredients(SequencedRecipe<?> recipe, IGuiItemStackGroup itemStacks, int x, int index) {
		return 0;
	}

	public int addFluidIngredients(SequencedRecipe<?> recipe, IGuiFluidStackGroup fluidStacks, int x, int index) {
		return 0;
	}

	public abstract void draw(SequencedRecipe<?> recipe, PoseStack ms, double mouseX, double mouseY, int index);

	public static class AssemblyPressing extends SequencedAssemblySubCategory {

		AnimatedPress press;

		public AssemblyPressing() {
			super(25);
			press = new AnimatedPress(false);
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, PoseStack ms, double mouseX, double mouseY, int index) {
			press.offset = index;
			ms.pushPose();
			ms.translate(-5, 50, 0);
			ms.scale(.6f, .6f, .6f);
			press.draw(ms, getWidth() / 2, 0);
			ms.popPose();
		}

	}

	public static class AssemblySpouting extends SequencedAssemblySubCategory {

		AnimatedSpout spout;

		public AssemblySpouting() {
			super(25);
			spout = new AnimatedSpout();
		}

		@Override
		public int addFluidIngredients(SequencedRecipe<?> recipe, IGuiFluidStackGroup fluidStacks, int x, int index) {
			FluidIngredient fluidIngredient = recipe.getRecipe()
				.getFluidIngredients()
				.get(0);
			fluidStacks.init(index, true, x + 4, 15);
			fluidStacks.set(index,
				CreateRecipeCategory.withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()));
			CreateRecipeCategory.addFluidTooltip(fluidStacks, ImmutableList.of(fluidIngredient),
				Collections.emptyList(), index);
			return 1;
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, PoseStack ms, double mouseX, double mouseY, int index) {
			spout.offset = index;
			AllGuiTextures.JEI_SLOT.render(ms, 3, 14);
			ms.pushPose();
			ms.translate(-7, 50, 0);
			ms.scale(.75f, .75f, .75f);
			spout.withFluids(recipe.getRecipe()
				.getFluidIngredients()
				.get(0)
				.getMatchingFluidStacks())
				.draw(ms, getWidth() / 2, 0);
			ms.popPose();
		}

	}

	public static class AssemblyDeploying extends SequencedAssemblySubCategory {

		AnimatedDeployer deployer;

		public AssemblyDeploying() {
			super(25);
			deployer = new AnimatedDeployer();
		}

		@Override
		public int addItemIngredients(SequencedRecipe<?> recipe, IGuiItemStackGroup itemStacks, int x, int index) {
			itemStacks.init(index, true, x + 3, 14);
			itemStacks.set(index, Arrays.asList(recipe.getRecipe()
				.getIngredients()
				.get(1)
				.getItems()));
			
			IAssemblyRecipe contained = recipe.getAsAssemblyRecipe();
			if (contained instanceof DeployerApplicationRecipe && ((DeployerApplicationRecipe) contained).shouldKeepHeldItem()) {
				itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
					if (!input)
						return;
					if (slotIndex != index)
						return;
					tooltip.add(1, Lang.translate("recipe.deploying.not_consumed")
						.withStyle(ChatFormatting.GOLD));
				});
			}
			
			return 1;
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, PoseStack ms, double mouseX, double mouseY, int index) {
			deployer.offset = index;
			ms.pushPose();
			ms.translate(-7, 50, 0);
			ms.scale(.75f, .75f, .75f);
			deployer.draw(ms, getWidth() / 2, 0);
			ms.popPose();
			AllGuiTextures.JEI_SLOT.render(ms, 3, 14);
		}

	}

	public static class AssemblyCutting extends SequencedAssemblySubCategory {

		AnimatedSaw saw;

		public AssemblyCutting() {
			super(25);
			saw = new AnimatedSaw();
		}

		@Override
		public void draw(SequencedRecipe<?> recipe, PoseStack ms, double mouseX, double mouseY, int index) {
			ms.pushPose();
			ms.translate(0, 51.5f, 0);
			ms.scale(.6f, .6f, .6f);
			saw.draw(ms, getWidth() / 2, 30);
			ms.popPose();
		}

	}

}
