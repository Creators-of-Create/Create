package com.simibubi.create.foundation.gui.widgets;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.IScreenRenderable;
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

	public <T extends ElementWidget> T showing(IScreenRenderable renderable) {
		return this.showingElement(RenderElement.of(renderable));
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

	public LerpedFloat fade() {
		return fade;
	}

	public <T extends ElementWidget> T fade(float target) {
		fade.chase(target, 0.1, LerpedFloat.Chaser.EXP);
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
	public void tick() {
		super.tick();
		fade.tickChaser();
	}

	@Override
	protected void beforeRender(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(ms, mouseX, mouseY, partialTicks);

		float fadeValue = fade.getValue(partialTicks);
		element.withAlpha(fadeValue);
		if (fadeValue < 1) {
			ms.translate((1 - fadeValue) * fadeModX, (1 - fadeValue) * fadeModY, 0);
		}
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.push();
		ms.translate(x, y, z);
		//element x/y get treated as a border around the element
		float eX = element.getX();
		float eY = element.getY();
		float eWidth = width;// - 2 * eX;
		float eHeight = height;// - 2 * eY;
		if (rescaleElement) {
			float xScale = eWidth / rescaleSizeX;
			float yScale = eHeight / rescaleSizeY;
			ms.scale(xScale, yScale, 1);
			element.at(eX / xScale, eY / yScale);
		}
		element.withBounds((int) eWidth, (int) eHeight).render(ms);
		ms.pop();
	}

	public RenderElement getRenderElement() {
		return element;
	}
}
