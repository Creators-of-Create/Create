package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FilterItem extends Item implements MenuProvider, SupportsItemCopying {

	private FilterType type;

	private enum FilterType {
		REGULAR, ATTRIBUTE, PACKAGE;
	}

	public static FilterItem regular(Properties properties) {
		return new FilterItem(FilterType.REGULAR, properties);
	}

	public static FilterItem attribute(Properties properties) {
		return new FilterItem(FilterType.ATTRIBUTE, properties);
	}

	public static FilterItem address(Properties properties) {
		return new FilterItem(FilterType.PACKAGE, properties);
	}

	private FilterItem(FilterType type, Properties properties) {
		super(properties);
		this.type = type;
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (AllKeys.shiftDown())
			return;
		List<Component> makeSummary = makeSummary(stack);
		if (makeSummary.isEmpty())
			return;
		tooltip.add(CommonComponents.SPACE);
		tooltip.addAll(makeSummary);
	}

	private List<Component> makeSummary(ItemStack filter) {
		List<Component> list = new ArrayList<>();
		if (filter.isComponentsPatchEmpty())
			return list;

		if (type == FilterType.REGULAR) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);

			list.add((blacklist ? CreateLang.translateDirect("gui.filter.deny_list")
				: CreateLang.translateDirect("gui.filter.allow_list")).withStyle(ChatFormatting.GOLD));
			int count = 0;
			for (int i = 0; i < filterItems.getSlots(); i++) {
				if (count > 3) {
					list.add(Component.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}

				ItemStack filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty())
					continue;
				list.add(Component.literal("- ")
					.append(filterStack.getHoverName())
					.withStyle(ChatFormatting.GRAY));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.ATTRIBUTE) {
			AttributeFilterWhitelistMode whitelistMode = filter.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
			list.add((whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ
				? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
				: whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ
				? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
				: CreateLang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

			int count = 0;
			List<ItemAttribute.ItemAttributeEntry> attributes = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, new ArrayList<>());
			//noinspection DataFlowIssue
			for (ItemAttribute.ItemAttributeEntry attributeEntry : attributes) {
				ItemAttribute attribute = attributeEntry.attribute();
				if (attribute == null)
					continue;
				boolean inverted = attributeEntry.inverted();
				if (count > 3) {
					list.add(Component.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}
				list.add(Component.literal("- ")
					.append(attribute.format(inverted)));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.PACKAGE) {
			String address = PackageItem.getAddress(filter);
			if (!address.isBlank())
				list.add(CreateLang.text("-> ")
					.style(ChatFormatting.GRAY)
					.add(CreateLang.text(address)
						.style(ChatFormatting.GOLD))
					.component());
		}

		return list;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer)
				player.openMenu(this, buf -> {
					ItemStack.STREAM_CODEC.encode(buf, heldItem);
				});
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		if (type == FilterType.REGULAR)
			return FilterMenu.create(id, inv, heldItem);
		if (type == FilterType.ATTRIBUTE)
			return AttributeFilterMenu.create(id, inv, heldItem);
		if (type == FilterType.PACKAGE)
			return PackageFilterMenu.create(id, inv, heldItem);
		return null;
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}

	public static ItemStackHandler getFilterItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		if (AllItems.FILTER.get() != stack.getItem())
			throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
		if (!stack.has(AllDataComponents.FILTER_ITEMS))
			return newInv;

		//noinspection DataFlowIssue - It's fine:tm: we check if it has the component before doing this
		ItemHelper.fillItemStackHandler(stack.get(AllDataComponents.FILTER_ITEMS), newInv);

		return newInv;
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

	@Override
	public DataComponentType<?> getComponentType() {
		return switch (type) {
		case ATTRIBUTE -> AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES;
		case PACKAGE -> AllDataComponents.PACKAGE_ADDRESS;
		case REGULAR -> AllDataComponents.FILTER_ITEMS;
		};
	}

}
