package com.simibubi.create.content.logistics.block.display.source;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.redstone.SmartObserverBlockEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.FluidFormatter;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidListDisplaySource extends ValueListDisplaySource {


	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
			return Stream.empty();

		TankManipulationBehaviour tankManipulationBehaviour = cobe.getBehaviour(TankManipulationBehaviour.OBSERVE);
		FilteringBehaviour filteringBehaviour = cobe.getBehaviour(FilteringBehaviour.TYPE);
		IFluidHandler handler = tankManipulationBehaviour.getInventory();

		if (handler == null)
			return Stream.empty();


		Map<Fluid, Integer> fluids = new HashMap<>();
		Map<Fluid, FluidStack> fluidNames = new HashMap<>();

		for (int i = 0; i < handler.getTanks(); i++) {
			FluidStack stack = handler.getFluidInTank(i);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;

			fluids.merge(stack.getFluid(), stack.getAmount(), Integer::sum);
			fluidNames.putIfAbsent(stack.getFluid(), stack);
		}

		return fluids.entrySet()
				.stream()
				.sorted(Comparator.<Map.Entry<Fluid, Integer>>comparingInt(value -> value.getValue()).reversed())
				.limit(maxRows)
				.map(entry -> IntAttached.with(
						entry.getValue(),
						Components.translatable(fluidNames.get(entry.getKey()).getTranslationKey()))
				);
	}

	@Override
	protected List<MutableComponent> createComponentsFromEntry(DisplayLinkContext context, IntAttached<MutableComponent> entry) {
		int amount = entry.getFirst();
		MutableComponent name = entry.getSecond().append(WHITESPACE);

		Couple<MutableComponent> formatted = FluidFormatter.asComponents(amount, shortenNumbers(context));

		return List.of(formatted.getFirst(), formatted.getSecond(), name);
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
		Integer max = ((MutableInt) context.flapDisplayContext).getValue();
		boolean shorten = shortenNumbers(context);
		int length = FluidFormatter.asString(max, shorten).length();
		String layoutKey = "FluidList_" + length;

		if (layout.isLayout(layoutKey))
			return;

		int maxCharCount = flapDisplay.getMaxCharCount(1);
		int numberLength = Math.min(maxCharCount, Math.max(3, length - 2));
		int nameLength = Math.max(maxCharCount - numberLength - 2, 0);

		FlapDisplaySection value = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * numberLength, "number", false, false).rightAligned();
		FlapDisplaySection unit = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * 2, "fluid_units", true, true);
		FlapDisplaySection name = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * nameLength, "alphabet", false, false);

		layout.configure(layoutKey, List.of(value, unit, name));
	}

	@Override
	protected String getTranslationKey() {
		return "list_fluids";
	}

	@Override
	protected boolean valueFirst() {
		return false;
	}
}
