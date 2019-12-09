package com.simibubi.create.foundation.item;

import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class WipScription extends ItemDescription {

	public WipScription(Palette palette) {
		super(palette);
		add(getLines(), TextFormatting.RED + Lang.translate("tooltip.workInProgress"));

		int descriptions = 0;
		while (I18n.hasKey("create.tooltip.randomWipDescription" + descriptions++))
			;

		if (--descriptions > 0) {
			int index = new Random().nextInt(descriptions);
			String translate = Lang.translate("tooltip.randomWipDescription" + index);
			add(getLines(), TooltipHelper.cutString(translate, TextFormatting.DARK_RED, TextFormatting.DARK_RED));
		}
	}
	
	@Override
	public List<ITextComponent> addInformation(List<ITextComponent> tooltip) {
		tooltip.set(0, new StringTextComponent(decorateName(tooltip.get(0).getString())));
		tooltip.addAll(getLines());
		return tooltip;
	}

	public static String decorateName(String name) {
		return TextFormatting.GRAY + "" + TextFormatting.STRIKETHROUGH + name + TextFormatting.GOLD + " "
				+ Lang.translate("tooltip.wip");
	}

}
