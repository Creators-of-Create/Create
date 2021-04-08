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
		add(getLines(), Lang.translate("tooltip.workInProgress")
			.formatted(TextFormatting.RED));

		int descriptions = 0;
		while (I18n.hasKey("create.tooltip.randomWipDescription" + descriptions++))
			;

		if (--descriptions > 0) {
			int index = new Random().nextInt(descriptions);
			ITextComponent translate = Lang.translate("tooltip.randomWipDescription" + index);
			List<ITextComponent> lines = getLines();
			lines.addAll(TooltipHelper.cutTextComponent(translate, TextFormatting.DARK_RED, TextFormatting.DARK_RED));
		}
	}

	@Override
	public List<ITextComponent> addInformation(List<ITextComponent> tooltip) {
		tooltip.set(0, decorateName(tooltip.get(0)));
		tooltip.addAll(getLines());
		return tooltip;
	}

	public static ITextComponent decorateName(ITextComponent name) {
		return StringTextComponent.EMPTY.copy()
			.append(name.copy()
				.formatted(TextFormatting.GRAY, TextFormatting.STRIKETHROUGH))
			.append(" ")
			.append(Lang.translate("tooltip.wip")
				.formatted(TextFormatting.GOLD));
	}

}
