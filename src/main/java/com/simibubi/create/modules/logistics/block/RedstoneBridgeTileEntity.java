package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.FrequencyHandler;
import com.simibubi.create.modules.logistics.FrequencyHandler.Frequency;
import com.simibubi.create.modules.logistics.IReceiveWireless;
import com.simibubi.create.modules.logistics.ITransmitWireless;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneBridgeTileEntity extends SyncedTileEntity
		implements ITickableTileEntity, IReceiveWireless, ITransmitWireless {

	public Frequency frequencyFirst;
	public Frequency frequencyLast;
	public boolean receivedSignal;
	public boolean transmittedSignal;

	public RedstoneBridgeTileEntity() {
		super(AllTileEntities.REDSTONE_BRIDGE.type);
		frequencyFirst = new Frequency(ItemStack.EMPTY);
		frequencyLast = new Frequency(ItemStack.EMPTY);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (world.isRemote)
			return;
		FrequencyHandler.addToNetwork(this);
	}

	@Override
	public void remove() {
		super.remove();
		if (world.isRemote)
			return;
		FrequencyHandler.removeFromNetwork(this);
	}

	public void setFrequency(boolean first, ItemStack stack) {
		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed = !ItemStack.areItemsEqual(stack, toCompare)
				|| !ItemStack.areItemStackTagsEqual(stack, toCompare);

		if (changed)
			FrequencyHandler.removeFromNetwork(this);

		if (first)
			frequencyFirst = new Frequency(stack);
		else
			frequencyLast = new Frequency(stack);

		if (!changed)
			return;

		sendData();
		FrequencyHandler.addToNetwork(this);
	}

	@Override
	public Frequency getFrequencyFirst() {
		return frequencyFirst;
	}
	
	@Override
	public Frequency getFrequencyLast() {
		return frequencyLast;
	}

	@Override
	public boolean getSignal() {
		return transmittedSignal;
	}
	
	public void transmit(boolean signal) {
		transmittedSignal = signal;
		notifySignalChange();
	}
	
	@Override
	public void setSignal(boolean powered) {
		receivedSignal = powered;
	}
	
	protected boolean isTransmitter() {
		return !getBlockState().get(RedstoneBridgeBlock.RECEIVER);
	}

	protected boolean isBlockPowered() {
		return getBlockState().get(POWERED);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("FrequencyFirst", frequencyFirst.getStack().write(new CompoundNBT()));
		compound.put("FrequencyLast", frequencyLast.getStack().write(new CompoundNBT()));
		compound.putBoolean("Transmit", transmittedSignal);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		frequencyFirst = new Frequency(ItemStack.read(compound.getCompound("FrequencyFirst")));
		frequencyLast = new Frequency(ItemStack.read(compound.getCompound("FrequencyLast")));
		transmittedSignal = compound.getBoolean("Transmit");
		super.read(compound);
	}

	@Override
	public void tick() {
		if (isTransmitter())
			return;
		if (world.isRemote)
			return;
		if (receivedSignal != isBlockPowered()) {
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			Direction attachedFace = getBlockState().get(BlockStateProperties.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.notifyNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
			return;
		}
	}

}
