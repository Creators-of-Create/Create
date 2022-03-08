package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TimedWaitCondition extends ScheduleWaitCondition {
	public TimedWaitCondition.TimeUnit timeUnit = TimeUnit.TICKS;
	public int value;

	public static enum TimeUnit {
		TICKS(1, "t", "generic.unit.ticks"),
		SECONDS(20, "s", "generic.unit.seconds"),
		MINUTES(20 * 60, "min", "generic.unit.minutes");

		public int ticksPer;
		public String suffix;
		public String key;

		private TimeUnit(int ticksPer, String suffix, String key) {
			this.ticksPer = ticksPer;
			this.suffix = suffix;
			this.key = key;
		}

		public static List<Component> translatedOptions() {
			return Lang.translatedOptions(null, TICKS.key, SECONDS.key, MINUTES.key);
		}
	}

	protected Component formatTime(boolean compact) {
		if (compact)
			return new TextComponent(value + timeUnit.suffix);
		return new TextComponent(value + " ").append(Lang.translate(timeUnit.key));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			new TranslatableComponent(getId().getNamespace() + ".schedule." + type + "." + getId().getPath()),
			Lang.translate("schedule.condition.for_x_time", formatTime(false))
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	protected void write(CompoundTag tag) {
		tag.putInt("Value", value);
		NBTHelper.writeEnum(tag, "Unit", timeUnit);
	}

	@Override
	protected void read(CompoundTag tag) {
		value = tag.getInt("Value");
		timeUnit = NBTHelper.readEnum(tag, "Unit", TimedWaitCondition.TimeUnit.class);
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return new ItemStack(Items.REPEATER);
	}

	@Override
	public List<Component> getSecondLineTooltip() {
		return ImmutableList.of(Lang.translate("generic.duration"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		EditBox editBox = new EditBox(screen.getFont(), x + 84, y + 52, 31, 10, new TextComponent(value + ""));
		editBox.setBordered(false);
		editBox.setValue(value + "");
		editBox.setTextColor(0xFFFFFF);
		editBox.changeFocus(false);
		editBox.mouseClicked(0, 0, 0);
		editBox.setFilter(s -> {
			if (s.isEmpty())
				return true;
			try {
				Integer.parseInt(s);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		dividers.add(40);

		Label label = new Label(x + 125, y + 52, Lang.translate(timeUnit.key)).withShadow();
		ScrollInput scrollInput =
			new SelectionScrollInput(x + 120, y + 48, 79, 16).forOptions(TimeUnit.translatedOptions())
				.titled(Lang.translate("generic.timeUnit"))
				.writingTo(label)
				.setState(timeUnit.ordinal());

		editorSubWidgets.add(Pair.of(editBox, (dest, box) -> {
			TimedWaitCondition c = (TimedWaitCondition) dest;
			String text = ((EditBox) box).getValue();
			if (text.isEmpty())
				c.value = 0;
			else
				c.value = Integer.parseInt(text);
		}));
		editorSubWidgets.add(Pair.of(scrollInput, (dest, box) -> {
			TimedWaitCondition c = (TimedWaitCondition) dest;
			c.timeUnit = TimeUnit.values()[((ScrollInput) box).getState()];
		}));
		editorSubWidgets.add(Pair.of(label, (d, l) -> {
		}));
	}
}