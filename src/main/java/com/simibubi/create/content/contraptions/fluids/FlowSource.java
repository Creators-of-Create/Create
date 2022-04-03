package com.simibubi.create.content.contraptions.fluids;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class FlowSource {

	BlockFace location;

	public FlowSource(BlockFace location) {
		this.location = location;
	}

	public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
		Storage<FluidVariant> tank = provideHandler();
		if (tank == null)
			return FluidStack.EMPTY;
		try (Transaction t = TransferUtil.getTransaction()) {
			ResourceAmount<FluidVariant> resource = StorageUtil.findExtractableContent(tank, v -> extractionPredicate.test(new FluidStack(v, 1)), t);
			return resource == null ? FluidStack.EMPTY : new FluidStack(resource.resource(), resource.amount());
		}
	}

	// Layer III. PFIs need active attention to prevent them from disengaging early
	public void keepAlive() {}

	public abstract boolean isEndpoint();

	public void manageSource(Level world) {}

	public void whileFlowPresent(Level world, boolean pulling) {}

	public Storage<FluidVariant> provideHandler() {
		return null;
	}

	public static class FluidHandler extends FlowSource {
		Storage<FluidVariant> fluidHandler;

		public FluidHandler(BlockFace location) {
			super(location);
			fluidHandler = null;
		}

		public void manageSource(Level world) {
			if (fluidHandler != null && world.getGameTime() % 20 != 0)
				return;
			BlockEntity tileEntity = world.getBlockEntity(location.getConnectedPos());
			if (tileEntity != null)
				fluidHandler = TransferUtil.getFluidStorage(tileEntity, location.getOppositeFace());
		}

		@Override
		public Storage<FluidVariant> provideHandler() {
			return fluidHandler;
		}

		@Override
		public boolean isEndpoint() {
			return true;
		}
	}

	public static class OtherPipe extends FlowSource {
		WeakReference<FluidTransportBehaviour> cached;

		public OtherPipe(BlockFace location) {
			super(location);
		}

		@Override
		public void manageSource(Level world) {
			if (cached != null && cached.get() != null && !cached.get().tileEntity.isRemoved())
				return;
			cached = null;
			FluidTransportBehaviour fluidTransportBehaviour =
				TileEntityBehaviour.get(world, location.getConnectedPos(), FluidTransportBehaviour.TYPE);
			if (fluidTransportBehaviour != null)
				cached = new WeakReference<>(fluidTransportBehaviour);
		}

		@Override
		public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
			if (cached == null || cached.get() == null)
				return FluidStack.EMPTY;
			FluidTransportBehaviour behaviour = cached.get();
			FluidStack providedOutwardFluid = behaviour.getProvidedOutwardFluid(location.getOppositeFace());
			return extractionPredicate.test(providedOutwardFluid) ? providedOutwardFluid : FluidStack.EMPTY;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

	public static class Blocked extends FlowSource {

		public Blocked(BlockFace location) {
			super(location);
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

}
