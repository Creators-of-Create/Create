package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DestinationInstruction extends ScheduleInstructionWithEditBox {

	public boolean isWaypoint;

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), new TextComponent(getLabelText()));
	}

	@Override
	public boolean supportsConditions() {
		return !isWaypoint;
	}

	@Override
	protected void write(CompoundTag tag) {
		tag.putBoolean("Waypoint", isWaypoint);
		super.write(tag);
	}
	
	@Override
	protected void read(CompoundTag tag) {
		isWaypoint = tag.getBoolean("Waypoint");
		super.read(tag);
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

	@Override
	public List<Component> getSecondLineTooltip() {
		return ImmutableList.of(Lang.translate("schedule.instruction.filter_edit_box"),
			Lang.translate("schedule.instruction.filter_edit_box_1")
				.withStyle(ChatFormatting.GRAY),
			Lang.translate("schedule.instruction.filter_edit_box_2")
				.withStyle(ChatFormatting.DARK_GRAY),
			Lang.translate("schedule.instruction.filter_edit_box_3")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {
		box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
	}

}