package com.simibubi.create.gui;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.block.SchematicannonContainer;
import com.simibubi.create.block.SchematicannonTileEntity;
import com.simibubi.create.gui.widgets.Indicator;
import com.simibubi.create.gui.widgets.Indicator.State;
import com.simibubi.create.gui.widgets.IconButton;
import com.simibubi.create.networking.ConfigureSchematicannonPacket;
import com.simibubi.create.networking.ConfigureSchematicannonPacket.Option;
import com.simibubi.create.networking.AllPackets;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonContainer> {

	protected Vector<Indicator> replaceLevelIndicators;
	protected Vector<IconButton> replaceLevelButtons;

	protected IconButton skipMissingButton;
	protected Indicator skipMissingIndicator;
	protected IconButton skipTilesButton;
	protected Indicator skipTilesIndicator;

	protected IconButton playButton;
	protected Indicator playIndicator;
	protected IconButton pauseButton;
	protected Indicator pauseIndicator;
	protected IconButton resetButton;
	protected Indicator resetIndicator;

	public SchematicannonScreen(SchematicannonContainer container, PlayerInventory inventory,
			ITextComponent p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
	}

	@Override
	protected void init() {
		setWindowSize(ScreenResources.SCHEMATICANNON.width + 50, ScreenResources.SCHEMATICANNON.height + 80);
		super.init();

		int x = guiLeft + 20;
		int y = guiTop;

		widgets.clear();

		// Play Pause Stop
		playButton = new IconButton(x + 70, y + 55, ScreenResources.ICON_PLAY);
		playIndicator = new Indicator(x + 70, y + 50, "");
		pauseButton = new IconButton(x + 88, y + 55, ScreenResources.ICON_PAUSE);
		pauseIndicator = new Indicator(x + 88, y + 50, "");
		resetButton = new IconButton(x + 106, y + 55, ScreenResources.ICON_STOP);
		resetIndicator = new Indicator(x + 106, y + 50, "Not Running");
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
				resetIndicator);

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<ScreenResources> icons = ImmutableList.of(ScreenResources.ICON_DONT_REPLACE, ScreenResources.ICON_REPLACE_SOLID,
				ScreenResources.ICON_REPLACE_ANY, ScreenResources.ICON_REPLACE_EMPTY);
		List<String> toolTips = ImmutableList.of("Don't Replace Solid Blocks", "Replace Solid with Solid",
				"Replace Solid with Any", "Replace Solid with Empty");

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 16 + i * 18, y + 96, ""));
			replaceLevelButtons.add(new IconButton(x + 16 + i * 18, y + 101, icons.get(i)));
			replaceLevelButtons.get(i).setToolTip(toolTips.get(i));
		}
		widgets.addAll(replaceLevelButtons);
		widgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 106, y + 101, ScreenResources.ICON_SKIP_MISSING);
		skipMissingButton.setToolTip("Skip missing Blocks");
		skipMissingIndicator = new Indicator(x + 106, y + 96, "");
		Collections.addAll(widgets, skipMissingButton, skipMissingIndicator);
		
		skipTilesButton = new IconButton(x + 124, y + 101, ScreenResources.ICON_SKIP_TILES);
		skipTilesButton.setToolTip("Protect Tile Entities");
		skipTilesIndicator = new Indicator(x + 124, y + 96, "");
		Collections.addAll(widgets, skipTilesButton, skipTilesIndicator);

		tick();
	}

	@Override
	public void tick() {

		SchematicannonTileEntity te = container.getTileEntity();
		for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++)
			replaceLevelIndicators.get(replaceMode).state = replaceMode <= te.replaceMode ? State.ON : State.OFF;

		skipMissingIndicator.state = te.skipMissing ? State.ON : State.OFF;
		skipTilesIndicator.state = !te.replaceTileEntities ? State.ON : State.OFF;

		playIndicator.state = State.OFF;
		pauseIndicator.state = State.OFF;
		resetIndicator.state = State.OFF;

		switch (te.state) {
		case PAUSED:
			pauseIndicator.state = State.YELLOW;
			playButton.active = true;
			pauseButton.active = false;
			resetButton.active = true;
			break;
		case RUNNING:
			playIndicator.state = State.GREEN;
			playButton.active = false;
			pauseButton.active = true;
			resetButton.active = true;
			break;
		case STOPPED:
			resetIndicator.state = State.RED;
			playButton.active = true;
			pauseButton.active = false;
			resetButton.active = false;
			break;
		default:
			break;
		}

		handleTooltips();

		super.tick();
	}

	protected void handleTooltips() {
		for (Widget w : widgets)
			if (w instanceof IconButton)
				if (!((IconButton) w).getToolTip().isEmpty()) {
					((IconButton) w).setToolTip(((IconButton) w).getToolTip().get(0));
					((IconButton) w).getToolTip()
							.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "[Ctrl] for more Info");
				}

		if (hasControlDown()) {
			if (skipMissingButton.isHovered()) {
				List<String> tip = skipMissingButton.getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE
						+ (skipMissingIndicator.state == State.ON ? "Currently Enabled" : "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "If the Schematicannon cannot find");
				tip.add(TextFormatting.GRAY + "a required Block for placement, it");
				tip.add(TextFormatting.GRAY + "will continue at the next Location.");
			}
			if (skipTilesButton.isHovered()) {
				List<String> tip = skipTilesButton.getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE
						+ (skipTilesIndicator.state == State.ON ? "Currently Enabled" : "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "The Schematicannon will avoid replacing");
				tip.add(TextFormatting.GRAY + "data holding blocks such as Chests.");
			}
			if (replaceLevelButtons.get(0).isHovered()) {
				List<String> tip = replaceLevelButtons.get(0).getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE + (replaceLevelIndicators.get(0).state == State.ON ? "Currently Enabled"
						: "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "The cannon will never replace");
				tip.add(TextFormatting.GRAY + "any Solid blocks in its working area,");
				tip.add(TextFormatting.GRAY + "only non-Solid and Air.");
			}
			if (replaceLevelButtons.get(1).isHovered()) {
				List<String> tip = replaceLevelButtons.get(1).getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE + (replaceLevelIndicators.get(1).state == State.ON ? "Currently Enabled"
						: "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "The cannon will only replace");
				tip.add(TextFormatting.GRAY + "Solid blocks in its working area,");
				tip.add(TextFormatting.GRAY + "if the Schematic contains a solid");
				tip.add(TextFormatting.GRAY + "Block at their location.");
			}
			if (replaceLevelButtons.get(2).isHovered()) {
				List<String> tip = replaceLevelButtons.get(2).getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE + (replaceLevelIndicators.get(2).state == State.ON ? "Currently Enabled"
						: "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "The cannon will replace");
				tip.add(TextFormatting.GRAY + "Solid blocks in its working area,");
				tip.add(TextFormatting.GRAY + "if the Schematic contains any");
				tip.add(TextFormatting.GRAY + "Block at their location.");
			}
			if (replaceLevelButtons.get(3).isHovered()) {
				List<String> tip = replaceLevelButtons.get(3).getToolTip();
				tip.remove(1);
				tip.add(TextFormatting.BLUE + (replaceLevelIndicators.get(3).state == State.ON ? "Currently Enabled"
						: "Currently Disabled"));
				tip.add(TextFormatting.GRAY + "The cannon will clear out all");
				tip.add(TextFormatting.GRAY + "blocks in its working area,");
				tip.add(TextFormatting.GRAY + "including those replaced by Air.");
			}

		}
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		ScreenResources.PLAYER_INVENTORY.draw(this, guiLeft - 10, guiTop + 145);
		ScreenResources.SCHEMATICANNON.draw(this, guiLeft + 20, guiTop);

		SchematicannonTileEntity te = container.getTileEntity();
		renderPrintingProgress(te.schematicProgress);
		renderFuelBar(te.fuelLevel);
		renderChecklistPrinterProgress(te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0).isEmpty())
			renderBlueprintHighlight();
		
		renderCannon();

		font.drawString("Schematicannon", guiLeft + 80, guiTop + 10, ScreenResources.FONT_COLOR);

		String msg = te.statusMsg;
		int stringWidth = font.getStringWidth(msg);
		if (stringWidth < 120)
			font.drawStringWithShadow(msg, guiLeft + 20 + 96 - stringWidth / 2, guiTop + 30, 0xCCDDFF);
		else 
			font.drawSplitString(msg, guiLeft + 20 + 45, guiTop + 24, 120, 0xCCDDFF);
		
		font.drawString("Placement Settings", guiLeft + 20 + 13, guiTop + 84, ScreenResources.FONT_COLOR);
		font.drawString("Inventory", guiLeft - 10 + 7, guiTop + 145 + 6, 0x666666);
	}

	protected void renderCannon() {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.translated(guiLeft + 240, guiTop + 120, 200);
		GlStateManager.scaled(5, 5, 5);

		itemRenderer.renderItemIntoGUI(new ItemStack(AllBlocks.SCHEMATICANNON.block), 0, 0);

		GlStateManager.popMatrix();
	}

	protected void renderBlueprintHighlight() {
		ScreenResources.SCHEMATICANNON_HIGHLIGHT.draw(this, guiLeft + 20 + 8, guiTop + 31);
	}

	protected void renderPrintingProgress(float progress) {
		progress = Math.min(progress, 1);
		ScreenResources sprite = ScreenResources.SCHEMATICANNON_PROGRESS;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 39, guiTop + 36, sprite.startX, sprite.startY, (int) (sprite.width * progress),
				sprite.height);
	}

	protected void renderChecklistPrinterProgress(float progress) {
		ScreenResources sprite = ScreenResources.SCHEMATICANNON_PROGRESS_2;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 222, guiTop + 42, sprite.startX, sprite.startY, sprite.width,
				(int) (sprite.height * progress));
	}

	protected void renderFuelBar(float amount) {
		ScreenResources sprite = ScreenResources.SCHEMATICANNON_FUEL;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 73, guiTop + 135, sprite.startX, sprite.startY, (int) (sprite.width * amount),
				sprite.height);
	}

	@Override
	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		int fuelX = guiLeft + 20 + 73, fuelY = guiTop + 135;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + ScreenResources.SCHEMATICANNON_FUEL.width
				&& mouseY <= fuelY + ScreenResources.SCHEMATICANNON_FUEL.height) {
			container.getTileEntity();
			SchematicannonTileEntity te = container.getTileEntity();
			int shotsLeft = (int) (te.fuelLevel / SchematicannonTileEntity.FUEL_USAGE_RATE);
			int shotsLeftWithItems = (int) (shotsLeft + te.inventory.getStackInSlot(4).getCount()
					* (SchematicannonTileEntity.FUEL_PER_GUNPOWDER / SchematicannonTileEntity.FUEL_USAGE_RATE));
			renderTooltip(
					ImmutableList.of("Gunpowder at " + (int) (te.fuelLevel * 100) + "%",
							TextFormatting.GRAY + "Shots left: " + TextFormatting.BLUE + shotsLeft,
							TextFormatting.GRAY + "With backup: " + TextFormatting.BLUE + shotsLeftWithItems),
					mouseX, mouseY);
		}

		int paperX = guiLeft + 20 + 202, paperY = guiTop + 20;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16) {
			renderTooltip("Material List Printer", mouseX, mouseY);
		}

		super.renderWindowForeground(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
			if (!replaceLevelButtons.get(replaceMode).isHovered())
				continue;
			if (container.getTileEntity().replaceMode == replaceMode)
				continue;
			sendOptionUpdate(Option.values()[replaceMode], true);
		}

		if (skipMissingButton.isHovered()) 
			sendOptionUpdate(Option.SKIP_MISSING, !container.getTileEntity().skipMissing);
		if (skipTilesButton.isHovered()) 
			sendOptionUpdate(Option.SKIP_TILES, !container.getTileEntity().replaceTileEntities);

		if (playButton.isHovered() && playButton.active) 
			sendOptionUpdate(Option.PLAY, true);
		if (pauseButton.isHovered() && pauseButton.active) 
			sendOptionUpdate(Option.PAUSE, true);
		if (resetButton.isHovered() && resetButton.active) 
			sendOptionUpdate(Option.STOP, true);


		return super.mouseClicked(x, y, button);
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		AllPackets.channel
				.sendToServer(ConfigureSchematicannonPacket.setOption(container.getTileEntity().getPos(), option, set));
	}

}
