package com.simibubi.create.content.contraptions.components.actors;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandlerModifiable;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferable;
import io.github.fabricators_of_create.porting_lib.util.ItemStackUtil;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortableItemInterfaceTileEntity extends PortableStorageInterfaceTileEntity implements ItemTransferable {

	protected LazyOptional<IItemHandlerModifiable> capability;

	public PortableItemInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
//		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		InterfaceItemHandler handler = ((InterfaceItemHandler) capability.orElse(null));
		handler.setWrapped(contraption.inventory);
//		oldCap.invalidate();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
//		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		InterfaceItemHandler handler = ((InterfaceItemHandler) capability.orElse(null));
		handler.setWrapped(new ItemStackHandler(0));
//		oldCap.invalidate();
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
	public LazyOptional<IItemHandler> getItemHandler(@Nullable Direction direction) {
		return capability.cast();
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

		private void setWrapped(IItemHandlerModifiable wrapped) {
			this.wrapped = wrapped;
		}
	}

}
