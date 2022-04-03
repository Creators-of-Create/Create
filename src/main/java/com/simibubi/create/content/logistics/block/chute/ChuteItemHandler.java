package com.simibubi.create.content.logistics.block.chute;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ChuteItemHandler extends SingleVariantStorage<ItemVariant> {

	private ChuteTileEntity te;

	public ChuteItemHandler(ChuteTileEntity te) {
		this.te = te;
		update();
	}

	public void update() {
		this.variant = ItemVariant.of(te.item);
		this.amount = te.item.getCount();
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		if (!te.canAcceptItem(insertedVariant.toStack()))
			return 0;
		return super.insert(insertedVariant, maxAmount, transaction);
	}

	@Override
	protected void onFinalCommit() {
		te.setItem(variant.toStack((int) amount));
	}

	@Override
	protected long getCapacity(ItemVariant variant) {
		return Math.min(64, variant.getItem().getMaxStackSize());
	}

	@Override
	protected ItemVariant getBlankVariant() {
		return ItemVariant.blank();
	}
}
