package com.simibubi.create.content.schematics;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.contraptions.StructureTransform;

import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SchematicInstances {

	private static final WorldAttached<Cache<Integer, SchematicLevel>> LOADED_SCHEMATICS = new WorldAttached<>($ -> CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build());

	@Nullable
	public static SchematicLevel get(Level world, ItemStack schematic) {
		Cache<Integer, SchematicLevel> map = LOADED_SCHEMATICS.get(world);
		int hash = getHash(schematic);
		SchematicLevel ifPresent = map.getIfPresent(hash);
		if (ifPresent != null)
			return ifPresent;
		SchematicLevel loadWorld = loadWorld(world, schematic);
		if (loadWorld == null)
			return null;
		map.put(hash, loadWorld);
		return loadWorld;
	}

	private static SchematicLevel loadWorld(Level wrapped, ItemStack schematic) {
		if (schematic == null || !schematic.has(AllDataComponents.SCHEMATIC_FILE))
			return null;
		if (!schematic.has(AllDataComponents.SCHEMATIC_DEPLOYED))
			return null;

		StructureTemplate activeTemplate =
			SchematicItem.loadSchematic(wrapped, schematic);

		if (activeTemplate.getSize()
			.equals(Vec3i.ZERO))
			return null;

		BlockPos anchor = schematic.get(AllDataComponents.SCHEMATIC_ANCHOR);
		SchematicLevel world = new SchematicLevel(anchor, wrapped);
		StructurePlaceSettings settings = SchematicItem.getSettings(schematic);
		activeTemplate.placeInWorld(world, anchor, anchor, settings, wrapped.getRandom(), Block.UPDATE_CLIENTS);

		StructureTransform transform = new StructureTransform(settings.getRotationPivot(), Direction.Axis.Y,
			settings.getRotation(), settings.getMirror());
		for (BlockEntity be : world.getBlockEntities())
			transform.apply(be);

		return world;
	}

	public static void clearHash(ItemStack schematic) {
		if (schematic == null || !schematic.has(AllDataComponents.SCHEMATIC_FILE))
			return;
		schematic.remove(AllDataComponents.SCHEMATIC_HASH);
	}

	public static int getHash(ItemStack schematic) {
		if (schematic == null || !schematic.has(AllDataComponents.SCHEMATIC_FILE))
			return -1;
		if (!schematic.has(AllDataComponents.SCHEMATIC_HASH))
			schematic.set(AllDataComponents.SCHEMATIC_HASH, schematic.getComponentsPatch().hashCode());
		return schematic.getOrDefault(AllDataComponents.SCHEMATIC_HASH, -1);
	}

}
