package com.simibubi.create.content.schematics.table;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static com.simibubi.create.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.ClientSchematicLoader;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableMenu> {

	private final Component uploading = Lang.translateDirect("gui.schematicTable.uploading");
	private final Component finished = Lang.translateDirect("gui.schematicTable.finished");
	private final Component refresh = Lang.translateDirect("gui.schematicTable.refresh");
	private final Component folder = Lang.translateDirect("gui.schematicTable.open_folder");
	private final Component noSchematics = Lang.translateDirect("gui.schematicTable.noSchematics");
	private final Component availableSchematicsTitle = Lang.translateDirect("gui.schematicTable.availableSchematics");

	protected AllGuiTextures background;

	private ScrollInput schematicsArea;
	private IconButton confirmButton;
	private IconButton folderButton;
	private IconButton refreshButton;
	private Label schematicsLabel;

	private float progress;
	private float chasingProgress;
	private float lastChasingProgress;

	private final ItemStack renderedItem = AllBlocks.SCHEMATIC_TABLE.asStack();

	private List<Rect2i> extraAreas = Collections.emptyList();

	public SchematicTableScreen(SchematicTableMenu menu, Inventory playerInventory,
		Component title) {
		super(menu, playerInventory, title);
		background = AllGuiTextures.SCHEMATIC_TABLE;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height + 4 + AllGuiTextures.PLAYER_INVENTORY.height);
		setWindowOffset(-11, 8);
		super.init();

		CreateClient.SCHEMATIC_SENDER.refresh();
		List<Component> availableSchematics = CreateClient.SCHEMATIC_SENDER.getAvailableSchematics();

		int x = leftPos;
		int y = topPos;

		schematicsLabel = new Label(x + 49, y + 26, Components.immutableEmpty()).withShadow();
		schematicsLabel.text = Components.immutableEmpty();
		if (!availableSchematics.isEmpty()) {
			schematicsArea =
				new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(availableSchematics)
					.titled(availableSchematicsTitle.plainCopy())
					.writingTo(schematicsLabel);
			addRenderableWidget(schematicsArea);
			addRenderableWidget(schematicsLabel);
		}

		confirmButton = new IconButton(x + 44, y + 56, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			if (menu.canWrite() && schematicsArea != null) {
				ClientSchematicLoader schematicSender = CreateClient.SCHEMATIC_SENDER;
				lastChasingProgress = chasingProgress = progress = 0;
				List<Component> availableSchematics1 = schematicSender.getAvailableSchematics();
				Component schematic = availableSchematics1.get(schematicsArea.getState());
				schematicSender.startNewUpload(schematic.getString());
			}
		});

		folderButton = new IconButton(x + 21, y + 21, AllIcons.I_OPEN_FOLDER);
		folderButton.withCallback(() -> {
			Util.getPlatform()
				.openFile(Paths.get("schematics/")
					.toFile());
		});
		folderButton.setToolTip(folder);
		refreshButton = new IconButton(x + 207, y + 21, AllIcons.I_REFRESH);
		refreshButton.withCallback(() -> {
			ClientSchematicLoader schematicSender = CreateClient.SCHEMATIC_SENDER;
			schematicSender.refresh();
			List<Component> availableSchematics1 = schematicSender.getAvailableSchematics();
			removeWidget(schematicsArea);

			if (!availableSchematics1.isEmpty()) {
				schematicsArea = new SelectionScrollInput(leftPos + 45, topPos + 21, 139, 18)
					.forOptions(availableSchematics1)
					.titled(availableSchematicsTitle.plainCopy())
					.writingTo(schematicsLabel);
				schematicsArea.onChanged();
				addRenderableWidget(schematicsArea);
			} else {
				schematicsArea = null;
				schematicsLabel.text = Components.immutableEmpty();
			}
		});
		refreshButton.setToolTip(refresh);

		addRenderableWidget(confirmButton);
		addRenderableWidget(folderButton);
		addRenderableWidget(refreshButton);

		extraAreas = ImmutableList.of(
			new Rect2i(x + background.width, y + background.height - 40, 48, 48),
			new Rect2i(refreshButton.x, refreshButton.y, refreshButton.getWidth(), refreshButton.getHeight())
		);
	}

	@Override
	protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.render(ms, x, y, this);

		Component titleText;
		if (menu.contentHolder.isUploading)
			titleText = uploading;
		else if (menu.getSlot(1)
			.hasItem())
			titleText = finished;
		else
			titleText = title;
		drawCenteredString(ms, font, titleText, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		if (schematicsArea == null)
			font.drawShadow(ms, noSchematics, x + 54, y + 26, 0xD3D3D3);

		GuiGameElement.of(renderedItem)
			.<GuiGameElement.GuiRenderBuilder>at(x + background.width, y + background.height - 40, -200)
			.scale(3)
			.render(ms);

		SCHEMATIC_TABLE_PROGRESS.bind();
		int width = (int) (SCHEMATIC_TABLE_PROGRESS.width
			* Mth.lerp(partialTicks, lastChasingProgress, chasingProgress));
		int height = SCHEMATIC_TABLE_PROGRESS.height;
		blit(ms, x + 70, y + 57, SCHEMATIC_TABLE_PROGRESS.startX,
			SCHEMATIC_TABLE_PROGRESS.startY, width, height);
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		boolean finished = menu.getSlot(1)
			.hasItem();

		if (menu.contentHolder.isUploading || finished) {
			if (finished) {
				chasingProgress = lastChasingProgress = progress = 1;
			} else {
				lastChasingProgress = chasingProgress;
				progress = menu.contentHolder.uploadingProgress;
				chasingProgress += (progress - chasingProgress) * .5f;
			}
			confirmButton.active = false;

			if (schematicsLabel != null) {
				schematicsLabel.colored(0xCCDDFF);
				String uploadingSchematic = menu.contentHolder.uploadingSchematic;
				schematicsLabel.text = uploadingSchematic == null ? null : Components.literal(uploadingSchematic);
			}
			if (schematicsArea != null)
				schematicsArea.visible = false;

		} else {
			progress = 0;
			chasingProgress = lastChasingProgress = 0;
			confirmButton.active = true;

			if (schematicsLabel != null)
				schematicsLabel.colored(0xFFFFFF);
			if (schematicsArea != null) {
				schematicsArea.writingTo(schematicsLabel);
				schematicsArea.visible = true;
			}
		}
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
