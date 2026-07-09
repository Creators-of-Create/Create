package com.simibubi.create.content.logistics.filter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;

public abstract class FilterItem extends Item implements MenuProvider, SupportsItemCopying {
	public static ListFilterItem regular(Properties properties) {
		return new ListFilterItem(properties);
	}

	public static AttributeFilterItem attribute(Properties properties) {
		return new AttributeFilterItem(properties);
	}

	public static PackageFilterItem address(Properties properties) {
		return new PackageFilterItem(properties);
	}

	protected FilterItem(Properties properties) {
		super(properties);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
		Consumer<Component> tooltip, TooltipFlag flagIn) {
		if (AllKeys.shiftDown())
			return;
		List<Component> makeSummary = makeSummary(stack);
		if (makeSummary.isEmpty())
			return;
		tooltip.accept(CommonComponents.SPACE);
		makeSummary.forEach(tooltip);
	}

	public abstract List<Component> makeSummary(ItemStack filter);

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide() && player instanceof ServerPlayer)
				player.openMenu(this, buf -> {
					ItemStack.STREAM_CODEC.encode(buf, heldItem);
				});
			return InteractionResult.SUCCESS.heldItemTransformedTo(heldItem);
		}
		return InteractionResult.PASS;
	}

	@Override
	public abstract AbstractContainerMenu createMenu(int id, Inventory inv, Player player);

	@Override
	public Component getDisplayName() {
		return getName(getDefaultInstance());
	}

	public static boolean testDirect(ItemStack filter, ItemStack stack, boolean matchNBT) {
		if (matchNBT) {
			if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
				return doPackagesHaveSameData(filter, stack);

			return ItemStack.isSameItemSameComponents(filter, stack);
		}

		if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
			return true;

		return ItemHelper.sameItem(filter, stack);
	}

	public static boolean doPackagesHaveSameData(@NotNull ItemStack a, @NotNull ItemStack b) {
		if (a.isEmpty())
			return false;
		if (!ItemStack.isSameItemSameComponents(a, b))
			return false;
		for (TypedDataComponent<?> component : a.getComponents()) {
			DataComponentType<?> type = component.type();
			if (type.equals(AllDataComponents.PACKAGE_ORDER_DATA) ||
				type.equals(AllDataComponents.PACKAGE_ORDER_CONTEXT))
				continue;
			if (!Objects.equals(a.get(type), b.get(type)))
				return false;
		}
		return true;
	}

	public abstract DataComponentType<?> getComponentType();

	public abstract FilterItemStack makeStackWrapper(ItemStack filter);

	public abstract ItemStack[] getFilterItems(ItemStack stack);
}
