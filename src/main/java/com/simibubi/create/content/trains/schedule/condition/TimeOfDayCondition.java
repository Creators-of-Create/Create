package com.simibubi.create.content.trains.schedule.condition;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimeOfDayCondition extends ScheduleWaitCondition {

	public TimeOfDayCondition() {
		data.putInt("Hour", 8);
		data.putInt("Rotation", 5);
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int maxTickDiff = 40;
		int targetHour = intData("Hour");
		int targetMinute = intData("Minute");
		int dayTime = (int) (level.getDayTime() % getRotation());
		int targetTicks =
			(int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % getRotation());
		int diff = dayTime - targetTicks;
		return diff >= 0 && maxTickDiff >= diff;
	}

	public int getRotation() {
		int index = intData("Rotation");
		return switch (index) {
		case 9 -> 250;
		case 8 -> 500;
		case 7 -> 750;
		case 6 -> 1000;
		case 5 -> 2000;
		case 4 -> 3000;
		case 3 -> 4000;
		case 2 -> 6000;
		case 1 -> 12000;
		default -> 24000;
		};
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(new ItemStack(Items.STRUCTURE_VOID),
			getDigitalDisplay(intData("Hour"), intData("Minute"), false));
	}

	public MutableComponent getDigitalDisplay(int hour, int minute, boolean doubleDigitHrs) {
		int hour12raw = hour % 12 == 0 ? 12 : hour % 12;
		String hr12 = doubleDigitHrs ? twoDigits(hour12raw) : ("" + hour12raw);
		String hr24 = doubleDigitHrs ? twoDigits(hour) : ("" + hour);
		return Lang.translateDirect("schedule.condition.time_of_day.digital_format", hr12, hr24, twoDigits(minute),
			hour > 11 ? Lang.translateDirect("generic.daytime.pm") : Lang.translateDirect("generic.daytime.am"));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translateDirect("schedule.condition.time_of_day.scheduled"),
			getDigitalDisplay(intData("Hour"), intData("Minute"), false).withStyle(ChatFormatting.DARK_AQUA)
				.append(Components.literal(" -> ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Lang
					.translatedOptions("schedule.condition.time_of_day.rotation", "every_24", "every_12", "every_6",
						"every_4", "every_3", "every_2", "every_1", "every_0_45", "every_0_30", "every_0_15")
					.get(intData("Rotation"))
					.copy()
					.withStyle(ChatFormatting.GRAY)));
	}

	public String twoDigits(int t) {
		return t < 10 ? "0" + t : "" + t;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("time_of_day");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean renderSpecialIcon(GuiGraphics graphics, int x, int y) {
		int displayHr = (intData("Hour") + 12) % 24;
		float progress = (displayHr * 60f + intData("Minute")) / (24 * 60);
		ResourceLocation location =
			new ResourceLocation("textures/item/clock_" + twoDigits(Mth.clamp((int) (progress * 64), 0, 63)) + ".png");
		graphics.blit(location, x, y, 0, 0, 0, 16, 16, 16, 16);
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		MutableObject<ScrollInput> minuteInput = new MutableObject<>();
		MutableObject<ScrollInput> hourInput = new MutableObject<>();
		MutableObject<Label> timeLabel = new MutableObject<>();

		builder.addScrollInput(0, 16, (i, l) -> {
			i.withRange(0, 24);
			timeLabel.setValue(l);
			hourInput.setValue(i);
		}, "Hour");

		builder.addScrollInput(18, 16, (i, l) -> {
			i.withRange(0, 60);
			minuteInput.setValue(i);
			l.visible = false;
		}, "Minute");

		builder.addSelectionScrollInput(52, 62, (i, l) -> {
			i.forOptions(Lang.translatedOptions("schedule.condition.time_of_day.rotation", "every_24", "every_12",
				"every_6", "every_4", "every_3", "every_2", "every_1", "every_0_45", "every_0_30", "every_0_15"))
				.titled(Lang.translateDirect("schedule.condition.time_of_day.rotation"));
		}, "Rotation");

		hourInput.getValue()
			.titled(Lang.translateDirect("generic.daytime.hour"))
			.calling(t -> {
				data.putInt("Hour", t);
				timeLabel.getValue().text = getDigitalDisplay(t, minuteInput.getValue()
					.getState(), true);
			})
			.writingTo(null)
			.withShiftStep(6);

		minuteInput.getValue()
			.titled(Lang.translateDirect("generic.daytime.minute"))
			.calling(t -> {
				data.putInt("Minute", t);
				timeLabel.getValue().text = getDigitalDisplay(hourInput.getValue()
					.getState(), t, true);
			})
			.writingTo(null)
			.withShiftStep(15);

		minuteInput.getValue().lockedTooltipX = hourInput.getValue().lockedTooltipX = -15;
		minuteInput.getValue().lockedTooltipY = hourInput.getValue().lockedTooltipY = 35;

		hourInput.getValue()
			.setState(intData("Hour"));
		minuteInput.getValue()
			.setState(intData("Minute"))
			.onChanged();

		builder.customArea(0, 52);
		builder.customArea(52, 69);
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		int targetHour = intData("Hour");
		int targetMinute = intData("Minute");
		int dayTime = (int) (level.getDayTime() % getRotation());
		int targetTicks =
			(int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % getRotation());
		int diff = targetTicks - dayTime;

		if (diff < 0)
			diff += getRotation();

		int departureTime = (int) (level.getDayTime() + diff) % 24000;
		int departingHour = (departureTime / 1000 + 6) % 24;
		int departingMinute = (departureTime % 1000) * 60 / 1000;

		return Lang.translateDirect("schedule.condition.time_of_day.status")
			.append(getDigitalDisplay(departingHour, departingMinute, false));
	}

}
