package com.simibubi.create.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.curiosities.tools.BlueprintContainer;
import com.simibubi.create.foundation.networking.AllPackets;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlueprintTransferHandler implements IRecipeTransferHandler<BlueprintContainer, CraftingRecipe> {

	@Override
	public Class<BlueprintContainer> getContainerClass() {
		return BlueprintContainer.class;
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(BlueprintContainer container, CraftingRecipe craftingRecipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!doTransfer)
			return null;

		AllPackets.channel.sendToServer(new BlueprintAssignCompleteRecipePacket(craftingRecipe.getId()));
		return null;
	}

}
