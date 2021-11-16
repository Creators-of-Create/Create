//package com.simibubi.create.compat.jei.category;
//
//import java.util.Arrays;
//import java.util.List;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.simibubi.create.AllItems;
//import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
//import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
//import com.simibubi.create.foundation.gui.AllGuiTextures;
//import com.simibubi.create.foundation.gui.element.GuiGameElement;
//
//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
//import mezz.jei.api.ingredients.IIngredients;
//import net.minecraft.core.NonNullList;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//
//public class PolishingCategory extends CreateRecipeCategory<SandPaperPolishingRecipe> {
//
//	private ItemStack renderedSandpaper;
//
//	public PolishingCategory() {
//		super(itemIcon(AllItems.SAND_PAPER.get()), emptyBackground(177, 55));
//		renderedSandpaper = AllItems.SAND_PAPER.asStack();
//	}
//
//	@Override
//	public Class<? extends SandPaperPolishingRecipe> getRecipeClass() {
//		return SandPaperPolishingRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(SandPaperPolishingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, SandPaperPolishingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		List<ProcessingOutput> results = recipe.getRollableResults();
//
//		itemStacks.init(0, true, 26, 28);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems()));
//		itemStacks.init(1, false, 131, 28);
//		itemStacks.set(1, results.get(0)
//			.getStack());
//
//		addStochasticTooltip(itemStacks, results);
//	}
//
//	@Override
//	public void draw(SandPaperPolishingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 28);
//		getRenderedSlot(recipe, 0).render(matrixStack, 131, 28);
//		AllGuiTextures.JEI_SHADOW.render(matrixStack, 61, 21);
//		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52, 32);
//
//		NonNullList<Ingredient> ingredients = recipe.getIngredients();
//		ItemStack[] matchingStacks = ingredients.get(0)
//			.getItems();
//		if (matchingStacks.length == 0)
//			return;
//
//
//		CompoundTag tag = renderedSandpaper.getOrCreateTag();
//		tag.put("Polishing", matchingStacks[0].serializeNBT());
//		tag.putBoolean("JEI", true);
//		GuiGameElement.of(renderedSandpaper)
//				.<GuiGameElement.GuiRenderBuilder>at(getBackground().getWidth() / 2 - 16, 0, 0)
//				.scale(2)
//				.render(matrixStack);
//	}
//
//}
