package com.simibubi.create.content.contraptions.components.structureMovement;

import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.WHITE;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<ITextComponent> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add(StringTextComponent.EMPTY);

		tooltip.add(IHaveGoggleInformation.componentSpacing.copy().append(Lang.translate("gui.assembly.exception").formatted(TextFormatting.GOLD)));

		String text = TooltipHelper.getUnformattedDeepText(e.component);
		Arrays.stream(text.split("\n"))
				.forEach(l -> TooltipHelper.cutStringTextComponent(l, GRAY, WHITE)
						.forEach(c -> tooltip.add(IHaveGoggleInformation.componentSpacing.copy().append(c))));

		return true;
	}

	AssemblyException getLastAssemblyException();
}
