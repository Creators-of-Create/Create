package com.simibubi.create.compat.rei;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.curiosities.tools.BlueprintContainer;
import com.simibubi.create.foundation.networking.AllPackets;

import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.simple.RecipeBookGridMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.world.item.crafting.Recipe;

@SuppressWarnings("rawtypes")
public class BlueprintTransferHandler implements SimpleMenuInfoProvider<BlueprintContainer, DefaultCraftingDisplay> {

	@Override
	public @Nullable MenuInfo<BlueprintContainer, DefaultCraftingDisplay> create(DefaultCraftingDisplay display) {
		return new RecipeBookGridMenuInfo(display);
	}

	@Override
	public Optional<MenuInfo<BlueprintContainer, DefaultCraftingDisplay>> provideClient(DefaultCraftingDisplay display, MenuSerializationContext<BlueprintContainer, ?, DefaultCraftingDisplay> context, BlueprintContainer menu) {
		Recipe<?> iRecipe = (Recipe<?>) display.getOptionalRecipe().get();
		// Continued server-side in BlueprintItem.assignCompleteRecipe()
		AllPackets.channel.sendToServer(new BlueprintAssignCompleteRecipePacket(iRecipe.getId()));
		return SimpleMenuInfoProvider.super.provideClient(display, context, menu);
	}
}
