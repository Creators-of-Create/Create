package com.simibubi.create.compat.jei.category.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import me.shedaniel.rei.api.client.gui.widgets.Widget;

import java.util.List;

import net.minecraft.client.gui.components.events.GuiEventListener;

public class AnimatedKineticsWidget extends Widget {

	private final AnimatedKinetics animatedKinetics;
	private int x,y;

	public AnimatedKineticsWidget(AnimatedKinetics animated, int x, int y) {
		this.animatedKinetics = animated;
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		animatedKinetics.draw(poseStack, x, y);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Lists.newArrayList();
	}
}
