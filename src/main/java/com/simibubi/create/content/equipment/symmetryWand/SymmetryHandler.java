package com.simibubi.create.content.equipment.symmetryWand;

import com.simibubi.create.AllItems;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

@EventBusSubscriber
public class SymmetryHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event) {
		if (event.getLevel()
			.isClientSide())
			return;
		if (!(event.getEntity() instanceof Player player))
			return;

		Inventory inv = player.getInventory();
		for (int i = 0; i < Inventory.getSelectionSize(); i++)
			if (AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i)))
				SymmetryWandItem.apply(player.level(), inv.getItem(i), player, event.getPos(), event.getPlacedBlock());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakBlockEvent event) {
		if (event.getLevel()
			.isClientSide())
			return;

		Player player = event.getPlayer();
		Inventory inv = player.getInventory();
		for (int i = 0; i < Inventory.getSelectionSize(); i++)
			if (AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i)))
				SymmetryWandItem.remove(player.level(), inv.getItem(i), player, event.getPos());
	}

}
