package com.simibubi.create.modules.contraptions.processing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class BasinTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	protected boolean updateProcessing;

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
			updateProcessing = true;
			sendData();
			markDirty();
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

	protected LazyOptional<IItemHandlerModifiable> inventory = LazyOptional
			.of(() -> new BasinInventory(inputInventory, outputInventory));
	public BasinInputInventory recipeInventory;

	public BasinTileEntity() {
		super(AllTileEntities.BASIN.type);
		updateProcessing = true;
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
		TileEntity te = world.getTileEntity(pos.up(2));
		if (te == null)
			return;
		if (te instanceof MechanicalMixerTileEntity)
			((MechanicalMixerTileEntity) te).basinRemoved = true;
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
		if (!updateProcessing)
			return;
		updateProcessing = false;

		TileEntity te = world.getTileEntity(pos.up(2));
		if (te == null)
			return;
		if (te instanceof MechanicalMixerTileEntity)
			((MechanicalMixerTileEntity) te).checkBasin = true;

	}

}
