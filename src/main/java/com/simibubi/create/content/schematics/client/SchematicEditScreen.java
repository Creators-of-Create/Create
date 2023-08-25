package com.simibubi.create.content.schematics.client;

import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class SchematicEditScreen extends AbstractSimiScreen {

	private final List<Component> rotationOptions =
		Lang.translatedOptions("schematic.rotation", "none", "cw90", "cw180", "cw270");
	private final List<Component> mirrorOptions =
		Lang.translatedOptions("schematic.mirror", "none", "leftRight", "frontBack");
	private final Component rotationLabel = Lang.translateDirect("schematic.rotation");
	private final Component mirrorLabel = Lang.translateDirect("schematic.mirror");

	private AllGuiTextures background;

	private EditBox xInput;
	private EditBox yInput;
	private EditBox zInput;
	private IconButton confirmButton;

	private ScrollInput rotationArea;
	private ScrollInput mirrorArea;
	private SchematicHandler handler;

	public SchematicEditScreen() {
		background = AllGuiTextures.SCHEMATIC;
		handler = CreateClient.SCHEMATIC_HANDLER;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-6, 0);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		xInput = new EditBox(font, x + 50, y + 26, 34, 10, Components.immutableEmpty());
		yInput = new EditBox(font, x + 90, y + 26, 34, 10, Components.immutableEmpty());
		zInput = new EditBox(font, x + 130, y + 26, 34, 10, Components.immutableEmpty());

		BlockPos anchor = handler.getTransformation()
				.getAnchor();
		if (handler.isDeployed()) {
			xInput.setValue("" + anchor.getX());
			yInput.setValue("" + anchor.getY());
			zInput.setValue("" + anchor.getZ());
		} else {
			BlockPos alt = minecraft.player.blockPosition();
			xInput.setValue("" + alt.getX());
			yInput.setValue("" + alt.getY());
			zInput.setValue("" + alt.getZ());
		}

		for (EditBox widget : new EditBox[] { xInput, yInput, zInput }) {
			widget.setMaxLength(6);
			widget.setBordered(false);
			widget.setTextColor(0xFFFFFF);
			widget.setFocused(false);
			widget.mouseClicked(0, 0, 0);
			widget.setFilter(s -> {
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

		StructurePlaceSettings settings = handler.getTransformation()
			.toSettings();
		Label labelR = new Label(x + 50, y + 48, Components.immutableEmpty()).withShadow();
		rotationArea = new SelectionScrollInput(x + 45, y + 43, 118, 18).forOptions(rotationOptions)
			.titled(rotationLabel.plainCopy())
			.setState(settings.getRotation()
				.ordinal())
			.writingTo(labelR);

		Label labelM = new Label(x + 50, y + 70, Components.immutableEmpty()).withShadow();
		mirrorArea = new SelectionScrollInput(x + 45, y + 65, 118, 18).forOptions(mirrorOptions)
			.titled(mirrorLabel.plainCopy())
			.setState(settings.getMirror()
				.ordinal())
			.writingTo(labelM);

		addRenderableWidgets(xInput, yInput, zInput);
		addRenderableWidgets(labelR, labelM, rotationArea, mirrorArea);

		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			onClose();
		});
		addRenderableWidget(confirmButton);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (isPaste(code)) {
			String coords = minecraft.keyboardHandler.getClipboard();
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
						xInput.setValue(split[0]);
						yInput.setValue(split[1]);
						zInput.setValue(split[2]);
						return true;
					}
				}
			}
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);
		String title = handler.getCurrentSchematicName();
		graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		GuiGameElement.of(AllItems.SCHEMATIC.asStack())
				.<GuiGameElement.GuiRenderBuilder>at(x + background.width + 6, y + background.height - 40, -200)
				.scale(3)
				.render(graphics);
	}

	@Override
	public void removed() {
		boolean validCoords = true;
		BlockPos newLocation = null;
		try {
			newLocation = new BlockPos(Integer.parseInt(xInput.getValue()), Integer.parseInt(yInput.getValue()),
				Integer.parseInt(zInput.getValue()));
		} catch (NumberFormatException e) {
			validCoords = false;
		}

		StructurePlaceSettings settings = new StructurePlaceSettings();
		settings.setRotation(Rotation.values()[rotationArea.getState()]);
		settings.setMirror(Mirror.values()[mirrorArea.getState()]);

		if (validCoords && newLocation != null) {
			ItemStack item = handler.getActiveSchematicItem();
			if (item != null) {
				item.getTag()
					.putBoolean("Deployed", true);
				item.getTag()
					.put("Anchor", NbtUtils.writeBlockPos(newLocation));
			}

			handler.getTransformation()
				.init(newLocation, settings, handler.getBounds());
			handler.markDirty();
			handler.deploy();
		}
	}

}
