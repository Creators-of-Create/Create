package com.simibubi.create.content.logistics.item.filter;

import com.simibubi.create.foundation.gui.GhostItemContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractFilterContainer extends GhostItemContainer<ItemStack> {

	protected AbstractFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id, inv, extraData);
	}

	protected AbstractFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, ItemStack contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == playerInventory.selected && clickTypeIn != ClickType.THROW)
			return ItemStack.EMPTY;
		return super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	protected boolean allowRepeats() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ItemStack createOnClient(PacketBuffer extraData) {
		return extraData.readItem();
	}

	protected abstract int getPlayerInventoryXOffset();

	protected abstract int getPlayerInventoryYOffset();

	protected abstract void addFilterSlots();

	@Override
	protected void addSlots() {
		addPlayerSlots(getPlayerInventoryXOffset(), getPlayerInventoryYOffset());
		addFilterSlots();
	}

	@Override
	protected void saveData(ItemStack contentHolder) {
		contentHolder.getOrCreateTag()
				.put("Items", ghostInventory.serializeNBT());
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return playerInventory.getSelected() == contentHolder;
	}

}
