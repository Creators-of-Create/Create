package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class BasinInventory extends SmartInventory {

	private BasinTileEntity te;

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
		this.te = te;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		// Only insert if no other slot already has a stack of this item
		try (Transaction t = transaction.openNested()) {
			long extracted = extract(resource, 1, t);
			t.abort();
			if (extracted != 0)
				return 0;
		}
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = super.extract(resource, maxAmount, transaction);
		if (extracted != 0) {
			transaction.addOuterCloseCallback(r -> {
				if (r.wasCommitted())
					te.notifyChangeOfContents();
			});
		}
		return extracted;
	}

}
