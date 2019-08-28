package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class ExtractorTileEntity extends SyncedTileEntity implements IExtractor, ITickableTileEntity {

	private State state;
	private int cooldown;
	private LazyOptional<IItemHandler> inventory;
	
	public ExtractorTileEntity() {
		super(AllTileEntities.EXTRACTOR.type);
		state = State.WAITING_FOR_ITEM;
		inventory = LazyOptional.empty();
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public void setState(State state) {
		if (state == State.ON_COOLDOWN)
			cooldown = EXTRACTOR_COOLDOWN;
		this.state = state;
	}
	
	@Override
	public int tickCooldown() {
		return cooldown--;
	}

	@Override
	public BlockPos getInventoryPos() {
		return getPos().offset(getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

	@Override
	public LazyOptional<IItemHandler> getInventory() {
		return inventory;
	}

	@Override
	public void setInventory(LazyOptional<IItemHandler> inventory) {
		this.inventory = inventory;
	}

}
