package com.simibubi.create.content.fluids;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.math.BlockFace;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public abstract class FlowSource {

	private static final ICapabilityProvider<IFluidHandler> EMPTY = null;

	BlockFace location;

	public FlowSource(BlockFace location) {
		this.location = location;
	}

	public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
		@Nullable ICapabilityProvider<IFluidHandler> tankCache = provideHandler();
		if (tankCache == null)
			return FluidStack.EMPTY;
		IFluidHandler tank = tankCache.getCapability();
		if (tank == null)
			return FluidStack.EMPTY;
		FluidStack immediateFluid = tank.drain(1, FluidAction.SIMULATE);
		if (extractionPredicate.test(immediateFluid))
			return immediateFluid;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack contained = tank.getFluidInTank(i);
			if (contained.isEmpty())
				continue;
			if (!extractionPredicate.test(contained))
				continue;
			FluidStack toExtract = contained.copy();
			toExtract.setAmount(1);
			return tank.drain(toExtract, FluidAction.SIMULATE);
		}

		return FluidStack.EMPTY;
	}

	// Layer III. PFIs need active attention to prevent them from disengaging early
	public void keepAlive() {}

	public abstract boolean isEndpoint();

	public void manageSource(Level world, BlockEntity networkBE) {
	}

	public void whileFlowPresent(Level world, boolean pulling) {}

	public @Nullable ICapabilityProvider<IFluidHandler> provideHandler() {
		return EMPTY;
	}

	public static class FluidHandler extends FlowSource {
		@Nullable
		ICapabilityProvider<IFluidHandler> fluidHandlerCache;

		public FluidHandler(BlockFace location) {
			super(location);
			fluidHandlerCache = EMPTY;
		}

		public void manageSource(Level level, BlockEntity networkBE) {
			if (fluidHandlerCache == null) {
				BlockEntity blockEntity = level.getBlockEntity(location.getConnectedPos());
				if (blockEntity != null) {
					if (level instanceof ServerLevel serverLevel) {
						fluidHandlerCache = ICapabilityProvider.of(BlockCapabilityCache.create(
							Capabilities.FluidHandler.BLOCK,
							serverLevel,
							blockEntity.getBlockPos(),
							location.getOppositeFace(),
							() -> !networkBE.isRemoved(),
							() -> fluidHandlerCache = EMPTY
						));
					} else if (level instanceof PonderLevel) {
						fluidHandlerCache = ICapabilityProvider.of(() -> level.getCapability(
							Capabilities.FluidHandler.BLOCK,
							blockEntity.getBlockPos(),
							location.getOppositeFace()
						));
					}
				}
			}
		}

		@Override
		@Nullable
		public ICapabilityProvider<IFluidHandler> provideHandler() {
			return fluidHandlerCache;
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
		public void manageSource(Level world, BlockEntity networkBE) {
			if (cached != null && cached.get() != null && !cached.get().blockEntity.isRemoved())
				return;
			cached = null;
			FluidTransportBehaviour fluidTransportBehaviour =
				BlockEntityBehaviour.get(world, location.getConnectedPos(), FluidTransportBehaviour.TYPE);
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
