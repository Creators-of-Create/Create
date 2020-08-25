package com.simibubi.create.content.contraptions.fluids;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OpenEndedPipe {

	World world;

	private OpenEndFluidHandler fluidHandler;
	private BlockPos outputPos;
	private boolean wasPulling;
	private boolean stale;

	public OpenEndedPipe(BlockFace face) {
		fluidHandler = new OpenEndFluidHandler();
		outputPos = face.getConnectedPos();
	}

	public void tick(World world, boolean pulling) {
		this.world = world;
		if (!world.isAreaLoaded(outputPos, 0))
			return;
		if (pulling != wasPulling) {
			if (pulling)
				fluidHandler.clear();
			wasPulling = pulling;
		}

		BlockState state = world.getBlockState(outputPos);
		IFluidState fluidState = state.getFluidState();
		boolean waterlog = state.has(BlockStateProperties.WATERLOGGED);

		if (!waterlog && !state.getMaterial()
			.isReplaceable())
			return;

		// TODO different pipe end types
		if (pulling) {
			if (fluidState.isEmpty() || !fluidState.isSource())
				return;
			if (!fluidHandler.tryCollectFluid(fluidState.getFluid()))
				return;
			if (waterlog) {
				world.setBlockState(outputPos, state.with(BlockStateProperties.WATERLOGGED, false), 3);
				return;
			}
			world.setBlockState(outputPos, fluidState.getBlockState()
				.with(FlowingFluidBlock.LEVEL, 14), 3);
			return;
		}

		Fluid providedFluid = fluidHandler.tryProvidingFluid();
		if (providedFluid == null)
			return;
		if (!fluidState.isEmpty() && fluidState.getFluid() != providedFluid) {
			FluidReactions.handlePipeSpillCollision(world, outputPos, providedFluid, fluidState);
			return;
		}
		if (fluidState.isSource())
			return;
		if (waterlog) {
			if (providedFluid.getFluid() != Fluids.WATER)
				return;
			world.setBlockState(outputPos, state.with(BlockStateProperties.WATERLOGGED, true), 3);
			return;
		}
		world.setBlockState(outputPos, providedFluid.getDefaultState()
			.getBlockState(), 3);
	}

	public LazyOptional<IFluidHandler> getCapability() {
		return LazyOptional.of(() -> fluidHandler);
	}

	public CompoundNBT writeToNBT(CompoundNBT compound) {
		fluidHandler.writeToNBT(compound);
		compound.putBoolean("Pulling", wasPulling);
		return compound;
	}

	public void readNBT(CompoundNBT compound) {
		fluidHandler.readFromNBT(compound);
		wasPulling = compound.getBoolean("Pulling");
	}

	public void markStale() {
		stale = true;
	}

	public void unmarkStale() {
		stale = false;
	}

	public boolean isStale() {
		return stale;
	}

	private class OpenEndFluidHandler extends FluidTank {

		public OpenEndFluidHandler() {
			super(1500);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			// Never allow being filled when a source is attached
			if (world == null)
				return 0;
			if (!world.isAreaLoaded(outputPos, 0))
				return 0;
			if (resource.isEmpty())
				return 0;
			BlockState state = world.getBlockState(outputPos);
			IFluidState fluidState = state.getFluidState();
			if (!fluidState.isEmpty() && fluidState.getFluid() != resource.getFluid()) {
				FluidReactions.handlePipeSpillCollision(world, outputPos, resource.getFluid(), fluidState);
				return 0;
			}
			if (fluidState.isSource())
				return 0;
			if (!(state.has(BlockStateProperties.WATERLOGGED) && resource.getFluid() == Fluids.WATER)
				&& !state.getMaterial()
					.isReplaceable())
				return 0;

			// Never allow being filled above 1000
			FluidStack insertable = resource.copy();
			insertable.setAmount(Math.min(insertable.getAmount(), Math.max(1000 - getFluidAmount(), 0)));
			return super.fill(insertable, action);
		}

		public boolean tryCollectFluid(Fluid fluid) {
			for (boolean simulate : Iterate.trueAndFalse)
				if (super.fill(new FluidStack(fluid, 1000),
					simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE) != 1000)
					return false;
			return true;
		}

		@Nullable
		public Fluid tryProvidingFluid() {
			Fluid fluid = getFluid().getFluid();
			for (boolean simulate : Iterate.trueAndFalse)
				if (drain(1000, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount() != 1000)
					return null;
			return fluid;
		}

		public void clear() {
			setFluid(FluidStack.EMPTY);
		}

	}

}
