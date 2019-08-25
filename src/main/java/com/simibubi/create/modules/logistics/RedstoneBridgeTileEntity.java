package com.simibubi.create.modules.logistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class RedstoneBridgeTileEntity extends SyncedTileEntity {

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

		Pair<Frequency, Frequency> networkKey = getNetworkKey();
		List<RedstoneBridgeTileEntity> TEs = connections.getOrDefault(networkKey, new ArrayList<>());
		TEs.add(this);
		connections.put(networkKey, TEs);
	}

	@Override
	public void remove() {
		super.remove();

		Pair<Frequency, Frequency> networkKey = getNetworkKey();
		List<RedstoneBridgeTileEntity> TEs = connections.get(networkKey);
		if (TEs != null)
			TEs.remove(this);
	}

	protected Pair<Frequency, Frequency> getNetworkKey() {
		return Pair.of(frequencyFirst, frequencyLast);
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

}
