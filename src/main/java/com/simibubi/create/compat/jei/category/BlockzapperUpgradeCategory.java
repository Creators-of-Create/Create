package com.simibubi.create.compat.jei.category;

import static com.simibubi.create.ScreenResources.BLOCKZAPPER_UPGRADE_RECIPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.ScreenResourceWrapper;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperUpgradeRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class BlockzapperUpgradeCategory extends CreateRecipeCategory<BlockzapperUpgradeRecipe> {

	public BlockzapperUpgradeCategory() {
		super("blockzapper_upgrade", itemIcon(AllItems.PLACEMENT_HANDGUN.get()),
				new ScreenResourceWrapper(BLOCKZAPPER_UPGRADE_RECIPE));
	}

	@Override
	public Class<? extends BlockzapperUpgradeRecipe> getRecipeClass() {
		return BlockzapperUpgradeRecipe.class;
	}

	@Override
	public void setIngredients(BlockzapperUpgradeRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlockzapperUpgradeRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		ShapedRecipe shape = recipe.getRecipe();
		NonNullList<Ingredient> shapedIngredients = shape.getIngredients();

		int top = 0;
		int left = 0;

		int i = 0;
		for (int y = 0; y < shape.getRecipeHeight(); y++) {
			for (int x = 0; x < shape.getRecipeWidth(); x++) {
				itemStacks.init(i, true, left + x * 18, top + y * 18);
				itemStacks.set(i, Arrays.asList(shapedIngredients.get(i).getMatchingStacks()));
				i++;
			}
		}
	}

	@Override
	public List<String> getTooltipStrings(BlockzapperUpgradeRecipe recipe, double mouseX, double mouseY) {
		List<String> list = new ArrayList<>();
		if (mouseX < 91 || mouseX > 91 + 52 || mouseY < 1 || mouseY > 53)
			return list;
		list.addAll(recipe.getRecipeOutput()
				.getTooltip(Minecraft.getInstance().player,
						Minecraft.getInstance().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED
								: ITooltipFlag.TooltipFlags.NORMAL)
				.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));
		return list;
	}

	@Override
	public void draw(BlockzapperUpgradeRecipe recipe, double mouseX, double mouseY) {
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		String componentName =
				Lang.translate("blockzapper.component." + Lang.asId(recipe.getUpgradedComponent().name()));
		String text = "+ " + recipe.getTier().color + componentName;
		font.drawStringWithShadow(text, (BLOCKZAPPER_UPGRADE_RECIPE.width - font.getStringWidth(text)) / 2, 57,
				0x8B8B8B);
		
		RenderSystem.pushMatrix();
		RenderSystem.translated(126, 0, 0);
		RenderSystem.scaled(3.5, 3.5, 3.5);
		RenderSystem.translated(-10, 0, 0);
		RenderSystem.color3f(1, 1, 1);
		GuiGameElement.of(recipe.getRecipeOutput()).render();
		RenderSystem.popMatrix();
	}
}