package com.simibubi.create.compat.rei;

//import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
//import com.simibubi.create.content.curiosities.tools.BlueprintContainer;
//import com.simibubi.create.foundation.networking.AllPackets;
//
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.recipe.transfer.IRecipeTransferError;
//import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.crafting.Recipe;
//
//@SuppressWarnings("rawtypes")
//public class BlueprintTransferHandler implements IRecipeTransferHandler<BlueprintContainer, Recipe> {
//
//	@Override
//	public Class<BlueprintContainer> getContainerClass() {
//		return BlueprintContainer.class;
//	}
//
//	@Override
//	public Class<Recipe> getRecipeClass() {
//		return Recipe.class;
//	}
//
//	@Override
//	public IRecipeTransferError transferRecipe(BlueprintContainer container, Recipe recipe,
//		IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
//		if (!(recipe instanceof Recipe))
//			return null;
//		if (!doTransfer)
//			return null;
//		Recipe<?> iRecipe = (Recipe<?>) recipe;
//		// Continued server-side in BlueprintItem.assignCompleteRecipe()
//		AllPackets.channel.sendToServer(new BlueprintAssignCompleteRecipePacket(iRecipe.getId()));
//		return null;
//	}
//
//}
