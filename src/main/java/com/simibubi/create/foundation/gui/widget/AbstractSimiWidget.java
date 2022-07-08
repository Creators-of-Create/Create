package com.simibubi.create.foundation.gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.TickableGuiEventListener;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class AbstractSimiWidget extends AbstractWidget implements TickableGuiEventListener {

	public static final int HEADER_RGB = 0x5391E1;
	
	protected float z;
	protected boolean wasHovered = false;
	protected List<Component> toolTip = new LinkedList<>();
	protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {};
	
	public int lockedTooltipX = -1;
	public int lockedTooltipY = -1;

	protected AbstractSimiWidget(int x, int y) {
		this(x, y, 16, 16);
	}

	protected AbstractSimiWidget(int x, int y, int width, int height) {
		this(x, y, width, height, TextComponent.EMPTY);
	}

	protected AbstractSimiWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
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

	@Override
	public void tick() {}

	@Override
	public void render(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			beforeRender(ms, mouseX, mouseY, partialTicks);
			renderButton(ms, mouseX, mouseY, partialTicks);
			afterRender(ms, mouseX, mouseY, partialTicks);
			wasHovered = isHoveredOrFocused();
		}
	}

	protected void beforeRender(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.pushPose();
	}

	@Override
	public void renderButton(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
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

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}
}
