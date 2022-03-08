package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemThresholdCondition extends CargoThresholdCondition {
	public ItemStack stack = ItemStack.EMPTY;
	public boolean stacks;

	@Override
	protected Component getUnit() {
		return new TextComponent(stacks ? "\u25A4" : "");
	}

	@Override
	protected ItemStack getIcon() {
		return stack;
	}

	@Override
	protected void write(CompoundTag tag) {
		super.write(tag);
		tag.put("Item", stack.serializeNBT());
		tag.putBoolean("Stacks", stacks);
	}

	@Override
	protected void read(CompoundTag tag) {
		super.read(tag);
		stack = ItemStack.of(tag.getCompound("Item"));
		stacks = tag.getBoolean("Stacks");
	}

	@Override
	public void setItem(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Lang.translate("schedule.condition.threshold.train_holds",
				Lang.translate("schedule.condition.threshold." + Lang.asId(ops.name()))),
			Lang.translate("schedule.condition.threshold.x_units_of_item", threshold,
				Lang.translate("schedule.condition.threshold." + (stacks ? "stacks" : "items")),
				stack.getItem() instanceof FilterItem ? Lang.translate("schedule.condition.threshold.matching_content")
					: stack.getHoverName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("item_threshold");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		Label label = new Label(x + 155, y + 52, new TextComponent(ops.formatted)).withShadow();
		ScrollInput scrollInput = new SelectionScrollInput(x + 150, y + 48, 49, 16)
			.forOptions(ImmutableList.of(Lang.translate("schedule.condition.threshold.items"),
				Lang.translate("schedule.condition.threshold.stacks")))
			.titled(Lang.translate("schedule.condition.threshold.item_measure"))
			.writingTo(label)
			.setState(stacks ? 1 : 0);

		editorSubWidgets.add(Pair.of(scrollInput, (dest, box) -> {
			ItemThresholdCondition c = (ItemThresholdCondition) dest;
			c.stacks = ((ScrollInput) box).getState() == 1;
		}));
		editorSubWidgets.add(Pair.of(label, (d, l) -> {
		}));
	}
}