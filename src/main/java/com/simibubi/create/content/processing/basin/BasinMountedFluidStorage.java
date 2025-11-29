package com.simibubi.create.content.processing.basin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.fluids.FluidStack;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BasinMountedFluidStorage extends MountedFluidStorage implements SyncedMountedStorage {
	public static final MapCodec<BasinMountedFluidStorage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		MountedBasinTankHalf.CODEC.fieldOf("inputTank").forGetter(BasinMountedFluidStorage::inputTank),
		MountedBasinTankHalf.CODEC.fieldOf("outputTank").forGetter(BasinMountedFluidStorage::outputTank)
	).apply(i, BasinMountedFluidStorage::new));

	protected BasinMountedFluidStorage(MountedBasinTankHalf input, MountedBasinTankHalf output) {
		super(AllMountedStorageTypes.BASIN_FLUID.get());

		this.inputTankHalf = input;
		this.inputTankHalf.setUpdateCallback(this::onFluidStackChange);

		this.outputTankHalf = output;
		this.outputTankHalf.setUpdateCallback(this::onFluidStackChange);

		this.bothTanks = new CombinedTankWrapper(input, output);
	}

	final MountedBasinTankHalf inputTankHalf;
	final MountedBasinTankHalf outputTankHalf;
	final CombinedTankWrapper bothTanks;

	private boolean dirty;

	public MountedBasinTankHalf inputTank() {
		return this.inputTankHalf;
	}

	public MountedBasinTankHalf outputTank() {
		return this.outputTankHalf;
	}

	@Override
	public boolean isDirty() { return this.dirty; }

	@Override
	public void markClean() { this.dirty = false; }

	@Override
	public void afterSync(Contraption contraption, BlockPos localPos) {}

	public void onFluidStackChange(FluidStack stack) {
		this.dirty = true;
	}

	public void tickChasers() {
		this.inputTankHalf.tickChasers();
		this.outputTankHalf.tickChasers();
	}

	public float getTotalFluidUnits(float partialTicks) {
		float fluidUnits = this.inputTankHalf.getTotalUnits(partialTicks) +
						   this.outputTankHalf.getTotalUnits(partialTicks);
		int totalRendered = this.inputTankHalf.getRenderedFluids() +
							this.outputTankHalf.getRenderedFluids();

		if (totalRendered == 0) return 0;
		if (fluidUnits < 1) return 0;
		return fluidUnits;
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if(be instanceof BasinBlockEntity basin) {
			basin.applyTanks(this.inputTankHalf, this.outputTankHalf);
		}
	}

	@Override
	public int getTanks() {
		return this.bothTanks.getTanks();
	}

	@Override
	@NotNull
	public FluidStack getFluidInTank(int tank) {
		return this.bothTanks.getFluidInTank(tank);
	}

	@Override
	public int getTankCapacity(int tank) {
		return 1000;
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
		return this.bothTanks.isFluidValid(tank, fluidStack);
	}

	@Override
	public int fill(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction) {
		return this.inputTankHalf.fill(fluidStack, fluidAction);
	}

	@Override
	@NotNull
	public FluidStack drain(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction) {
		return this.bothTanks.drain(fluidStack, fluidAction);
	}

	@Override
	@NotNull
	public FluidStack drain(int tank, @NotNull FluidAction fluidAction) {
		return this.bothTanks.drain(tank, fluidAction);
	}

	public static class MountedBasinTankHalf extends CombinedTankWrapper {
		public static final MapCodec<MountedBasinTankHalf> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.fieldOf("insertionAllowed").forGetter(MountedBasinTankHalf::insertionAllowed),
			FluidStack.OPTIONAL_CODEC.fieldOf("firstTank").forGetter(MountedBasinTankHalf::firstStack),
			FluidStack.OPTIONAL_CODEC.fieldOf("secondTank").forGetter(MountedBasinTankHalf::secondStack),
			Codec.FLOAT.fieldOf("optionalFirst").forGetter(t -> t.firstLevel.getValue(1)),
			Codec.FLOAT.fieldOf("optionalSecond").forGetter(t -> t.secondLevel.getValue(1))
		).apply(i, MountedBasinTankHalf::fromStacks));

 		private final boolean insertionAllowed;
		private final LerpedFloat firstLevel;
		private final LerpedFloat secondLevel;
		private Consumer<FluidStack> updateCallback;

		public MountedBasinTankHalf(boolean insertionAllowed, IFluidHandler firstTank, IFluidHandler secondTank) {
			super(firstTank, secondTank);

			firstLevel = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .25, Chaser.EXP);

			secondLevel = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .25, Chaser.EXP);

			this.insertionAllowed = insertionAllowed;
			enforceVariety();
		}

		public static MountedBasinTankHalf fromStacks(boolean insertionAllowed, FluidStack first, FluidStack second, float previousFirst, float previousSecond) {
			SmartFluidTank firstTank = new SmartFluidTank(getTankCapacity(), s -> {});
			SmartFluidTank secondTank = new SmartFluidTank(getTankCapacity(), s -> {});

			MountedBasinTankHalf tankHalf = new MountedBasinTankHalf(insertionAllowed, firstTank, secondTank);
			tankHalf.fillTank(0, first);
			tankHalf.fillTank(1, second);

			tankHalf.firstLevel.setValue(previousFirst);
 			tankHalf.firstLevel.chase(tankHalf.firstStack().getAmount() / (float) getTankCapacity(), .25, Chaser.EXP);

			tankHalf.secondLevel.setValue(previousSecond);
			tankHalf.secondLevel.chase(tankHalf.secondStack().getAmount() / (float) getTankCapacity(), .25, Chaser.EXP);

			firstTank.setUpdateCallback(tankHalf::onFluidStackChanged);
			secondTank.setUpdateCallback(tankHalf::onFluidStackChanged);

			return tankHalf;
		}

		public void onFluidStackChanged(FluidStack stack) {
			firstLevel.chase(firstStack().getAmount() / (float) getTankCapacity(), .25, Chaser.EXP);
			secondLevel.chase(secondStack().getAmount() / (float) getTankCapacity(), .25, Chaser.EXP);

			if(this.updateCallback != null) this.updateCallback.accept(stack);
		}

		public void tickChasers() {
			firstLevel.tickChaser();
			secondLevel.tickChaser();
		}

		public int getRenderedFluids() {
			int total = 0;
			for(int i = 0; i < this.getTanks(); i++) {
				if(!this.getFluidInTank(i).isEmpty()) total++;
			}

			return total;
		}

		public FluidStack firstStack() {
			return this.getFluidInTank(0);
		}

		public FluidStack secondStack() {
			return this.getFluidInTank(1);
		}

		public float getTotalUnits(float partialTicks, int tank) {
			if(tank == 0) return firstLevel.getValue(partialTicks) * 1000;
			else return secondLevel.getValue(partialTicks) * 1000;
		}

		public float getTotalUnits(float partialTicks) {
			return firstLevel.getValue(partialTicks) * 1000 +
				  secondLevel.getValue(partialTicks) * 1000;
		}

		public void setUpdateCallback(Consumer<FluidStack> updateCallback) {
			this.updateCallback = updateCallback;
		}

		public static int getTankCapacity() {
			return 1000;
		}

		public boolean insertionAllowed() {
			return insertionAllowed;
		}

		private void fillTank(int tank, FluidStack stack) {
			this.getHandlerFromIndex(tank).fill(stack, FluidAction.EXECUTE);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!insertionAllowed)
				return 0;
			return super.fill(resource, action);
		}
	}
}
