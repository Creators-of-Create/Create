package com.simibubi.create.content.equipment.goggles;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/*
* Implement this Interface in the BlockEntity class that wants to add info to the screen
* */
public interface IHaveGoggleInformation {

	/**
	 * Use Lang.[...].forGoggles(list)
	 */
	String spacing = "    ";

	/**
	 * Use Lang.[...].forGoggles(list)
	 */
	@Deprecated
	Component componentSpacing = Components.literal(spacing);

	/**
	 * this method will be called when looking at a BlockEntity that implemented this
	 * interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be
	 *         displayed, or {@code false} if the overlay should not be displayed
	 */
	default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	default boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking,
		LazyOptional<IFluidHandler> handler) {
		Optional<IFluidHandler> resolve = handler.resolve();
		if (!resolve.isPresent())
			return false;

		IFluidHandler tank = resolve.get();
		if (tank.getTanks() == 0)
			return false;

		LangBuilder mb = Lang.translate("generic.unit.millibuckets");
		Lang.translate("gui.goggles.fluid_container")
			.forGoggles(tooltip);

		boolean isEmpty = true;
		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluidStack = tank.getFluidInTank(i);
			if (fluidStack.isEmpty())
				continue;

			Lang.fluidName(fluidStack)
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip, 1);

			Lang.builder()
				.add(Lang.number(fluidStack.getAmount())
					.add(mb)
					.style(ChatFormatting.GOLD))
				.text(ChatFormatting.GRAY, " / ")
				.add(Lang.number(tank.getTankCapacity(i))
					.add(mb)
					.style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 1);

			isEmpty = false;
		}

		if (tank.getTanks() > 1) {
			if (isEmpty)
				tooltip.remove(tooltip.size() - 1);
			return true;
		}

		if (!isEmpty)
			return true;

		Lang.translate("gui.goggles.fluid_container.capacity")
			.add(Lang.number(tank.getTankCapacity(0))
				.add(mb)
				.style(ChatFormatting.GOLD))
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip, 1);

		return true;
	}

}
