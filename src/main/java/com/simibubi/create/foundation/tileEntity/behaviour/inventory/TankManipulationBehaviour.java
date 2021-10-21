package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TankManipulationBehaviour extends CapManipulationBehaviourBase<IFluidHandler, TankManipulationBehaviour> {

	public static BehaviourType<TankManipulationBehaviour> OBSERVE = new BehaviourType<>();
	private BehaviourType<TankManipulationBehaviour> behaviourType;

	public TankManipulationBehaviour(SmartTileEntity te, InterfaceProvider target) {
		this(OBSERVE, te, target);
	}

	private TankManipulationBehaviour(BehaviourType<TankManipulationBehaviour> type, SmartTileEntity te,
		InterfaceProvider target) {
		super(te, target);
		behaviourType = type;
	}

	public FluidStack extractAny() {
		if (!hasInventory())
			return FluidStack.EMPTY;
		IFluidHandler inventory = getInventory();
		Predicate<FluidStack> filterTest = getFilterTest(Predicates.alwaysTrue());
		for (int i = 0; i < inventory.getTanks(); i++) {
			FluidStack fluidInTank = inventory.getFluidInTank(i);
			if (fluidInTank.isEmpty())
				continue;
			if (!filterTest.test(fluidInTank))
				continue;
			FluidStack drained =
				inventory.drain(fluidInTank, simulateNext ? FluidAction.SIMULATE : FluidAction.EXECUTE);
			if (!drained.isEmpty())
				return drained;
		}

		return FluidStack.EMPTY;
	}

	protected Predicate<FluidStack> getFilterTest(Predicate<FluidStack> customFilter) {
		Predicate<FluidStack> test = customFilter;
		FilteringBehaviour filter = tileEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	protected Capability<IFluidHandler> capability() {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

}
