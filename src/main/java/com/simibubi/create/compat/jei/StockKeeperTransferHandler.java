package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.ItemStackHandler;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StockKeeperTransferHandler implements IUniversalRecipeTransferHandler<StockKeeperRequestMenu> {
	private IJeiHelpers helpers;

	public StockKeeperTransferHandler(IJeiHelpers helpers) {
		this.helpers = helpers;
	}

	@Override
	public Class<? extends StockKeeperRequestMenu> getContainerClass() {
		return StockKeeperRequestMenu.class;
	}

	@Override
	public Optional<MenuType<StockKeeperRequestMenu>> getMenuType() {
		return Optional.of(AllMenuTypes.STOCK_KEEPER_REQUEST.get());
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(StockKeeperRequestMenu container, Object object,
														 IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		Level level = player.level();
		if (!(object instanceof RecipeHolder<?> recipe))
			return null;
		MutableObject<IRecipeTransferError> result = new MutableObject<>();
		if (level.isClientSide())
			//noinspection unchecked
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> result
				.setValue(transferRecipeOnClient(container, (RecipeHolder<Recipe<?>>) recipe, recipeSlots, player, maxTransfer, doTransfer)));
		return result.getValue();
	}

	private @Nullable IRecipeTransferError transferRecipeOnClient(StockKeeperRequestMenu container, RecipeHolder<Recipe<?>> recipeHolder,
																  IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!(container.screenReference instanceof StockKeeperRequestScreen screen))
			return RecipeTransferErrorInternal.INSTANCE;

		Recipe<?> recipe = recipeHolder.value();

		if (recipe.getIngredients().size() > 9)
			return RecipeTransferErrorInternal.INSTANCE;

		for (CraftableBigItemStack cbis : screen.recipesToOrder)
			if (cbis.recipe == recipe)
				return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.already_ordering_recipe")
					.component());

		if (screen.itemsToOrder.size() >= 9)
			return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
				.component());

		InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
		if (summary == null)
			return RecipeTransferErrorInternal.INSTANCE;

		Container outputDummy = new ItemHandlerContainer(new ItemStackHandler(9));
		List<Slot> craftingSlots = new ArrayList<>();
		for (int i = 0; i < outputDummy.getContainerSize(); i++)
			craftingSlots.add(new Slot(outputDummy, i, 0, 0));

		List<BigItemStack> stacksByCount = summary.getStacksByCount();
		Container inputDummy = new ItemHandlerContainer(new ItemStackHandler(stacksByCount.size()));
		Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
		for (int j = 0; j < stacksByCount.size(); j++) {
			BigItemStack bigItemStack = stacksByCount.get(j);
			availableItemStacks.put(new Slot(inputDummy, j, 0, 0),
				bigItemStack.stack.copyWithCount(bigItemStack.count));
		}

		RecipeTransferOperationsResult transferOperations =
			RecipeTransferUtil.getRecipeTransferOperations(helpers.getStackHelper(), availableItemStacks,
				recipeSlots.getSlotViews(RecipeIngredientRole.INPUT), craftingSlots);

		if (!transferOperations.missingItems.isEmpty())
			return new RecipeTransferErrorMissingSlots(CreateLang.translate("gui.stock_keeper.not_in_stock")
				.component(), transferOperations.missingItems);

		if (!doTransfer)
			return null;

		ItemStack result = recipe.getResultItem(player.level().registryAccess());
		if (result.isEmpty())
			return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.recipe_result_empty").component());

		CraftableBigItemStack cbis = new CraftableBigItemStack(result, recipe);

		screen.recipesToOrder.add(cbis);
		screen.searchBox.setValue("");
		screen.refreshSearchNextTick = true;
		screen.requestCraftable(cbis, maxTransfer ? cbis.stack.getMaxStackSize() : 1);

		return null;
	}
}
