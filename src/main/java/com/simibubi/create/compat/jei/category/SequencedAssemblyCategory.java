package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.compat.jei.display.SequencedAssemblyDisplay;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class SequencedAssemblyCategory extends CreateRecipeCategory<SequencedAssemblyRecipe, SequencedAssemblyDisplay> {

	Map<ResourceLocation, SequencedAssemblySubCategory> subCategories = new HashMap<>();

	public SequencedAssemblyCategory() {
		super(itemIcon(AllItems.PRECISION_MECHANISM.get()), new EmptyBackground(180, 115));
	}

//	@Override
//	public Class<? extends SequencedAssemblyRecipe> getRecipeClass() {
//		return SequencedAssemblyRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(SequencedAssemblyRecipe recipe, IIngredients ingredients) {
//		List<Ingredient> assemblyIngredients = getAllItemIngredients(recipe);
//		List<FluidIngredient> assemblyFluidIngredients = getAllFluidIngredients(recipe);
//		ingredients.setInputIngredients(assemblyIngredients);
//		if (!assemblyFluidIngredients.isEmpty())
//			ingredients.setInputLists(VanillaTypes.FLUID, assemblyFluidIngredients.stream()
//				.map(FluidIngredient::getMatchingFluidStacks)
//				.collect(Collectors.toList()));
//		ingredients.setOutputs(VanillaTypes.ITEM,
//			ImmutableList.of(recipe.getResultItem(), recipe.getTransitionalItem()));
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, SequencedAssemblyRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
//		int xOffset = recipe.getOutputChance() == 1 ? 0 : -7;
//
//		itemStacks.init(0, true, 26 + xOffset, 90);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredient()
//			.getItems()));
//
//		ItemStack result = recipe.getResultItem();
//		itemStacks.init(1, false, 131 + xOffset, 90);
//		itemStacks.set(1, result);
//
//		int width = 0;
//		int margin = 3;
//		for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence())
//			width += getSubCategory(sequencedRecipe).getWidth() + margin;
//		width -= margin;
//		int x = width / -2 + getBackground().getWidth() / 2;
//		int index = 2;
//		int fluidIndex = 0;
//		for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence()) {
//			SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
//			index += subCategory.addItemIngredients(sequencedRecipe, itemStacks, x, index);
//			fluidIndex += subCategory.addFluidIngredients(sequencedRecipe, fluidStacks, x, fluidIndex);
//			x += subCategory.getWidth() + margin;
//		}

		// In case machines should be displayed as ingredients

//		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
//		int catalystX = -2;
//		int catalystY = 14;
//		for (; index < inputs.size(); index++) {
//			itemStacks.init(index, true, catalystX, catalystY);
//			itemStacks.set(index, inputs.get(index));
//			catalystY += 19;
//		}

//		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
//			if (slotIndex != 1)
//				return;
//			float chance = recipe.getOutputChance();
//			if (chance != 1)
//				tooltip.add(1, Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
//					.withStyle(ChatFormatting.GOLD));
//		});
//	}

	private SequencedAssemblySubCategory getSubCategory(SequencedRecipe<?> sequencedRecipe) {
		return subCategories.computeIfAbsent(Registry.RECIPE_SERIALIZER.getKey(sequencedRecipe.getRecipe()
			.getSerializer()),
			rl -> sequencedRecipe.getAsAssemblyRecipe()
				.getJEISubCategory()
				.get()
				.get());

	}

	final String[] romans = { "I", "II", "III", "IV", "V", "VI", "-" };

	@Override
	public void draw(SequencedAssemblyRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;

		matrixStack.pushPose();
		matrixStack.translate(0, 15, 0);
		boolean singleOutput = recipe.getOutputChance() == 1;
		int xOffset = singleOutput ? 0 : -7;
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26 + xOffset, 75);
		(singleOutput ? AllGuiTextures.JEI_SLOT : AllGuiTextures.JEI_CHANCE_SLOT).render(matrixStack, 131 + xOffset, 75);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52 + xOffset, 79);
		if (!singleOutput) {
			AllGuiTextures.JEI_CHANCE_SLOT.render(matrixStack, 150 + xOffset, 75);
			Component component = new TextComponent("?").withStyle(ChatFormatting.BOLD);
			font.drawShadow(matrixStack, component, font.width(component) / -2 + 8 + 150 + xOffset, 2 + 78,
				0xefefef);
		}

		if (recipe.getLoops() > 1) {
			matrixStack.pushPose();
			matrixStack.translate(15, 9, 0);
			AllIcons.I_SEQ_REPEAT.render(matrixStack, 50 + xOffset, 75);
			Component repeat = new TextComponent("x" + recipe.getLoops());
			font.draw(matrixStack, repeat, 66 + xOffset, 80, 0x888888);
			matrixStack.popPose();
		}

		matrixStack.popPose();

		int width = 0;
		int margin = 3;
		for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence())
			width += getSubCategory(sequencedRecipe).getWidth() + margin;
		width -= margin;
		matrixStack.translate(width / -2 /*+ getBackground().getWidth() / 2 todo*/, 0, 0);

		matrixStack.pushPose();
		List<SequencedRecipe<?>> sequence = recipe.getSequence();
		for (int i = 0; i < sequence.size(); i++) {
			SequencedRecipe<?> sequencedRecipe = sequence.get(i);
			SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
			int subWidth = subCategory.getWidth();
			TextComponent component = new TextComponent("" + romans[Math.min(i, 6)]);
			font.draw(matrixStack, component, font.width(component) / -2 + subWidth / 2, 2, 0x888888);
			subCategory.draw(sequencedRecipe, matrixStack, mouseX, mouseY, i);
			matrixStack.translate(subWidth + margin, 0, 0);
		}
		matrixStack.popPose();
	}

//	@Override
//	public List<Component> getTooltipStrings(SequencedAssemblyRecipe recipe, double mouseX, double mouseY) {
//		List<Component> tooltip = new ArrayList<Component>();
//
//		TranslatableComponent junk = Lang.translate("recipe.assembly.junk");
//
//		boolean singleOutput = recipe.getOutputChance() == 1;
//		boolean willRepeat = recipe.getLoops() > 1;
//
//		int xOffset = -7;
//		int minX = 150 + xOffset;
//		int maxX = minX + 18;
//		int minY = 90;
//		int maxY = minY + 18;
//		if (!singleOutput && mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY) {
//			float chance = recipe.getOutputChance();
//			tooltip.add(junk);
//			tooltip.add(Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : 100 - (int) (chance * 100))
//				.withStyle(ChatFormatting.GOLD));
//			return tooltip;
//		}
//
//		minX = 55 + xOffset;
//		maxX = minX + 65;
//		minY = 92;
//		maxY = minY + 24;
//		if (willRepeat && mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY) {
//			tooltip.add(Lang.translate("recipe.assembly.repeat", recipe.getLoops()));
//			return tooltip;
//		}
//
//		if (mouseY > 5 && mouseY < 84) {
//			int width = 0;
//			int margin = 3;
//			for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence())
//				width += getSubCategory(sequencedRecipe).getWidth() + margin;
//			width -= margin;
//			xOffset = width / 2 /*+ getBackground().getWidth() / -2*/;
//
//			double relativeX = mouseX + xOffset;
//			List<SequencedRecipe<?>> sequence = recipe.getSequence();
//			for (int i = 0; i < sequence.size(); i++) {
//				SequencedRecipe<?> sequencedRecipe = sequence.get(i);
//				SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
//				if (relativeX >= 0 && relativeX < subCategory.getWidth()) {
//					tooltip.add(Lang.translate("recipe.assembly.step", i + 1));
//					tooltip.add(sequencedRecipe.getAsAssemblyRecipe()
//						.getDescriptionForAssembly()
//						.plainCopy()
//						.withStyle(ChatFormatting.DARK_GREEN));
//					return tooltip;
//				}
//				relativeX -= subCategory.getWidth() + margin;
//			}
//		}
//
//		return tooltip;
//	}

	private List<FluidIngredient> getAllFluidIngredients(SequencedAssemblyRecipe recipe) {
		List<FluidIngredient> assemblyFluidIngredients = new ArrayList<>();
		recipe.addAdditionalFluidIngredients(assemblyFluidIngredients);
		return assemblyFluidIngredients;
	}

	private List<Ingredient> getAllItemIngredients(SequencedAssemblyRecipe recipe) {
		List<Ingredient> assemblyIngredients = new ArrayList<>();
		assemblyIngredients.add(recipe.getIngredient());
		assemblyIngredients.add(Ingredient.of(recipe.getTransitionalItem()));
		recipe.addAdditionalIngredientsAndMachines(assemblyIngredients);
		return assemblyIngredients;
	}

}
