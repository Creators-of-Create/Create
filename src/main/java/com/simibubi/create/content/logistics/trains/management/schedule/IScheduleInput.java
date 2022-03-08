package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScheduleInput {

	public abstract Pair<ItemStack, Component> getSummary();

	public abstract ResourceLocation getId();

	public default boolean needsSlot() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public default void createWidgets(ScheduleScreen screen,
		List<Pair<GuiEventListener, BiConsumer<IScheduleInput, GuiEventListener>>> editorSubWidgets,
		List<Integer> dividers, int x, int y) {}

	public default List<Component> getTitleAs(String type) {
		ResourceLocation id = getId();
		return ImmutableList.of(new TranslatableComponent(id.getNamespace() + ".schedule." + type + "." + id.getPath()));
	}

	public default ItemStack getSecondLineIcon() {
		return ItemStack.EMPTY;
	}

	public default void setItem(ItemStack stack) {}

	@Nullable
	public default List<Component> getSecondLineTooltip() {
		return null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public default boolean renderSpecialIcon(PoseStack ms, int x, int y) {
		return false;
	}

}