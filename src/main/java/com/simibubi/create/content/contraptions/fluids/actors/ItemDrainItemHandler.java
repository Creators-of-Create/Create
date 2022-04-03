package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class ItemDrainItemHandler implements Storage<ItemVariant> {

	private ItemDrainTileEntity te;
	private Direction side;

	public ItemDrainItemHandler(ItemDrainTileEntity te, Direction side) {
		this.te = te;
		this.side = side;
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		if (!te.getHeldItemStack().isEmpty())
			return 0;
		int toInsert = (int) Math.min(maxAmount, 64);
		ItemStack tryInsert = insertedVariant.toStack(toInsert);
		if (maxAmount > 1 && EmptyingByBasin.canItemBeEmptied(te.getLevel(), tryInsert)) {
			TransportedItemStack heldItem = new TransportedItemStack(ItemHandlerHelper.copyStackWithSize(tryInsert, 1));
			heldItem.prevBeltPosition = 0;
			te.snapshotParticipant.updateSnapshots(transaction);
			te.setHeldItem(heldItem, side.getOpposite());
			return 1;
		}

		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack held = te.heldItem;
		if (held == null)
			return 0;

		ItemStack stack = held.stack.copy();
		ItemStack extracted = stack.split((int) maxAmount);
		te.snapshotParticipant.updateSnapshots(transaction);
		te.heldItem.stack = stack;
		if (stack.isEmpty())
			te.heldItem = null;
		return extracted.getCount();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return null;
	}
}
