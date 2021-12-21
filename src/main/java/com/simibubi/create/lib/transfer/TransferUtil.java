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
import com.simibubi.create.lib.util.LazyOptional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings({"UnstableApiUsage"})
public class TransferUtil {
	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be) {
		if (Objects.requireNonNull(be.getLevel()).isClientSide) return LazyOptional.empty();
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, Direction.UP);
		return simplifyItem(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, Direction side) {
		if (Objects.requireNonNull(be.getLevel()).isClientSide) return LazyOptional.empty();
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
		return simplifyItem(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos) {
		if (level.isClientSide) return LazyOptional.empty();
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, pos, Direction.UP);
		return simplifyItem(itemStorage).cast();
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos, Direction direction) {
		if(level.isClientSide) return LazyOptional.empty();
		Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, pos, direction);
		return simplifyItem(itemStorage).cast();
	}

	// Fluids

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be) {
		if (Objects.requireNonNull(be.getLevel()).isClientSide) return LazyOptional.empty();
		Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, Direction.UP);
		return simplifyFluid(fluidStorage).cast();
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, Direction side) {
		if (Objects.requireNonNull(be.getLevel()).isClientSide) return LazyOptional.empty();
		Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
		return simplifyFluid(fluidStorage).cast();
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side) {
		if (level.isClientSide) return LazyOptional.empty();
		Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(level, pos, side);
		return simplifyFluid(fluidStorage).cast();
	}

	// Fluid-containing items

	public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return LazyOptional.empty();
		Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
		return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(stack, fluidStorage));
	}

	// Helpers

	public static LazyOptional<?> getHandler(BlockEntity be, Direction direction, Class<?> handler) {
		if(Objects.requireNonNull(be.getLevel()).isClientSide) return LazyOptional.empty();
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
