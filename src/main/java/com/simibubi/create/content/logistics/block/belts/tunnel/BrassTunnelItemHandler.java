package com.simibubi.create.content.logistics.block.belts.tunnel;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BrassTunnelItemHandler extends ItemStackHandler {

	private BrassTunnelTileEntity te;

	public BrassTunnelItemHandler(BrassTunnelTileEntity te) {
		super(1);
		this.te = te;
		stacks[0] = te.stackToDistribute;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!te.hasDistributionBehaviour()) {
			Storage<ItemVariant> beltCapability = te.getBeltCapability();
			if (beltCapability == null)
				return 0;
			return beltCapability.insert(resource, maxAmount, transaction);
		}

		if (!te.canTakeItems())
			return 0;
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		Storage<ItemVariant> beltCapability = te.getBeltCapability();
		if (beltCapability == null)
			return 0;
		return beltCapability.extract(resource, maxAmount, transaction);
	}

	@Override
	public int getSlotLimit(int slot) {
		return stacks[slot].isEmpty() ? 64 : stacks[slot].getMaxStackSize();
	}

	@Override
	protected void onFinalCommit() {
		te.setStackToDistribute(stacks[0]);
	}
}
