package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class FilterItem extends Item implements MenuProvider {

	private FilterType type;

	private enum FilterType {
		REGULAR, ATTRIBUTE;
	}

	public static FilterItem regular(Properties properties) {
		return new FilterItem(FilterType.REGULAR, properties);
	}

	public static FilterItem attribute(Properties properties) {
		return new FilterItem(FilterType.ATTRIBUTE, properties);
	}

	private FilterItem(FilterType type, Properties properties) {
		super(properties);
		this.type = type;
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (!AllKeys.shiftDown()) {
			List<Component> makeSummary = makeSummary(stack);
			if (makeSummary.isEmpty())
				return;
			tooltip.add(Components.literal(" "));
			tooltip.addAll(makeSummary);
		}
	}

	private List<Component> makeSummary(ItemStack filter) {
		List<Component> list = new ArrayList<>();
		if (!filter.hasTag())
			return list;

		if (type == FilterType.REGULAR) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrCreateTag()
				.getBoolean("Blacklist");

			list.add((blacklist ? Lang.translateDirect("gui.filter.deny_list")
				: Lang.translateDirect("gui.filter.allow_list")).withStyle(ChatFormatting.GOLD));
			int count = 0;
			for (int i = 0; i < filterItems.getSlots(); i++) {
				if (count > 3) {
					list.add(Components.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}

				ItemStack filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty())
					continue;
				list.add(Components.literal("- ")
					.append(filterStack.getHoverName())
					.withStyle(ChatFormatting.GRAY));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.ATTRIBUTE) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag()
				.getInt("WhitelistMode")];
			list.add((whitelistMode == WhitelistMode.WHITELIST_CONJ
				? Lang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
				: whitelistMode == WhitelistMode.WHITELIST_DISJ
					? Lang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
					: Lang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

			int count = 0;
			ListTag attributes = filter.getOrCreateTag()
				.getList("MatchedAttributes", Tag.TAG_COMPOUND);
			for (Tag inbt : attributes) {
				CompoundTag compound = (CompoundTag) inbt;
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				if (attribute == null)
					continue;
				boolean inverted = compound.getBoolean("Inverted");
				if (count > 3) {
					list.add(Components.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}
				list.add(Components.literal("- ")
					.append(attribute.format(inverted)));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		return list;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer)
				NetworkHooks.openGui((ServerPlayer) player, this, buf -> {
					buf.writeItem(heldItem);
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
		if (!stack.hasTag())
			return newInv;
		CompoundTag invNBT = stack.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static boolean testDirect(ItemStack filter, ItemStack stack, boolean matchNBT) {
		if (matchNBT) {
			return ItemHandlerHelper.canItemStacksStack(filter, stack);
		} else {
			return ItemStack.isSame(filter, stack);
		}
	}

}
