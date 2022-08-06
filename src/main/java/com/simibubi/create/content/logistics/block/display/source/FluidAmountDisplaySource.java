package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.FluidFormatter;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidAmountDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return EMPTY_LINE;

		TankManipulationBehaviour tankManipulationBehaviour = cote.getBehaviour(TankManipulationBehaviour.OBSERVE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		IFluidHandler handler = tankManipulationBehaviour.getInventory();

		if (handler == null)
			return EMPTY_LINE;

		long collected = 0;
		for (int i = 0; i < handler.getTanks(); i++) {
			FluidStack stack = handler.getFluidInTank(i);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;
			collected += stack.getAmount();
		}

		return Components.literal(FluidFormatter.asString(collected, false));
	}

	@Override
	protected String getTranslationKey() {
		return "fluid_amount";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
