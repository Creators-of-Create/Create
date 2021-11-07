package com.simibubi.create.content.schematics.block;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static com.simibubi.create.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableContainer> {

	protected AllGuiTextures background;
	private List<Rect2i> extraAreas = Collections.emptyList();

	private ScrollInput schematicsArea;
	private IconButton confirmButton;
	private IconButton folderButton;
	private IconButton refreshButton;
	private Label schematicsLabel;

	private final Component uploading = Lang.translate("gui.schematicTable.uploading");
	private final Component finished = Lang.translate("gui.schematicTable.finished");
	private final Component refresh = Lang.translate("gui.schematicTable.refresh");
	private final Component folder = Lang.translate("gui.schematicTable.open_folder");
	private final Component noSchematics = Lang.translate("gui.schematicTable.noSchematics");
	private final Component availableSchematicsTitle = Lang.translate("gui.schematicTable.availableSchematics");
	private final ItemStack renderedItem = AllBlocks.SCHEMATIC_TABLE.asStack();

	private float progress;
	private float chasingProgress;
	private float lastChasingProgress;

	public SchematicTableScreen(SchematicTableContainer container, Inventory playerInventory,
		Component title) {
		super(container, playerInventory, title);
		background = AllGuiTextures.SCHEMATIC_TABLE;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height + 4 + AllGuiTextures.PLAYER_INVENTORY.height);
		setWindowOffset(-11, 8);
		super.init();
		widgets.clear();

		CreateClient.SCHEMATIC_SENDER.refresh();
		List<Component> availableSchematics = CreateClient.SCHEMATIC_SENDER.getAvailableSchematics();

		int x = leftPos;
		int y = topPos;

		schematicsLabel = new Label(x + 49, y + 26, TextComponent.EMPTY).withShadow();
		schematicsLabel.text = TextComponent.EMPTY;
		if (!availableSchematics.isEmpty()) {
			schematicsArea =
				new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(availableSchematics)
					.titled(availableSchematicsTitle.plainCopy())
					.writingTo(schematicsLabel);
			widgets.add(schematicsArea);
			widgets.add(schematicsLabel);
		}

		confirmButton = new IconButton(x + 44, y + 56, AllIcons.I_CONFIRM);

		folderButton = new IconButton(x + 21, y + 21, AllIcons.I_OPEN_FOLDER);
		folderButton.setToolTip(folder);
		refreshButton = new IconButton(x + 207, y + 21, AllIcons.I_REFRESH);
		refreshButton.setToolTip(refresh);

		widgets.add(confirmButton);
		widgets.add(folderButton);
		widgets.add(refreshButton);

		extraAreas = ImmutableList.of(
			new Rect2i(x + background.width, y + background.height - 40, 48, 48),
			new Rect2i(refreshButton.x, refreshButton.y, refreshButton.getWidth(), refreshButton.getHeight())
		);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.draw(ms, this, x, y);

		Component titleText;
		if (menu.getTileEntity().isUploading)
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
		boolean finished = menu.getSlot(1)
			.hasItem();

		if (menu.getTileEntity().isUploading || finished) {
			if (finished) {
				chasingProgress = lastChasingProgress = progress = 1;
			} else {
				lastChasingProgress = chasingProgress;
				progress = menu.getTileEntity().uploadingProgress;
				chasingProgress += (progress - chasingProgress) * .5f;
			}
			confirmButton.active = false;

			if (schematicsLabel != null) {
				schematicsLabel.colored(0xCCDDFF);
				String uploadingSchematic = menu.getTileEntity().uploadingSchematic;
				schematicsLabel.text = uploadingSchematic == null ? null : new TextComponent(uploadingSchematic);
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
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		ClientSchematicLoader schematicSender = CreateClient.SCHEMATIC_SENDER;

		if (confirmButton.active && confirmButton.isHovered() && ((SchematicTableContainer) menu).canWrite()
			&& schematicsArea != null) {

			lastChasingProgress = chasingProgress = progress = 0;
			List<Component> availableSchematics = schematicSender.getAvailableSchematics();
			Component schematic = availableSchematics.get(schematicsArea.getState());
			schematicSender.startNewUpload(schematic.getContents());
		}

		if (folderButton.isHovered()) {
			Util.getPlatform()
				.openFile(Paths.get("schematics/")
					.toFile());
		}

		if (refreshButton.isHovered()) {
			schematicSender.refresh();
			List<Component> availableSchematics = schematicSender.getAvailableSchematics();
			widgets.remove(schematicsArea);

			if (!availableSchematics.isEmpty()) {
				schematicsArea = new SelectionScrollInput(leftPos + 45, topPos + 21, 139, 18)
					.forOptions(availableSchematics)
					.titled(availableSchematicsTitle.plainCopy())
					.writingTo(schematicsLabel);
				schematicsArea.onChanged();
				widgets.add(schematicsArea);
			} else {
				schematicsArea = null;
				schematicsLabel.text = TextComponent.EMPTY;
			}
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
