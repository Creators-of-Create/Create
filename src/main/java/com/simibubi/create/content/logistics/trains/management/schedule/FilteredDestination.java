package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.ScheduleScreen;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FilteredDestination extends ScheduleDestination {
	public String nameFilter = "";

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), new TextComponent(nameFilter));
	}

	@Override
	protected void read(CompoundTag tag) {
		nameFilter = tag.getString("Filter");
	}

	@Override
	protected void write(CompoundTag tag) {
		tag.putString("Filter", nameFilter);
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("filtered");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return AllBlocks.TRACK_STATION.asStack();
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translate("schedule.destination.filtered_matching",
			new TextComponent(nameFilter).withStyle(ChatFormatting.YELLOW)));
	}

	@Override
	public List<Component> getSecondLineTooltip() {
		return ImmutableList.of(Lang.translate("schedule.destination.filter"),
			Lang.translate("schedule.destination.filter_2")
				.withStyle(ChatFormatting.GRAY),
			Lang.translate("schedule.destination.filter_3")
				.withStyle(ChatFormatting.DARK_GRAY),
			Lang.translate("schedule.destination.filter_4")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);
		EditBox editBox = new EditBox(screen.getFont(), x + 84, y + 52, 112, 10, new TextComponent(nameFilter));
		editBox.setBordered(false);
		editBox.setTextColor(0xFFFFFF);
		editBox.setValue(nameFilter);
		editBox.changeFocus(false);
		editBox.mouseClicked(0, 0, 0);
		editorSubWidgets
			.add(Pair.of(editBox, (dest, box) -> ((FilteredDestination) dest).nameFilter = ((EditBox) box).getValue()));
	}

}