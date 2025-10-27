package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FactoryPanelSetItemMenu extends GhostItemMenu<FactoryPanelBehaviour> {

	public FactoryPanelSetItemMenu(MenuType<?> type, int id, Inventory inv, FactoryPanelBehaviour contentHolder) {
		super(type, id, inv, contentHolder);
	}

	public FactoryPanelSetItemMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public static FactoryPanelSetItemMenu create(int id, Inventory inv, FactoryPanelBehaviour be) {
		return new FactoryPanelSetItemMenu(AllMenuTypes.FACTORY_PANEL_SET_ITEM.get(), id, inv, be);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(1);
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected FactoryPanelBehaviour createOnClient(RegistryFriendlyByteBuf extraData) {
		FactoryPanelPosition pos = FactoryPanelPosition.STREAM_CODEC.decode(extraData);
		return FactoryPanelBehaviour.at(Minecraft.getInstance().level, pos);
	}

	@Override
	protected void addSlots() {
		int playerX = 13;
		int playerY = 112;
		int slotX = 74;
		int slotY = 28;

		addPlayerSlots(playerX, playerY);
		addSlot(new SlotItemHandler(ghostInventory, 0, slotX, slotY));
	}

	@Override
	protected void saveData(FactoryPanelBehaviour contentHolder) {
		if (!contentHolder.setFilter(ghostInventory.getStackInSlot(0))) {
			player.displayClientMessage(CreateLang.translateDirect("logistics.filter.invalid_item"), true);
			AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
			return;
		}
		player.level()
			.playSound(null, contentHolder.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
	}

}
