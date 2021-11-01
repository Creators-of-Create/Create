package com.simibubi.create.content.contraptions.components.structureMovement;

import static net.minecraft.util.text.TextFormatting.GRAY;
import staticnet.minecraft.ChatFormattingg.WHITE;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.text.TextFormatting;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<Component> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add(TextComponent.EMPTY);

		tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy().append(Lang.translate("gui.assembly.exception").withStyle(ChatFormatting.GOLD)));

		String text = e.component.getString();
		Arrays.stream(text.split("\n"))
				.forEach(l -> TooltipHelper.cutStringTextComponent(l, GRAY, WHITE)
						.forEach(c -> tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy().append(c))));

		return true;
	}

	AssemblyException getLastAssemblyException();

}
