package com.simibubi.create.content.trains.schedule.destination;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TextScheduleInstruction extends ScheduleInstruction {

	protected String getLabelText() {
		return textData("Text");
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translateDirect("schedule." + type + "." + getId().getPath() + ".summary")
			.withStyle(ChatFormatting.GOLD), Lang.translateDirect("generic.in_quotes", Components.literal(getLabelText())));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		builder.addTextInput(0, 121, (e, t) -> modifyEditBox(e), "Text");
	}

	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {}

}
