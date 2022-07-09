package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChangeTitleInstruction extends TextScheduleInstruction {

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(icon(), new TextComponent(getLabelText()));
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("rename");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return icon();
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}
	
	public String getScheduleTitle() {
		return getLabelText();
	}

	private ItemStack icon() {
		return new ItemStack(Items.NAME_TAG);
	}

	@Override
	public List<Component> getSecondLineTooltip(int slot) {
		return ImmutableList.of(Lang.translateDirect("schedule.instruction.name_edit_box"),
			Lang.translateDirect("schedule.instruction.name_edit_box_1")
				.withStyle(ChatFormatting.GRAY),
			Lang.translateDirect("schedule.instruction.name_edit_box_2")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

}
