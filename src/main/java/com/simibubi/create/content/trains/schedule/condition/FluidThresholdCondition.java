package com.simibubi.create.content.trains.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidThresholdCondition extends CargoThresholdCondition {
	public ItemStack compareStack = ItemStack.EMPTY;
	public FluidStack fluidStack = null;

	@Override
	protected Component getUnit() {
		return Components.literal("b");
	}

	@Override
	protected ItemStack getIcon() {
		return compareStack;
	}

	@Override
	protected boolean test(Level level, Train train, CompoundTag context) {
		Ops operator = getOperator();
		int target = getThreshold();

		int foundFluid = 0;
		for (Carriage carriage : train.carriages) {
			IFluidHandler fluids = carriage.storage.getFluids();
			for (int i = 0; i < fluids.getTanks(); i++) {
				FluidStack fluidInTank = fluids.getFluidInTank(i);
				if (!FilterItem.test(level, fluidInTank, compareStack))
					continue;
				foundFluid += fluidInTank.getAmount();
			}
		}

		requestStatusToUpdate(foundFluid / 1000, context);
		return operator.test(foundFluid, target * 1000);
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {
		super.writeAdditional(tag);
		tag.put("Bucket", compareStack.serializeNBT());
	}

	@Override
	protected void readAdditional(CompoundTag tag) {
		super.readAdditional(tag);
		if (tag.contains("Bucket"))
			compareStack = ItemStack.of(tag.getCompound("Bucket"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return super.tickCompletion(level, train, context);
	}

	@OnlyIn(Dist.CLIENT)
	private FluidStack loadFluid() {
		if (fluidStack != null)
			return fluidStack;
		fluidStack = FluidStack.EMPTY;
		if (!GenericItemEmptying.canItemBeEmptied(Minecraft.getInstance().level, compareStack))
			return fluidStack;
		FluidStack fluidInFilter = GenericItemEmptying.emptyItem(Minecraft.getInstance().level, compareStack, true)
			.getFirst();
		if (fluidInFilter == null)
			return fluidStack;
		return fluidStack = fluidInFilter;
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			CreateLang.translateDirect("schedule.condition.threshold.train_holds",
				CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(getOperator().name()))),
			CreateLang.translateDirect("schedule.condition.threshold.x_units_of_item", getThreshold(),
				CreateLang.translateDirect("schedule.condition.threshold.buckets"),
				compareStack.isEmpty() ? CreateLang.translateDirect("schedule.condition.threshold.anything")
					: compareStack.getItem() instanceof FilterItem
						? CreateLang.translateDirect("schedule.condition.threshold.matching_content")
						: loadFluid().getDisplayName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		compareStack = stack;
	}

	@Override
	public ItemStack getItem(int slot) {
		return compareStack;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("fluid_threshold");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		super.initConfigurationWidgets(builder);
		builder.addSelectionScrollInput(71, 50, (i, l) -> {
			i.forOptions(ImmutableList.of(CreateLang.translateDirect("schedule.condition.threshold.buckets")))
				.titled(null);
		}, "Measure");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		int lastDisplaySnapshot = getLastDisplaySnapshot(tag);
		if (lastDisplaySnapshot == -1)
			return Components.empty();
		int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
		return CreateLang.translateDirect("schedule.condition.threshold.status", lastDisplaySnapshot,
			Math.max(0, getThreshold() + offset), CreateLang.translateDirect("schedule.condition.threshold.buckets"));
	}

}
