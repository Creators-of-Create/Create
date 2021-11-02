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

import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity.CreativeSmartFluidTank;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankConnectivityHandler {

	public static void formTanks(FluidTankTileEntity te) {
		TankSearchCache cache = new TankSearchCache();
		List<FluidTankTileEntity> frontier = new ArrayList<>();
		frontier.add(te);
		formTanks(te.getType(), te.getLevel(), cache, frontier);
	}

	private static void formTanks(BlockEntityType<?> type, BlockGetter world, TankSearchCache cache,
		List<FluidTankTileEntity> frontier) {
		PriorityQueue<Pair<Integer, FluidTankTileEntity>> creationQueue = makeCreationQueue();
		Set<BlockPos> visited = new HashSet<>();

		int minX = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (FluidTankTileEntity fluidTankTileEntity : frontier) {
			BlockPos pos = fluidTankTileEntity.getBlockPos();
			minX = Math.min(pos.getX(), minX);
			minZ = Math.min(pos.getZ(), minZ);
		}
		minX -= FluidTankTileEntity.getMaxSize();
		minZ -= FluidTankTileEntity.getMaxSize();

		while (!frontier.isEmpty()) {
			FluidTankTileEntity tank = frontier.remove(0);
			BlockPos tankPos = tank.getBlockPos();
			if (visited.contains(tankPos))
				continue;

			visited.add(tankPos);

			int amount = tryToFormNewTank(tank, cache, true);
			if (amount > 1)
				creationQueue.add(Pair.of(amount, tank));

			for (Axis axis : Iterate.axes) {
				Direction d = Direction.get(AxisDirection.NEGATIVE, axis);
				BlockPos next = tankPos.relative(d);

				if (next.getX() <= minX || next.getZ() <= minZ)
					continue;
				if (visited.contains(next))
					continue;
				FluidTankTileEntity nextTank = tankAt(type, world, next);
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
			if (visited.contains(toCreate.getBlockPos()))
				continue;
			visited.add(toCreate.getBlockPos());
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
				state = state.setValue(FluidTankBlock.BOTTOM, true);
				state = state.setValue(FluidTankBlock.TOP, te.height == 1);
				te.getLevel()
					.setBlock(te.getBlockPos(), state, 22);
			}

			te.setWindows(te.window);
			te.onFluidStackChanged(te.tankInventory.getFluid());
			te.setChanged();
		}

		return bestAmount;
	}

	private static int tryToFormNewTankOfWidth(FluidTankTileEntity te, int width, TankSearchCache cache,
		boolean simulate) {
		int amount = 0;
		int height = 0;
		BlockEntityType<?> type = te.getType();
		Level world = te.getLevel();
		BlockPos origin = te.getBlockPos();
		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		FluidTank teTank = (FluidTank) capability.orElse(null);
		FluidStack fluid = capability.map(ifh -> ifh.getFluidInTank(0))
			.orElse(FluidStack.EMPTY);

		Search:

		for (int yOffset = 0; yOffset < FluidTankTileEntity.getMaxHeight(); yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = origin.offset(xOffset, yOffset, zOffset);
					Optional<FluidTankTileEntity> tank = cache.getOrCache(type, world, pos);
					if (!tank.isPresent())
						break Search;

					FluidTankTileEntity controller = tank.get();
					int otherWidth = controller.width;
					if (otherWidth > width)
						break Search;

					BlockPos controllerPos = controller.getBlockPos();
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

		boolean opaque = false;

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = origin.offset(xOffset, yOffset, zOffset);
					FluidTankTileEntity tank = tankAt(type, world, pos);
					if (tank == te)
						continue;

					opaque |= !tank.window;
					FluidTank tankTank = tank.tankInventory;
					FluidStack fluidInTank = tankTank.getFluid();
					if (!fluidInTank.isEmpty()) {
						if (teTank.isEmpty() && teTank instanceof CreativeSmartFluidTank)
							((CreativeSmartFluidTank) teTank).setContainedFluid(fluidInTank);
						teTank.fill(fluidInTank, FluidAction.EXECUTE);
					}
					tankTank.setFluid(FluidStack.EMPTY);

					splitTankAndInvalidate(tank, cache, false);
					tank.setController(origin);
					tank.updateConnectivity = false;
					cache.put(pos, te);

					BlockState state = world.getBlockState(pos);
					if (!FluidTankBlock.isTank(state))
						continue;
					state = state.setValue(FluidTankBlock.BOTTOM, yOffset == 0);
					state = state.setValue(FluidTankBlock.TOP, yOffset == height - 1);
					world.setBlock(pos, state, 22);
				}
			}
		}

		te.setWindows(!opaque);

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

		Level world = te.getLevel();
		BlockPos origin = te.getBlockPos();
		List<FluidTankTileEntity> frontier = new ArrayList<>();
		FluidStack toDistribute = te.tankInventory.getFluid()
			.copy();
		int maxCapacity = FluidTankTileEntity.getCapacityMultiplier();
		if (!toDistribute.isEmpty() && !te.isRemoved())
			toDistribute.shrink(maxCapacity);
		te.applyFluidTankSize(1);

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = origin.offset(xOffset, yOffset, zOffset);
					FluidTankTileEntity tankAt = tankAt(te.getType(), world, pos);
					if (tankAt == null)
						continue;
					if (!tankAt.getController()
						.equals(origin))
						continue;
					FluidTankTileEntity controllerTE = tankAt.getControllerTE();
					tankAt.window = controllerTE == null || controllerTE.window;
					tankAt.removeController(true);

					if (!toDistribute.isEmpty() && tankAt != te) {
						FluidStack copy = toDistribute.copy();
						FluidTank tankInventory = tankAt.tankInventory;
						if (tankInventory.isEmpty() && tankInventory instanceof CreativeSmartFluidTank)
							((CreativeSmartFluidTank) tankInventory).setContainedFluid(toDistribute);
						else {
							int split = Math.min(maxCapacity, toDistribute.getAmount());
							copy.setAmount(split);
							toDistribute.shrink(split);
							tankInventory.fill(copy, FluidAction.EXECUTE);
						}
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
			formTanks(te.getType(), world, cache == null ? new TankSearchCache() : cache, frontier);
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
	public static FluidTankTileEntity tankAt(BlockEntityType<?> type, BlockGetter world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof FluidTankTileEntity && te.getType() == type)
			return (FluidTankTileEntity) te;
		return null;
	}

	@Nullable
	public static FluidTankTileEntity anyTankAt(BlockGetter world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
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

		Optional<FluidTankTileEntity> getOrCache(BlockEntityType<?> type, BlockGetter world, BlockPos pos) {
			if (hasVisited(pos))
				return controllerMap.get(pos);
			FluidTankTileEntity tankAt = tankAt(type, world, pos);
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

	public static boolean isConnected(BlockGetter world, BlockPos tankPos, BlockPos otherTankPos) {
		BlockEntity te1 = world.getBlockEntity(tankPos);
		BlockEntity te2 = world.getBlockEntity(otherTankPos);
		if (!(te1 instanceof FluidTankTileEntity) || !(te2 instanceof FluidTankTileEntity))
			return false;
		return ((FluidTankTileEntity) te1).getController()
			.equals(((FluidTankTileEntity) te2).getController());
	}

}
