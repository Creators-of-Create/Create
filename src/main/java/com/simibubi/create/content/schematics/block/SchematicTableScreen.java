package com.simibubi.create.content.schematics.block;

import static com.simibubi.create.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

import java.nio.file.Paths;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
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

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableContainer>
	implements IHasContainer<SchematicTableContainer> {

	private ScrollInput schematicsArea;
	private IconButton confirmButton;
	private IconButton folderButton;
	private IconButton refreshButton;
	private Label schematicsLabel;

	private final String title = Lang.translate("gui.schematicTable.title");
	private final String uploading = Lang.translate("gui.schematicTable.uploading");
	private final String finished = Lang.translate("gui.schematicTable.finished");
	private final String noSchematics = Lang.translate("gui.schematicTable.noSchematics");
	private final String availableSchematicsTitle = Lang.translate("gui.schematicTable.availableSchematics");

	private float progress;
	private float chasingProgress;
	private float lastChasingProgress;

	public SchematicTableScreen(SchematicTableContainer container, PlayerInventory playerInventory,
		ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void init() {
		setWindowSize(SCHEMATIC_TABLE.width, SCHEMATIC_TABLE.height + 50);
		super.init();
		widgets.clear();

		int mainLeft = guiLeft - 56;
		int mainTop = guiTop - 16;

		CreateClient.schematicSender.refresh();
		List<String> availableSchematics = CreateClient.schematicSender.getAvailableSchematics();

		schematicsLabel = new Label(mainLeft + 36, mainTop + 26, "").withShadow();
		schematicsLabel.text = "";
		if (!availableSchematics.isEmpty()) {
			schematicsArea =
				new SelectionScrollInput(mainLeft + 33, mainTop + 23, 134, 14).forOptions(availableSchematics)
					.titled(availableSchematicsTitle)
					.writingTo(schematicsLabel);
			widgets.add(schematicsArea);
			widgets.add(schematicsLabel);
		}

		confirmButton = new IconButton(mainLeft + 69, mainTop + 55, AllIcons.I_CONFIRM);
		folderButton = new IconButton(mainLeft + 204, mainTop + 6, AllIcons.I_OPEN_FOLDER);
		refreshButton = new IconButton(mainLeft + 204, mainTop + 26, AllIcons.I_REFRESH);
		widgets.add(confirmButton);
		widgets.add(folderButton);
		widgets.add(refreshButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {

		int x = guiLeft + 20;
		int y = guiTop;

		int mainLeft = guiLeft - 56;
		int mainTop = guiTop - 16;

		AllGuiTextures.PLAYER_INVENTORY.draw(this, x - 16, y + 70 + 14);
		font.drawString(playerInventory.getDisplayName()
			.getFormattedText(), x - 15 + 7, y + 64 + 26, 0x666666);

		SCHEMATIC_TABLE.draw(this, mainLeft, mainTop);
		
		if (container.getTileEntity().isUploading)
			font.drawString(uploading, mainLeft + 76, mainTop + 10, AllGuiTextures.FONT_COLOR);
		else if (container.getSlot(1).getHasStack())
			font.drawString(finished, mainLeft + 60, mainTop + 10, AllGuiTextures.FONT_COLOR);
		else
			font.drawString(title, mainLeft + 60, mainTop + 10, AllGuiTextures.FONT_COLOR);
		if (schematicsArea == null) 
			font.drawStringWithShadow(noSchematics, mainLeft + 39, mainTop + 26, 0xFFDD44);
		
		RenderSystem.pushMatrix();
		RenderSystem.translated(mainLeft + 217, mainTop + 48, 200);
		RenderSystem.scaled(3, 3, 3);
		GuiGameElement.of(AllBlocks.SCHEMATIC_TABLE.asStack()).render();
		RenderSystem.popMatrix();

		minecraft.getTextureManager()
			.bindTexture(SCHEMATIC_TABLE_PROGRESS.location);
		int width = (int) (SCHEMATIC_TABLE_PROGRESS.width
			* MathHelper.lerp(partialTicks, lastChasingProgress, chasingProgress));
		int height = SCHEMATIC_TABLE_PROGRESS.height;
		RenderSystem.disableLighting();
		drawTexture(mainLeft + 94, mainTop + 56, SCHEMATIC_TABLE_PROGRESS.startX, SCHEMATIC_TABLE_PROGRESS.startY, width,
			height);

	}

	@Override
	public void tick() {
		super.tick();
		boolean finished = container.getSlot(1)
			.getHasStack();

		if (container.getTileEntity().isUploading || finished) {
			if (finished) {
				chasingProgress = lastChasingProgress = progress = 1;
			} else {
				lastChasingProgress = chasingProgress;
				progress = container.getTileEntity().uploadingProgress;
				chasingProgress += (progress - chasingProgress) * .5f;
			}
			confirmButton.active = false;

			if (schematicsLabel != null) {
				schematicsLabel.colored(0xCCDDFF);
				schematicsLabel.text = container.getTileEntity().uploadingSchematic;
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
		ClientSchematicLoader schematicSender = CreateClient.schematicSender;

		if (confirmButton.active && confirmButton.isHovered() && ((SchematicTableContainer) container).canWrite()
			&& schematicsArea != null) {

			lastChasingProgress = chasingProgress = progress = 0;
			List<String> availableSchematics = schematicSender.getAvailableSchematics();
			String schematic = availableSchematics.get(schematicsArea.getState());
			schematicSender.startNewUpload(schematic);
		}

		if (folderButton.isHovered()) {
			Util.getOSType()
				.openFile(Paths.get("schematics/")
					.toFile());
		}

		if (refreshButton.isHovered()) {
			schematicSender.refresh();
			List<String> availableSchematics = schematicSender.getAvailableSchematics();
			widgets.remove(schematicsArea);

			if (!availableSchematics.isEmpty()) {
				schematicsArea = new SelectionScrollInput(guiLeft - 56 + 33, guiTop - 16 + 23, 134, 14)
					.forOptions(availableSchematics)
					.titled(availableSchematicsTitle)
					.writingTo(schematicsLabel);
				widgets.add(schematicsArea);
			} else {
				schematicsArea = null;
				schematicsLabel.text = "";
			}
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

}
