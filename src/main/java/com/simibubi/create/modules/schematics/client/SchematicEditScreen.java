package com.simibubi.create.modules.schematics.client;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;

public class SchematicEditScreen extends AbstractSimiScreen {

	private TextFieldWidget xInput;
	private TextFieldWidget yInput;
	private TextFieldWidget zInput;

	private final List<String> rotationOptions =
		Lang.translatedOptions("schematic.rotation", "none", "cw90", "cw180", "cw270");
	private final List<String> mirrorOptions =
		Lang.translatedOptions("schematic.mirror", "none", "leftRight", "frontBack");
	private final String positionLabel = Lang.translate("schematic.position");
	private final String rotationLabel = Lang.translate("schematic.rotation");
	private final String mirrorLabel = Lang.translate("schematic.mirror");

	private ScrollInput rotationArea;
	private ScrollInput mirrorArea;
	private SchematicHandler handler;

	@Override
	protected void init() {
		setWindowSize(ScreenResources.SCHEMATIC.width + 50, ScreenResources.SCHEMATIC.height);
		int x = guiLeft;
		int y = guiTop;
		handler = CreateClient.schematicHandler;

		xInput = new TextFieldWidget(font, x + 75, y + 32, 32, 10, "");
		yInput = new TextFieldWidget(font, x + 115, y + 32, 32, 10, "");
		zInput = new TextFieldWidget(font, x + 155, y + 32, 32, 10, "");

		BlockPos anchor = handler.getTransformation().getAnchor();
		if (handler.isDeployed()) {
			xInput.setText("" + anchor.getX());
			yInput.setText("" + anchor.getY());
			zInput.setText("" + anchor.getZ());
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

		PlacementSettings settings = handler.getTransformation().toSettings();
		Label labelR = new Label(x + 99, y + 52, "").withShadow();
		rotationArea = new SelectionScrollInput(x + 96, y + 49, 94, 14).forOptions(rotationOptions).titled("Rotation")
				.setState(settings.getRotation().ordinal()).writingTo(labelR);

		Label labelM = new Label(x + 99, y + 72, "").withShadow();
		mirrorArea = new SelectionScrollInput(x + 96, y + 69, 94, 14).forOptions(mirrorOptions).titled("Mirror")
				.setState(settings.getMirror().ordinal()).writingTo(labelM);

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
		int x = guiLeft;
		int y = guiTop;
		ScreenResources.SCHEMATIC.draw(this, x, y);

		font.drawStringWithShadow(handler.getCurrentSchematicName(),
				x + 103 - font.getStringWidth(handler.getCurrentSchematicName()) / 2, y + 10, 0xDDEEFF);

		font.drawString(positionLabel, x + 10, y + 32, ScreenResources.FONT_COLOR);
		font.drawString(rotationLabel, x + 10, y + 52, ScreenResources.FONT_COLOR);
		font.drawString(mirrorLabel, x + 10, y + 72, ScreenResources.FONT_COLOR);

		GlStateManager.pushMatrix();
		GlStateManager.translated(guiLeft + 220, guiTop + 20, 0);
		GlStateManager.scaled(3, 3, 3);
		itemRenderer.renderItemIntoGUI(new ItemStack(AllItems.BLUEPRINT.get()), 0, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public void removed() {
		boolean validCoords = true;
		BlockPos newLocation = null;
		try {
			newLocation = new BlockPos(Integer.parseInt(xInput.getText()), Integer.parseInt(yInput.getText()),
					Integer.parseInt(zInput.getText()));
		} catch (NumberFormatException e) {
			validCoords = false;
		}

		PlacementSettings settings = new PlacementSettings();
		settings.setRotation(Rotation.values()[rotationArea.getState()]);
		settings.setMirror(Mirror.values()[mirrorArea.getState()]);
		
		if (validCoords && newLocation != null) {
			ItemStack item = handler.getActiveSchematicItem();
			if (item != null) {
				item.getTag().putBoolean("Deployed", true);
				item.getTag().put("Anchor", NBTUtil.writeBlockPos(newLocation));
			}

			handler.getTransformation().init(newLocation, settings, handler.getBounds());
			handler.markDirty();
			handler.deploy();
		}

	}

}
