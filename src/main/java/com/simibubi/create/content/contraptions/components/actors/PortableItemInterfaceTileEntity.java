package com.simibubi.create.content.contraptions.components.actors;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.fabricmc.fabric.impl.lookup.block.ServerWorldCache;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortableItemInterfaceTileEntity extends PortableStorageInterfaceTileEntity implements ItemTransferable {

	protected InterfaceItemHandler capability;

	public PortableItemInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		capability.setWrapped(contraption.inventory);
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		capability.setWrapped(Storage.empty());
		super.stopTransferring();
	}

	private InterfaceItemHandler createEmptyHandler() {
		return new InterfaceItemHandler(Storage.empty());
	}

	@Override
	protected void invalidateCapability() {
		capability.setWrapped(Storage.empty());
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction face) {
		return capability;
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(Storage<ItemVariant> wrapped) {
			super(wrapped);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted != 0) {
				transaction.addOuterCloseCallback(result -> {
					if (result.wasCommitted())
						onContentTransferred();
				});
			}
			return extracted;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted != 0) {
				transaction.addOuterCloseCallback(result -> {
					if (result.wasCommitted())
						onContentTransferred();
				});
			}
			return inserted;
		}

		private void setWrapped(Storage<ItemVariant> wrapped) {
			this.wrapped = wrapped;
		}
	}
}
