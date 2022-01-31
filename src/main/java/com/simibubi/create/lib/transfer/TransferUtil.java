package com.simibubi.create.lib.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.lib.transfer.fluid.FluidStorageHandler;
import com.simibubi.create.lib.transfer.fluid.FluidStorageHandlerItem;
import com.simibubi.create.lib.transfer.fluid.FluidTransferable;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;
import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;
import com.simibubi.create.lib.transfer.fluid.StorageFluidHandler;
import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.ItemStorageHandler;
import com.simibubi.create.lib.transfer.item.ItemTransferable;
import com.simibubi.create.lib.transfer.item.StorageItemHandler;
import com.simibubi.create.lib.util.FluidTileDataHandler;
import com.simibubi.create.lib.util.LazyOptional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings({"UnstableApiUsage"})
public class TransferUtil {
	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be) {
		return getItemHandler(be, null);
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos) {
		return getItemHandler(level, pos, null);
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos, @Nullable Direction direction) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) return LazyOptional.empty();
		return getItemHandler(be, direction);
	}

	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, @Nullable Direction side) {
		// Create handling
		if (be instanceof ItemTransferable transferable) return LazyOptional.ofObject(transferable.getItemHandler(side));
		// client handling
		if (Objects.requireNonNull(be.getLevel()).isClientSide()) {
			return LazyOptional.empty();
		}
		// external handling
		List<Storage<ItemVariant>> itemStorages = new ArrayList<>();
		Level l = be.getLevel();
		BlockPos pos = be.getBlockPos();
		BlockState state = be.getBlockState();

		for (Direction direction : getDirections(side)) {
			Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(l, pos, state, be, direction);

			if (itemStorage != null) {
				if (itemStorages.size() == 0) {
					itemStorages.add(itemStorage);
					continue;
				}

				for (Storage<ItemVariant> storage : itemStorages) {
					if (!storage.equals(itemStorage)) {
						itemStorages.add(itemStorage);
						break;
					}
				}
			}
		}

		if (itemStorages.isEmpty()) return LazyOptional.empty();
		if (itemStorages.size() == 1) return simplifyItem(itemStorages.get(0)).cast();
		return simplifyItem(new CombinedStorage<>(itemStorages)).cast();
	}

	// Fluids

	public static LazyOptional<IFluidHandler> getFluidHandler(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) return LazyOptional.empty();
		return getFluidHandler(be);
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be) {
		return getFluidHandler(be, null);
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, @Nullable Direction side) {
		// Create handling
		if (be instanceof FluidTransferable transferable) return LazyOptional.ofObject(transferable.getFluidHandler(side));
		// client handling
		if (Objects.requireNonNull(be.getLevel()).isClientSide()) {
			IFluidHandler cached = FluidTileDataHandler.getCachedHandler(be);
			return LazyOptional.ofObject(cached);
		}
		// external handling
		List<Storage<FluidVariant>> fluidStorages = new ArrayList<>();
		Level l = be.getLevel();
		BlockPos pos = be.getBlockPos();
		BlockState state = be.getBlockState();

		for (Direction direction : getDirections(side)) {
			Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(l, pos, state, be, direction);

			if (fluidStorage != null) {
				if (fluidStorages.size() == 0) {
					fluidStorages.add(fluidStorage);
					continue;
				}

				for (Storage<FluidVariant> storage : fluidStorages) {
					if (!storage.equals(fluidStorage)) {
						fluidStorages.add(fluidStorage);
						break;
					}
				}
			}
		}

		if (fluidStorages.isEmpty()) return LazyOptional.empty();
		if (fluidStorages.size() == 1) return simplifyFluid(fluidStorages.get(0)).cast();
		return simplifyFluid(new CombinedStorage<>(fluidStorages)).cast();
	}

	// Fluid-containing items

	public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return LazyOptional.empty();
		ContainerItemContext ctx = ContainerItemContext.withInitial(stack);
		Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ctx);
		return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(ctx, fluidStorage));
	}

	// Helpers

	public static LazyOptional<?> getHandler(BlockEntity be, @Nullable Direction direction, Class<?> handler) {
		if (handler == IItemHandler.class) {
			return getItemHandler(be, direction);
		} else if (handler == IFluidHandler.class) {
			return getFluidHandler(be, direction);
		} else throw new RuntimeException("Handler class must be IItemHandler or IFluidHandler");
	}

	public static LazyOptional<IItemHandler> simplifyItem(Storage<ItemVariant> storage) {
		if (storage == null) return LazyOptional.empty();
		if (storage instanceof StorageItemHandler handler) return LazyOptional.ofObject(handler.getHandler());
		return LazyOptional.ofObject(new ItemStorageHandler(storage));
	}

	public static LazyOptional<IFluidHandler> simplifyFluid(Storage<FluidVariant> storage) {
		if (storage == null) return LazyOptional.empty();
		if (storage instanceof StorageFluidHandler handler) return LazyOptional.ofObject(handler.getHandler());
		return LazyOptional.ofObject(new FluidStorageHandler(storage));
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorageForBE(BlockEntity be, Direction side) {
		if (be instanceof FluidTransferable transferable) {
			IFluidHandler handler = transferable.getFluidHandler(side);
			return handler == null ? null : new StorageFluidHandler(handler);
		}
		return null;
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorageForBE(BlockEntity be, Direction side) {
		if (be instanceof ChuteTileEntity && side == Direction.DOWN) return null; // special case for #114
		if (be instanceof ItemTransferable transferable) {
			IItemHandler handler = transferable.getItemHandler(side);
			return handler == null ? null : new StorageItemHandler(handler);
		}
		return null;
	}

	private static Direction[] getDirections(@Nullable Direction direction) {
		if (direction == null) return Direction.values();
		return new Direction[] {direction};
	}

	public static void registerFluidStorage(BlockEntityType<?> type) {
		FluidStorage.SIDED.registerForBlockEntities(TransferUtil::getFluidStorageForBE, type);
	}

	public static void registerItemStorage(BlockEntityType<?> type) {
		ItemStorage.SIDED.registerForBlockEntities(TransferUtil::getItemStorageForBE, type);
	}
}
