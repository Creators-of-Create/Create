package com.simibubi.create.gui;

import java.nio.file.Paths;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.block.SchematicTableContainer;
import com.simibubi.create.gui.widgets.Label;
import com.simibubi.create.gui.widgets.SelectionScrollInput;
import com.simibubi.create.gui.widgets.ScrollInput;
import com.simibubi.create.gui.widgets.IconButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.AtlasTexture;
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

	private float progress;
	private float chasingProgress;
	private float lastChasingProgress;

	public SchematicTableScreen(SchematicTableContainer container, PlayerInventory playerInventory,
			ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void init() {
		setWindowSize(ScreenResources.SCHEMATIC_TABLE.width, ScreenResources.SCHEMATIC_TABLE.height + 50);
		super.init();
		widgets.clear();
		
		int mainLeft = guiLeft - 56;
		int mainTop = guiTop - 16;

		Create.cSchematicLoader.refresh();
		List<String> availableSchematics = Create.cSchematicLoader.getAvailableSchematics();

		if (!availableSchematics.isEmpty()) {
			schematicsLabel = new Label(mainLeft + 36, mainTop + 26, "").withShadow();
			schematicsArea = new SelectionScrollInput(mainLeft + 33, mainTop + 23, 134, 14).forOptions(availableSchematics)
					.titled("Available Schematics").writingTo(schematicsLabel);
			widgets.add(schematicsArea);
			widgets.add(schematicsLabel);
		} 

		confirmButton = new IconButton(mainLeft + 69, mainTop + 55, ScreenResources.ICON_CONFIRM);
		folderButton = new IconButton(mainLeft + 204, mainTop + 6, ScreenResources.ICON_OPEN_FOLDER);
		refreshButton = new IconButton(mainLeft + 204, mainTop + 26, ScreenResources.ICON_REFRESH);
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

		ScreenResources.PLAYER_INVENTORY.draw(this, x- 16, y + 70 + 14);
		font.drawString("Inventory", x - 15 + 7, y + 64 + 26, 0x666666);

		ScreenResources.SCHEMATIC_TABLE.draw(this, mainLeft, mainTop);
		if (container.getTileEntity().isUploading)
			font.drawString("Uploading...", mainLeft + 76, mainTop + 10, ScreenResources.FONT_COLOR);
		else if (container.getSlot(1).getHasStack())
			font.drawString("Upload Finished!", mainLeft + 60, mainTop + 10, ScreenResources.FONT_COLOR);
		else
			font.drawString("Schematic Table", mainLeft + 60, mainTop + 10, ScreenResources.FONT_COLOR);

		if (schematicsArea == null) {
			font.drawStringWithShadow("  No Schematics Saved  ", mainLeft + 39, mainTop + 26, 0xFFDD44);
		}
		
		minecraft.getTextureManager().bindTexture(ScreenResources.SCHEMATIC_TABLE_PROGRESS.location);
		int width = (int) (ScreenResources.SCHEMATIC_TABLE_PROGRESS.width
				* MathHelper.lerp(partialTicks, lastChasingProgress, chasingProgress));
		int height = ScreenResources.SCHEMATIC_TABLE_PROGRESS.height;
		GlStateManager.disableLighting();
		blit(mainLeft + 94, mainTop + 56, ScreenResources.SCHEMATIC_TABLE_PROGRESS.startX,
				ScreenResources.SCHEMATIC_TABLE_PROGRESS.startY, width, height);

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.translated(mainLeft + 270, mainTop + 100, 200);
		GlStateManager.rotatef(50, -.5f, 1, -.2f);
		GlStateManager.scaled(50, -50, 50);

		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		minecraft.getBlockRendererDispatcher().renderBlockBrightness(AllBlocks.SCHEMATIC_TABLE.get().getDefaultState(),
				1);

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();

		GlStateManager.popMatrix();
	}

	@Override
	public void tick() {
		super.tick();
		boolean finished = container.getSlot(1).getHasStack();

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
		if (confirmButton.active && confirmButton.isHovered() && ((SchematicTableContainer) container).canWrite()
				&& schematicsArea != null) {

			lastChasingProgress = chasingProgress = progress = 0;
			List<String> availableSchematics = Create.cSchematicLoader.getAvailableSchematics();
			String schematic = availableSchematics.get(schematicsArea.getState());
			Create.cSchematicLoader.startNewUpload(schematic);
		}
		
		if (folderButton.isHovered()) {
			Util.getOSType().openFile(Paths.get("schematics/").toFile());
		}
		
		if (refreshButton.isHovered()) {
			Create.cSchematicLoader.refresh();
			List<String> availableSchematics = Create.cSchematicLoader.getAvailableSchematics();
			widgets.remove(schematicsArea);
			schematicsArea = new SelectionScrollInput(guiLeft - 56 + 33, guiTop - 16 + 23, 134, 14).forOptions(availableSchematics)
					.titled("Available Schematics").writingTo(schematicsLabel);
			widgets.add(schematicsArea);
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

}
