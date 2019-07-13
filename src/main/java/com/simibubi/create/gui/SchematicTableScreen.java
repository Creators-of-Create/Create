package com.simibubi.create.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.block.SchematicTableContainer;
import com.simibubi.create.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.gui.widgets.DynamicLabel;
import com.simibubi.create.gui.widgets.OptionScrollArea;
import com.simibubi.create.gui.widgets.ScrollArea;
import com.simibubi.create.gui.widgets.SimiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicTableScreen extends ContainerScreen<SchematicTableContainer>
		implements IHasContainer<SchematicTableContainer> {

	private ScrollArea schematics;
	private SimiButton button;
	private DynamicLabel label;

	private float progress;
	private float lastProgress;

	private int xTopLeft;
	private int yTopLeft;

	private int xMainWindow;
	private int yMainWindow;

	public SchematicTableScreen(SchematicTableContainer container, PlayerInventory playerInventory,
			ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void init() {
		super.init();
		xTopLeft = (width - GuiResources.SCHEMATIC_TABLE.width) / 2;
		yTopLeft = (height - GuiResources.SCHEMATIC_TABLE.height) / 2;
		xMainWindow = xTopLeft - 40;
		yMainWindow = yTopLeft - 80;
		buttons.clear();

		Create.cSchematicLoader.refresh();
		List<String> availableSchematics = Create.cSchematicLoader.getAvailableSchematics();

		if (!availableSchematics.isEmpty()) {
			label = new DynamicLabel(xMainWindow + 36, yMainWindow + 26, "").withShadow();
			schematics = new OptionScrollArea(xMainWindow + 33, yMainWindow + 23, 134, 14)
					.forOptions(availableSchematics).titled("Available Schematics").writingTo(label);
			buttons.add(schematics);
			buttons.add(label);
		} else {

		}

		button = new SimiButton(xMainWindow + 69, yMainWindow + 55, GuiResources.ICON_CONFIRM);
		buttons.add(button);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int x = xTopLeft;
		int y = yTopLeft;

		GuiResources.SCHEMATIC_TABLE.draw(this, xMainWindow, yMainWindow);
		GuiResources.PLAYER_INVENTORY.draw(this, x, y + 20);

		if (container.isUploading)
			font.drawString("Uploading...", xMainWindow + 76, yMainWindow + 10, GuiResources.FONT_COLOR);
		else
			font.drawString("Choose a Schematic", xMainWindow + 50, yMainWindow + 10, GuiResources.FONT_COLOR);
		font.drawString("Inventory", x + 7, y + 26, 0x666666);

		if (schematics == null) {
			font.drawStringWithShadow("  No Schematics Saved  ", xMainWindow + 39, yMainWindow + 26, 0xFFDD44);
		}

	}

	@Override
	public void render(int mouseX, int mouseY, float pt) {
		renderBackground();
		super.render(mouseX, mouseY, pt);

		minecraft.getTextureManager().bindTexture(GuiResources.SCHEMATIC_TABLE_PROGRESS.location);
		int width = (int) (GuiResources.SCHEMATIC_TABLE_PROGRESS.width * MathHelper.lerp(pt, lastProgress, progress));
		int height = GuiResources.SCHEMATIC_TABLE_PROGRESS.height;
		GlStateManager.disableLighting();
		blit(xMainWindow + 94, yMainWindow + 56, GuiResources.SCHEMATIC_TABLE_PROGRESS.startX,
				GuiResources.SCHEMATIC_TABLE_PROGRESS.startY, width, height);

		GlStateManager.pushLightingAttributes();
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.translated(xTopLeft + 220, yTopLeft + 30, 200);
		GlStateManager.rotatef(140, -.1f, 1, -.2f);
		GlStateManager.scaled(50, -50, 50);
		
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		minecraft.getBlockRendererDispatcher().renderBlock(AllBlocks.SCHEMATIC_TABLE.get().getDefaultState(),
				new BlockPos(0, 0, 0), minecraft.world, buffer, minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();

		GlStateManager.popMatrix();
		GlStateManager.popAttributes();

		renderHoveredToolTip(mouseX, mouseY);
		for (Widget w : buttons) {
			if (w instanceof AbstractSimiWidget && w.isHovered()) {
				List<String> toolTip = ((AbstractSimiWidget) w).getToolTip();
				renderTooltip(toolTip, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (container.isUploading) {
			lastProgress = progress;
			progress = Create.cSchematicLoader.getProgress(container.schematicUploading);
			label.colored(0xCCDDFF);
			button.active = false;

		} else {
			progress = 0;
			lastProgress = 0;
			label.colored(0xFFFFFF);
			button.active = true;
		}
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (button.isHovered() && ((SchematicTableContainer) container).canWrite() && schematics != null) {
			button.active = false;
			schematics.active = false;
			label.colored(0xCCDDFF);

			List<String> availableSchematics = Create.cSchematicLoader.getAvailableSchematics();
			lastProgress = progress = 0;
			String schematic = availableSchematics.get(schematics.getState());
			Create.cSchematicLoader.startNewUpload(schematic);
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		boolean b = false;
		for (Widget w : buttons) {
			if (w.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_))
				b = true;
		}
		return b || super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
	}

}
