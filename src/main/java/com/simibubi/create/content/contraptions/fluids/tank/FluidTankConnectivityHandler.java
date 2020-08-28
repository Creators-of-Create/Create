package com.simibubi.create.content.contraptions.fluids.tank;

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

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidTankConnectivityHandler {

	public static void formTanks(FluidTankTileEntity te) {
		TankSearchCache cache = new TankSearchCache();
		List<FluidTankTileEntity> frontier = new ArrayList<>();
		frontier.add(te);
		formTanks(te.getWorld(), cache, frontier);
	}

	private static void formTanks(IBlockReader world, TankSearchCache cache, List<FluidTankTileEntity> frontier) {
		PriorityQueue<Pair<Integer, FluidTankTileEntity>> creationQueue = makeCreationQueue();
		Set<BlockPos> visited = new HashSet<>();

		int minX = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (FluidTankTileEntity fluidTankTileEntity : frontier) {
			BlockPos pos = fluidTankTileEntity.getPos();
			minX = Math.min(pos.getX(), minX);
			minZ = Math.min(pos.getZ(), minZ);
		}
		minX -= FluidTankTileEntity.getMaxSize();
		minZ -= FluidTankTileEntity.getMaxSize();

		while (!frontier.isEmpty()) {
			FluidTankTileEntity tank = frontier.remove(0);
			BlockPos tankPos = tank.getPos();
			if (visited.contains(tankPos))
				continue;

			visited.add(tankPos);

			int amount = tryToFormNewTank(tank, cache, true);
			if (amount > 1)
				creationQueue.add(Pair.of(amount, tank));

			for (Axis axis : Iterate.axes) {
				Direction d = Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
				BlockPos next = tankPos.offset(d);

				if (next.getX() <= minX || next.getZ() <= minZ)
					continue;
				if (visited.contains(next))
					continue;
				FluidTankTileEntity nextTank = tankAt(world, next);
				if (nextTank == null)
					continue;
				if (nextTank.isRemoved())
					continue;
				frontier.add(nextTank);
			}
		}

		visited.clear();

		while (!creationQueue.isEmpty()) {
			Pair<Integer, FluidTankTileEntity> next = creationQueue.poll();
			FluidTankTileEntity toCreate = next.getValue();
			if (visited.contains(toCreate.getPos()))
				continue;
			visited.add(toCreate.getPos());
			tryToFormNewTank(toCreate, cache, false);
		}

	}

	public static void splitTank(FluidTankTileEntity te) {
		splitTankAndInvalidate(te, null, false);
	}

	private static int tryToFormNewTank(FluidTankTileEntity te, TankSearchCache cache, boolean simulate) {
		int bestWidth = 1;
		int bestAmount = -1;

		if (!te.isController())
			return 0;

		for (int w = 1; w <= FluidTankTileEntity.getMaxSize(); w++) {
			int amount = tryToFormNewTankOfWidth(te, w, cache, true);
			if (amount < bestAmount)
				continue;
			bestWidth = w;
			bestAmount = amount;
		}

		if (!simulate) {
			if (te.width == bestWidth && te.width * te.width * te.height == bestAmount)
				return bestAmount;

			splitTankAndInvalidate(te, cache, false);
			te.applyFluidTankSize(bestAmount);
			tryToFormNewTankOfWidth(te, bestWidth, cache, simulate);
			te.updateConnectivity = false;
			te.width = bestWidth;
			te.height = bestAmount / bestWidth / bestWidth;

			BlockState state = te.getBlockState();
			if (FluidTankBlock.isTank(state)) {
				state = state.with(FluidTankBlock.BOTTOM, true);
				state = state.with(FluidTankBlock.TOP, te.height == 1);
				te.getWorld()
					.setBlockState(te.getPos(), state, 22);
			}

			te.setWindows(te.window);
			te.onFluidStackChanged(te.tankInventory.getFluid());
			te.markDirty();
		}

		return bestAmount;
	}

	private static int tryToFormNewTankOfWidth(FluidTankTileEntity te, int width, TankSearchCache cache,
		boolean simulate) {
		int amount = 0;
		int height = 0;
		World world = te.getWorld();
		BlockPos origin = te.getPos();
		FluidStack fluid = te.getTankInventory()
			.getFluid();

		Search:

		for (int yOffset = 0; yOffset < FluidTankTileEntity.getMaxHeight(); yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = origin.add(xOffset, yOffset, zOffset);
					Optional<FluidTankTileEntity> tank = cache.getOrCache(world, pos);
					if (!tank.isPresent())
						break Search;

					FluidTankTileEntity controller = tank.get();
					int otherWidth = controller.width;
					if (otherWidth > width)
						break Search;

					BlockPos controllerPos = controller.getPos();
					if (!controllerPos.equals(origin)) {
						if (controllerPos.getX() < origin.getX())
							break Search;
						if (controllerPos.getZ() < origin.getZ())
							break Search;
						if (controllerPos.getX() + otherWidth > origin.getX() + width)
							break Search;
						if (controllerPos.getZ() + otherWidth > origin.getZ() + width)
							break Search;
					}

					FluidStack otherFluid = controller.getTankInventory()
						.getFluid();
					if (!fluid.isEmpty() && !otherFluid.isEmpty() && !fluid.isFluidEqual(otherFluid))
						break Search;

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
					BlockPos pos = origin.add(xOffset, yOffset, zOffset);
					FluidTankTileEntity tank = tankAt(world, pos);
					if (tank == te)
						continue;
					
					if (tank.isController()) {
						te.tankInventory.fill(tank.tankInventory.getFluid(), FluidAction.EXECUTE);
						tank.tankInventory.setFluid(FluidStack.EMPTY);
					}
					
					splitTankAndInvalidate(tank, cache, false);
					tank.setController(origin);
					tank.updateConnectivity = false;
					cache.put(pos, te);

					BlockState state = world.getBlockState(pos);
					if (!FluidTankBlock.isTank(state))
						continue;
					state = state.with(FluidTankBlock.BOTTOM, yOffset == 0);
					state = state.with(FluidTankBlock.TOP, yOffset == height - 1);
					world.setBlockState(pos, state, 22);
				}
			}
		}

		return amount;
	}

	private static void splitTankAndInvalidate(FluidTankTileEntity te, @Nullable TankSearchCache cache,
		boolean tryReconnect) {
		// tryReconnect helps whenever only few tanks have been removed

		te = te.getControllerTE();
		if (te == null)
			return;

		int height = te.height;
		int width = te.width;
		if (width == 1 && height == 1)
			return;

		World world = te.getWorld();
		BlockPos origin = te.getPos();
		List<FluidTankTileEntity> frontier = new ArrayList<>();
		FluidStack toDistribute = te.tankInventory.getFluid()
			.copy();
		int maxCapacity = FluidTankTileEntity.getCapacityMultiplier();
		if (!toDistribute.isEmpty())
			toDistribute.shrink(maxCapacity);

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = origin.add(xOffset, yOffset, zOffset);
					FluidTankTileEntity tankAt = tankAt(world, pos);
					if (tankAt == null)
						continue;
					if (!tankAt.getController()
						.equals(origin))
						continue;
					FluidTankTileEntity controllerTE = tankAt.getControllerTE();
					tankAt.window = controllerTE == null || controllerTE.window;
					tankAt.removeController();

					if (!toDistribute.isEmpty() && tankAt != te) {
						int split = Math.min(maxCapacity, toDistribute.getAmount());
						FluidStack copy = toDistribute.copy();
						copy.setAmount(split);
						toDistribute.shrink(split);
						tankAt.tankInventory.fill(copy, FluidAction.EXECUTE);
					}

					if (tryReconnect) {
						frontier.add(tankAt);
						tankAt.updateConnectivity = false;
					}
					if (cache != null)
						cache.put(pos, tankAt);
				}
			}
		}

		te.fluidCapability.invalidate();
		if (tryReconnect)
			formTanks(world, cache == null ? new TankSearchCache() : cache, frontier);
	}

	private static PriorityQueue<Pair<Integer, FluidTankTileEntity>> makeCreationQueue() {
		return new PriorityQueue<>(new Comparator<Pair<Integer, FluidTankTileEntity>>() {
			@Override
			public int compare(Pair<Integer, FluidTankTileEntity> o1, Pair<Integer, FluidTankTileEntity> o2) {
				return o2.getKey() - o1.getKey();
			}
		});
	}

	@Nullable
	public static FluidTankTileEntity tankAt(IBlockReader world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) te;
		return null;
	}

	private static class TankSearchCache {
		Map<BlockPos, Optional<FluidTankTileEntity>> controllerMap;

		public TankSearchCache() {
			controllerMap = new HashMap<>();
		}

		void put(BlockPos pos, FluidTankTileEntity target) {
			controllerMap.put(pos, Optional.of(target));
		}

		void putEmpty(BlockPos pos) {
			controllerMap.put(pos, Optional.empty());
		}

		boolean hasVisited(BlockPos pos) {
			return controllerMap.containsKey(pos);
		}

		Optional<FluidTankTileEntity> getOrCache(IBlockReader world, BlockPos pos) {
			if (hasVisited(pos))
				return controllerMap.get(pos);
			FluidTankTileEntity tankAt = tankAt(world, pos);
			if (tankAt == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			FluidTankTileEntity controller = tankAt.getControllerTE();
			if (controller == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			put(pos, controller);
			return Optional.of(controller);
		}

	}

	public static boolean isConnected(IBlockReader world, BlockPos tankPos, BlockPos otherTankPos) {
		TileEntity te1 = world.getTileEntity(tankPos);
		TileEntity te2 = world.getTileEntity(otherTankPos);
		if (!(te1 instanceof FluidTankTileEntity) || !(te2 instanceof FluidTankTileEntity))
			return false;
		return ((FluidTankTileEntity) te1).getController()
			.equals(((FluidTankTileEntity) te2).getController());
	}

}
