package com.simibubi.create.gui;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.gui.widgets.DynamicLabel;
import com.simibubi.create.gui.widgets.OptionScrollArea;
import com.simibubi.create.gui.widgets.ScrollArea;
import com.simibubi.create.schematic.BlueprintHandler;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class BlueprintEditScreen extends AbstractSimiScreen {

	private TextFieldWidget xInput;
	private TextFieldWidget yInput;
	private TextFieldWidget zInput;

	private static final List<String> rotationOptions = ImmutableList.of("None", "Clockwise 90", "Clockwise 180",
			"Clockwise 270");
	private static final List<String> mirrorOptions = ImmutableList.of("None", "Left-Right", "Front-Back");

	private ScrollArea rotationArea;
	private ScrollArea mirrorArea;

	@Override
	protected void init() {
		setWindowSize(GuiResources.SCHEMATIC.width + 50, GuiResources.SCHEMATIC.height);
		int x = topLeftX;
		int y = topLeftY;
		BlueprintHandler bh = BlueprintHandler.instance;

		xInput = new TextFieldWidget(font, x + 75, y + 32, 32, 10, "");
		yInput = new TextFieldWidget(font, x + 115, y + 32, 32, 10, "");
		zInput = new TextFieldWidget(font, x + 155, y + 32, 32, 10, "");

		if (bh.deployed) {
			xInput.setText("" + bh.anchor.getX());
			yInput.setText("" + bh.anchor.getY());
			zInput.setText("" + bh.anchor.getZ());
		} else {
			BlockPos alt = minecraft.player.getPosition();
			xInput.setText("" + alt.getX());
			yInput.setText("" + alt.getY());
			zInput.setText("" + alt.getZ());
		}

		for (TextFieldWidget widget : new TextFieldWidget[] { xInput, yInput, zInput }) {
			widget.setMaxStringLength(6);
			widget.setEnableBackgroundDrawing(false);
			widget.setTextColor(0xFFFFFF);
			widget.changeFocus(false);
			widget.mouseClicked(0, 0, 0);
			widget.setValidator(s -> {
				if (s.isEmpty() || s.equals("-"))
					return true;
				try {
					Integer.parseInt(s);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			});
		}

		DynamicLabel labelR = new DynamicLabel(x + 99, y + 52, "").withShadow();
		rotationArea = new OptionScrollArea(x + 96, y + 49, 94, 14).forOptions(rotationOptions).titled("Rotation")
				.setState(bh.cachedSettings.getRotation().ordinal()).writingTo(labelR);

		DynamicLabel labelM = new DynamicLabel(x + 99, y + 72, "").withShadow();
		mirrorArea = new OptionScrollArea(x + 96, y + 69, 94, 14).forOptions(mirrorOptions).titled("Mirror")
				.setState(bh.cachedSettings.getMirror().ordinal()).writingTo(labelM);

		Collections.addAll(widgets, xInput, yInput, zInput);
		Collections.addAll(widgets, labelR, labelM, rotationArea, mirrorArea);

		super.init();
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {

		if (isPaste(code)) {
			String coords = minecraft.keyboardListener.getClipboardString();
			if (coords != null && !coords.isEmpty()) {
				coords.replaceAll(" ", "");
				String[] split = coords.split(",");
				if (split.length == 3) {
					boolean valid = true;
					for (String s : split) {
						try {
							Integer.parseInt(s);
						} catch (NumberFormatException e) {
							valid = false;
						}
					}
					if (valid) {
						xInput.setText(split[0]);
						yInput.setText(split[1]);
						zInput.setText(split[2]);
						return true;
					}
				}
			}
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int x = topLeftX;
		int y = topLeftY;
		GuiResources.SCHEMATIC.draw(this, x, y);
		BlueprintHandler bh = BlueprintHandler.instance;

		font.drawStringWithShadow(bh.cachedSchematicName, x + 103 - font.getStringWidth(bh.cachedSchematicName) / 2,
				y + 10, 0xDDEEFF);

		font.drawString("Position", x + 10, y + 32, GuiResources.FONT_COLOR);
		font.drawString("Rotation", x + 10, y + 52, GuiResources.FONT_COLOR);
		font.drawString("Mirror", x + 10, y + 72, GuiResources.FONT_COLOR);

		GlStateManager.pushMatrix();
		GlStateManager.translated(topLeftX + 220, topLeftY + 20, 0);
		GlStateManager.scaled(3, 3, 3);
		itemRenderer.renderItemIntoGUI(new ItemStack(AllItems.BLUEPRINT.get()), 0, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public void removed() {
		// notify Blueprinthandler
		BlueprintHandler bh = BlueprintHandler.instance;

		boolean validCoords = true;
		BlockPos newLocation = null;
		try {
			newLocation = new BlockPos(Integer.parseInt(xInput.getText()), Integer.parseInt(yInput.getText()),
					Integer.parseInt(zInput.getText()));
		} catch (NumberFormatException e) {
			validCoords = false;
		}

		if (validCoords) 
			bh.moveTo(newLocation);
		bh.setRotation(Rotation.values()[rotationArea.getState()]);
		bh.setMirror(Mirror.values()[mirrorArea.getState()]);
	}

}
