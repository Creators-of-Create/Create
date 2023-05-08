package com.simibubi.create.compat.jei;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.curiosities.tools.BlueprintMenu;
import com.simibubi.create.foundation.networking.AllPackets;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlueprintTransferHandler implements IRecipeTransferHandler<BlueprintMenu, CraftingRecipe> {

	@Override
	public Class<BlueprintMenu> getContainerClass() {
		return BlueprintMenu.class;
	}

	@Override
	public Optional<MenuType<BlueprintMenu>> getMenuType() {
		return Optional.empty();
	}

	@Override
	public RecipeType<CraftingRecipe> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(BlueprintMenu menu, CraftingRecipe craftingRecipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!doTransfer)
			return null;

		AllPackets.getChannel().sendToServer(new BlueprintAssignCompleteRecipePacket(craftingRecipe.getId()));
		return null;
	}

}
