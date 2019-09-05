package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.logistics.IReceiveWireless;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class LinkedExtractorTileEntity extends LinkedTileEntity
		implements IReceiveWireless, ITickableTileEntity, IExtractor, IHaveFilter {

	public boolean receivedSignal;

	private State state;
	private ItemStack filter;
	private int cooldown;
	private LazyOptional<IItemHandler> inventory;

	public LinkedExtractorTileEntity() {
		super(AllTileEntities.LINKED_EXTRACTOR.type);
		state = State.WAITING_FOR_INVENTORY;
		inventory = LazyOptional.empty();
		filter = ItemStack.EMPTY;
	}

	@Override
	public void setSignal(boolean powered) {
		receivedSignal = powered;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		filter = ItemStack.read(compound.getCompound("Filter"));
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Filter", filter.serializeNBT());
		return super.write(compound);
	}
	
	@Override
	public void tick() {
		IExtractor.super.tick();
		if (world.isRemote)
			return;
		if (receivedSignal != getBlockState().get(POWERED)) {
			setLocked(receivedSignal);
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			return;
		}
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

	@Override
	public void setFilter(ItemStack stack) {
		filter = stack;
		sendData();
	}

	@Override
	public ItemStack getFilter() {
		return filter;
	}

}
