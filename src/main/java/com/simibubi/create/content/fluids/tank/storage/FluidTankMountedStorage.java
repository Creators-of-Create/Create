package com.simibubi.create.content.fluids.tank.storage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage.Handler;

import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidTankMountedStorage extends WrapperMountedFluidStorage<Handler> implements SyncedMountedStorage {
	public static final MapCodec<FluidTankMountedStorage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FluidTankMountedStorage::getCapacity),
		FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FluidTankMountedStorage::getFluid)
	).apply(i, FluidTankMountedStorage::new));

	private boolean dirty;

	protected FluidTankMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
		super(type, new Handler(capacity, stack));
		this.wrapped.onChange = () -> this.dirty = true;
	}

	protected FluidTankMountedStorage(int capacity, FluidStack stack) {
		this(AllMountedStorageTypes.FLUID_TANK.get(), capacity, stack);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof FluidTankBlockEntity tank && tank.isController()) {
			FluidTank inventory = tank.getTankInventory();
			// capacity shouldn't change, leave it
			inventory.setFluid(this.wrapped.getFluid());
		}
	}

	public FluidStack getFluid() {
		return this.wrapped.getFluid();
	}

	public int getCapacity() {
		return this.wrapped.getCapacity();
	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	public void markClean() {
		this.dirty = false;
	}

	@Override
	public void afterSync(Contraption contraption, BlockPos localPos) {
		BlockEntity be = contraption.getBlockEntityClientSide(localPos);
		if (!(be instanceof FluidTankBlockEntity tank))
			return;

		FluidTank inv = tank.getTankInventory();
		inv.setFluid(this.getFluid());
		float fillLevel = inv.getFluidAmount() / (float) inv.getCapacity();
		if (tank.getFluidLevel() == null) {
			tank.setFluidLevel(LerpedFloat.linear().startWithValue(fillLevel));
		}
		tank.getFluidLevel().chase(fillLevel, 0.5, LerpedFloat.Chaser.EXP);
	}

	public static FluidTankMountedStorage fromTank(FluidTankBlockEntity tank) {
		// tank has update callbacks, make an isolated copy
		FluidTank inventory = tank.getTankInventory();
		return new FluidTankMountedStorage(inventory.getCapacity(), inventory.getFluid().copy());
	}

	public static FluidTankMountedStorage fromLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
		int capacity = nbt.getInt("Capacity");
		FluidStack fluid = FluidStack.parseOptional(registries, nbt);
		return new FluidTankMountedStorage(capacity, fluid);
	}

	public static final class Handler extends FluidTank {
		private Runnable onChange = () -> {
		};

		public Handler(int capacity, FluidStack stack) {
			super(capacity);
			this.setFluid(stack);
		}

		@Override
		protected void onContentsChanged() {
			this.onChange.run();
		}
	}
}
