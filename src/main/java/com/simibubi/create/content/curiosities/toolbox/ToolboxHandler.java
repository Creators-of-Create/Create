package com.simibubi.create.content.curiosities.toolbox;

import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

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

	static int validationTimer = 20;

	public static void entityTick(Entity entity, World world) {
		if (world.isClientSide)
			return;
		if (!(world instanceof ServerWorld))
			return;
		if (!(entity instanceof ServerPlayerEntity))
			return;
		if (entity.tickCount % validationTimer != 0)
			return;

		ServerPlayerEntity player = (ServerPlayerEntity) entity;
		if (!player.getPersistentData()
			.contains("CreateToolboxData"))
			return;

		boolean sendData = false;
		CompoundNBT compound = player.getPersistentData()
			.getCompound("CreateToolboxData");
		for (int i = 0; i < 9; i++) {
			String key = String.valueOf(i);
			if (!compound.contains(key))
				continue;

			CompoundNBT data = compound.getCompound(key);
			BlockPos pos = NBTUtil.readBlockPos(data.getCompound("Pos"));
			int slot = data.getInt("Slot");

			if (!world.isAreaLoaded(pos, 0))
				continue;
			if (!(world.getBlockState(pos)
				.getBlock() instanceof ToolboxBlock)) {
				compound.remove(key);
				sendData = true;
				continue;
			}

			TileEntity prevBlockEntity = world.getBlockEntity(pos);
			if (prevBlockEntity instanceof ToolboxTileEntity)
				((ToolboxTileEntity) prevBlockEntity).connectPlayer(slot, player, i);
		}

		if (sendData)
			syncData(player);
	}

	public static void playerLogin(PlayerEntity player) {
		if (!(player instanceof ServerPlayerEntity))
			return;
		if (player.getPersistentData()
			.contains("CreateToolboxData")
			&& !player.getPersistentData()
				.getCompound("CreateToolboxData")
				.isEmpty()) {
			syncData(player);
		}
	}

	public static void syncData(PlayerEntity player) {
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
			new ISyncPersistentData.Packet(player));
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

	public static void unequip(PlayerEntity player, int hotbarSlot, boolean keepItems) {
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
		if (prevBlockEntity instanceof ToolboxTileEntity) {
			ToolboxTileEntity toolbox = (ToolboxTileEntity) prevBlockEntity;
			toolbox.unequip(prevSlot, player, hotbarSlot, keepItems || !ToolboxHandler.withinRange(player, toolbox));
		}
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
		return AllConfigs.SERVER.curiosities.toolboxRange.get()
			.doubleValue();
	}

}
