package com.simibubi.create.modules.logistics;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneBridgeTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public static final int RANGE = 128;

	static Map<Pair<Frequency, Frequency>, List<RedstoneBridgeTileEntity>> connections;

	public static class Frequency {
		private ItemStack stack;
		private Item item;
		private int color;

		public Frequency(ItemStack stack) {
			this.stack = stack;
			item = stack.getItem();
			CompoundNBT displayTag = stack.getChildTag("display");
			color = displayTag != null && displayTag.contains("color") ? displayTag.getInt("color") : -1;
		}

		public ItemStack getStack() {
			return stack;
		}

		@Override
		public int hashCode() {
			return item.hashCode() ^ color;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Frequency ? ((Frequency) obj).item == item && ((Frequency) obj).color == color
					: false;
		}

	}

	public Frequency frequencyFirst;
	public Frequency frequencyLast;
	public boolean networkChanged;

	public RedstoneBridgeTileEntity() {
		super(AllTileEntities.REDSTONE_BRIDGE.type);
		frequencyFirst = new Frequency(ItemStack.EMPTY);
		frequencyLast = new Frequency(ItemStack.EMPTY);

		if (connections == null)
			connections = new HashMap<>();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (world.isRemote)
			return;

		addToNetwork();
	}

	@Override
	public void remove() {
		super.remove();
		if (world.isRemote)
			return;

		removeFromNetwork();
	}

	public void setFrequency(boolean first, ItemStack stack) {
		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = first ? frequencyFirst.stack : frequencyLast.stack;
		boolean changed = !ItemStack.areItemsEqual(stack, toCompare)
				|| !ItemStack.areItemStackTagsEqual(stack, toCompare);

		if (changed)
			removeFromNetwork();

		if (first)
			frequencyFirst = new Frequency(stack);
		else
			frequencyLast = new Frequency(stack);

		if (!changed)
			return;

		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 18);
		addToNetwork();
	}

	protected Pair<Frequency, Frequency> getNetworkKey() {
		return Pair.of(frequencyFirst, frequencyLast);
	}

	protected void addToNetwork() {
		Pair<Frequency, Frequency> networkKey = getNetworkKey();
		List<RedstoneBridgeTileEntity> TEs = connections.getOrDefault(networkKey, new ArrayList<>());
		TEs.add(this);
		connections.put(networkKey, TEs);
		notifyNetwork();
	}

	protected void removeFromNetwork() {
		Pair<Frequency, Frequency> networkKey = getNetworkKey();
		List<RedstoneBridgeTileEntity> TEs = connections.get(networkKey);
		if (TEs != null)
			TEs.remove(this);
		if (TEs.isEmpty()) {
			connections.remove(networkKey);
			return;
		}
		notifyNetwork();
	}

	protected boolean isNetworkPowered() {
		List<RedstoneBridgeTileEntity> TEs = connections.get(getNetworkKey());
		for (RedstoneBridgeTileEntity te : TEs) {
			if (te == this)
				continue;
			if (te.canProvideNetworkPower())
				return true;
		}
		return false;
	}

	protected void notifyNetwork() {
		for (RedstoneBridgeTileEntity te : connections.get(getNetworkKey()))
			te.networkChanged = true;
	}

	public boolean canProvideNetworkPower() {
		return isBlockPowered() && isTransmitter();
	}

	public boolean isTransmitter() {
		return !getBlockState().get(RedstoneBridgeBlock.RECEIVER);
	}

	public boolean isBlockPowered() {
		return getBlockState().get(POWERED);
	}

	public void blockChanged() {
		notifyNetwork();
		networkChanged = true;
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
	public void tick() {
		if (!networkChanged)
			return;
		networkChanged = false;

		if (isTransmitter())
			return;
		if (isNetworkPowered() != isBlockPowered()) {
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			Direction attachedFace = getBlockState().get(BlockStateProperties.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.notifyNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
			return;
		}
	}

}
