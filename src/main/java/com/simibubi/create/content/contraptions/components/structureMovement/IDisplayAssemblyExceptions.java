package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<ITextComponent> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add(StringTextComponent.EMPTY);

		tooltip.add(IHaveGoggleInformation.componentSpacing.copy().append(Lang.translate("gui.assembly.exception").formatted(TextFormatting.GOLD)));
		String text = TooltipHelper.getUnformattedDeepText(e.component);
		Arrays.stream(text.split("\n")).forEach(l -> tooltip.add(IHaveGoggleInformation.componentSpacing.copy().append(new StringTextComponent(l).setStyle(e.component.getStyle()).formatted(TextFormatting.GRAY))));

		return true;
	}

	AssemblyException getLastAssemblyException();
}
