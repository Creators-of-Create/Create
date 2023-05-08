package com.simibubi.create.content.schematics.filtering;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SchematicInstances {

	public static final WorldAttached<Cache<Integer, SchematicWorld>> loadedSchematics;

	static {
		loadedSchematics = new WorldAttached<>($ -> CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build());
	}

	public static void register() {}

	@Nullable
	public static SchematicWorld get(Level world, ItemStack schematic) {
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

	private static SchematicWorld loadWorld(Level wrapped, ItemStack schematic) {
		if (schematic == null || !schematic.hasTag())
			return null;
		if (!schematic.getTag()
			.getBoolean("Deployed"))
			return null;

		StructureTemplate activeTemplate = SchematicItem.loadSchematic(schematic);

		if (activeTemplate.getSize()
			.equals(Vec3i.ZERO))
			return null;

		BlockPos anchor = NbtUtils.readBlockPos(schematic.getTag()
			.getCompound("Anchor"));
		SchematicWorld world = new SchematicWorld(anchor, wrapped);
		StructurePlaceSettings settings = SchematicItem.getSettings(schematic);
		activeTemplate.placeInWorld(world, anchor, anchor, settings, wrapped.getRandom(), Block.UPDATE_CLIENTS);

		StructureTransform transform = new StructureTransform(settings.getRotationPivot(), Direction.Axis.Y,
			settings.getRotation(), settings.getMirror());
		for (BlockEntity be : world.getBlockEntities())
			transform.apply(be);
		
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
		CompoundTag tag = schematic.getTag();
		if (!tag.contains("SchematicHash"))
			tag.putInt("SchematicHash", tag.toString()
				.hashCode());
		return tag.getInt("SchematicHash");
	}

}
