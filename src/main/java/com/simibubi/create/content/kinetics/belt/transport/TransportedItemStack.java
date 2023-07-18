package com.simibubi.create.content.kinetics.belt.transport;

import java.util.Random;

import com.simibubi.create.content.kinetics.belt.BeltHelper;

import com.simibubi.create.content.kinetics.fan.AbstractFanProcessingType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TransportedItemStack implements Comparable<TransportedItemStack> {

	private static Random R = new Random();

	public ItemStack stack;
	public float beltPosition;
	public float sideOffset;
	public int angle;
	public int insertedAt;
	public Direction insertedFrom;
	public boolean locked;
	public boolean lockedExternally;

	public float prevBeltPosition;
	public float prevSideOffset;

	public AbstractFanProcessingType processedBy;
	public int processingTime;

	public TransportedItemStack(ItemStack stack) {
		this.stack = stack;
		boolean centered = BeltHelper.isItemUpright(stack);
		angle = centered ? 180 : R.nextInt(360);
		sideOffset = prevSideOffset = getTargetSideOffset();
		insertedFrom = Direction.UP;
	}

	public float getTargetSideOffset() {
		return (angle - 180) / (360 * 3f);
	}

	@Override
	public int compareTo(TransportedItemStack o) {
		return beltPosition < o.beltPosition ? 1 : beltPosition > o.beltPosition ? -1 : 0;
	}

	public TransportedItemStack getSimilar() {
		TransportedItemStack copy = new TransportedItemStack(stack.copy());
		copy.beltPosition = beltPosition;
		copy.insertedAt = insertedAt;
		copy.insertedFrom = insertedFrom;
		copy.prevBeltPosition = prevBeltPosition;
		copy.prevSideOffset = prevSideOffset;
		copy.processedBy = processedBy;
		copy.processingTime = processingTime;
		return copy;
	}

	public TransportedItemStack copy() {
		TransportedItemStack copy = getSimilar();
		copy.angle = angle;
		copy.sideOffset = sideOffset;
		return copy;
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Item", stack.serializeNBT());
		nbt.putFloat("Pos", beltPosition);
		nbt.putFloat("PrevPos", prevBeltPosition);
		nbt.putFloat("Offset", sideOffset);
		nbt.putFloat("PrevOffset", prevSideOffset);
		nbt.putInt("InSegment", insertedAt);
		nbt.putInt("Angle", angle);
		nbt.putInt("InDirection", insertedFrom.get3DDataValue());
		if (locked)
			nbt.putBoolean("Locked", locked);
		if (lockedExternally)
			nbt.putBoolean("LockedExternally", lockedExternally);
		return nbt;
	}

	public static TransportedItemStack read(CompoundTag nbt) {
		TransportedItemStack stack = new TransportedItemStack(ItemStack.of(nbt.getCompound("Item")));
		stack.beltPosition = nbt.getFloat("Pos");
		stack.prevBeltPosition = nbt.getFloat("PrevPos");
		stack.sideOffset = nbt.getFloat("Offset");
		stack.prevSideOffset = nbt.getFloat("PrevOffset");
		stack.insertedAt = nbt.getInt("InSegment");
		stack.angle = nbt.getInt("Angle");
		stack.insertedFrom = Direction.from3DDataValue(nbt.getInt("InDirection"));
		stack.locked = nbt.getBoolean("Locked");
		stack.lockedExternally = nbt.getBoolean("LockedExternally");
		return stack;
	}

}
