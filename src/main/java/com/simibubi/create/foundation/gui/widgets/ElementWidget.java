package com.simibubi.create.foundation.gui.widgets;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.RenderElement;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

public class ElementWidget extends AbstractSimiWidget {

	protected RenderElement element = RenderElement.EMPTY;

	protected boolean usesFade = false;
	protected int fadeModX;
	protected int fadeModY;
	protected LerpedFloat fade = LerpedFloat.linear().startWithValue(1);

	protected boolean rescaleElement = false;
	protected float rescaleSizeX;
	protected float rescaleSizeY;

	public ElementWidget(int x, int y) {
		super(x, y);
	}

	public ElementWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public <T extends ElementWidget> T showingElement(RenderElement element) {
		this.element = element;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T modifyElement(Consumer<RenderElement> consumer) {
		if (element != null)
			consumer.accept(element);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T mapElement(UnaryOperator<RenderElement> function) {
		if (element != null)
			element = function.apply(element);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T enableFade(int fadeModifierX, int fadeModifierY) {
		this.fade.startWithValue(0);
		this.usesFade = true;
		this.fadeModX = fadeModifierX;
		this.fadeModY = fadeModifierY;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T disableFade() {
		this.fade.startWithValue(1);
		this.usesFade = false;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T rescaleElement(float rescaleSizeX, float rescaleSizeY) {
		this.rescaleElement = true;
		this.rescaleSizeX = rescaleSizeX;
		this.rescaleSizeY = rescaleSizeY;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends ElementWidget> T disableRescale() {
		this.rescaleElement = false;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	protected void beforeRender(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(ms, mouseX, mouseY, partialTicks);

		//todo fade
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.push();
		ms.translate(x, y, 0);
		if (rescaleElement) {
			ms.scale(width / rescaleSizeX, height / rescaleSizeY, 1);
		}
		element.withBounds(width, height).render(ms);
		ms.pop();
	}

	public RenderElement getRenderElement() {
		return element;
	}
}
