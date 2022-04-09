package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BasinInventory extends SmartInventory {

	private BasinTileEntity te;
	private boolean output;

	public BasinInventory(int slots, BasinTileEntity te, boolean output) {
		super(slots, te, 16, true);
		this.te = te;
		this.output = output;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		// Only insert if no other slot already has a stack of this item
		// fabric: on forge this excludes the slot being inserted to. We don't deal with slots,
		// so we allow any insertion into the output inv for hopefully identical behavior.
		if (!output) {
			try (Transaction t = transaction.openNested()) {
				long extracted = extract(resource, 1, t);
				t.abort();
				if (extracted != 0)
					return 0;
			}
		}
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		te.notifyChangeOfContents();
	}
}
