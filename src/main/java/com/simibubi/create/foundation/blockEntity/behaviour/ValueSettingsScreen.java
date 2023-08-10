package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ValueSettingsScreen extends AbstractSimiScreen {

	private int ticksOpen;
	private ValueSettingsBoard board;
	private int maxLabelWidth;
	private int valueBarWidth;
	private BlockPos pos;
	private ValueSettings initialSettings;
	private ValueSettings lastHovered = new ValueSettings(-1, -1);
	private Consumer<ValueSettings> onHover;
	private boolean iconMode;
	private int milestoneSize;
	private int soundCoolDown;

	public ValueSettingsScreen(BlockPos pos, ValueSettingsBoard board, ValueSettings valueSettings,
		Consumer<ValueSettings> onHover) {
		this.pos = pos;
		this.board = board;
		this.initialSettings = valueSettings;
		this.onHover = onHover;
		this.iconMode = board.formatter() instanceof ScrollOptionSettingsFormatter;
		this.milestoneSize = iconMode ? 8 : 4;
	}

	@Override
	protected void init() {
		int maxValue = board.maxValue();
		maxLabelWidth = 0;
		int milestoneCount = maxValue / board.milestoneInterval() + 1;
		int scale = maxValue > 128 ? 1 : 2;

		for (Component component : board.rows())
			maxLabelWidth = Math.max(maxLabelWidth, font.width(component));
		if (iconMode)
			maxLabelWidth = -18;

		valueBarWidth = (maxValue + 1) * scale + 1 + milestoneCount * milestoneSize;
		int width = (maxLabelWidth + 14) + (valueBarWidth + 10);
		int height = (board.rows()
			.size() * 11);

		setWindowSize(width, height);
		super.init();

		Vec2 coordinateOfValue = getCoordinateOfValue(initialSettings.row(), initialSettings.value());
		setCursor(coordinateOfValue);
	}

	private void setCursor(Vec2 coordinateOfValue) {
		double guiScale = minecraft.getWindow()
			.getGuiScale();
		GLFW.glfwSetCursorPos(minecraft.getWindow()
			.getWindow(), coordinateOfValue.x * guiScale, coordinateOfValue.y * guiScale);
	}

	public ValueSettings getClosestCoordinate(int mouseX, int mouseY) {
		int row = 0;
		int column = 0;
		boolean milestonesOnly = hasShiftDown();

		double bestDiff = Double.MAX_VALUE;
		for (; row < board.rows()
			.size(); row++) {
			Vec2 coord = getCoordinateOfValue(row, 0);
			double diff = Math.abs(coord.y - mouseY);
			if (bestDiff < diff)
				break;
			bestDiff = diff;
		}
		row -= 1;

		bestDiff = Double.MAX_VALUE;
		for (; column <= board.maxValue(); column++) {
			Vec2 coord = getCoordinateOfValue(row, milestonesOnly ? column * board.milestoneInterval() : column);
			double diff = Math.abs(coord.x - mouseX);
			if (bestDiff < diff)
				break;
			bestDiff = diff;
		}
		column -= 1;

		return new ValueSettings(row,
			milestonesOnly ? Math.min(column * board.milestoneInterval(), board.maxValue()) : column);
	}

	public Vec2 getCoordinateOfValue(int row, int column) {
		int scale = board.maxValue() > 128 ? 1 : 2;
		float xOut =
			guiLeft + ((Math.max(1, column) - 1) / board.milestoneInterval()) * milestoneSize + column * scale + 1.5f;
		xOut += maxLabelWidth + 14 + 4;

		if (column % board.milestoneInterval() == 0)
			xOut += milestoneSize / 2;
		if (column > 0)
			xOut += milestoneSize;

		float yOut = guiTop + (row + .5f) * 11 - .5f;
		return new Vec2(xOut, yOut);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;
		int milestoneCount = board.maxValue() / board.milestoneInterval() + 1;
		int blitOffset = getBlitOffset();
		int scale = board.maxValue() > 128 ? 1 : 2;

		Component title = board.title();
		Component tip = CreateLang.translateDirect("gui.value_settings.release_to_confirm", Components.keybind("key.use"));
		double fadeIn = Math.pow(Mth.clamp((ticksOpen + partialTicks) / 4.0, 0, 1), 1);

		int fattestLabel = Math.max(font.width(tip), font.width(title));
		if (iconMode)
			for (int i = 0; i <= board.maxValue(); i++)
				fattestLabel = Math.max(fattestLabel, font.width(board.formatter()
					.format(new ValueSettings(0, i))));

		int fatTipOffset = Math.max(0, fattestLabel + 10 - (windowWidth + 13)) / 2;
		int bgWidth = Math.max((windowWidth + 13), fattestLabel + 10);
		int fadeInWidth = (int) (bgWidth * fadeIn);
		int fadeInStart = (bgWidth - fadeInWidth) / 2 - fatTipOffset;
		int additionalHeight = iconMode ? 46 : 33;

		UIRenderHelper.drawStretched(ms, x - 11 + fadeInStart, y - 17, fadeInWidth, windowHeight + additionalHeight,
			blitOffset, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);
		UIRenderHelper.drawStretched(ms, x - 10 + fadeInStart, y - 18, fadeInWidth - 2, 1, blitOffset,
			AllGuiTextures.VALUE_SETTINGS_OUTER_BG);
		UIRenderHelper.drawStretched(ms, x - 10 + fadeInStart, y - 17 + windowHeight + additionalHeight,
			fadeInWidth - 2, 1, blitOffset, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);

		if (fadeInWidth > fattestLabel) {
			int textX = x - 11 - fatTipOffset + bgWidth / 2;
			font.draw(ms, title, textX - font.width(title) / 2, y - 14, 0xdddddd);
			font.draw(ms, tip, textX - font.width(tip) / 2, y + windowHeight + additionalHeight - 27, 0xdddddd);
		}

		renderBrassFrame(ms, x + maxLabelWidth + 14, y - 3, valueBarWidth + 8, board.rows()
			.size() * 11 + 5);
		UIRenderHelper.drawStretched(ms, x + maxLabelWidth + 17, y, valueBarWidth + 2, board.rows()
			.size() * 11 - 1, blitOffset, AllGuiTextures.VALUE_SETTINGS_BAR_BG);

		int originalY = y;
		for (Component component : board.rows()) {
			int valueBarX = x + maxLabelWidth + 14 + 4;

			if (!iconMode) {
				UIRenderHelper.drawCropped(ms, x - 4, y, maxLabelWidth + 8, 11, blitOffset,
					AllGuiTextures.VALUE_SETTINGS_LABEL_BG);
				for (int w = 0; w < valueBarWidth; w += AllGuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1)
					UIRenderHelper.drawCropped(ms, valueBarX + w, y + 1,
						Math.min(AllGuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1, valueBarWidth - w), 8, blitOffset,
						AllGuiTextures.VALUE_SETTINGS_BAR);
				font.draw(ms, component, x, y + 1, 0x442000);
			}

			int milestoneX = valueBarX;
			for (int milestone = 0; milestone < milestoneCount; milestone++) {
				if (iconMode)
					AllGuiTextures.VALUE_SETTINGS_WIDE_MILESTONE.render(ms, milestoneX, y + 1);
				else
					AllGuiTextures.VALUE_SETTINGS_MILESTONE.render(ms, milestoneX, y + 1);
				milestoneX += milestoneSize + board.milestoneInterval() * scale;
			}

			y += 11;
		}

		if (!iconMode)
			renderBrassFrame(ms, x - 7, originalY - 3, maxLabelWidth + 14, board.rows()
				.size() * 11 + 5);

		if (ticksOpen < 1)
			return;

		ValueSettings closest = getClosestCoordinate(mouseX, mouseY);

		if (!closest.equals(lastHovered)) {
			onHover.accept(closest);
			if (soundCoolDown == 0) {
				float pitch = (closest.value()) / (float) (board.maxValue());
				pitch = Mth.lerp(pitch, 1.15f, 1.5f);
				minecraft.getSoundManager()
					.play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(), pitch, 0.25F));
				ScrollValueHandler.wrenchCog.bump(3, -(closest.value() - lastHovered.value()) * 10);
				soundCoolDown = 1;
			}
		}
		lastHovered = closest;

		Vec2 coordinate = getCoordinateOfValue(closest.row(), closest.value());
		Component cursorText = board.formatter()
			.format(closest);

		AllIcons cursorIcon = null;
		if (board.formatter() instanceof ScrollOptionSettingsFormatter sosf)
			cursorIcon = sosf.getIcon(closest);

		int cursorWidth = ((cursorIcon != null ? 16 : font.width(cursorText)) / 2) * 2 + 3;
		int cursorX = ((int) (coordinate.x)) - cursorWidth / 2;
		int cursorY = ((int) (coordinate.y)) - 7;

		if (cursorIcon != null) {
			AllGuiTextures.VALUE_SETTINGS_CURSOR_ICON.render(ms, cursorX - 2, cursorY - 3);
			RenderSystem.setShaderColor(0.265625f, 0.125f, 0, 1);
			cursorIcon.render(ms, cursorX + 1, cursorY - 1);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			if (fadeInWidth > fattestLabel)
				font.draw(ms, cursorText, x - 11 - fatTipOffset + (bgWidth - font.width(cursorText)) / 2,
					originalY + windowHeight + additionalHeight - 40, 0xFBDC7D);
			return;
		}

		AllGuiTextures.VALUE_SETTINGS_CURSOR_LEFT.render(ms, cursorX - 3, cursorY);
		UIRenderHelper.drawCropped(ms, cursorX, cursorY, cursorWidth, 14, blitOffset,
			AllGuiTextures.VALUE_SETTINGS_CURSOR);
		AllGuiTextures.VALUE_SETTINGS_CURSOR_RIGHT.render(ms, cursorX + cursorWidth, cursorY);

		font.draw(ms, cursorText, cursorX + 2, cursorY + 3, 0x442000);
	}

	protected void renderBrassFrame(PoseStack ms, int x, int y, int w, int h) {
		AllGuiTextures.BRASS_FRAME_TL.render(ms, x, y);
		AllGuiTextures.BRASS_FRAME_TR.render(ms, x + w - 4, y);
		AllGuiTextures.BRASS_FRAME_BL.render(ms, x, y + h - 4);
		AllGuiTextures.BRASS_FRAME_BR.render(ms, x + w - 4, y + h - 4);

		if (h > 8) {
			UIRenderHelper.drawStretched(ms, x, y + 4, 3, h - 8, getBlitOffset(), AllGuiTextures.BRASS_FRAME_LEFT);
			UIRenderHelper.drawStretched(ms, x + w - 3, y + 4, 3, h - 8, getBlitOffset(),
				AllGuiTextures.BRASS_FRAME_RIGHT);
		}

		if (w > 8) {
			UIRenderHelper.drawCropped(ms, x + 4, y, w - 8, 3, getBlitOffset(), AllGuiTextures.BRASS_FRAME_TOP);
			UIRenderHelper.drawCropped(ms, x + 4, y + h - 3, w - 8, 3, getBlitOffset(),
				AllGuiTextures.BRASS_FRAME_BOTTOM);
		}

	}

	@Override
	public void renderBackground(PoseStack p_238651_1_, int p_238651_2_) {
		int a = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f))) << 24;
		fillGradient(p_238651_1_, 0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
	}

	@Override
	public void tick() {
		ticksOpen++;
		if (soundCoolDown > 0)
			soundCoolDown--;
		super.tick();
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		ValueSettings closest = getClosestCoordinate((int) pMouseX, (int) pMouseY);
		int column = closest.value() + ((int) Math.signum(pDelta)) * (hasShiftDown() ? board.milestoneInterval() : 1);
		column = Mth.clamp(column, 0, board.maxValue());
		if (column == closest.value())
			return false;
		setCursor(getCoordinateOfValue(closest.row(), column));
		return true;
	}

	@Override
	public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
		if (minecraft.options.keyUse.matches(pKeyCode, pScanCode)) {
			Window window = minecraft.getWindow();
			double x = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			saveAndClose(x, y);
			return true;
		}
		return super.keyReleased(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		if (minecraft.options.keyUse.matchesMouse(pButton)) {
			saveAndClose(pMouseX, pMouseY);
			return true;
		}
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	protected void saveAndClose(double pMouseX, double pMouseY) {
		ValueSettings closest = getClosestCoordinate((int) pMouseX, (int) pMouseY);
		// FIXME: value settings may be face-sensitive on future components
		AllPackets.getChannel()
			.sendToServer(new ValueSettingsPacket(pos, closest.row(), closest.value(), null, Direction.UP,
				AllKeys.ctrlDown()));
		onClose();
	}

	@Override
	public void onClose() {
		super.onClose();
	}

}
