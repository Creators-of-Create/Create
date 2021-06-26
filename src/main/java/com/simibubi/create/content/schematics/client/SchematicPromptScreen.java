package com.simibubi.create.content.schematics.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SchematicPromptScreen extends AbstractSimiScreen {

	private AllGuiTextures background;

	private final ITextComponent convertLabel = Lang.translate("schematicAndQuill.convert");
	private final ITextComponent abortLabel = Lang.translate("action.discard");
	private final ITextComponent confirmLabel = Lang.translate("action.saveToFile");

	private TextFieldWidget nameField;
	private IconButton confirm;
	private IconButton abort;
	private IconButton convert;

	public SchematicPromptScreen() {
		super(Lang.translate("schematicAndQuill.title"));
		background = AllGuiTextures.SCHEMATIC_PROMPT;
	}

	@Override
	public void init() {
		setWindowSize(background.width, background.height);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		nameField = new TextFieldWidget(textRenderer, x + 49, y + 26, 131, 10, StringTextComponent.EMPTY);
		nameField.setTextColor(-1);
		nameField.setDisabledTextColour(-1);
		nameField.setEnableBackgroundDrawing(false);
		nameField.setMaxStringLength(35);
		nameField.changeFocus(true);

		abort = new IconButton(x + 7, y + 53, AllIcons.I_TRASH);
		abort.setToolTip(abortLabel);
		widgets.add(abort);

		confirm = new IconButton(x + 158, y + 53, AllIcons.I_CONFIRM);
		confirm.setToolTip(confirmLabel);
		widgets.add(confirm);

		convert = new IconButton(x + 180, y + 53, AllIcons.I_SCHEMATIC);
		convert.setToolTip(convertLabel);
		widgets.add(convert);

		widgets.add(confirm);
		widgets.add(convert);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);
		drawCenteredText(ms, textRenderer, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
		GuiGameElement.of(AllItems.SCHEMATIC.asStack())
				.at(x + 22, y + 23, 0)
				.render(ms);

		GuiGameElement.of(AllItems.SCHEMATIC_AND_QUILL.asStack())
				.scale(3)
				.at(x + background.width + 6, y + background.height - 40, -200)
				.render(ms);
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			confirm(false);
			return true;
		}
		if (keyCode == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		}
		return nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirm.isHovered()) {
			confirm(false);
			return true;
		}
		if (abort.isHovered()) {
			CreateClient.SCHEMATIC_AND_QUILL_HANDLER.discard();
			client.player.closeScreen();
			return true;
		}
		if (convert.isHovered()) {
			confirm(true);
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	private void confirm(boolean convertImmediately) {
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.saveSchematic(nameField.getText(), convertImmediately);
		client.player.closeScreen();
	}

}
