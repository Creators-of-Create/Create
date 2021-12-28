package com.simibubi.create.compat.rei.category.sequencedAssembly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.rei.category.animations.AnimatedPress;
import com.simibubi.create.compat.rei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.rei.category.animations.AnimatedSpout;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public abstract class SequencedAssemblySubCategory {

	private int width;

	public SequencedAssemblySubCategory(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int addIngredients(SequencedRecipe<?> recipe, List<Widget> widgets, int x, int index) {
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
		public int addIngredients(SequencedRecipe<?> recipe, List<Widget> widgets, int x, int index) {
			FluidIngredient fluidIngredient = recipe.getRecipe()
				.getFluidIngredients()
				.get(0);
			widgets.add(Widgets.createSlot(new Point(x + 4, 15)).markInput().entries(EntryIngredient.of(CreateRecipeCategory.createFluidEntryStack(CreateRecipeCategory.withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()).get(index)))));
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

//		@Override
//		public int addItemIngredients(SequencedRecipe<?> recipe, IGuiItemStackGroup itemStacks, int x, int index) {
//			itemStacks.init(index, true, x + 3, 14);
//			itemStacks.set(index, Arrays.asList(recipe.getRecipe()
//				.getIngredients()
//				.get(1)
//				.getItems()));
//
//			IAssemblyRecipe contained = recipe.getAsAssemblyRecipe();
//			if (contained instanceof DeployerApplicationRecipe && ((DeployerApplicationRecipe) contained).shouldKeepHeldItem()) {
//				itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
//					if (!input)
//						return;
//					if (slotIndex != index)
//						return;
//					tooltip.add(1, Lang.translate("recipe.deploying.not_consumed")
//						.withStyle(ChatFormatting.GOLD));
//				});
//			}
//
//			return 1;
//		}

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
