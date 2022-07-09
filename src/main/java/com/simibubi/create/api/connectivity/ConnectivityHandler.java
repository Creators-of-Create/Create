package com.simibubi.create.api.connectivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.foundation.tileEntity.IMultiTileContainer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class ConnectivityHandler {

	public static <T extends BlockEntity & IMultiTileContainer> void formMulti(T be) {
		SearchCache<T> cache = new SearchCache<>();
		List<T> frontier = new ArrayList<>();
		frontier.add(be);
		formMulti(be.getType(), be.getLevel(), cache, frontier);
	}

	private static <T extends BlockEntity & IMultiTileContainer> void formMulti(BlockEntityType<?> type,
		BlockGetter level, SearchCache<T> cache, List<T> frontier) {
		PriorityQueue<Pair<Integer, T>> creationQueue = makeCreationQueue();
		Set<BlockPos> visited = new HashSet<>();
		Direction.Axis mainAxis = frontier.get(0)
			.getMainConnectionAxis();

		// essentially, if it's a vertical multi then the search won't be restricted by
		// Y
		// alternately, a horizontal multi search shouldn't be restricted by X or Z
		int minX = (mainAxis == Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);
		int minY = (mainAxis != Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);
		int minZ = (mainAxis == Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);

		for (T be : frontier) {
			BlockPos pos = be.getBlockPos();
			minX = Math.min(pos.getX(), minX);
			minY = Math.min(pos.getY(), minY);
			minZ = Math.min(pos.getZ(), minZ);
		}
		if (mainAxis == Direction.Axis.Y)
			minX -= frontier.get(0)
				.getMaxWidth();
		if (mainAxis != Direction.Axis.Y)
			minY -= frontier.get(0)
				.getMaxWidth();
		if (mainAxis == Direction.Axis.Y)
			minZ -= frontier.get(0)
				.getMaxWidth();

		while (!frontier.isEmpty()) {
			T part = frontier.remove(0);
			BlockPos partPos = part.getBlockPos();
			if (visited.contains(partPos))
				continue;

			visited.add(partPos);

			int amount = tryToFormNewMulti(part, cache, true);
			if (amount > 1) {
				creationQueue.add(Pair.of(amount, part));
			}

			for (Direction.Axis axis : Iterate.axes) {
				Direction dir = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
				BlockPos next = partPos.relative(dir);

				if (next.getX() <= minX || next.getY() <= minY || next.getZ() <= minZ)
					continue;
				if (visited.contains(next))
					continue;
				T nextBe = partAt(type, level, next);
				if (nextBe == null)
					continue;
				if (nextBe.isRemoved())
					continue;
				frontier.add(nextBe);
			}
		}
		visited.clear();

		while (!creationQueue.isEmpty()) {
			Pair<Integer, T> next = creationQueue.poll();
			T toCreate = next.getValue();
			if (visited.contains(toCreate.getBlockPos()))
				continue;

			visited.add(toCreate.getBlockPos());
			tryToFormNewMulti(toCreate, cache, false);
		}
	}

	private static <T extends BlockEntity & IMultiTileContainer> int tryToFormNewMulti(T be, SearchCache<T> cache,
		boolean simulate) {
		int bestWidth = 1;
		int bestAmount = -1;
		if (!be.isController())
			return 0;

		int radius = be.getMaxWidth();
		for (int w = 1; w <= radius; w++) {
			int amount = tryToFormNewMultiOfWidth(be, w, cache, true);
			if (amount < bestAmount)
				continue;
			bestWidth = w;
			bestAmount = amount;
		}

		if (!simulate) {
			int beWidth = be.getWidth();
			if (beWidth == bestWidth && beWidth * beWidth * be.getHeight() == bestAmount)
				return bestAmount;

			splitMultiAndInvalidate(be, cache, false);
			if (be instanceof IMultiTileContainer.Fluid ifluid && ifluid.hasTank())
				ifluid.setTankSize(0, bestAmount);

			tryToFormNewMultiOfWidth(be, bestWidth, cache, false);

			be.preventConnectivityUpdate();
			be.setWidth(bestWidth);
			be.setHeight(bestAmount / bestWidth / bestWidth);
			be.notifyMultiUpdated();
		}
		return bestAmount;
	}

	private static <T extends BlockEntity & IMultiTileContainer> int tryToFormNewMultiOfWidth(T be, int width,
		SearchCache<T> cache, boolean simulate) {
		int amount = 0;
		int height = 0;
		BlockEntityType<?> type = be.getType();
		Level level = be.getLevel();
		if (level == null)
			return 0;
		BlockPos origin = be.getBlockPos();

		// optional fluid handling
		IFluidTank beTank = null;
		FluidStack fluid = FluidStack.EMPTY;
		if (be instanceof IMultiTileContainer.Fluid ifluid && ifluid.hasTank()) {
			beTank = ifluid.getTank(0);
			fluid = beTank.getFluid();
		}
		Direction.Axis axis = be.getMainConnectionAxis();

		Search: for (int yOffset = 0; yOffset < be.getMaxLength(axis, width); yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = switch (axis) {
					case X -> origin.offset(yOffset, xOffset, zOffset);
					case Y -> origin.offset(xOffset, yOffset, zOffset);
					case Z -> origin.offset(xOffset, zOffset, yOffset);
					};
					Optional<T> part = cache.getOrCache(type, level, pos);
					if (part.isEmpty())
						break Search;

					T controller = part.get();
					int otherWidth = controller.getWidth();
					if (otherWidth > width)
						break Search;
					if (otherWidth == width && controller.getHeight() == be.getMaxLength(axis, width))
						break Search;

					Direction.Axis conAxis = controller.getMainConnectionAxis();
					if (axis != conAxis)
						break Search;

					BlockPos conPos = controller.getBlockPos();
					if (!conPos.equals(origin)) {
						if (axis == Direction.Axis.Y) { // vertical multi, like a FluidTank
							if (conPos.getX() < origin.getX())
								break Search;
							if (conPos.getZ() < origin.getZ())
								break Search;
							if (conPos.getX() + otherWidth > origin.getX() + width)
								break Search;
							if (conPos.getZ() + otherWidth > origin.getZ() + width)
								break Search;
						} else { // horizontal multi, like an ItemVault
							if (axis == Direction.Axis.Z && conPos.getX() < origin.getX())
								break Search;
							if (conPos.getY() < origin.getY())
								break Search;
							if (axis == Direction.Axis.X && conPos.getZ() < origin.getZ())
								break Search;
							if (axis == Direction.Axis.Z && conPos.getX() + otherWidth > origin.getX() + width)
								break Search;
							if (conPos.getY() + otherWidth > origin.getY() + width)
								break Search;
							if (axis == Direction.Axis.X && conPos.getZ() + otherWidth > origin.getZ() + width)
								break Search;
						}
					}
					if (controller instanceof IMultiTileContainer.Fluid ifluidCon && ifluidCon.hasTank()) {
						FluidStack otherFluid = ifluidCon.getFluid(0);
						if (!fluid.isEmpty() && !otherFluid.isEmpty() && !fluid.isFluidEqual(otherFluid))
							break Search;
					}
				}
			}
			amount += width * width;
			height++;
		}

		if (simulate)
			return amount;

		Object extraData = be.getExtraData();

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = switch (axis) {
					case X -> origin.offset(yOffset, xOffset, zOffset);
					case Y -> origin.offset(xOffset, yOffset, zOffset);
					case Z -> origin.offset(xOffset, zOffset, yOffset);
					};
					T part = partAt(type, level, pos);
					if (part == null)
						continue;
					if (part == be)
						continue;

					extraData = be.modifyExtraData(extraData);

					if (part instanceof IMultiTileContainer.Fluid ifluidPart && ifluidPart.hasTank()) {
						IFluidTank tankAt = ifluidPart.getTank(0);
						FluidStack fluidAt = tankAt.getFluid();
						if (!fluidAt.isEmpty()) {
							// making this generic would be a rather large mess, unfortunately
							if (beTank != null && fluid.isEmpty()
								&& beTank instanceof CreativeFluidTankTileEntity.CreativeSmartFluidTank) {
								((CreativeFluidTankTileEntity.CreativeSmartFluidTank) beTank)
									.setContainedFluid(fluidAt);
							}
							if (be instanceof IMultiTileContainer.Fluid ifluidBE && ifluidBE.hasTank()
								&& beTank != null) {
								beTank.fill(fluidAt, IFluidHandler.FluidAction.EXECUTE);
							}
						}
						tankAt.drain(tankAt.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
					}

					splitMultiAndInvalidate(part, cache, false);
					part.setController(origin);
					part.preventConnectivityUpdate();
					cache.put(pos, be);
					part.setHeight(height);
					part.setWidth(width);
					part.notifyMultiUpdated();
				}
			}
		}
		be.setExtraData(extraData);
		be.notifyMultiUpdated();
		return amount;
	}

	public static <T extends BlockEntity & IMultiTileContainer> void splitMulti(T be) {
		splitMultiAndInvalidate(be, null, false);
	}

	// tryReconnect helps whenever only a few tanks have been removed
	private static <T extends BlockEntity & IMultiTileContainer> void splitMultiAndInvalidate(T be,
		@Nullable SearchCache<T> cache, boolean tryReconnect) {
		Level level = be.getLevel();
		if (level == null)
			return;

		be = be.getControllerTE();
		if (be == null)
			return;

		int height = be.getHeight();
		int width = be.getWidth();
		if (width == 1 && height == 1)
			return;

		BlockPos origin = be.getBlockPos();
		List<T> frontier = new ArrayList<>();
		Direction.Axis axis = be.getMainConnectionAxis();

		// fluid handling, if present
		FluidStack toDistribute = FluidStack.EMPTY;
		int maxCapacity = 0;
		if (be instanceof IMultiTileContainer.Fluid ifluidBE && ifluidBE.hasTank()) {
			toDistribute = ifluidBE.getFluid(0);
			maxCapacity = ifluidBE.getTankSize(0);
			if (!toDistribute.isEmpty() && !be.isRemoved())
				toDistribute.shrink(maxCapacity);
			ifluidBE.setTankSize(0, 1);
		}

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					
					BlockPos pos = switch (axis) {
					case X -> origin.offset(yOffset, xOffset, zOffset);
					case Y -> origin.offset(xOffset, yOffset, zOffset);
					case Z -> origin.offset(xOffset, zOffset, yOffset);
					};
					
					T partAt = partAt(be.getType(), level, pos);
					if (partAt == null)
						continue;
					if (!partAt.getController()
						.equals(origin))
						continue;

					T controllerBE = partAt.getControllerTE();
					partAt.setExtraData((controllerBE == null ? null : controllerBE.getExtraData()));
					partAt.removeController(true);

					if (!toDistribute.isEmpty() && partAt != be) {
						FluidStack copy = toDistribute.copy();
						IFluidTank tank =
							(partAt instanceof IMultiTileContainer.Fluid ifluidPart ? ifluidPart.getTank(0) : null);
						// making this generic would be a rather large mess, unfortunately
						if (tank instanceof CreativeFluidTankTileEntity.CreativeSmartFluidTank creativeTank) {
							if (creativeTank.isEmpty())
								creativeTank.setContainedFluid(toDistribute);
						} else {
							int split = Math.min(maxCapacity, toDistribute.getAmount());
							copy.setAmount(split);
							toDistribute.shrink(split);
							if (tank != null)
								tank.fill(copy, IFluidHandler.FluidAction.EXECUTE);
						}
					}
					if (tryReconnect) {
						frontier.add(partAt);
						partAt.preventConnectivityUpdate();
					}
					if (cache != null) 
						cache.put(pos, partAt);
				}
			}
		}
		
		if (be instanceof IMultiTileContainer.Inventory iinv && iinv.hasInventory())
			be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.invalidate();
		if (be instanceof IMultiTileContainer.Fluid ifluid && ifluid.hasTank())
			be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				.invalidate();
		
		if (tryReconnect)
			formMulti(be.getType(), level, cache == null ? new SearchCache<>() : cache, frontier);
	}

	private static <T extends BlockEntity & IMultiTileContainer> PriorityQueue<Pair<Integer, T>> makeCreationQueue() {
		return new PriorityQueue<>((one, two) -> two.getKey() - one.getKey());
	}

	@Nullable
	public static <T extends BlockEntity & IMultiTileContainer> T partAt(BlockEntityType<?> type, BlockGetter level,
		BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be != null && be.getType() == type && !be.isRemoved())
			return checked(be);
		return null;
	}

	public static <T extends BlockEntity & IMultiTileContainer> boolean isConnected(BlockGetter level, BlockPos pos,
		BlockPos other) {
		T one = checked(level.getBlockEntity(pos));
		T two = checked(level.getBlockEntity(other));
		if (one == null || two == null)
			return false;
		return one.getController()
			.equals(two.getController());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity & IMultiTileContainer> T checked(BlockEntity be) {
		if (be instanceof IMultiTileContainer)
			return (T) be;
		return null;
	}

	private static class SearchCache<T extends BlockEntity & IMultiTileContainer> {
		Map<BlockPos, Optional<T>> controllerMap;

		public SearchCache() {
			controllerMap = new HashMap<>();
		}

		void put(BlockPos pos, T target) {
			controllerMap.put(pos, Optional.of(target));
		}

		void putEmpty(BlockPos pos) {
			controllerMap.put(pos, Optional.empty());
		}

		boolean hasVisited(BlockPos pos) {
			return controllerMap.containsKey(pos);
		}

		Optional<T> getOrCache(BlockEntityType<?> type, BlockGetter level, BlockPos pos) {
			if (hasVisited(pos))
				return controllerMap.get(pos);

			T partAt = partAt(type, level, pos);
			if (partAt == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			T controller = checked(level.getBlockEntity(partAt.getController()));
			if (controller == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			put(pos, controller);
			return Optional.of(controller);
		}
	}
}
