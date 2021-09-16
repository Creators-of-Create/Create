package com.simibubi.create.content.curiosities.toolbox;

import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ToolboxHandler {

	public static final WorldAttached<WeakHashMap<BlockPos, ToolboxTileEntity>> toolboxes =
		new WorldAttached<>(w -> new WeakHashMap<>());

	public static void onLoad(ToolboxTileEntity te) {
		toolboxes.get(te.getLevel())
			.put(te.getBlockPos(), te);
	}

	public static void onUnload(ToolboxTileEntity te) {
		toolboxes.get(te.getLevel())
			.remove(te.getBlockPos());
	}

	public static List<ToolboxTileEntity> getNearest(IWorld world, PlayerEntity player, int maxAmount) {
		Vector3d location = player.position();
		double maxRange = getMaxRange(player);
		return toolboxes.get(world)
			.keySet()
			.stream()
			.filter(p -> distance(location, p) < maxRange * maxRange)
			.sorted((p1, p2) -> Double.compare(distance(location, p1), distance(location, p2)))
			.limit(maxAmount)
			.map(toolboxes.get(world)::get)
			.collect(Collectors.toList());
	}

	public static void unequip(PlayerEntity player, int hotbarSlot) {
		CompoundNBT compound = player.getPersistentData()
			.getCompound("CreateToolboxData");
		World world = player.level;
		String key = String.valueOf(hotbarSlot);
		if (!compound.contains(key))
			return;

		CompoundNBT prevData = compound.getCompound(key);
		BlockPos prevPos = NBTUtil.readBlockPos(prevData.getCompound("Pos"));
		int prevSlot = prevData.getInt("Slot");

		TileEntity prevBlockEntity = world.getBlockEntity(prevPos);
		if (prevBlockEntity instanceof ToolboxTileEntity)
			((ToolboxTileEntity) prevBlockEntity).unequip(prevSlot, player, hotbarSlot);
		compound.remove(key);
	}

	public static boolean withinRange(PlayerEntity player, ToolboxTileEntity box) {
		if (player.level != box.getLevel())
			return false;
		double maxRange = getMaxRange(player);
		return distance(player.position(), box.getBlockPos()) < maxRange * maxRange;
	}

	public static double distance(Vector3d location, BlockPos p) {
		return location.distanceToSqr(p.getX() + 0.5f, p.getY(), p.getZ() + 0.5f);
	}

	public static double getMaxRange(PlayerEntity player) {
		return 10;
	}

}
