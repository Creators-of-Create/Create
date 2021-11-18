package com.simibubi.create.lib.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;

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
import com.simibubi.create.lib.utility.LazyOptional;

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

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class TransferUtil {
	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be) {
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, Direction.UP);
		return handleTypeChecks(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, Direction side) {
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
		return handleTypeChecks(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos) {
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, pos, Direction.UP);
		return handleTypeChecks(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos, Direction direction) {
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, pos, direction);
		return handleTypeChecks(itemStorage).cast();
	}

	// Fluids

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be) {
		Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, Direction.UP);
		return handleTypeChecks(fluidStorage).cast();
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, Direction side) {
		Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
		return handleTypeChecks(fluidStorage).cast();
	}

	// Fluid-containing items

	public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
		Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
		return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(stack, fluidStorage));
	}

	// Helpers

	public static LazyOptional<?> getHandler(BlockEntity be, Direction direction, Class<?> handler) {
		if (handler == IItemHandler.class) {
			return getItemHandler(be, direction);
		} else if (handler == IFluidHandler.class) {
			return getFluidHandler(be, direction);
		} else throw new RuntimeException("Handler class must be IItemHandler or IFluidHandler");
	}

	/**
	 * Returns either an IFluidHandler or an IItemHandler, wrapped in a LazyOptional.
	 */
	public static LazyOptional<?> handleTypeChecks(Storage<?> storage) {
		if (storage == null) return LazyOptional.empty();
		if (storage instanceof StorageItemHandler handler) {
			return LazyOptional.ofObject(handler.getHandler());
		} else if (storage instanceof StorageFluidHandler handler) {
			return LazyOptional.ofObject(handler.getHandler());
		} else {
			try {
				Storage<ItemVariant> itemStorage = ((Storage<ItemVariant>) storage);
				return LazyOptional.ofObject(new ItemStorageHandler(itemStorage));
			} catch (ClassCastException e) {
				try {
					Storage<FluidVariant> fluidStorage = ((Storage<FluidVariant>) storage);
					return LazyOptional.ofObject(new FluidStorageHandler(fluidStorage));
				} catch (ClassCastException ex) {
					throw new RuntimeException("Storage did not contain an item or fluid.", ex);
				}
			}
		}
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
		if (be instanceof ItemTransferable transferable) {
			IItemHandler handler = transferable.getItemHandler(side);
			return handler == null ? null : new StorageItemHandler(handler);
		}
		return null;
	}

	public static void registerStorages(boolean fluid, BlockEntityType<?>... type) {
		if (fluid) {
			FluidStorage.SIDED.registerForBlockEntities(TransferUtil::getFluidStorageForBE, type);
		} else {
			ItemStorage.SIDED.registerForBlockEntities(TransferUtil::getItemStorageForBE, type);
		}
	}
}
