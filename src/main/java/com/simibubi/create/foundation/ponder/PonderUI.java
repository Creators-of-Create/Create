package com.simibubi.create.foundation.ponder;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderUI extends AbstractSimiScreen {

	public static final String PONDERING = PonderLocalization.LANG_PREFIX + "pondering";
	private List<PonderScene> scenes;
	private LerpedFloat fadeIn;
	ItemStack stack;

	private LerpedFloat lazyIndex;
	private int index = 0;

	private PonderButton left, right, icon;

	public PonderUI(List<PonderScene> scenes) {
		this.scenes = scenes;
		lazyIndex = LerpedFloat.linear()
			.startWithValue(index);
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .1f, Chaser.EXP);
	}

	@Override
	protected void init() {
		super.init();
		widgets.clear();

		ResourceLocation component = scenes.get(0).component;
		if (ForgeRegistries.ITEMS.containsKey(component))
			stack = new ItemStack(ForgeRegistries.ITEMS.getValue(component));
		else
			stack = new ItemStack(ForgeRegistries.BLOCKS.getValue(component));

		int bY = height - 20 - 31;
		widgets.add(icon = new PonderButton(31, 31, () -> {
		}).showing(stack)
			.fade(0, -1));

		int spacing = 8;
		int bX = (width - 20) / 2 - (20 + spacing);
		GameSettings bindings = minecraft.gameSettings;
		widgets.add(left = new PonderButton(bX, bY, () -> this.scroll(false)).showing(AllIcons.I_MTD_LEFT)
			.shortcut(bindings.keyBindLeft)
			.fade(0, -1));
		bX += 20 + spacing;
		widgets.add(new PonderButton(bX, bY, this::onClose).showing(AllIcons.I_MTD_CLOSE)
			.shortcut(bindings.keyBindInventory)
			.fade(0, -1));
		bX += 20 + spacing;
		widgets.add(right = new PonderButton(bX, bY, () -> this.scroll(true)).showing(AllIcons.I_MTD_RIGHT)
			.shortcut(bindings.keyBindRight)
			.fade(0, -1));

	}

	@Override
	public void tick() {
		lazyIndex.tickChaser();
		fadeIn.tickChaser();
		scenes.get(index)
			.tick();
		float lazyIndexValue = lazyIndex.getValue();
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			scenes.get(lazyIndexValue < index ? index - 1 : index + 1)
				.tick();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (scroll(delta > 0))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	protected boolean scroll(boolean forward) {
		int prevIndex = index;
		index = forward ? index + 1 : index - 1;
		index = MathHelper.clamp(index, 0, scenes.size() - 1);
		if (prevIndex != index && Math.abs(index - lazyIndex.getValue()) < 1.5f) {
			scenes.get(prevIndex)
				.fadeOut();
			scenes.get(index)
				.begin();
			lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
			return true;
		} else
			index = prevIndex;
		return false;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		RenderSystem.enableBlend();
		renderVisibleScenes(partialTicks);
		renderWidgets(mouseX, mouseY, partialTicks);
	}

	protected void renderVisibleScenes(float partialTicks) {
		renderScene(index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		PonderScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		double value = lazyIndex.getValue(partialTicks);
		double diff = i - value;
		double slide = MathHelper.lerp(diff * diff, 200, 600) * diff;

		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		
		ms.push();
		story.transform.updateScreenParams(width, height, slide);
		story.transform.apply(ms);
		story.renderScene(buffer, ms);
		buffer.draw();
		
		// coords for debug
		if (PonderIndex.EDITOR_MODE) {
			MutableBoundingBox bounds = story.getBounds();
			
			RenderSystem.pushMatrix();
			RenderSystem.multMatrix(ms.peek().getModel());
			RenderSystem.scaled(-1/16d, -1/16d, 1/16d);
			RenderSystem.translated(1, -8, -1/64f);
			
			RenderSystem.pushMatrix();
			for (int x = 0; x <= bounds.getXSize(); x++) {
				RenderSystem.translated(-16, 0, 0);
				font.drawString(x == bounds.getXSize() ? "x" : "" + x, 0, 0, 0xFFFFFFFF);
			}
			RenderSystem.popMatrix();
			
			RenderSystem.pushMatrix();
			RenderSystem.scaled(-1, 1, 1);
			RenderSystem.rotatef(-90, 0, 1, 0);
			RenderSystem.translated(-8, -2, 2/64f);
			for (int z = 0; z <= bounds.getZSize(); z++) {
				RenderSystem.translated(16, 0, 0);
				font.drawString(z == bounds.getZSize() ? "z" : "" + z, 0, 0, 0xFFFFFFFF);
			}
			RenderSystem.popMatrix();
			
			buffer.draw();
			RenderSystem.popMatrix();
		}
		
		ms.pop();
	}

	protected void renderWidgets(int mouseX, int mouseY, float partialTicks) {
		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		int textColor = 0xeeeeee;

		{
			// Chapter title
			RenderSystem.pushMatrix();
			RenderSystem.translated(0, 0, 800);
			int x = icon.x + icon.getWidth() + 8;
			int y = icon.y;
			drawString(font, Lang.translate(PONDERING), x, y, 0xffa3a3a3);
			y += 12;
			x += 0;
			RenderSystem.translated(0, 3 * (indexDiff), 0);
			font.drawSplitString(scenes.get(index)
				.getTitle(), x, y, left.x - x, ColorHelper.applyAlpha(textColor, 1 - indexDiff));
			RenderSystem.popMatrix();
		}

		{
			// Scene overlay
			RenderSystem.pushMatrix();
			RenderSystem.translated(0, 0, 100);
			renderOverlay(index, partialTicks);
			if (indexDiff > 1 / 512f)
				renderOverlay(lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
			RenderSystem.popMatrix();
		}

		// Widgets
		widgets.forEach(w -> {
			if (w instanceof PonderButton) {
				PonderButton mtdButton = (PonderButton) w;
				mtdButton.fade(fade);
			}
		});

		if (index == 0 || index == 1 && lazyIndexValue < index)
			left.fade(lazyIndexValue);
		if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
			right.fade(scenes.size() - lazyIndexValue - 1);

		if (scenes.get(index).finished)
			right.flash();
		else
			right.dim();
	}

	protected void lowerButtonGroup(int index, int mouseX, int mouseY, float fade, AllIcons icon, KeyBinding key) {
		int bWidth = 20;
		int bHeight = 20;
		int bX = (width - bWidth) / 2 + (index - 1) * (bWidth + 8);
		int bY = height - bHeight - 31;

		RenderSystem.pushMatrix();
		if (fade < fadeIn.getChaseTarget())
			RenderSystem.translated(0, (1 - fade) * 5, 0);
		boolean hovered = isMouseOver(mouseX, mouseY, bX, bY, bWidth, bHeight);
		renderBox(bX, bY, bWidth, bHeight, hovered);
		icon.draw(bX + 2, bY + 2);
		drawCenteredString(font, key.getLocalizedName(), bX + bWidth / 2 + 8, bY + bHeight - 6, 0xff606060);
		RenderSystem.popMatrix();
	}

	private void renderOverlay(int i, float partialTicks) {
		RenderSystem.pushMatrix();
		PonderScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		story.renderOverlay(this, ms, partialTicks);
		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		MutableBoolean handled = new MutableBoolean(false);
		widgets.forEach(w -> {
			if (handled.booleanValue())
				return;
			if (!w.isMouseOver(x, y))
				return;
			if (w instanceof PonderButton) {
				PonderButton mtdButton = (PonderButton) w;
				mtdButton.runCallback();
				handled.setTrue();
				return;
			}
		});

		if (handled.booleanValue())
			return true;

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		GameSettings settings = Minecraft.getInstance().gameSettings;
		int sCode = settings.keyBindBack.getKey()
			.getKeyCode();
		int aCode = settings.keyBindLeft.getKey()
			.getKeyCode();
		int dCode = settings.keyBindRight.getKey()
			.getKeyCode();

		if (code == sCode) {
			onClose();
			return true;
		}

		if (code == aCode) {
			scroll(false);
			return true;
		}

		if (code == dCode) {
			scroll(true);
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	public FontRenderer getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public void drawString(String s, int x, int y, int color) {
		drawString(font, s, x, y, color);
	}

	public static void renderBox(int x, int y, int w, int h, boolean highlighted) {
		renderBox(x, y, w, h, 0xdd000000, highlighted ? 0x70ffffff : 0x30eebb00, highlighted ? 0x30ffffff : 0x10eebb00);
	}

	public static void renderBox(int x, int y, int w, int h, int backgroundColor, int borderColorStart,
		int borderColorEnd) {
		int zLevel = 100;
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 4, x + w + 3, y - 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y + h + 3, x + w + 3, y + h + 4, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + w + 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 4, y - 3, x - 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x + w + 3, y - 3, x + w + 4, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + h + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, x + w + 2, y - 3 + 1, x + w + 3, y + h + 3 - 1, borderColorStart,
			borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + w + 3, y - 3 + 1, borderColorStart, borderColorStart);
		GuiUtils.drawGradientRect(zLevel, x - 3, y + h + 2, x + w + 3, y + h + 3, borderColorEnd, borderColorEnd);
	}

}
