package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.ItemTransferable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.lib.util.ItemStackUtil;

import com.simibubi.create.lib.util.LazyOptional;
import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

public class PortableItemInterfaceTileEntity extends PortableStorageInterfaceTileEntity implements ItemTransferable {

	protected LazyOptional<IItemHandlerModifiable> capability;

	public PortableItemInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		capability = LazyOptional.of(() -> new InterfaceItemHandler(contraption.inventory));
		oldCap.invalidate();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		capability = createEmptyHandler();
		oldCap.invalidate();
		super.stopTransferring();
	}

	private LazyOptional<IItemHandlerModifiable> createEmptyHandler() {
		return LazyOptional.of(() -> new InterfaceItemHandler(new ItemStackHandler(0)));
	}

	@Override
	protected void invalidateCapability() {
		capability.invalidate();
	}

	@Nullable
	@Override
	public IItemHandler getItemHandler(@Nullable Direction direction) {
		return capability.orElse(null);
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(IItemHandlerModifiable wrapped) {
			super(wrapped);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (!canTransfer())
				return ItemStack.EMPTY;
			ItemStack extractItem = super.extractItem(slot, amount, simulate);
			if (!simulate && !extractItem.isEmpty())
				onContentTransferred();
			return extractItem;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (!canTransfer())
				return stack;
			ItemStack insertItem = super.insertItem(slot, stack, simulate);
			if (!simulate && !ItemStackUtil.equals(insertItem, stack, false))
				onContentTransferred();
			return insertItem;
		}

	}

}
