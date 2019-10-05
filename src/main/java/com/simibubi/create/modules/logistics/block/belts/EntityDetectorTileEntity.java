package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.block.IHaveFilter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class EntityDetectorTileEntity extends SyncedTileEntity implements IHaveFilter {

	private ItemStack filter;
	
	public EntityDetectorTileEntity() {
		super(AllTileEntities.ENTITY_DETECTOR.type);
		filter = ItemStack.EMPTY;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Filter", filter.serializeNBT());
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		filter = ItemStack.read(compound.getCompound("Filter"));
		super.read(compound);
	}

	@Override
	public void setFilter(ItemStack stack) {
		filter = stack.copy();
		markDirty();
		sendData();
	}

	@Override
	public ItemStack getFilter() {
		return filter.copy();
	}

}
