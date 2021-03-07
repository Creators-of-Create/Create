package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSimiScreen extends Screen {

	protected int sWidth, sHeight;
	protected int guiLeft, guiTop;
	protected int depthPointX, depthPointY;
	protected List<Widget> widgets;
	public final LerpedFloat transition = LerpedFloat.linear()
		.startWithValue(0)
		.chase(0, .1f, LerpedFloat.Chaser.LINEAR);

	protected AbstractSimiScreen() {
		super(new StringTextComponent(""));
		widgets = new ArrayList<>();
		MainWindow window = Minecraft.getInstance()
			.getWindow();
		depthPointX = window.getScaledWidth() / 2;
		depthPointY = window.getScaledHeight() / 2;
	}

	protected void setWindowSize(int width, int height) {
		sWidth = width;
		sHeight = height;
		guiLeft = (this.width - sWidth) / 2;
		guiTop = (this.height - sHeight) / 2;
	}

	@Override
	public void tick() {
		super.tick();
		transition.tickChaser();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		partialTicks = partialTicks == 10 ? 0
			: Minecraft.getInstance()
				.getRenderPartialTicks();

		RenderSystem.pushMatrix();

		renderTransition(mouseX, mouseY, partialTicks);

		renderWindow(mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.render(mouseX, mouseY, partialTicks);

		renderWindowForeground(mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.renderToolTip(mouseX, mouseY);

		RenderSystem.popMatrix();

		renderBreadcrumbs(mouseX, mouseY, partialTicks);
	}

	private void renderTransition(int mouseX, int mouseY, float partialTicks) {
		if (transition.getChaseTarget() == 0) {
			renderBackground();
			return;
		}

		renderBackground();

		Screen lastScreen = ScreenOpener.getPreviouslyRenderedScreen();
		float transitionValue = transition.getValue(partialTicks);
		double scale = 1 + 0.5 * transitionValue;

		// draw last screen into buffer
		if (lastScreen != null && lastScreen != this) {
			RenderSystem.pushMatrix();// 1
			UIRenderHelper.framebuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
			UIRenderHelper.prepFramebufferSize();
			RenderSystem.pushMatrix();// 2
			RenderSystem.translated(0, 0, -1000);
			UIRenderHelper.framebuffer.bindFramebuffer(true);
			lastScreen.render(mouseX, mouseY, 10);
			RenderSystem.popMatrix();// 2

			// use the buffer texture
			Minecraft.getInstance()
				.getFramebuffer()
				.bindFramebuffer(true);

			MainWindow window = Minecraft.getInstance()
				.getWindow();
			int dpx = window.getScaledWidth() / 2;
			int dpy = window.getScaledHeight() / 2;
			if (lastScreen instanceof AbstractSimiScreen) {
				dpx = ((AbstractSimiScreen) lastScreen).depthPointX;
				dpy = ((AbstractSimiScreen) lastScreen).depthPointY;
			}

			// transitionV is 1/-1 when the older screen is hidden
			// transitionV is 0 when the older screen is still fully visible
			RenderSystem.translated(dpx, dpy, 0);
			RenderSystem.scaled(scale, scale, 1);
			RenderSystem.translated(-dpx, -dpy, 0);
			UIRenderHelper.drawFramebuffer(1f - Math.abs(transitionValue));
			RenderSystem.popMatrix();// 1
		}

		// modify current screen as well
		scale = transitionValue > 0 ? 1 - 0.5 * (1 - transitionValue) : 1 + .5 * (1 + transitionValue);
		RenderSystem.translated(depthPointX, depthPointY, 0);
		RenderSystem.scaled(scale, scale, 1);
		RenderSystem.translated(-depthPointX, -depthPointY, 0);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets)
			if (widget.mouseClicked(x, y, button))
				result = true;
		return result;
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (Widget widget : widgets)
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;

		if (code == GLFW.GLFW_KEY_BACKSPACE) {
			ScreenOpener.openPreviousScreen(this);
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (Widget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
		if (character == 'e')
			onClose();
		return super.charTyped(character, code);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		for (Widget widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, delta))
				return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseReleased(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets) {
			if (widget.mouseReleased(x, y, button))
				result = true;
		}
		return result | super.mouseReleased(x, y, button);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void onClose() {
		ScreenOpener.clearStack();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(int mouseX, int mouseY, float partialTicks);

	protected void renderBreadcrumbs(int mouseX, int mouseY, float partialTicks) {
		List<Screen> history = ScreenOpener.getScreenHistory();
		if (history.isEmpty())
			return;

		history.add(0, Minecraft.getInstance().currentScreen);
		int spacing = 20;

		List<String> names = history.stream()
			.map(AbstractSimiScreen::screenTitle)
			.collect(Collectors.toList());

		int bWidth = names.stream()
			.mapToInt(s -> font.getStringWidth(s) + spacing)
			.sum();

		MutableInt x = new MutableInt(width - bWidth);
		MutableInt y = new MutableInt(height - 18);
		MutableBoolean first = new MutableBoolean(true);

		if (x.getValue() < 25)
			x.setValue(25);

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 600);
		names.forEach(s -> {
			int sWidth = font.getStringWidth(s);
			// UIRenderHelper.breadcrumbArrow(x.getValue(), y.getValue(), sWidth + spacing,
			// 14, spacing/2, 0xbbababab, 0x22ababab);
			UIRenderHelper.breadcrumbArrow(x.getValue(), y.getValue(), sWidth + spacing, 14, spacing / 2, 0xdd101010,
				0x44101010);
			drawString(font, s, x.getValue() + 5, y.getValue() + 3, first.getValue() ? 0xffeeffee : 0xffddeeff);
			first.setFalse();

			x.add(sWidth + spacing);
		});
		RenderSystem.popMatrix();
	}

	private static String screenTitle(Screen screen) {
		if (screen instanceof AbstractSimiScreen)
			return ((AbstractSimiScreen) screen).getBreadcrumbTitle();

		return screen.getClass()
			.getSimpleName();
	}

	protected String getBreadcrumbTitle() {
		return this.getClass()
			.getSimpleName();
	}

	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		for (Widget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip()
				.isEmpty()) {
				renderTooltip(((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
			}
		}
	}

	public void centerScalingOn(int x, int y) {
		depthPointX = x;
		depthPointY = y;
	}

	public void centerScalingOnMouse() {
		MainWindow w = minecraft.getWindow();
		double mouseX = minecraft.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = minecraft.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		centerScalingOn((int) mouseX, (int) mouseY);
	}
	
	public boolean isEquivalentTo(AbstractSimiScreen other) {
		return false;
	}

}
