package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ScheduleInstructionWithEditBox extends ScheduleInstruction {

	private String labelText = "";

	protected String getLabelText() {
		return labelText;
	}

	protected void setLabelText(String labelText) {
		this.labelText = labelText;
	}

	@Override
	protected void read(CompoundTag tag) {
		labelText = tag.getString("Text");
	}

	@Override
	protected void write(CompoundTag tag) {
		tag.putString("Text", labelText);
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translate("schedule." + type + "." + getId().getPath() + ".summary")
			.withStyle(ChatFormatting.GOLD), Lang.translate("generic.in_quotes", new TextComponent(getLabelText())));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		EditBox editBox = new EditBox(screen.getFont(), x + 84, y + 52, 112, 10, new TextComponent(labelText));
		editBox.setBordered(false);
		editBox.setTextColor(0xFFFFFF);
		editBox.setValue(labelText);
		editBox.changeFocus(false);
		editBox.mouseClicked(0, 0, 0);
		editorSubWidgets.add(Pair.of(editBox,
			(dest, box) -> ((ScheduleInstructionWithEditBox) dest).labelText = ((EditBox) box).getValue()));
	}

	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {}

}
