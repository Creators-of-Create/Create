package com.simibubi.create.content.contraptions;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.FontHelper.Palette;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<Component> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add(Components.immutableEmpty());

		tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy()
			.append(CreateLang.translateDirect("gui.assembly.exception")
				.withStyle(ChatFormatting.GOLD)));

		String text = e.component.getString();
		Arrays.stream(text.split("\n"))
			.forEach(l -> TooltipHelper.cutStringTextComponent(l, Palette.GRAY_AND_WHITE)
				.forEach(c -> tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy()
					.append(c))));

		return true;
	}

	AssemblyException getLastAssemblyException();

}
