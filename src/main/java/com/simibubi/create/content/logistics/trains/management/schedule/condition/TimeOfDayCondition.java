package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
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
		data.putInt("GracePeriod", 5);
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int maxTickDiff = Math.max(20, intData("GracePeriod") * 60 * 20);
		int dayTime = (int) (level.getDayTime() % 24000);
		int targetTicks = (int) (((intData("Hour") + 18) % 24) * 1000 + (intData("Minute") / 60f) * 100);
		int diff = dayTime - targetTicks;
		return diff >= 0 && maxTickDiff >= diff;
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
		return Lang.translate("schedule.condition.time_of_day.digital_format", hr12, hr24, twoDigits(minute),
			hour > 11 ? Lang.translate("generic.daytime.pm") : Lang.translate("generic.daytime.am"));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translate("schedule.condition.time_of_day.scheduled",
			getDigitalDisplay(intData("Hour"), intData("Minute"), false).withStyle(ChatFormatting.DARK_AQUA)));
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
	public boolean renderSpecialIcon(PoseStack ms, int x, int y) {
		int displayHr = (intData("Hour") + 12) % 24;
		float progress = (displayHr * 60f + intData("Minute")) / (24 * 60);
		RenderSystem.setShaderTexture(0,
			new ResourceLocation("textures/item/clock_" + twoDigits(Mth.clamp((int) (progress * 64), 0, 63)) + ".png"));
		GuiComponent.blit(ms, x, y, 0, 0, 0, 16, 16, 16, 16);
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

		builder.addScrollInput(68, 49, (i, l) -> {
			i.withRange(0, 12)
				.titled(Lang.translate("schedule.condition.time_of_day.grace_period"))
				.format(t -> Lang.translate("schedule.condition.time_of_day.grace_period.format", t));
		}, "GracePeriod");

		hourInput.getValue()
			.titled(Lang.translate("generic.daytime.hour"))
			.calling(t -> {
				data.putInt("Hour", t);
				timeLabel.getValue().text = getDigitalDisplay(t, minuteInput.getValue()
					.getState(), true);
			})
			.writingTo(null)
			.withShiftStep(6);

		minuteInput.getValue()
			.titled(Lang.translate("generic.daytime.minute"))
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

		builder.customArea(0, 60);
		builder.customArea(65, 56);
	}

}
