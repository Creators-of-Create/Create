package com.simibubi.create.api.connectivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConnectivityHandler {

	public static <T extends BlockEntity & IMultiBlockEntityContainer> void formMulti(T be) {
		SearchCache<T> cache = new SearchCache<>();
		List<T> frontier = new ArrayList<>();
		BlockEntityType<?> type = be.getType();
		BlockGetter level = be.getLevel();

		if (level==null)return;

		PriorityQueue<Pair<Sizing, T>> creationQueue = new PriorityQueue<>(
			(one, two) -> two.getKey().volume - one.getKey().volume
		);
		Set<BlockPos> visited = new HashSet<>();
		Direction.Axis mainAxis = be.getMainConnectionAxis();

		BlockPos pos = be.getBlockPos();
		int maxWidth = be.getMaxWidth();

		// essentially, if it's a vertical multi then the search won't be restricted by Y
		// alternately, a horizontal multi search shouldn't be restricted by X or Z
		int minX = (mainAxis == Direction.Axis.Y ? pos.getX() - maxWidth : Integer.MIN_VALUE);
		int minY = (mainAxis != Direction.Axis.Y ? pos.getY() - maxWidth : Integer.MIN_VALUE);
		int minZ = (mainAxis == Direction.Axis.Y ? pos.getZ() - maxWidth : Integer.MIN_VALUE);

		frontier.add(be);
		while (!frontier.isEmpty()) {
			T part = frontier.removeFirst();
			BlockPos partPos = part.getBlockPos();
			if (visited.contains(partPos))
				continue;

			visited.add(partPos);

			Sizing size = getSizing(part, cache);
			if (size.volume > 1) {
				creationQueue.add(Pair.of(size, part));
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
			Pair<Sizing, T> next = creationQueue.poll();
			Sizing size = next.getKey();
			T toCreate = next.getValue();
			if (visited.contains(toCreate.getBlockPos()))
				continue;

			visited.add(toCreate.getBlockPos());
			assembleMultiWithSize(toCreate, size, cache);
		}
	}

	private static <T extends BlockEntity & IMultiBlockEntityContainer> void assembleMultiWithSize(
		T be, Sizing size, SearchCache<T> cache
	) {
		BlockEntityType<?> type = be.getType();
		Level level = be.getLevel();
		BlockPos origin = be.getBlockPos();
		Direction.Axis axis = be.getMainConnectionAxis();

		if (level == null) return;

		if (be.getWidth() == size.width && be.getHeight() == size.height)
			return;

		splitMultiAndInvalidate(be, cache);
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank())
			ifluid.setTankSize(0, size.volume);

		//optional fluid handling
		IFluidTank beTank;
		FluidStack fluid;
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) {
			beTank = ifluid.getTank(0);
			fluid = beTank.getFluid();
		} else {
			fluid = FluidStack.EMPTY;
			beTank = null;
		}

		forEachPosition(origin, axis, size.width, size.height, (pos) -> {
			T part = partAt(type, level, pos);
			if (part == null || part == be)
				return;

			if (part instanceof IMultiBlockEntityContainer.Fluid ifluidPart && ifluidPart.hasTank()) {
				IFluidTank tankAt = ifluidPart.getTank(0);
				FluidStack fluidAt = tankAt.drain(tankAt.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
				if (!fluidAt.isEmpty()) {
					// making this generic would be a rather large mess, unfortunately
					if (beTank != null && fluid.isEmpty()
						&& beTank instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank) {
						((CreativeFluidTankBlockEntity.CreativeSmartFluidTank) beTank)
							.setContainedFluid(fluidAt);
					}
					if (be instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank() && beTank != null) {
						beTank.fill(fluidAt, IFluidHandler.FluidAction.EXECUTE);
					}
				}
			}

			splitMultiAndInvalidate(part, cache);
			part.setController(origin);
			part.preventConnectivityUpdate();
			cache.put(pos, be);
			part.setHeight(size.height);
			part.setWidth(size.width);
			part.notifyMultiUpdated();
		});
		be.preventConnectivityUpdate();
		be.setWidth(size.width);
		be.setHeight(size.height);
		be.notifyMultiUpdated();
	}

	private static <T extends BlockEntity & IMultiBlockEntityContainer> Sizing getSizing (
		T be, SearchCache<T> cache
	) {
		Sizing bestFit = new Sizing(0, 0, 0);
		Level level = be.getLevel();
		if (level == null || !be.isController()) {
			return bestFit;
		}

		BlockEntityType<?> type = be.getType();
		BlockPos origin = be.getBlockPos();
		Direction.Axis axis = be.getMainConnectionAxis();

		// optional fluid handling
		FluidStack fluid = FluidStack.EMPTY;
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) {
			fluid = ifluid.getTank(0).getFluid();
		}

		for (int width = 1; width <= be.getMaxWidth(); width++) {
			int height = 0;

			int maxLength = be.getMaxLength(axis, width);

			Search:
			for (int yOffset = 0; yOffset < maxLength; yOffset++) {
				for (int xOffset = 0; xOffset < width; xOffset++) {
					for (int zOffset = 0; zOffset < width; zOffset++) {
						BlockPos pos = switch (axis) {
							case X -> origin.offset(yOffset, xOffset, zOffset);
							case Y -> origin.offset(xOffset, yOffset, zOffset);
							case Z -> origin.offset(xOffset, zOffset, yOffset);
						};

						Optional<T> part = cache.getOrCache(type, level, pos);
						if (part.isEmpty()) {
							break Search;
						}

						T controller = part.get();
						if(!sizedAlignedBounded(controller, origin, axis, width, maxLength)) {
							break Search;
						}

						if (controller instanceof IMultiBlockEntityContainer.Fluid ifluidCon && ifluidCon.hasTank()) {
							FluidStack otherFluid = ifluidCon.getFluid(0);
							if (!fluid.isEmpty() && !otherFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, otherFluid)) {
								break Search;
							}
						}
					}
				}
				height++;
			}

			Sizing size = new Sizing(width, height);
			if (size.volume < bestFit.volume)
				continue;
			bestFit = size;
		}

		return bestFit;
	}

	private static <T extends BlockEntity & IMultiBlockEntityContainer> boolean sizedAlignedBounded (
		T controller, BlockPos origin, Direction.Axis axis, int width, int maxLength
	) {
		BlockPos conPos = controller.getBlockPos();
		int otherWidth = controller.getWidth();
		//check size
		if (otherWidth > width) {
			return false;
		}
		if (otherWidth == width && controller.getHeight() == maxLength) {
			return false;
		}
		//check alignment
		if (axis != controller.getMainConnectionAxis()) {
			return false;
		}
		//check if within bounds
		if (conPos.equals(origin))
			return true; //return early if equivalent
		if (axis != Direction.Axis.Y) {
			if (conPos.getY() < origin.getY() ||
				conPos.getY() + otherWidth > origin.getY() + width) {
				return false;
			}
		}
		if (axis != Direction.Axis.X) {
			if (conPos.getX() < origin.getX() ||
				conPos.getX() + otherWidth > origin.getX() + width) {
				return false;
			}
		}
		if (axis != Direction.Axis.Z) {
			if (conPos.getZ() < origin.getZ() ||
				conPos.getZ() + otherWidth > origin.getZ() + width) {
				return false;
			}
		}
		return true;
	}

	public static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMulti(T be) {
		splitMultiAndInvalidate(be, null);
	}

	private static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMultiAndInvalidate(
		T be, @Nullable SearchCache<T> cache
	) {
		Level level = be.getLevel();
		if (level == null)
			return;

		T controller = be.getControllerBE();
		if (controller == null)
			return;

		int height = controller.getHeight();
		int width = controller.getWidth();
		if (width == 1 && height == 1)
			return;

		BlockPos origin = controller.getBlockPos();
		Direction.Axis axis = controller.getMainConnectionAxis();
		BlockEntityType<?> type = controller.getType();

		// fluid handling, if present
		FluidStack toDistribute;
		int maxCapacity;
		if (controller instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank()) {
			toDistribute = ifluidBE.getFluid(0);
			maxCapacity = ifluidBE.getTankSize(0);
			if (!toDistribute.isEmpty() && !controller.isRemoved())
				toDistribute.shrink(maxCapacity);
			ifluidBE.setTankSize(0, 1);
		} else {
			maxCapacity = 0;
			toDistribute = FluidStack.EMPTY;
		}

		forEachPosition(origin, axis, width, height, (pos) -> {
			T partAt = partAt(type, level, pos);
			if (partAt == null)
				return;
			if (!partAt.getController().equals(origin))
				return;

			T controllerBE = partAt.getControllerBE();
			partAt.setExtraData((controllerBE == null ? null : controllerBE.getExtraData()));
			partAt.removeController(true);

			if (!toDistribute.isEmpty() && partAt != be) {
				FluidStack copy = toDistribute.copy();
				IFluidTank tank =
					(partAt instanceof IMultiBlockEntityContainer.Fluid ifluidPart ? ifluidPart.getTank(0) : null);
				// making this generic would be a rather large mess, unfortunately
				if (tank instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank creativeTank) {
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
			if (cache != null)
				cache.put(pos, partAt);
		});

		if (be instanceof IMultiBlockEntityContainer.Inventory inv && inv.hasInventory()||
			be instanceof IMultiBlockEntityContainer.Fluid fluid && fluid.hasTank())
			level.invalidateCapabilities(be.getBlockPos());
	}

	@Nullable
	public static <T extends BlockEntity & IMultiBlockEntityContainer> T partAt(
		BlockEntityType<?> type, BlockGetter level, BlockPos pos
	) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be != null && be.getType() == type && !be.isRemoved())
			return checked(be);
		return null;
	}

	public static <T extends BlockEntity & IMultiBlockEntityContainer> boolean isConnected(
		BlockGetter level, BlockPos pos, BlockPos other
	) {
		T one = checked(level.getBlockEntity(pos));
		T two = checked(level.getBlockEntity(other));
		if (one == null || two == null)
			return false;
		return one.getController()
			.equals(two.getController());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity & IMultiBlockEntityContainer> T checked(BlockEntity be) {
		if (be instanceof IMultiBlockEntityContainer)
			return (T) be;
		return null;
	}

	private static void forEachPosition (
		BlockPos origin, Direction.Axis axis, int width, int height, Consumer<BlockPos> func
	) {
		for (int Y = 0; Y < height; Y++) { for (int X = 0; X < width; X++) { for (int Z = 0; Z < width; Z++) {
			BlockPos pos = switch (axis) {
				case X -> origin.offset(Y, X, Z);
				case Y -> origin.offset(X, Y, Z);
				case Z -> origin.offset(X, Z, Y);
			};

			func.accept(pos);
		} } }
	}

	private static class SearchCache<T extends BlockEntity & IMultiBlockEntityContainer> {
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

	private record Sizing(int width, int height, int volume) {
		public Sizing(int width, int height) {
			this(width, height, width*width*height);
		}
	}
}
