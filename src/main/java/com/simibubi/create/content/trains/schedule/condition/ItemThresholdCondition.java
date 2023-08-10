package com.simibubi.create.content.trains.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemThresholdCondition extends CargoThresholdCondition {
	public ItemStack stack = ItemStack.EMPTY;

	@Override
	protected Component getUnit() {
		return Components.literal(inStacks() ? "\u25A4" : "");
	}

	@Override
	protected ItemStack getIcon() {
		return stack;
	}

	@Override
	protected boolean test(Level level, Train train, CompoundTag context) {
		Ops operator = getOperator();
		int target = getThreshold();
		boolean stacks = inStacks();

		int foundItems = 0;
		for (Carriage carriage : train.carriages) {
			IItemHandlerModifiable items = carriage.storage.getItems();
			for (int i = 0; i < items.getSlots(); i++) {
				ItemStack stackInSlot = items.getStackInSlot(i);
				if (!FilterItem.test(level, stackInSlot, stack))
					continue;

				if (stacks)
					foundItems += stackInSlot.getCount() == stackInSlot.getMaxStackSize() ? 1 : 0;
				else
					foundItems += stackInSlot.getCount();
			}
		}

		requestStatusToUpdate(foundItems, context);
		return operator.test(foundItems, target);
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {
		super.writeAdditional(tag);
		tag.put("Item", stack.serializeNBT());
	}

	@Override
	protected void readAdditional(CompoundTag tag) {
		super.readAdditional(tag);
		if (tag.contains("Item"))
			stack = ItemStack.of(tag.getCompound("Item"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return super.tickCompletion(level, train, context);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public ItemStack getItem(int slot) {
		return stack;
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			CreateLang.translateDirect("schedule.condition.threshold.train_holds",
				CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(getOperator().name()))),
			CreateLang.translateDirect("schedule.condition.threshold.x_units_of_item", getThreshold(),
				CreateLang.translateDirect("schedule.condition.threshold." + (inStacks() ? "stacks" : "items")),
				stack.isEmpty() ? CreateLang.translateDirect("schedule.condition.threshold.anything")
					: stack.getItem() instanceof FilterItem
						? CreateLang.translateDirect("schedule.condition.threshold.matching_content")
						: stack.getHoverName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	private boolean inStacks() {
		return intData("Measure") == 1;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("item_threshold");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		super.initConfigurationWidgets(builder);
		builder.addSelectionScrollInput(71, 50, (i, l) -> {
			i.forOptions(ImmutableList.of(CreateLang.translateDirect("schedule.condition.threshold.items"),
				CreateLang.translateDirect("schedule.condition.threshold.stacks")))
				.titled(CreateLang.translateDirect("schedule.condition.threshold.item_measure"));
		}, "Measure");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		int lastDisplaySnapshot = getLastDisplaySnapshot(tag);
		if (lastDisplaySnapshot == -1)
			return Components.empty();
		int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
		return CreateLang.translateDirect("schedule.condition.threshold.status", lastDisplaySnapshot,
			Math.max(0, getThreshold() + offset),
			CreateLang.translateDirect("schedule.condition.threshold." + (inStacks() ? "stacks" : "items")));
	}
}
