package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.FluidTank;
import com.simibubi.create.lib.transfer.fluid.FluidTransferable;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

import com.simibubi.create.lib.utility.LazyOptional;

import org.jetbrains.annotations.Nullable;

public class PortableFluidInterfaceTileEntity extends PortableStorageInterfaceTileEntity implements FluidTransferable {

	protected LazyOptional<IFluidHandler> capability;

	public PortableFluidInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		LazyOptional<IFluidHandler> oldcap = capability;
		//capability = LazyOptional.of(() -> new InterfaceFluidHandler(contraption.fluidInventory));
		capability = LazyOptional.empty();
		oldcap.invalidate();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void invalidateCapability() {
		capability.invalidate();
	}

	@Override
	protected void stopTransferring() {
		LazyOptional<IFluidHandler> oldcap = capability;
		capability = createEmptyHandler();
		oldcap.invalidate();
		super.stopTransferring();
	}

	private LazyOptional<IFluidHandler> createEmptyHandler() {
		return LazyOptional.of(() -> new InterfaceFluidHandler(new FluidTank(0)));
	}

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(@Nullable Direction direction) {
		return capability.getValueUnsafer();
	}

//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
//		if (isFluidHandlerCap(cap))
//			return capability.cast();
//		return super.getCapability(cap, side);
//	}

	public class InterfaceFluidHandler implements IFluidHandler {

		private IFluidHandler wrapped;

		public InterfaceFluidHandler(IFluidHandler wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public int getTanks() {
			return wrapped.getTanks();
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return wrapped.getFluidInTank(tank);
		}

		@Override
		public long getTankCapacity(int tank) {
			return wrapped.getTankCapacity(tank);
		}

//		@Override
//		public boolean isFluidValid(int tank, FluidStack stack) {
//			return wrapped.isFluidValid(tank, stack);
//		}

		@Override
		public long fill(FluidStack resource, boolean sim) {
			if (!isConnected())
				return 0;
			long fill = wrapped.fill(resource, sim);
			if (fill > 0 && !sim)
				keepAlive();
			return fill;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean sim) {
			if (!canTransfer())
				return FluidStack.empty();
			FluidStack drain = wrapped.drain(resource, sim);
			if (!drain.isEmpty() && !sim)
				keepAlive();
			return drain;
		}

		@Override
		public FluidStack drain(long maxDrain, boolean sim) {
			if (!canTransfer())
				return FluidStack.empty();
			FluidStack drain = wrapped.drain(maxDrain, sim);
			if (!drain.isEmpty() && !sim)
				keepAlive();
			return drain;
		}

		public void keepAlive() {
			onContentTransferred();
		}

	}

}
