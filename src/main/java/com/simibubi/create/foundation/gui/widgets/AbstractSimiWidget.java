package com.simibubi.create.foundation.gui.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class AbstractSimiWidget extends AbstractWidget {

	protected float z;
	protected boolean wasHovered = false;
	protected List<Component> toolTip = new LinkedList<>();
	protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {};

	protected AbstractSimiWidget() {
		this(0, 0);
	}

	protected AbstractSimiWidget(int x, int y) {
		this(x, y, 16, 16);
	}

	protected AbstractSimiWidget(int x, int y, int width, int height) {
		super(x, y, width, height, TextComponent.EMPTY);
	}

	public <T extends AbstractSimiWidget> T withCallback(BiConsumer<Integer, Integer> cb) {
		this.onClick = cb;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends AbstractSimiWidget> T withCallback(Runnable cb) {
		return withCallback((_$, _$$) -> cb.run());
	}

	public <T extends AbstractSimiWidget> T atZLevel(float z) {
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	public void tick() {}

	@Override
	public void render(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = isMouseOver(mouseX, mouseY);
			beforeRender(ms, mouseX, mouseY, partialTicks);
			renderButton(ms, mouseX, mouseY, partialTicks);
			afterRender(ms, mouseX, mouseY, partialTicks);
			wasHovered = isHovered();
		}
	}

	@Override
	public void renderButton(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return active && visible && isMouseOver(mouseX, mouseY);
	}

	protected void beforeRender(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.pushPose();
	}

	protected void afterRender(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.popPose();
	}

	public void runCallback(double mouseX, double mouseY) {
		onClick.accept((int) mouseX, (int) mouseY);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		runCallback(mouseX, mouseY);
	}
}
