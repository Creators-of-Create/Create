package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;

public class NBTHelper {

	public static void putMarker(CompoundNBT nbt, String marker) {
		nbt.putBoolean(marker, true);
	}

	public static <T extends Enum<?>> T readEnum(CompoundNBT nbt, String key, Class<T> enumClass) {
		T[] enumConstants = enumClass.getEnumConstants();
		String name = nbt.getString(key);
		if (enumConstants == null)
			throw new IllegalArgumentException("Non-Enum class passed to readEnum(): " + enumClass.getName());
		for (T t : enumConstants) {
			if (t.name()
				.equals(name))
				return t;
		}
		return enumConstants[0];
	}

	public static <T extends Enum<?>> void writeEnum(CompoundNBT nbt, String key, T enumConstant) {
		nbt.putString(key, enumConstant.name());
	}

	public static <T> ListNBT writeCompoundList(Iterable<T> list, Function<T, CompoundNBT> serializer) {
		ListNBT listNBT = new ListNBT();
		list.forEach(t -> listNBT.add(serializer.apply(t)));
		return listNBT;
	}

	public static <T> List<T> readCompoundList(ListNBT listNBT, Function<CompoundNBT, T> deserializer) {
		List<T> list = new ArrayList<>(listNBT.size());
		listNBT.forEach(inbt -> list.add(deserializer.apply((CompoundNBT) inbt)));
		return list;
	}

	public static <T> void iterateCompoundList(ListNBT listNBT, Consumer<CompoundNBT> consumer) {
		listNBT.forEach(inbt -> consumer.accept((CompoundNBT) inbt));
	}

	public static ListNBT writeItemList(Iterable<ItemStack> stacks) {
		return writeCompoundList(stacks, ItemStack::serializeNBT);
	}

	public static List<ItemStack> readItemList(ListNBT stacks) {
		return readCompoundList(stacks, ItemStack::of);
	}

	public static ListNBT writeAABB(AxisAlignedBB bb) {
		ListNBT bbtag = new ListNBT();
		bbtag.add(FloatNBT.valueOf((float) bb.minX));
		bbtag.add(FloatNBT.valueOf((float) bb.minY));
		bbtag.add(FloatNBT.valueOf((float) bb.minZ));
		bbtag.add(FloatNBT.valueOf((float) bb.maxX));
		bbtag.add(FloatNBT.valueOf((float) bb.maxY));
		bbtag.add(FloatNBT.valueOf((float) bb.maxZ));
		return bbtag;
	}

	public static AxisAlignedBB readAABB(ListNBT bbtag) {
		if (bbtag == null || bbtag.isEmpty())
			return null;
		return new AxisAlignedBB(bbtag.getFloat(0), bbtag.getFloat(1), bbtag.getFloat(2), bbtag.getFloat(3),
			bbtag.getFloat(4), bbtag.getFloat(5));

	}

	@Nonnull
	public static INBT getINBT(CompoundNBT nbt, String id) {
		INBT inbt = nbt.get(id);
		if (inbt != null)
			return inbt;
		return new CompoundNBT();
	}

}
