package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
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
	List<Pair<ItemAttribute, Boolean>> selectedAttributes;

	public AttributeFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id, inv, extraData);
	}

	public AttributeFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, ItemStack stack) {
		super(type, id, inv, stack);
	}

	public static AttributeFilterContainer create(int id, PlayerInventory inv, ItemStack stack) {
		return new AttributeFilterContainer(AllContainerTypes.ATTRIBUTE_FILTER.get(), id, inv, stack);
	}

	public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
		selectedAttributes.add(Pair.of(itemAttribute, inverted));
	}

	@Override
	protected void init(PlayerInventory inv, ItemStack contentHolder) {
		super.init(inv, contentHolder);
		ItemStack stack = new ItemStack(Items.NAME_TAG);
		stack.setHoverName(
				new StringTextComponent("Selected Tags").withStyle(TextFormatting.RESET, TextFormatting.BLUE));
		ghostInventory.setStackInSlot(1, stack);
	}

	@Override
	protected int getPlayerInventoryXOffset() {
		return 51;
	}

	@Override
	protected int getPlayerInventoryYOffset() {
		return 105;
	}

	@Override
	protected void addFilterSlots() {
		this.addSlot(new SlotItemHandler(ghostInventory, 0, 16, 22));
		this.addSlot(new SlotItemHandler(ghostInventory, 1, 22, 57) {
			@Override
			public boolean mayPickup(PlayerEntity playerIn) {
				return false;
			}
		});
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(2);
	}

	@Override
	public void clearContents() {
		selectedAttributes.clear();
	}

	@Override
	public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == 37)
			return ItemStack.EMPTY;
		return super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public boolean canDragTo(Slot slotIn) {
		if (slotIn.index == 37)
			return false;
		return super.canDragTo(slotIn);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		if (slotIn.index == 37)
			return false;
		return super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		if (index == 37)
			return ItemStack.EMPTY;
		if (index == 36) {
			ghostInventory.setStackInSlot(37, ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getItem(index);
			ItemStack copy = stackToInsert.copy();
			copy.setCount(1);
			ghostInventory.setStackInSlot(0, copy);
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void initAndReadInventory(ItemStack filterItem) {
		super.initAndReadInventory(filterItem);
		selectedAttributes = new ArrayList<>();
		whitelistMode = WhitelistMode.values()[filterItem.getOrCreateTag()
			.getInt("WhitelistMode")];
		ListNBT attributes = filterItem.getOrCreateTag()
			.getList("MatchedAttributes", NBT.TAG_COMPOUND);
		attributes.forEach(inbt -> {
			CompoundNBT compound = (CompoundNBT) inbt;
			selectedAttributes.add(Pair.of(ItemAttribute.fromNBT(compound), compound.getBoolean("Inverted")));
		});
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		super.saveData(filterItem);
		filterItem.getOrCreateTag()
				.putInt("WhitelistMode", whitelistMode.ordinal());
		ListNBT attributes = new ListNBT();
		selectedAttributes.forEach(at -> {
			if (at == null)
				return;
			CompoundNBT compoundNBT = new CompoundNBT();
			at.getFirst()
					.serializeNBT(compoundNBT);
			compoundNBT.putBoolean("Inverted", at.getSecond());
			attributes.add(compoundNBT);
		});
		filterItem.getOrCreateTag()
			.put("MatchedAttributes", attributes);
	}

}
