package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
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

public class ComparisonTimeOfDayCondition extends ScheduleCondition {
	public static enum Ops {
		GREATER(">"),
		LESS("<");

		public String formatted;

		private Ops(String formatted) {
			this.formatted = formatted;
		}

		public boolean test(int dayTime, int diff, int maxTickDiff) {
			return switch (this) {
				case GREATER -> diff > 0;
				case LESS -> diff < 0;
				default -> throw new IllegalArgumentException("Unexpected value: " + this);
			};
		}

		public static List<? extends Component> translatedOptions() {
			return Arrays.stream(values())
					.map(op -> Lang.translateDirect("schedule.condition.threshold." + Lang.asId(op.name())))
					.toList();
		}
	}

	public ComparisonTimeOfDayCondition() {
		data.putInt("Hour", 8);
	}

	public ComparisonTimeOfDayCondition(ScheduleConditionType type) {
		super(type);
		data.putInt("Hour", 8);
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int maxTickDiff = 40;
		int targetHour = intData("Hour");
		int targetMinute = intData("Minute");
		int dayTime = (int) ((level.getDayTime() + 6000) % 24000);
		int targetTicks =
				(int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % 24000);
		int diff = dayTime - targetTicks;

		System.out.println("dayTime: " + dayTime + ", targetTicks: " + targetTicks + ", diff: " + diff + ", realDiff " + (diff + (dayTime >= 18000 ? 18000 : 0)) + ", maxTickDiff: " + maxTickDiff + ", test " + getOperator().test(dayTime, diff, maxTickDiff));

		return getOperator().test(dayTime, diff, maxTickDiff);
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context, GlobalStation station) {
		return this.tickCompletion(level, train, context);
	}

	public Ops getOperator() {
		return enumData("Operator", Ops.class);
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(new ItemStack(Items.STRUCTURE_VOID),
				getDigitalDisplay(intData("Hour"), intData("Minute"), false, true));
	}

	public MutableComponent getDigitalDisplay(int hour, int minute, boolean doubleDigitHrs, boolean operator) {
		int hour12raw = hour % 12 == 0 ? 12 : hour % 12;
		String hr12 = doubleDigitHrs ? twoDigits(hour12raw) : ("" + hour12raw);
		String hr24 = doubleDigitHrs ? twoDigits(hour) : ("" + hour);

		if (operator) {
			return Components.literal(getOperator().formatted + " ").append(Lang.translateDirect("schedule.condition.time_of_day.digital_format", hr12, hr24, twoDigits(minute),
					hour > 11 ? Lang.translateDirect("generic.daytime.pm") : Lang.translateDirect("generic.daytime.am")));
		} else {
			return Lang.translateDirect("schedule.condition.time_of_day.digital_format", hr12, hr24, twoDigits(minute),
					hour > 11 ? Lang.translateDirect("generic.daytime.pm") : Lang.translateDirect("generic.daytime.am"));
		}
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(Lang.translateDirect("schedule.condition.time_of_day.scheduled"),
				getDigitalDisplay(intData("Hour"), intData("Minute"), false, true).withStyle(ChatFormatting.DARK_AQUA)
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
		return Create.asResource("comparison_time_of_day");
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

		builder.addSelectionScrollInput(0, 24, (i, l) -> {
			i.forOptions(Ops.translatedOptions())
					.titled(Lang.translateDirect("schedule.condition.threshold.time"))
					.format(state -> Components.literal(" " + Ops.values()[state].formatted));
		}, "Operator");

		builder.addScrollInput(24, 16, (i, l) -> {
			i.withRange(0, 24);
			timeLabel.setValue(l);
			hourInput.setValue(i);
		}, "Hour");

		builder.addScrollInput(42, 16, (i, l) -> {
			i.withRange(0, 60);
			minuteInput.setValue(i);
			l.visible = false;
		}, "Minute");

		hourInput.getValue()
				.titled(Lang.translateDirect("generic.daytime.hour"))
				.calling(t -> {
					data.putInt("Hour", t);
					timeLabel.getValue().text = getDigitalDisplay(t, minuteInput.getValue()
							.getState(), true, false);
				})
				.writingTo(null)
				.withShiftStep(6);

		minuteInput.getValue()
				.titled(Lang.translateDirect("generic.daytime.minute"))
				.calling(t -> {
					data.putInt("Minute", t);
					timeLabel.getValue().text = getDigitalDisplay(hourInput.getValue().getState(), t,
							true, false);
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

		timeLabel.getValue().text = getDigitalDisplay(intData("Hour"), intData("Minute"), true,
				false);

		builder.customArea(0, 24);
		builder.customArea(24, 52);
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		int targetHour = intData("Hour");
		int targetMinute = intData("Minute");
		int dayTime = (int) ((level.getDayTime() + 6000) % 24000);
		int targetTicks =
				(int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % 24000);
		int diff = targetTicks - dayTime;

		if (diff < 0) {
			diff += 24000;
		}

		int departureTime = (int) (level.getDayTime() + diff) % 24000;
		int departingHour = (departureTime / 1000 + 6) % 24;
		int departingMinute = (departureTime % 1000) * 60 / 1000;

		return Lang.translateDirect("schedule.condition.time_of_day.status")
				.append(getDigitalDisplay(departingHour, departingMinute, false, true));
	}
}
