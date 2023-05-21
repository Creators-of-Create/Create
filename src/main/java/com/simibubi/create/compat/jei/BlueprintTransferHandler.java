package com.simibubi.create.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintMenu;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlueprintTransferHandler implements IRecipeTransferHandler<BlueprintMenu, CraftingRecipe> {

	@Override
	public Class<BlueprintMenu> getContainerClass() {
		return BlueprintMenu.class;
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(BlueprintMenu menu, CraftingRecipe craftingRecipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!doTransfer)
			return null;

		AllPackets.getChannel().sendToServer(new BlueprintAssignCompleteRecipePacket(craftingRecipe.getId()));
		return null;
	}

}
