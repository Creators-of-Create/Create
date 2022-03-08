package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class FluidThresholdCondition extends CargoThresholdCondition {
	public ItemStack compareStack = ItemStack.EMPTY;
	public FluidStack fluidStack = null;

	@Override
	protected Component getUnit() {
		return new TextComponent("b");
	}

	@Override
	protected ItemStack getIcon() {
		return compareStack;
	}

	@Override
	protected void write(CompoundTag tag) {
		super.write(tag);
		tag.put("Bucket", compareStack.serializeNBT());
	}

	@Override
	protected void read(CompoundTag tag) {
		super.read(tag);
		compareStack = ItemStack.of(tag.getCompound("Bucket"));
	}

	@OnlyIn(Dist.CLIENT)
	private FluidStack loadFluid() {
		if (fluidStack != null)
			return fluidStack;
		fluidStack = FluidStack.EMPTY;
		if (!EmptyingByBasin.canItemBeEmptied(Minecraft.getInstance().level, compareStack))
			return fluidStack;
		FluidStack fluidInFilter = EmptyingByBasin.emptyItem(Minecraft.getInstance().level, compareStack, true)
			.getFirst();
		if (fluidInFilter == null)
			return fluidStack;
		return fluidStack = fluidInFilter;
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Lang.translate("schedule.condition.threshold.train_holds",
				Lang.translate("schedule.condition.threshold." + Lang.asId(ops.name()))),
			Lang.translate("schedule.condition.threshold.x_units_of_item", threshold,
				Lang.translate("schedule.condition.threshold.buckets"),
				compareStack.getItem() instanceof FilterItem
					? Lang.translate("schedule.condition.threshold.matching_content")
					: loadFluid().getDisplayName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public void setItem(ItemStack stack) {
		compareStack = stack;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("fluid_threshold");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {
		super.createWidgets(screen, editorSubWidgets, dividers, x, y);

		TranslatableComponent buckets = Lang.translate("schedule.condition.threshold.buckets");
		Label label = new Label(x + 155, y + 52, buckets).withShadow();
		label.text = buckets;
		editorSubWidgets.add(Pair.of(label, (d, l) -> {
		}));
	}
}