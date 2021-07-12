package com.simibubi.create.content.schematics.filtering;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class SchematicInstances {

	public static WorldAttached<Cache<Integer, SchematicWorld>> loadedSchematics;

	static {
		loadedSchematics = new WorldAttached<>($ -> CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build());
	}

	public static void register() {}

	@Nullable
	public static SchematicWorld get(World world, ItemStack schematic) {
		Cache<Integer, SchematicWorld> map = loadedSchematics.get(world);
		int hash = getHash(schematic);
		SchematicWorld ifPresent = map.getIfPresent(hash);
		if (ifPresent != null)
			return ifPresent;
		SchematicWorld loadWorld = loadWorld(world, schematic);
		if (loadWorld == null)
			return null;
		map.put(hash, loadWorld);
		return loadWorld;
	}

	private static SchematicWorld loadWorld(World wrapped, ItemStack schematic) {
		if (schematic == null || !schematic.hasTag())
			return null;
		if (!schematic.getTag()
			.getBoolean("Deployed"))
			return null;

		Template activeTemplate = SchematicItem.loadSchematic(schematic);

		if (activeTemplate.getSize()
			.equals(BlockPos.ZERO))
			return null;

		BlockPos anchor = NBTUtil.readBlockPos(schematic.getTag()
			.getCompound("Anchor"));
		SchematicWorld world = new SchematicWorld(anchor, wrapped);
		PlacementSettings settings = SchematicItem.getSettings(schematic);
		activeTemplate.place(world, anchor, settings, wrapped.getRandom());

		return world;
	}

	public static void clearHash(ItemStack schematic) {
		if (schematic == null || !schematic.hasTag())
			return;
		schematic.getTag()
			.remove("SchematicHash");
	}

	public static int getHash(ItemStack schematic) {
		if (schematic == null || !schematic.hasTag())
			return -1;
		CompoundNBT tag = schematic.getTag();
		if (!tag.contains("SchematicHash"))
			tag.putInt("SchematicHash", tag.toString()
				.hashCode());
		return tag.getInt("SchematicHash");
	}

}
