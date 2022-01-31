package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.animations.AnimatedItemDrain;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.simibubi.create.lib.mixin.common.accessor.BucketItemAccessor;
import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;

import com.simibubi.create.lib.util.LazyOptional;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ItemDrainCategory extends CreateRecipeCategory<EmptyingRecipe> {

	AnimatedItemDrain drain;

	public ItemDrainCategory() {
		super(doubleItemIcon(AllBlocks.ITEM_DRAIN, () -> Items.WATER_BUCKET), emptyBackground(177, 57));
		drain = new AnimatedItemDrain();
	}

	public static List<EmptyingRecipe> getRecipes() {
		List<EmptyingRecipe> recipes = new ArrayList<>();

		EntryRegistry.getInstance().getEntryStacks()
				.filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM))
				.<EntryStack<ItemStack>>map(EntryStack::cast)
				.collect(Collectors.toList())
			.stream()
			.forEach(entryStack -> {
				ItemStack stack = entryStack.getValue();
				if (stack.getItem() instanceof PotionItem) {
					FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
					Ingredient potion = Ingredient.of(stack);
					recipes.add(new ProcessingRecipeBuilder<>(EmptyingRecipe::new, Create.asResource("potions"))
						.withItemIngredients(potion)
						.withFluidOutputs(fluidFromPotionItem)
						.withSingleItemOutput(new ItemStack(Items.GLASS_BOTTLE))
						.build());
					return;
				}

				LazyOptional<IFluidHandlerItem> capability =
					TransferUtil.getFluidHandlerItem(stack);
				if (!capability.isPresent())
					return;

				ItemStack copy = stack.copy();
				capability = TransferUtil.getFluidHandlerItem(copy);
				IFluidHandlerItem handler = capability.orElse(null);
				FluidStack extracted = handler.drain(FluidConstants.BUCKET, false);
				ItemStack result = handler.getContainer();
				if (extracted.isEmpty())
					return;
				if (result.isEmpty())
					return;

				Ingredient ingredient = Ingredient.of(stack);
				ResourceLocation itemName = Registry.ITEM.getKey(stack.getItem());
				ResourceLocation fluidName = Registry.FLUID.getKey(extracted.getFluid());

				recipes.add(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
					Create.asResource("empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_of_"
						+ fluidName.getNamespace() + "_" + fluidName.getPath())).withItemIngredients(ingredient)
							.withFluidOutputs(extracted)
							.withSingleItemOutput(result)
							.build());
			});

		return recipes;
	}

	@Override
	public void addWidgets(CreateDisplay<EmptyingRecipe> display, List<Widget> ingredients, Point origin) {
		FluidStack fluidOutput = display.getRecipe().getResultingFluid();
		List<ItemStack> matchingIngredients = Arrays.asList(display.getRecipe().getIngredients()
				.get(0)
				.getItems());

		ingredients.add(basicSlot(point(origin.x + 132, origin.y + 8))
				.markOutput()
				.entries(EntryIngredient.of(createFluidEntryStack(fluidOutput))));
		ingredients.add(basicSlot(point(origin.x + 27, origin.y + 8))
				.markOutput()
				.entries(EntryIngredients.ofItemStacks(matchingIngredients)));
		ingredients.add(basicSlot(point(origin.x + 132, origin.y + 27))
				.markInput()
				.entries(display.getOutputEntries().get(0)));

//		addFluidTooltip(fluidStacks, Collections.emptyList(), ImmutableList.of(fluidOutput));
	}

	@Override
	public void draw(EmptyingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 131, 7);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 7);
		getRenderedSlot(recipe, 0).render(matrixStack, 131, 26);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 37);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 73, 4);
		drain.withFluid(recipe.getResultingFluid())
			.draw(matrixStack, getDisplayWidth(null) / 2 - 13, 40);
	}

}
