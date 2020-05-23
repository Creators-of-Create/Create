package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class FilterItem extends Item implements INamedContainerProvider {

	public FilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand()).getType();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!AllKeys.shiftDown()) {
			List<String> makeSummary = makeSummary(stack);
			if (makeSummary.isEmpty())
				return;
			ItemDescription.add(tooltip, " ");
			ItemDescription.add(tooltip, makeSummary);
		}
	}
	
	private List<String> makeSummary(ItemStack filter) {
		List<String> list = new ArrayList<>();

		if (AllItems.FILTER.typeOf(filter)) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrCreateTag().getBoolean("Blacklist");

			list.add(TextFormatting.GOLD
					+ (blacklist ? Lang.translate("gui.filter.blacklist") : Lang.translate("gui.filter.whitelist")));
			int count = 0;
			for (int i = 0; i < filterItems.getSlots(); i++) {
				if (count > 3) {
					list.add(TextFormatting.DARK_GRAY + "- ...");
					break;
				}

				ItemStack filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty())
					continue;
				list.add(TextFormatting.GRAY + "- " + filterStack.getDisplayName().getFormattedText());
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (AllItems.PROPERTY_FILTER.typeOf(filter)) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag().getInt("WhitelistMode")];
			list.add(TextFormatting.GOLD + (whitelistMode == WhitelistMode.WHITELIST_CONJ
					? Lang.translate("gui.attribute_filter.whitelist_conjunctive")
					: whitelistMode == WhitelistMode.WHITELIST_DISJ
							? Lang.translate("gui.attribute_filter.whitelist_disjunctive")
							: Lang.translate("gui.attribute_filter.blacklist")));

			int count = 0;
			ListNBT attributes = filter.getOrCreateTag().getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (INBT inbt : attributes) {
				ItemAttribute attribute = ItemAttribute.fromNBT((CompoundNBT) inbt);
				if (count > 3) {
					list.add(TextFormatting.DARK_GRAY + "- ...");
					break;
				}
				list.add(TextFormatting.GRAY + "- " + attribute.format());
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		return list;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
			if (!world.isRemote && player instanceof ServerPlayerEntity)
				NetworkHooks.openGui((ServerPlayerEntity) player, this, buf -> {
					buf.writeItemStack(heldItem);
				});
			return ActionResult.success(heldItem);
		}
		return ActionResult.pass(heldItem);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (AllItems.FILTER.typeOf(heldItem))
			return new FilterContainer(id, inv, heldItem);
		if (AllItems.PROPERTY_FILTER.typeOf(heldItem))
			return new AttributeFilterContainer(id, inv, heldItem);
		return null;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getTranslationKey());
	}

	public static ItemStackHandler getFilterItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		if (!AllItems.FILTER.typeOf(stack))
			throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
		CompoundNBT invNBT = stack.getOrCreateChildTag("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static boolean test(World world, ItemStack stack, ItemStack filter) {
		return test(world, stack, filter, false);
	}

	private static boolean test(World world, ItemStack stack, ItemStack filter, boolean matchNBT) {
		if (filter.isEmpty())
			return true;
		
		if (!(filter.getItem() instanceof FilterItem))
			return (matchNBT ? ItemHandlerHelper.canItemStacksStack(filter, stack)
					: ItemStack.areItemsEqual(filter, stack));

		if (AllItems.FILTER.typeOf(filter)) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean respectNBT = filter.getOrCreateTag().getBoolean("RespectNBT");
			boolean blacklist = filter.getOrCreateTag().getBoolean("Blacklist");
			for (int slot = 0; slot < filterItems.getSlots(); slot++) {
				ItemStack stackInSlot = filterItems.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;
				boolean matches = test(world, stack, stackInSlot, respectNBT);
				if (matches)
					return !blacklist;
			}
			return blacklist;
		}

		if (AllItems.PROPERTY_FILTER.typeOf(filter)) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag().getInt("WhitelistMode")];
			ListNBT attributes = filter.getOrCreateTag().getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (INBT inbt : attributes) {
				ItemAttribute attribute = ItemAttribute.fromNBT((CompoundNBT) inbt);
				boolean matches = attribute.appliesTo(stack, world);

				if (matches) {
					switch (whitelistMode) {
					case BLACKLIST:
						return false;
					case WHITELIST_CONJ:
						continue;
					case WHITELIST_DISJ:
						return true;
					}
				} else {
					switch (whitelistMode) {
					case BLACKLIST:
						continue;
					case WHITELIST_CONJ:
						return false;
					case WHITELIST_DISJ:
						continue;
					}
				}
			}

			switch (whitelistMode) {
			case BLACKLIST:
				return true;
			case WHITELIST_CONJ:
				return true;
			case WHITELIST_DISJ:
				return false;
			}
		}

		return false;
	}

}
