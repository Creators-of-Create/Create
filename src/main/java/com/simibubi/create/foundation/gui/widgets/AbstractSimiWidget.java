package com.simibubi.create.foundation.gui.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public abstract class AbstractSimiWidget extends Widget {

	protected List<ITextComponent> toolTip;
	protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {};

	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, StringTextComponent.EMPTY);
		toolTip = new LinkedList<>();
	}
	
	public List<ITextComponent> getToolTip() {
		return toolTip;
	}
	
	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
	}

	public <T extends AbstractSimiWidget> T withCallback(BiConsumer<Integer, Integer> cb) {
		this.onClick = cb;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends AbstractSimiWidget> T withCallback(Runnable cb) {
		return withCallback((_$, _$$) -> cb.run());
	}

	public void runCallback(double mouseX, double mouseY) {
		onClick.accept((int) mouseX, (int) mouseY);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		runCallback(mouseX, mouseY);
	}
}
