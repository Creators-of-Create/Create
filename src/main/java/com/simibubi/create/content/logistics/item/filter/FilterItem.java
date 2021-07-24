package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class FilterItem extends Item implements INamedContainerProvider {

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
	public ActionResultType useOn(ItemUseContext context) {
		if (context.getPlayer() == null)
			return ActionResultType.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!AllKeys.shiftDown()) {
			List<ITextComponent> makeSummary = makeSummary(stack);
			if (makeSummary.isEmpty())
				return;
			ItemDescription.add(tooltip, new StringTextComponent(" "));
			ItemDescription.add(tooltip, makeSummary);
		}
	}

	private List<ITextComponent> makeSummary(ItemStack filter) {
		List<ITextComponent> list = new ArrayList<>();

		if (type == FilterType.REGULAR) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrCreateTag()
				.getBoolean("Blacklist");

			list.add((blacklist ? Lang.translate("gui.filter.deny_list") : Lang.translate("gui.filter.allow_list")).withStyle(TextFormatting.GOLD));
			int count = 0;
			for (int i = 0; i < filterItems.getSlots(); i++) {
				if (count > 3) {
					list.add(new StringTextComponent("- ...").withStyle(TextFormatting.DARK_GRAY));
					break;
				}

				ItemStack filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty())
					continue;
				list.add(new StringTextComponent("- ").append(filterStack.getHoverName()).withStyle(TextFormatting.GRAY));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.ATTRIBUTE) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag()
				.getInt("WhitelistMode")];
			list.add((whitelistMode == WhitelistMode.WHITELIST_CONJ
				? Lang.translate("gui.attribute_filter.allow_list_conjunctive")
				: whitelistMode == WhitelistMode.WHITELIST_DISJ
					? Lang.translate("gui.attribute_filter.allow_list_disjunctive")
					: Lang.translate("gui.attribute_filter.deny_list")).withStyle(TextFormatting.GOLD));

			int count = 0;
			ListNBT attributes = filter.getOrCreateTag()
				.getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (INBT inbt : attributes) {
				CompoundNBT compound = (CompoundNBT) inbt;
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				boolean inverted = compound.getBoolean("Inverted");
				if (count > 3) {
					list.add(new StringTextComponent("- ...").withStyle(TextFormatting.DARK_GRAY));
					break;
				}
				list.add(new StringTextComponent("- ").append(attribute.format(inverted)));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		return list;
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == Hand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayerEntity)
				NetworkHooks.openGui((ServerPlayerEntity) player, this, buf -> {
					buf.writeItem(heldItem);
				});
			return ActionResult.success(heldItem);
		}
		return ActionResult.pass(heldItem);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		ItemStack heldItem = player.getMainHandItem();
		if (type == FilterType.REGULAR)
			return FilterContainer.create(id, inv, heldItem);
		if (type == FilterType.ATTRIBUTE)
			return AttributeFilterContainer.create(id, inv, heldItem);
		return null;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(getDescriptionId());
	}

	public static ItemStackHandler getFilterItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		if (AllItems.FILTER.get() != stack.getItem())
			throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
		CompoundNBT invNBT = stack.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static boolean test(World world, ItemStack stack, ItemStack filter) {
		return test(world, stack, filter, false);
	}

	public static boolean test(World world, FluidStack stack, ItemStack filter) {
		return test(world, stack, filter, true);
	}

	private static boolean test(World world, ItemStack stack, ItemStack filter, boolean matchNBT) {
		if (filter.isEmpty())
			return true;

		if (!(filter.getItem() instanceof FilterItem))
			return (matchNBT ? ItemHandlerHelper.canItemStacksStack(filter, stack)
				: ItemStack.isSame(filter, stack));

		if (AllItems.FILTER.get() == filter.getItem()) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean respectNBT = filter.getOrCreateTag()
				.getBoolean("RespectNBT");
			boolean blacklist = filter.getOrCreateTag()
				.getBoolean("Blacklist");
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

		if (AllItems.ATTRIBUTE_FILTER.get() == filter.getItem()) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag()
				.getInt("WhitelistMode")];
			ListNBT attributes = filter.getOrCreateTag()
				.getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (INBT inbt : attributes) {
				CompoundNBT compound = (CompoundNBT) inbt;
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				boolean matches = attribute.appliesTo(stack, world) != compound.getBoolean("Inverted");

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

	private static boolean test(World world, FluidStack stack, ItemStack filter, boolean matchNBT) {
		if (filter.isEmpty())
			return true;
		if (stack.isEmpty())
			return false;

		if (!(filter.getItem() instanceof FilterItem)) {
			if (!EmptyingByBasin.canItemBeEmptied(world, filter))
				return false;
			FluidStack fluidInFilter = EmptyingByBasin.emptyItem(world, filter, true)
				.getFirst();
			if (fluidInFilter == null)
				return false;
			if (!matchNBT)
				return fluidInFilter.getFluid()
					.isSame(stack.getFluid());
			boolean fluidEqual = fluidInFilter.isFluidEqual(stack);
			return fluidEqual;
		}

		if (AllItems.FILTER.get() == filter.getItem()) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean respectNBT = filter.getOrCreateTag()
				.getBoolean("RespectNBT");
			boolean blacklist = filter.getOrCreateTag()
				.getBoolean("Blacklist");
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
		return false;
	}

}
