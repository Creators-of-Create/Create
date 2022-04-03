package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class TankManipulationBehaviour extends CapManipulationBehaviourBase<FluidVariant, TankManipulationBehaviour> {

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
		Storage<FluidVariant> inventory = getInventory();
		Predicate<FluidStack> filterTest = getFilterTest(Predicates.alwaysTrue());

		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<FluidVariant> view : inventory.iterable(t)) {
				if (!view.isResourceBlank()) {
					FluidStack stack = new FluidStack(view);
					if (!filterTest.test(stack))
						continue;
					long extracted = view.extract(view.getResource(), view.getAmount(), t);
					if (extracted != 0) {
						if (!simulateNext) t.commit();
						return stack.setAmount(extracted);
					}
				}
			}
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
	protected Class<FluidVariant> capability() {
		return FluidVariant.class;
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

}
