package com.simibubi.create.modules.logistics.block.belts;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.logistics.IReceiveWireless;
import com.simibubi.create.modules.logistics.block.IExtractor;
import com.simibubi.create.modules.logistics.block.IHaveFilter;
import com.simibubi.create.modules.logistics.block.LinkedTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class LinkedExtractorTileEntity extends LinkedTileEntity
		implements IReceiveWireless, ITickableTileEntity, IExtractor, IHaveFilter {

	public boolean receivedSignal;

	private State state;
	private ItemStack filter;
	private int cooldown;
	private LazyOptional<IItemHandler> inventory;
	private boolean initialize;

	public LinkedExtractorTileEntity() {
		super(AllTileEntities.LINKED_EXTRACTOR.type);
		setState(State.ON_COOLDOWN);
		inventory = LazyOptional.empty();
		filter = ItemStack.EMPTY;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		initialize = true;
	}

	@Override
	public World getWirelessWorld() {
		return super.getWorld();
	}

	@Override
	public void setSignal(boolean powered) {
		receivedSignal = powered;
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
	public void tick() {
		if (initialize && hasWorld()) {
			if (world.isBlockPowered(pos))
				state = State.LOCKED;
			neighborChanged();
			initialize = false;
		}

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
		BlockState blockState = getBlockState();
		Block block = blockState.getBlock();
		if (!(block instanceof ExtractorBlock))
			return null;
		return getPos().offset(((ExtractorBlock) block).getBlockFacing(blockState));
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
