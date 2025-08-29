package com.simibubi.create.compat.jei;

import java.util.Optional;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;

public record StockKeeperGuiContainerHandler(IIngredientManager ingredientManager)
	implements IGuiContainerHandler<StockKeeperRequestScreen> {

	@Override
	public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(StockKeeperRequestScreen containerScreen, double mouseX, double mouseY) {
		return containerScreen.getHoveredIngredient((int) mouseX, (int) mouseY)
			.flatMap(pair -> ingredientManager.createClickableIngredient(pair.getFirst(), pair.getSecond(), true));
	}
}
