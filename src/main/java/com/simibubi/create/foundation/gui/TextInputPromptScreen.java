package com.simibubi.create.foundation.gui;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;

public class TextInputPromptScreen extends AbstractSimiScreen {

	private final ITextComponent defaultConfirm = Lang.translate("action.confirm");
	private final ITextComponent defaultAbort = Lang.translate("action.abort");
	
	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private TextFieldWidget nameField;
	private Button confirm;
	private Button abort;

	private ITextComponent buttonTextConfirm;
	private ITextComponent buttonTextAbort;
	private ITextComponent title;

	private boolean confirmed;

	public TextInputPromptScreen(Consumer<String> callBack, Consumer<String> abortCallback) {
		super();
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = defaultConfirm;
		buttonTextAbort = defaultAbort;
		confirmed = false;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(AllGuiTextures.TEXT_INPUT.width, AllGuiTextures.TEXT_INPUT.height + 30);

		this.nameField = new TextFieldWidget(textRenderer, guiLeft + 33, guiTop + 26, 128, 8, StringTextComponent.EMPTY);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.changeFocus(true);

		confirm = new Button(guiLeft - 5, guiTop + 50, 100, 20, buttonTextConfirm, button -> {
			callback.accept(nameField.getText());
			confirmed = true;
			client.displayGuiScreen(null);
		});

		abort = new Button(guiLeft + 100, guiTop + 50, 100, 20, buttonTextAbort, button -> {
			client.displayGuiScreen(null);
		});

		widgets.add(confirm);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	public void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.TEXT_INPUT.draw(this, guiLeft, guiTop);
		textRenderer.draw(matrixStack, title, guiLeft + (sWidth / 2) - (textRenderer.getWidth(title) / 2), guiTop + 11,
				AllGuiTextures.FONT_COLOR);
	}

	@Override
	public void removed() {
		if (!confirmed)
			abortCallback.accept(nameField.getText());
		super.removed();
	}

	public void setButtonTextConfirm(ITextComponent buttonTextConfirm) {
		this.buttonTextConfirm = buttonTextConfirm;
	}

	public void setButtonTextAbort(ITextComponent buttonTextAbort) {
		this.buttonTextAbort = buttonTextAbort;
	}

	public void setTitle(ITextComponent title) {
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
