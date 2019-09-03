package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.FrequencyHandler.Frequency;
import com.simibubi.create.modules.logistics.IHaveWireless;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public abstract class LinkedTileEntity extends SyncedTileEntity implements IHaveWireless {

	public Frequency frequencyFirst;
	public Frequency frequencyLast;

	public LinkedTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		frequencyFirst = new Frequency(ItemStack.EMPTY);
		frequencyLast = new Frequency(ItemStack.EMPTY);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (world.isRemote)
			return;
		getHandler().addToNetwork(this);
	}

	@Override
	public void remove() {
		super.remove();
		if (world.isRemote)
			return;
		getHandler().removeFromNetwork(this);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("FrequencyFirst", frequencyFirst.getStack().write(new CompoundNBT()));
		compound.put("FrequencyLast", frequencyLast.getStack().write(new CompoundNBT()));
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		frequencyFirst = new Frequency(ItemStack.read(compound.getCompound("FrequencyFirst")));
		frequencyLast = new Frequency(ItemStack.read(compound.getCompound("FrequencyLast")));
		super.read(compound);
	}

	@Override
	public void setFrequency(boolean first, ItemStack stack) {
		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed = !ItemStack.areItemsEqual(stack, toCompare)
				|| !ItemStack.areItemStackTagsEqual(stack, toCompare);

		if (changed)
			getHandler().removeFromNetwork(this);

		if (first)
			frequencyFirst = new Frequency(stack);
		else
			frequencyLast = new Frequency(stack);

		if (!changed)
			return;

		sendData();
		getHandler().addToNetwork(this);
	}

	@Override
	public Frequency getFrequencyFirst() {
		return frequencyFirst;
	}

	@Override
	public Frequency getFrequencyLast() {
		return frequencyLast;
	}

}
