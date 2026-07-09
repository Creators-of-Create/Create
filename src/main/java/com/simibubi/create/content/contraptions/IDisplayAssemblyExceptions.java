package com.simibubi.create.content.contraptions;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public interface IDisplayAssemblyExceptions {

	default boolean addExceptionToTooltip(List<Component> tooltip) {
		AssemblyException e = getLastAssemblyException();
		if (e == null)
			return false;

		if (!tooltip.isEmpty())
			tooltip.add(CommonComponents.EMPTY);

		CreateLang.translate("gui.assembly.exception").style(ChatFormatting.GOLD)
			.forGoggles(tooltip);

		String text = e.component.getString();
		Arrays.stream(text.split("\n"))
			.forEach(l -> TooltipHelper.cutStringTextComponent(l, Palette.GRAY_AND_WHITE)
				.forEach(c -> CreateLang.builder().add(c).forGoggles(tooltip)));

		return true;
	}

	AssemblyException getLastAssemblyException();

}
