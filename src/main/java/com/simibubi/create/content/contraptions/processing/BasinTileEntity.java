package com.simibubi.create.content.contraptions.processing;

import java.util.Optional;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class BasinTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public boolean contentsChanged;

	protected ItemStackHandler outputInventory = new ItemStackHandler(9) {
		protected void onContentsChanged(int slot) {
			sendData();
			markDirty();
		}
	};

	public class BasinInputInventory extends RecipeWrapper {
		public BasinInputInventory() {
			super(inputInventory);
		}
	}

	protected ItemStackHandler inputInventory = new ItemStackHandler(9) {
		protected void onContentsChanged(int slot) {
			contentsChanged = true;
			sendData();
			markDirty();
		};

		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			for (int i = 0; i < getSlots(); i++) {
				ItemStack stackInSlot = getStackInSlot(i);
				if (ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
					if (stackInSlot.getCount() == getStackLimit(i, stackInSlot))
						return stack;
			}
			return super.insertItem(slot, stack, simulate);
		};
	};

	public static class BasinInventory extends CombinedInvWrapper {
		public BasinInventory(ItemStackHandler input, ItemStackHandler output) {
			super(input, output);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (isInput(slot))
				return ItemStack.EMPTY;
			return super.extractItem(slot, amount, simulate);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (!isInput(slot))
				return stack;
			return super.insertItem(slot, stack, simulate);
		}

		public boolean isInput(int slot) {
			return getIndexForSlot(slot) == 0;
		}

		public IItemHandlerModifiable getInputHandler() {
			return itemHandler[0];
		}

		public IItemHandlerModifiable getOutputHandler() {
			return itemHandler[1];
		}

	}

	protected LazyOptional<IItemHandlerModifiable> inventory =
		LazyOptional.of(() -> new BasinInventory(inputInventory, outputInventory));
	public BasinInputInventory recipeInventory;

	public BasinTileEntity() {
		super(AllTileEntities.BASIN.type);
		contentsChanged = true;
		recipeInventory = new BasinInputInventory();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.put("InputItems", inputInventory.serializeNBT());
		compound.put("OutputItems", outputInventory.serializeNBT());
		return compound;
	}

	public void onEmptied() {
		getOperator().ifPresent(te -> te.basinRemoved = true);
	}

	@Override
	public void remove() {
		onEmptied();
		inventory.invalidate();
		super.remove();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return inventory.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void tick() {
		if (!contentsChanged)
			return;
		contentsChanged = false;
		getOperator().ifPresent(te -> te.basinChecker.scheduleUpdate());
	}

	private Optional<BasinOperatingTileEntity> getOperator() {
		TileEntity te = world.getTileEntity(pos.up(2));
		if (te instanceof BasinOperatingTileEntity)
			return Optional.of((BasinOperatingTileEntity) te);
		return Optional.empty();
	}

}
