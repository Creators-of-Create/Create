package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AttributeFilterContainer extends AbstractFilterContainer {

	public enum WhitelistMode {
		WHITELIST_DISJ, WHITELIST_CONJ, BLACKLIST;
	}

	WhitelistMode whitelistMode;
	List<ItemAttribute> selectedAttributes;

	public AttributeFilterContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainerTypes.ATTRIBUTE_FILTER.type, id, inv, extraData);
	}

	public AttributeFilterContainer(int id, PlayerInventory inv, ItemStack stack) {
		super(AllContainerTypes.ATTRIBUTE_FILTER.type, id, inv, stack);
	}

	public void appendSelectedAttribute(ItemAttribute itemAttribute) {
		selectedAttributes.add(itemAttribute);
	}

	@Override
	protected void clearContents() {
		selectedAttributes.clear();
	}
	
	@Override
	protected void init() {
		super.init();
		ItemStack stack = new ItemStack(Items.NAME_TAG);
		stack.setDisplayName(
				new StringTextComponent("Selected Tags").applyTextStyles(TextFormatting.RESET, TextFormatting.BLUE));
		filterInventory.setStackInSlot(1, stack);
	}

	@Override
	protected ItemStackHandler createFilterInventory() {
		return new ItemStackHandler(2);
	}

	protected void addFilterSlots() {
		this.addSlot(new SlotItemHandler(filterInventory, 0, 16, 23));
		this.addSlot(new SlotItemHandler(filterInventory, 1, 59, 56) {
			@Override
			public boolean canTakeStack(PlayerEntity playerIn) {
				return false;
			}
		});
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == 37)
			return ItemStack.EMPTY;
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public boolean canDragIntoSlot(Slot slotIn) {
		if (slotIn.slotNumber == 37)
			return false;
		return super.canDragIntoSlot(slotIn);
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		if (slotIn.slotNumber == 37)
			return false;
		return super.canMergeSlot(stack, slotIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		if (index == 37)
			return ItemStack.EMPTY;
		if (index == 36) {
			filterInventory.setStackInSlot(37, ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getStackInSlot(index);
			ItemStack copy = stackToInsert.copy();
			copy.setCount(1);
			filterInventory.setStackInSlot(0, copy);
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected int getInventoryOffset() {
		return 86;
	}

	@Override
	protected void readData(ItemStack filterItem) {
		selectedAttributes = new ArrayList<>();
		whitelistMode = WhitelistMode.values()[filterItem.getOrCreateTag().getInt("WhitelistMode")];
		ListNBT attributes = filterItem.getOrCreateTag().getList("MatchedAttributes", NBT.TAG_COMPOUND);
		attributes.forEach(inbt -> selectedAttributes.add(ItemAttribute.fromNBT((CompoundNBT) inbt)));
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		filterItem.getOrCreateTag().putInt("WhitelistMode", whitelistMode.ordinal());
		ListNBT attributes = new ListNBT();
		selectedAttributes.forEach(at -> {
			if (at == null)
				return;
			CompoundNBT compoundNBT = new CompoundNBT();
			at.serializeNBT(compoundNBT);
			attributes.add(compoundNBT);
		});
		filterItem.getOrCreateTag().put("MatchedAttributes", attributes);
	}

}
