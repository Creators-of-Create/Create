package com.simibubi.create.compat.jei.widgets;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class AnimatedKineticsWidget extends Widget {

	private Point pos;

	public void setPos(Point point) {
		this.pos = point;
	}

	public Point getPos() {
		return pos;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		draw(poseStack, pos.getX(), pos.getY());
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Lists.newArrayList();
	}

	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
	}
}
