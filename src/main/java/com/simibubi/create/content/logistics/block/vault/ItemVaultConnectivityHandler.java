package com.simibubi.create.content.logistics.block.vault;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemVaultConnectivityHandler {

	public static void formVaults(ItemVaultTileEntity te) {
		VaultSearchCache cache = new VaultSearchCache();
		List<ItemVaultTileEntity> frontier = new ArrayList<>();
		frontier.add(te);
		formVaults(te.getType(), te.getLevel(), cache, frontier);
	}

	private static void formVaults(BlockEntityType<?> type, BlockGetter world, VaultSearchCache cache,
		List<ItemVaultTileEntity> frontier) {
		PriorityQueue<Pair<Integer, ItemVaultTileEntity>> creationQueue = makeCreationQueue();
		Set<BlockPos> visited = new HashSet<>();

		int minY = Integer.MAX_VALUE;
		for (ItemVaultTileEntity fluidTankTileEntity : frontier) {
			BlockPos pos = fluidTankTileEntity.getBlockPos();
			minY = Math.min(pos.getY(), minY);
		}

		minY -= 3;

		while (!frontier.isEmpty()) {
			ItemVaultTileEntity tank = frontier.remove(0);
			BlockPos tankPos = tank.getBlockPos();
			if (visited.contains(tankPos))
				continue;

			visited.add(tankPos);

			int amount = tryToFormNewVault(tank, cache, true);
			if (amount > 1)
				creationQueue.add(Pair.of(amount, tank));

			for (Axis axis : Iterate.axes) {
				Direction d = Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE);
				BlockPos next = tankPos.relative(d);

				if (next.getY() <= minY)
					continue;
				if (visited.contains(next))
					continue;
				ItemVaultTileEntity nextTank = vaultAt(type, world, next);
				if (nextTank == null)
					continue;
				if (nextTank.isRemoved())
					continue;
				frontier.add(nextTank);
			}
		}

		visited.clear();

		while (!creationQueue.isEmpty()) {
			Pair<Integer, ItemVaultTileEntity> next = creationQueue.poll();
			ItemVaultTileEntity toCreate = next.getValue();
			if (visited.contains(toCreate.getBlockPos()))
				continue;
			visited.add(toCreate.getBlockPos());
			tryToFormNewVault(toCreate, cache, false);
		}

	}

	public static void splitVault(ItemVaultTileEntity te) {
		splitVaultAndInvalidate(te, null, false);
	}

	private static int tryToFormNewVault(ItemVaultTileEntity te, VaultSearchCache cache, boolean simulate) {
		int bestWidth = 1;
		int bestAmount = -1;

		if (!te.isController())
			return 0;

		for (int w = 1; w <= 3; w++) {
			int amount = tryToFormNewVaultOfRadius(te, w, cache, true);
			if (amount < bestAmount)
				continue;
			bestWidth = w;
			bestAmount = amount;
		}

		if (!simulate) {
			if (te.radius == bestWidth && te.radius * te.radius * te.length == bestAmount)
				return bestAmount;

			splitVaultAndInvalidate(te, cache, false);
			tryToFormNewVaultOfRadius(te, bestWidth, cache, simulate);
			te.updateConnectivity = false;
			te.radius = bestWidth;
			te.length = bestAmount / bestWidth / bestWidth;

			BlockState state = te.getBlockState();
			if (ItemVaultBlock.isVault(state))
				te.getLevel()
					.setBlock(te.getBlockPos(), state.setValue(ItemVaultBlock.LARGE, te.radius > 2), 22);

			te.itemCapability.invalidate();
			te.setChanged();
		}

		return bestAmount;
	}

	private static int tryToFormNewVaultOfRadius(ItemVaultTileEntity te, int width, VaultSearchCache cache,
		boolean simulate) {
		int amount = 0;
		int height = 0;
		BlockEntityType<?> type = te.getType();
		Level world = te.getLevel();
		BlockPos origin = te.getBlockPos();
		boolean alongZ = ItemVaultBlock.getVaultBlockAxis(te.getBlockState()) == Axis.Z;

		Search:

		for (int yOffset = 0; yOffset < ItemVaultTileEntity.getMaxLength(width); yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos =
						alongZ ? origin.offset(xOffset, zOffset, yOffset) : origin.offset(yOffset, xOffset, zOffset);
					Optional<ItemVaultTileEntity> tank = cache.getOrCache(type, world, pos);
					if (!tank.isPresent())
						break Search;

					ItemVaultTileEntity controller = tank.get();
					int otherWidth = controller.radius;
					if (otherWidth > width)
						break Search;
					if (otherWidth == width && controller.length == ItemVaultTileEntity.getMaxLength(width))
						break Search;
					if ((ItemVaultBlock.getVaultBlockAxis(controller.getBlockState()) == Axis.Z) != alongZ)
						break Search;

					BlockPos controllerPos = controller.getBlockPos();
					if (!controllerPos.equals(origin)) {
						if (alongZ && controllerPos.getX() < origin.getX())
							break Search;
						if (controllerPos.getY() < origin.getY())
							break Search;
						if (!alongZ && controllerPos.getZ() < origin.getZ())
							break Search;
						if (alongZ && controllerPos.getX() + otherWidth > origin.getX() + width)
							break Search;
						if (controllerPos.getY() + otherWidth > origin.getY() + width)
							break Search;
						if (!alongZ && controllerPos.getZ() + otherWidth > origin.getZ() + width)
							break Search;
					}

				}
			}

			amount += width * width;
			height++;
		}

		if (simulate)
			return amount;

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos =
						alongZ ? origin.offset(xOffset, zOffset, yOffset) : origin.offset(yOffset, xOffset, zOffset);
					ItemVaultTileEntity tank = vaultAt(type, world, pos);
					if (tank == te)
						continue;

					splitVaultAndInvalidate(tank, cache, false);
					tank.setController(origin);
					tank.updateConnectivity = false;
					cache.put(pos, te);

					BlockState state = world.getBlockState(pos);
					if (!ItemVaultBlock.isVault(state))
						continue;
					state = state.setValue(ItemVaultBlock.LARGE, width > 2);
					world.setBlock(pos, state, 22);
				}
			}
		}

		return amount;
	}

	private static void splitVaultAndInvalidate(ItemVaultTileEntity te, @Nullable VaultSearchCache cache,
		boolean tryReconnect) {
		// tryReconnect helps whenever only few tanks have been removed

		te = te.getControllerTE();
		if (te == null)
			return;

		int height = te.length;
		int width = te.radius;
		BlockState state = te.getBlockState();
		boolean alongZ = ItemVaultBlock.getVaultBlockAxis(state) == Axis.Z;
		if (width == 1 && height == 1)
			return;

		Level world = te.getLevel();
		BlockPos origin = te.getBlockPos();
		List<ItemVaultTileEntity> frontier = new ArrayList<>();

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos =
						alongZ ? origin.offset(xOffset, zOffset, yOffset) : origin.offset(yOffset, xOffset, zOffset);
					ItemVaultTileEntity tankAt = vaultAt(te.getType(), world, pos);
					if (tankAt == null)
						continue;
					if (!tankAt.getController()
						.equals(origin))
						continue;

					tankAt.removeController(true);

					if (tryReconnect) {
						frontier.add(tankAt);
						tankAt.updateConnectivity = false;
					}
					if (cache != null)
						cache.put(pos, tankAt);
				}
			}
		}

		te.itemCapability.invalidate();
		if (tryReconnect)
			formVaults(te.getType(), world, cache == null ? new VaultSearchCache() : cache, frontier);
	}

	private static PriorityQueue<Pair<Integer, ItemVaultTileEntity>> makeCreationQueue() {
		return new PriorityQueue<>(new Comparator<Pair<Integer, ItemVaultTileEntity>>() {
			@Override
			public int compare(Pair<Integer, ItemVaultTileEntity> o1, Pair<Integer, ItemVaultTileEntity> o2) {
				return o2.getKey() - o1.getKey();
			}
		});
	}

	@Nullable
	public static ItemVaultTileEntity vaultAt(BlockEntityType<?> type, BlockGetter world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof ItemVaultTileEntity && te.getType() == type)
			return (ItemVaultTileEntity) te;
		return null;
	}

	private static class VaultSearchCache {
		Map<BlockPos, Optional<ItemVaultTileEntity>> controllerMap;

		public VaultSearchCache() {
			controllerMap = new HashMap<>();
		}

		void put(BlockPos pos, ItemVaultTileEntity target) {
			controllerMap.put(pos, Optional.of(target));
		}

		void putEmpty(BlockPos pos) {
			controllerMap.put(pos, Optional.empty());
		}

		boolean hasVisited(BlockPos pos) {
			return controllerMap.containsKey(pos);
		}

		Optional<ItemVaultTileEntity> getOrCache(BlockEntityType<?> type, BlockGetter world, BlockPos pos) {
			if (hasVisited(pos))
				return controllerMap.get(pos);
			ItemVaultTileEntity tankAt = vaultAt(type, world, pos);
			if (tankAt == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			ItemVaultTileEntity controller = tankAt.getControllerTE();
			if (controller == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			put(pos, controller);
			return Optional.of(controller);
		}

	}

	public static boolean isConnected(BlockGetter world, BlockPos tankPos, BlockPos otherTankPos) {
		BlockEntity te1 = world.getBlockEntity(tankPos);
		BlockEntity te2 = world.getBlockEntity(otherTankPos);
		if (!(te1 instanceof ItemVaultTileEntity) || !(te2 instanceof ItemVaultTileEntity))
			return false;
		return ((ItemVaultTileEntity) te1).getController()
			.equals(((ItemVaultTileEntity) te2).getController());
	}

}
