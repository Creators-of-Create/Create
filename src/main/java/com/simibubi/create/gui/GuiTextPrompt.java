package com.simibubi.create.gui;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;

public class GuiTextPrompt extends AbstractSimiScreen {

	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private TextFieldWidget nameField;
	private Button confirm;
	private Button abort;

	private String buttonTextConfirm;
	private String buttonTextAbort;
	private String title;

	private boolean confirmed;

	public GuiTextPrompt(Consumer<String> callBack, Consumer<String> abortCallback) {
		super();
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = "Confirm";
		buttonTextAbort = "Abort";
		confirmed = false;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(GuiResources.TEXT_INPUT.width, GuiResources.TEXT_INPUT.height + 30);

		this.nameField = new TextFieldWidget(font, topLeftX + 33, topLeftY + 26, 128, 8, "");
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.changeFocus(true);

		confirm = new Button(topLeftX - 5, topLeftY + 50, 100, 20, buttonTextConfirm, button -> {
			callback.accept(nameField.getText());
			confirmed = true;
			minecraft.displayGuiScreen(null);
		});

		abort = new Button(topLeftX + 100, topLeftY + 50, 100, 20, buttonTextAbort, button -> {
			minecraft.displayGuiScreen(null);
		});

		widgets.add(confirm);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	public void renderWindow(int mouseX, int mouseY, float partialTicks) {
		GuiResources.TEXT_INPUT.draw(this, topLeftX, topLeftY);
		font.drawString(title, topLeftX + (sWidth / 2) - (font.getStringWidth(title) / 2), topLeftY + 11,
				GuiResources.FONT_COLOR);
	}

	@Override
	public void removed() {
		if (!confirmed)
			abortCallback.accept(nameField.getText());
		super.removed();
	}

	public void setButtonTextConfirm(String buttonTextConfirm) {
		this.buttonTextConfirm = buttonTextConfirm;
	}

	public void setButtonTextAbort(String buttonTextAbort) {
		this.buttonTextAbort = buttonTextAbort;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			confirm.onPress();
			return true;
		}
		if (keyCode == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		}
		return nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
	}

}
