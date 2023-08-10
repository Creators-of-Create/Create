package com.simibubi.create.content.trains.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneLinkCondition extends ScheduleWaitCondition {

	public Couple<Frequency> freq;

	public RedstoneLinkCondition() {
		freq = Couple.create(() -> Frequency.EMPTY);
	}

	@Override
	public int slotsTargeted() {
		return 2;
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.REDSTONE_LINK.asStack(),
			lowActivation() ? CreateLang.translateDirect("schedule.condition.redstone_link_off")
				: CreateLang.translateDirect("schedule.condition.redstone_link_on"));
	}

	@Override
	public List<Component> getSecondLineTooltip(int slot) {
		return ImmutableList.of(CreateLang.translateDirect(slot == 0 ? "logistics.firstFrequency" : "logistics.secondFrequency")
			.withStyle(ChatFormatting.RED));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			CreateLang.translateDirect("schedule.condition.redstone_link.frequency_" + (lowActivation() ? "unpowered" : "powered")),
			Components.literal(" #1 ").withStyle(ChatFormatting.GRAY)
				.append(freq.getFirst()
					.getStack()
					.getHoverName()
					.copy()
					.withStyle(ChatFormatting.DARK_AQUA)),
			Components.literal(" #2 ").withStyle(ChatFormatting.GRAY)
				.append(freq.getSecond()
					.getStack()
					.getHoverName()
					.copy()
					.withStyle(ChatFormatting.DARK_AQUA)));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int lastChecked = context.contains("LastChecked") ? context.getInt("LastChecked") : -1;
		int status = Create.REDSTONE_LINK_NETWORK_HANDLER.globalPowerVersion.get();
		if (status == lastChecked)
			return false;
		context.putInt("LastChecked", status);
		return Create.REDSTONE_LINK_NETWORK_HANDLER.hasAnyLoadedPower(freq) != lowActivation();
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		freq.set(slot == 0, Frequency.of(stack));
		super.setItem(slot, stack);
	}

	@Override
	public ItemStack getItem(int slot) {
		return freq.get(slot == 0)
			.getStack();
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("redstone_link");
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {
		tag.put("Frequency", freq.serializeEach(f -> f.getStack()
			.serializeNBT()));
	}

	public boolean lowActivation() {
		return intData("Inverted") == 1;
	}

	@Override
	protected void readAdditional(CompoundTag tag) {
		if (tag.contains("Frequency"))
			freq = Couple.deserializeEach(tag.getList("Frequency", Tag.TAG_COMPOUND), c -> Frequency.of(ItemStack.of(c)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		builder.addSelectionScrollInput(20, 101,
			(i, l) -> i.forOptions(CreateLang.translatedOptions("schedule.condition.redstone_link", "powered", "unpowered"))
				.titled(CreateLang.translateDirect("schedule.condition.redstone_link.frequency_state")),
			"Inverted");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		return CreateLang.translateDirect("schedule.condition.redstone_link.status");
	}

}
