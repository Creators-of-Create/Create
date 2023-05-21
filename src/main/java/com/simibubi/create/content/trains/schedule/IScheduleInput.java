package com.simibubi.create.content.trains.schedule;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScheduleInput {

	public abstract Pair<ItemStack, Component> getSummary();

	public abstract ResourceLocation getId();

	public abstract CompoundTag getData();

	public abstract void setData(CompoundTag data);

	public default int slotsTargeted() {
		return 0;
	}

	public default List<Component> getTitleAs(String type) {
		ResourceLocation id = getId();
		return ImmutableList
			.of(Components.translatable(id.getNamespace() + ".schedule." + type + "." + id.getPath()));
	}

	public default ItemStack getSecondLineIcon() {
		return ItemStack.EMPTY;
	}

	public default void setItem(int slot, ItemStack stack) {}

	public default ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Nullable
	public default List<Component> getSecondLineTooltip(int slot) {
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public default void initConfigurationWidgets(ModularGuiLineBuilder builder) {};

	@OnlyIn(Dist.CLIENT)
	public default boolean renderSpecialIcon(PoseStack ms, int x, int y) {
		return false;
	}

}
