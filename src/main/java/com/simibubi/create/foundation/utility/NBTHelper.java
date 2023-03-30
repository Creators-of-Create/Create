package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class NBTHelper {

	public static void putMarker(CompoundTag nbt, String marker) {
		nbt.putBoolean(marker, true);
	}

	public static <T extends Enum<?>> T readEnum(CompoundTag nbt, String key, Class<T> enumClass) {
		T[] enumConstants = enumClass.getEnumConstants();
		if (enumConstants == null)
			throw new IllegalArgumentException("Non-Enum class passed to readEnum: " + enumClass.getName());
		if (nbt.contains(key, Tag.TAG_STRING)) {
			String name = nbt.getString(key);
			for (T t : enumConstants) {
				if (t.name()
					.equals(name))
					return t;
			}
		}
		return enumConstants[0];
	}

	public static <T extends Enum<?>> void writeEnum(CompoundTag nbt, String key, T enumConstant) {
		nbt.putString(key, enumConstant.name());
	}

	public static <T> ListTag writeCompoundList(Iterable<T> list, Function<T, CompoundTag> serializer) {
		ListTag listNBT = new ListTag();
		list.forEach(t -> {
			CompoundTag apply = serializer.apply(t);
			if (apply == null)
				return;
			listNBT.add(apply);
		});
		return listNBT;
	}

	public static <T> List<T> readCompoundList(ListTag listNBT, Function<CompoundTag, T> deserializer) {
		List<T> list = new ArrayList<>(listNBT.size());
		listNBT.forEach(inbt -> list.add(deserializer.apply((CompoundTag) inbt)));
		return list;
	}

	public static <T> void iterateCompoundList(ListTag listNBT, Consumer<CompoundTag> consumer) {
		listNBT.forEach(inbt -> consumer.accept((CompoundTag) inbt));
	}

	public static ListTag writeItemList(Iterable<ItemStack> stacks) {
		return writeCompoundList(stacks, ItemStack::serializeNBT);
	}

	public static List<ItemStack> readItemList(ListTag stacks) {
		return readCompoundList(stacks, ItemStack::of);
	}

	public static ListTag writeAABB(AABB bb) {
		ListTag bbtag = new ListTag();
		bbtag.add(FloatTag.valueOf((float) bb.minX));
		bbtag.add(FloatTag.valueOf((float) bb.minY));
		bbtag.add(FloatTag.valueOf((float) bb.minZ));
		bbtag.add(FloatTag.valueOf((float) bb.maxX));
		bbtag.add(FloatTag.valueOf((float) bb.maxY));
		bbtag.add(FloatTag.valueOf((float) bb.maxZ));
		return bbtag;
	}

	public static AABB readAABB(ListTag bbtag) {
		if (bbtag == null || bbtag.isEmpty())
			return null;
		return new AABB(bbtag.getFloat(0), bbtag.getFloat(1), bbtag.getFloat(2), bbtag.getFloat(3),
			bbtag.getFloat(4), bbtag.getFloat(5));
	}

	public static ListTag writeVec3i(Vec3i vec) {
		ListTag tag = new ListTag();
		tag.add(IntTag.valueOf(vec.getX()));
		tag.add(IntTag.valueOf(vec.getY()));
		tag.add(IntTag.valueOf(vec.getZ()));
		return tag;
	}

	public static Vec3i readVec3i(ListTag tag) {
		return new Vec3i(tag.getInt(0), tag.getInt(1), tag.getInt(2));
	}

	@Nonnull
	public static Tag getINBT(CompoundTag nbt, String id) {
		Tag inbt = nbt.get(id);
		if (inbt != null)
			return inbt;
		return new CompoundTag();
	}

	public static void writeResourceLocation(CompoundTag nbt, String key, ResourceLocation location) {
		// Ensure correct format
		nbt.putString(key, location.toString());
	}

	public static ResourceLocation readResourceLocation(CompoundTag nbt, String key) {
		if (!nbt.contains(key))
			return null;
		String[] data = nbt.getString(key).split(":");
		if (data.length != 2)
			return null;
		return new ResourceLocation(data[0], data[1]);
	}

}
