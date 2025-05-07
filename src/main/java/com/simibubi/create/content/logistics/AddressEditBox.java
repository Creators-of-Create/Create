package com.simibubi.create.content.logistics;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AddressEditBox extends EditBox {

	private DestinationSuggestions destinationSuggestions;
	private Consumer<String> mainResponder;
	private String prevValue = "=)";
	private Field shiftPressedField;
	
	public AddressEditBox(Screen screen, Font pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom) {
		this(screen, pFont, pX, pY, pWidth, pHeight, anchorToBottom, null);
	}
	
	public AddressEditBox(Screen screen, Font pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom, String localAddress) {
        super(pFont, pX, pY, pWidth, pHeight, Component.empty());

		try {
			shiftPressedField = EditBox.class.getDeclaredField("shiftPressed");
			shiftPressedField.setAccessible(true);
		} catch (Exception e) {
			Create.LOGGER.error("Failed to get shiftPressed field from EditBox", e);
		}

		destinationSuggestions = AddressEditBoxHelper.createSuggestions(screen, this, anchorToBottom, localAddress);
		destinationSuggestions.setAllowSuggestions(true);
		destinationSuggestions.updateCommandInfo();
		mainResponder = t -> {
			if (!t.equals(prevValue))
				destinationSuggestions.updateCommandInfo();
			prevValue = t;
		};
		setResponder(mainResponder);
		setBordered(false);
		setFocused(false);
		mouseClicked(0, 0, 0);
		setMaxLength(25);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (destinationSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers))
			return true;
		if (isFocused() && pKeyCode == GLFW.GLFW_KEY_ENTER) {
			setFocused(false);
			moveCursorToEnd();
			mouseClicked(0, 0, 0);
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (destinationSuggestions.mouseScrolled(Mth.clamp(pDelta, -1.0D, 1.0D)))
			return true;
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			if (isMouseOver(pMouseX, pMouseY)) {
				setValue("");
				return true;
			}
		}
		
		boolean wasFocused = isFocused();
		if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
			if (!wasFocused) {
				setHighlightPos(0);
				setCursorPosition(getValue().length());
			}
			return true;
		}
		if (destinationSuggestions.mouseClicked((int) pMouseX, (int) pMouseY, pButton))
			return true;
		return false;
	}

	public boolean getShiftPressed() {
		try {
			return (boolean) shiftPressedField.get(this);
		} catch (Exception e) {
			Create.LOGGER.error("Failed to get shiftPressed field on EditBox", e);
			return false;
		}
	}
	public void setShiftPressed(boolean pressed) {
		try {
			shiftPressedField.setBoolean(this, pressed);
		} catch (Exception e) {
			Create.LOGGER.error("Failed to set shiftPressed field on EditBox", e);
		}
	}

	@Override
	public void setValue(String text) {
		/*
		 * Ensuring that shiftPressed is false before setting the value is a necessary workaround
		 * because the vanilla EditBox implementation doesn't account for setValue being called
		 * via non-key events (e.g. mouse click).
		 */
		boolean wasShiftPressed = getShiftPressed();
		setShiftPressed(false);
		super.setValue(text);
		setShiftPressed(wasShiftPressed);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		PoseStack matrixStack = pGuiGraphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 500);
		destinationSuggestions.render(pGuiGraphics, pMouseX, pMouseY);
		matrixStack.popPose();

		if (!destinationSuggestions.isEmpty())
			return;

		int itemX = getX() + width + 4;
		int itemY = getY() - 4;
		pGuiGraphics.renderItem(AllBlocks.CLIPBOARD.asStack(), itemX, itemY);
		if (pMouseX >= itemX && pMouseX < itemX + 16 && pMouseY >= itemY && pMouseY < itemY + 16) {
			List<Component> promiseTip = List.of();
			promiseTip = List.of(CreateLang.translate("gui.address_box.clipboard_tip")
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.address_box.clipboard_tip_1")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.address_box.clipboard_tip_2")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.address_box.clipboard_tip_3")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.address_box.clipboard_tip_4")
					.style(ChatFormatting.DARK_GRAY)
					.component());
			pGuiGraphics.renderComponentTooltip(Minecraft.getInstance().font, promiseTip, pMouseX, pMouseY);
		}
	}

	@Override
	public void setResponder(Consumer<String> pResponder) {
		super.setResponder(pResponder == mainResponder ? mainResponder : mainResponder.andThen(pResponder));
	}

	@Override
	public void tick() {
		super.tick();
		if (!isFocused())
			destinationSuggestions.hide();
		if (isFocused() && destinationSuggestions.suggestions == null)
			destinationSuggestions.updateCommandInfo();
		destinationSuggestions.tick();
	}

}
