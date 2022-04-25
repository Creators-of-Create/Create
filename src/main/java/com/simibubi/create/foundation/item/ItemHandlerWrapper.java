package com.simibubi.create.foundation.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ItemHandlerWrapper implements Storage<ItemVariant> {

	protected Storage<ItemVariant> wrapped;

	public ItemHandlerWrapper(Storage<ItemVariant> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean supportsInsertion() {
		return wrapped.supportsInsertion();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public long simulateInsert(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
		return wrapped.simulateInsert(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsExtraction() {
		return wrapped.supportsExtraction();
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public long simulateExtract(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
		return wrapped.simulateExtract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return wrapped.iterator(transaction);
	}

	@Override
	public Iterable<? extends StorageView<ItemVariant>> iterable(TransactionContext transaction) {
		return wrapped.iterable(transaction);
	}

	@Override
	public @Nullable StorageView<ItemVariant> exactView(TransactionContext transaction, ItemVariant resource) {
		return wrapped.exactView(transaction, resource);
	}

	@Override
	public long getVersion() {
		return wrapped.getVersion();
	}
}
