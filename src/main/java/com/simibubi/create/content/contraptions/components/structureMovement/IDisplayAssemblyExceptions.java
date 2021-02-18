package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.text.TextFormatting;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<String> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add("");

		tooltip.add(IHaveGoggleInformation.spacing + TextFormatting.GOLD + Lang.translate("gui.assembly.exception"));
		String text = e.getFormattedText();
		Arrays.stream(text.split("\n")).forEach(l -> tooltip.add(IHaveGoggleInformation.spacing + TextFormatting.GRAY + l));

		return true;
	}

	AssemblyException getLastAssemblyException();
}
