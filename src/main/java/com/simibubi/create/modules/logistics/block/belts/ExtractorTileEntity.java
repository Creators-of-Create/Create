package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.block.IExtractor;
import com.simibubi.create.modules.logistics.block.IHaveFilter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class ExtractorTileEntity extends SyncedTileEntity implements IExtractor, ITickableTileEntity, IHaveFilter {

	private State state;
	private ItemStack filter;
	private int cooldown;
	private LazyOptional<IItemHandler> inventory;
	private boolean initialize;
	
	public ExtractorTileEntity() {
		super(AllTileEntities.EXTRACTOR.type);
		state = State.ON_COOLDOWN;
		cooldown = CreateConfig.parameters.extractorDelay.get();
		inventory = LazyOptional.empty();
		filter = ItemStack.EMPTY;
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		filter = ItemStack.read(compound.getCompound("Filter"));
		if (compound.getBoolean("Locked"))
			setState(State.LOCKED);
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Filter", filter.serializeNBT());
		compound.putBoolean("Locked", getState() == State.LOCKED);
		return super.write(compound);
	}
	
	@Override
	public void onLoad() {
		initialize = true;
	}
	
	@Override
	public void tick() {
		if (initialize && hasWorld()) {
			if (world.isBlockPowered(pos))
				state = State.LOCKED;
			neighborChanged();
			initialize = false;
		}
		IExtractor.super.tick();
	}
	
	@Override
	public void setState(State state) {
		if (state == State.ON_COOLDOWN)
			cooldown = CreateConfig.parameters.extractorDelay.get();
		if (state == State.WAITING_FOR_INVENTORY)
			cooldown = CreateConfig.parameters.extractorInventoryScanDelay.get();
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

	@Override
	public void setFilter(ItemStack stack) {
		filter = stack.copy();
		markDirty();
		sendData();
		neighborChanged();
	}

	@Override
	public ItemStack getFilter() {
		return filter.copy();
	}

}
