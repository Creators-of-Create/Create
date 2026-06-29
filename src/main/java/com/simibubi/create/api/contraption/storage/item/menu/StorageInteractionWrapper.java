package com.simibubi.create.api.contraption.storage.item.menu;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;

import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class StorageInteractionWrapper extends ItemHandlerContainer {
	private final Predicate<Player> stillValid;
	private final Consumer<Player> onClose;

	public StorageInteractionWrapper(IItemHandlerModifiable inv, Predicate<Player> stillValid, Consumer<Player> onClose) {
		super(inv);
		this.stillValid = stillValid;
		this.onClose = onClose;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.stillValid.test(player);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void stopOpen(Player player) {
		this.onClose.accept(player);
	}
}
