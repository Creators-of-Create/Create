package com.simibubi.create.content.trains.schedule.destination;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DestinationInstruction extends TextScheduleInstruction {

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), Components.literal(getLabelText()));
	}

	@Override
	public boolean supportsConditions() {
		return true;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("destination");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return AllBlocks.TRACK_STATION.asStack();
	}

	public String getFilter() {
		return getLabelText();
	}

	public String getFilterForRegex() {
		String filter = getFilter();
		if (filter.isBlank())
			return filter;
		return "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
	}

	@Override
	public List<Component> getSecondLineTooltip(int slot) {
		return ImmutableList.of(CreateLang.translateDirect("schedule.instruction.filter_edit_box"),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_1")
				.withStyle(ChatFormatting.GRAY),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_2")
				.withStyle(ChatFormatting.DARK_GRAY),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_3")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {
		box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
	}

}
