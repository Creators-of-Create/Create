package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.Arrays;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CargoThresholdCondition extends ScheduleWaitCondition {
	public static enum Ops {
		GREATER(">"), LESS("<"), EQUAL("=");

		public String formatted;

		private Ops(String formatted) {
			this.formatted = formatted;
		}

		public static List<? extends Component> translatedOptions() {
			return Arrays.stream(values())
				.map(op -> Lang.translate("schedule.condition.threshold." + Lang.asId(op.name())))
				.toList();
		}
	}

	public CargoThresholdCondition.Ops ops = Ops.GREATER;
	public int threshold;

	protected abstract Component getUnit();

	protected abstract ItemStack getIcon();

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(getIcon(), new TextComponent(ops.formatted + " " + threshold).append(getUnit()));
	}

	@Override
	protected void write(CompoundTag tag) {
		NBTHelper.writeEnum(tag, "Operator", ops);
		tag.putInt("Threshold", threshold);
	}

	@Override
	protected void read(CompoundTag tag) {
		ops = NBTHelper.readEnum(tag, "Operator", CargoThresholdCondition.Ops.class);
		threshold = tag.getInt("Threshold");
	}

	@Override
	public boolean needsSlot() {
		return true;
	}

	@Override
	public List<Component> getSecondLineTooltip() {
		return ImmutableList.of(Lang.translate("schedule.condition.threshold.place_item"),
			Lang.translate("schedule.condition.threshold.place_item_2")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		EditBox editBox = new EditBox(screen.getFont(), x + 109, y + 52, 35, 10, new TextComponent(threshold + ""));
		editBox.setBordered(false);
		editBox.setValue(threshold + "");
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

		Label label = new Label(x + 87, y + 52, new TextComponent(ops.formatted)).withShadow();
		label.text = new TextComponent(ops.formatted);
		ScrollInput scrollInput = new SelectionScrollInput(x + 76, y + 48, 24, 16).forOptions(Ops.translatedOptions())
			.titled(Lang.translate("schedule.condition.threshold.train_holds"))
			.calling(state -> {
				label.text = new TextComponent(Ops.values()[state].formatted);
			})
			.setState(ops.ordinal());

		editorSubWidgets.add(Pair.of(editBox, (dest, box) -> {
			CargoThresholdCondition c = (CargoThresholdCondition) dest;
			String text = ((EditBox) box).getValue();
			if (text.isEmpty())
				c.threshold = 0;
			else
				c.threshold = Integer.parseInt(text);
		}));
		editorSubWidgets.add(Pair.of(scrollInput, (dest, box) -> {
			CargoThresholdCondition c = (CargoThresholdCondition) dest;
			c.ops = Ops.values()[((ScrollInput) box).getState()];
		}));
		editorSubWidgets.add(Pair.of(label, (d, l) -> {
		}));

		dividers.add(24);
		dividers.add(70);
	}
}