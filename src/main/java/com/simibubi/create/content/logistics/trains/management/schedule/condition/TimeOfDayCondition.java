package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimeOfDayCondition extends ScheduleWaitCondition {

	int hour;
	int minute;
	int gracePeriod;

	public TimeOfDayCondition() {
		hour = 8;
		minute = 0;
		gracePeriod = 5;
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int maxTickDiff = Math.max(20, gracePeriod * 60 * 20);
		int dayTime = (int) (level.getDayTime() % 24000);
		int targetTicks = (int) (((hour + 18) % 24) * 1000 + (minute / 60f) * 100);
		int diff = dayTime - targetTicks;
		return diff >= 0 && maxTickDiff >= diff;
	}
	
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(new ItemStack(Items.STRUCTURE_VOID), getDigitalDisplay(hour, minute, false));
	}

	public MutableComponent getDigitalDisplay(int hour, int minute, boolean doubleDigitHrs) {
		int hour12raw = hour % 12 == 0 ? 12 : hour % 12;
		String hr12 = doubleDigitHrs ? twoDigits(hour12raw) : ("" + hour12raw);
		String hr24 = doubleDigitHrs ? twoDigits(hour) : ("" + hour);
		return Lang.translate("schedule.condition.time_of_day.digital_format", hr12, hr24, twoDigits(minute),
			hour > 11 ? Lang.translate("generic.daytime.pm") : Lang.translate("generic.daytime.am"));
	}

	@Override
	public List<Component> getSecondLineTooltip() {
		return super.getSecondLineTooltip();
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translate("schedule.condition.time_of_day.scheduled",
			getDigitalDisplay(hour, minute, false).withStyle(ChatFormatting.DARK_AQUA)));
	}

	public String twoDigits(int t) {
		return t < 10 ? "0" + t : "" + t;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("time_of_day");
	}

	@Override
	protected void write(CompoundTag tag) {
		tag.putInt("Hour", hour);
		tag.putInt("Minute", minute);
		tag.putInt("GracePeriod", gracePeriod);
	}

	@Override
	protected void read(CompoundTag tag) {
		hour = tag.getInt("Hour");
		minute = tag.getInt("Minute");
		gracePeriod = tag.getInt("GracePeriod");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean renderSpecialIcon(PoseStack ms, int x, int y) {
		int displayHr = (hour + 12) % 24;
		float progress = (displayHr * 60f + minute) / (24 * 60);
		RenderSystem.setShaderTexture(0,
			new ResourceLocation("textures/item/clock_" + twoDigits(Mth.clamp((int) (progress * 64), 0, 63)) + ".png"));
		GuiComponent.blit(ms, x, y, 0, 0, 0, 16, 16, 16, 16);
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		Label timeLabel = new Label(x + 87, y + 52, new TextComponent("")).withShadow();
		timeLabel.text = getDigitalDisplay(hour, minute, true);
		ScrollInput hourInput = new ScrollInput(x + 82, y + 48, 16, 16).withRange(0, 24);
		ScrollInput minuteInput = new ScrollInput(x + 82 + 18, y + 48, 16, 16).withRange(0, 60);

		hourInput.titled(Lang.translate("generic.daytime.hour"))
			.calling(t -> {
				hour = t;
				timeLabel.text = getDigitalDisplay(hour, minute, true);
			})
			.withShiftStep(6)
			.setState(hour);

		minuteInput.titled(Lang.translate("generic.daytime.minute"))
			.calling(t -> {
				minute = t;
				timeLabel.text = getDigitalDisplay(hour, minute, true);
			})
			.withShiftStep(15)
			.setState(minute);
		
		minuteInput.lockedTooltipX = hourInput.lockedTooltipX = x + 83 + 40;
		minuteInput.lockedTooltipY = hourInput.lockedTooltipY = y + 55;

		dividers.add(70);

		Label graceLabel = new Label(x + 155, y + 52, new TextComponent("")).withShadow();
		graceLabel.text = Lang.translate("schedule.condition.time_of_day.grace_period.format", gracePeriod);
		ScrollInput scrollInput = new ScrollInput(x + 150, y + 48, 49, 16).withRange(0, 12)
			.titled(Lang.translate("schedule.condition.time_of_day.grace_period"))
			.calling(t -> graceLabel.text = Lang.translate("schedule.condition.time_of_day.grace_period.format", t))
			.setState(gracePeriod);

		editorSubWidgets.add(Pair.of(scrollInput,
			(dest, box) -> ((TimeOfDayCondition) dest).gracePeriod = ((ScrollInput) box).getState()));
		editorSubWidgets
			.add(Pair.of(hourInput, (dest, box) -> ((TimeOfDayCondition) dest).hour = ((ScrollInput) box).getState()));
		editorSubWidgets.add(
			Pair.of(minuteInput, (dest, box) -> ((TimeOfDayCondition) dest).minute = ((ScrollInput) box).getState()));

		editorSubWidgets.add(Pair.of(timeLabel, (d, l) -> {
		}));
		editorSubWidgets.add(Pair.of(graceLabel, (d, l) -> {
		}));
	}

}
