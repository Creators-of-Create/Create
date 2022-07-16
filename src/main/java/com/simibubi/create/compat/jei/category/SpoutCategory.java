package com.simibubi.create.compat.jei.category;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@ParametersAreNonnullByDefault
public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	private final AnimatedSpout spout = new AnimatedSpout();

	public SpoutCategory(Info<FillingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<FillingRecipe> consumer, IIngredientManager ingredientManager) {
		Collection<FluidStack> fluidStacks = ingredientManager.getAllIngredients(ForgeTypes.FLUID_STACK);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (stack.getItem() instanceof PotionItem) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new, Create.asResource("potions"))
					.withItemIngredients(bottle)
					.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
					.withSingleItemOutput(stack)
					.build());
				continue;
			}

			LazyOptional<IFluidHandlerItem> capability =
				stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
			if (!capability.isPresent())
				continue;

			for (FluidStack fluidStack : fluidStacks) {
				ItemStack copy = stack.copy();
				copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
					.ifPresent(fhi -> {
						if (!GenericItemFilling.isFluidHandlerValid(copy, fhi))
							return;
						FluidStack fluidCopy = fluidStack.copy();
						fluidCopy.setAmount(1000);
						fhi.fill(fluidCopy, FluidAction.EXECUTE);
						ItemStack container = fhi.getContainer();
						if (container.sameItem(copy))
							return;
						if (container.isEmpty())
							return;

						Ingredient bucket = Ingredient.of(stack);
						ResourceLocation itemName = stack.getItem()
							.getRegistryName();
						ResourceLocation fluidName = fluidCopy.getFluid()
							.getRegistryName();
						consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new,
							Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
								+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
									.withItemIngredients(bucket)
									.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
									.withSingleItemOutput(container)
									.build());
					});
			}
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FillingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 32)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(recipe.getRequiredFluid().getMatchingFluidStacks()))
				.addTooltipCallback(addFluidTooltip(recipe.getRequiredFluid().getRequiredAmount()));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(recipe.getResultItem());
	}

	@Override
	public void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 126, 29);
		spout.withFluids(recipe.getRequiredFluid()
			.getMatchingFluidStacks())
			.draw(matrixStack, getBackground().getWidth() / 2 - 13, 22);
	}

}
