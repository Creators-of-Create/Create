package com.simibubi.create.content.kinetics.belt.transport;

import java.util.Random;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.utility.LegacyItemStackNbtBridge;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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

	public FanProcessingType processedBy;
	public int processingTime;

	public TransportedItemStack(ItemStack stack) {
		this.stack = stack;
		boolean centered = BeltHelper.isItemUpright(stack);
		angle = centered ? 180 : R.nextInt(360);
		if (PackageItem.isPackage(stack))
			angle = R.nextInt(4) * 90 + R.nextInt(20) - 10;
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

	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Item", LegacyItemStackNbtBridge.saveOptional(stack, registries));
		nbt.putFloat("Pos", beltPosition);
		nbt.putFloat("PrevPos", prevBeltPosition);
		nbt.putFloat("Offset", sideOffset);
		nbt.putFloat("PrevOffset", prevSideOffset);
		nbt.putInt("InSegment", insertedAt);
		nbt.putInt("Angle", angle);
		nbt.putInt("InDirection", insertedFrom.get3DDataValue());

		if (processedBy != null) {
			Identifier key = CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(processedBy);
			if (key == null)
				throw new IllegalArgumentException("Could not get id for FanProcessingType " + processedBy + "!");

			nbt.putString("FanProcessingType", key.toString());
			nbt.putInt("FanProcessingTime", processingTime);
		}

		if (locked)
			nbt.putBoolean("Locked", locked);
		if (lockedExternally)
			nbt.putBoolean("LockedExternally", lockedExternally);
		return nbt;
	}

	public static TransportedItemStack read(CompoundTag nbt, HolderLookup.Provider registries) {
		TransportedItemStack stack = new TransportedItemStack(LegacyItemStackNbtBridge.parseOptional(registries, nbt.getCompound("Item")));
		stack.beltPosition = nbt.getFloatOr("Pos", 0);
		stack.prevBeltPosition = nbt.getFloatOr("PrevPos", 0);
		stack.sideOffset = nbt.getFloatOr("Offset", 0);
		stack.prevSideOffset = nbt.getFloatOr("PrevOffset", 0);
		stack.insertedAt = nbt.getIntOr("InSegment", 0);
		stack.angle = nbt.getIntOr("Angle", 0);
		stack.insertedFrom = Direction.from3DDataValue(nbt.getIntOr("InDirection", Direction.UP.get3DDataValue()));
		stack.locked = nbt.getBooleanOr("Locked", false);
		stack.lockedExternally = nbt.getBooleanOr("LockedExternally", false);

		if (nbt.contains("FanProcessingType")) {
			stack.processedBy = AllFanProcessingTypes.parseLegacy(nbt.getStringOr("FanProcessingType", ""));
			stack.processingTime = nbt.getIntOr("FanProcessingTime", 0);
		}

		return stack;
	}

	public void clearFanProcessingData() {
		processedBy = null;
		processingTime = 0;
	}

}
