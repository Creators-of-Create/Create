package com.simibubi.create.gui;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.block.SchematicannonContainer;
import com.simibubi.create.block.SchematicannonTileEntity;
import com.simibubi.create.gui.widgets.GuiIndicator;
import com.simibubi.create.gui.widgets.GuiIndicator.State;
import com.simibubi.create.gui.widgets.SimiButton;
import com.simibubi.create.networking.PacketConfigureSchematicannon;
import com.simibubi.create.networking.PacketConfigureSchematicannon.Option;
import com.simibubi.create.networking.Packets;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonContainer> {

	protected Vector<GuiIndicator> replaceLevelIndicators;
	protected Vector<SimiButton> replaceLevelButtons;

	protected SimiButton skipMissingButton;
	protected GuiIndicator skipMissingIndicator;

	protected SimiButton playButton;
	protected GuiIndicator playIndicator;
	protected SimiButton pauseButton;
	protected GuiIndicator pauseIndicator;
	protected SimiButton resetButton;
	protected GuiIndicator resetIndicator;

	public SchematicannonScreen(SchematicannonContainer container, PlayerInventory inventory,
			ITextComponent p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
	}

	@Override
	protected void init() {
		setWindowSize(GuiResources.SCHEMATICANNON.width + 50, GuiResources.SCHEMATICANNON.height + 80);
		super.init();

		int x = guiLeft + 20;
		int y = guiTop;

		widgets.clear();

		// Play Pause Stop
		playButton = new SimiButton(x + 69, y + 55, GuiResources.ICON_PLAY);
		playIndicator = new GuiIndicator(x + 69, y + 50, "");
		pauseButton = new SimiButton(x + 88, y + 55, GuiResources.ICON_PAUSE);
		pauseIndicator = new GuiIndicator(x + 88, y + 50, "");
		resetButton = new SimiButton(x + 107, y + 55, GuiResources.ICON_STOP);
		resetIndicator = new GuiIndicator(x + 107, y + 50, "Not Running");
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
				resetIndicator);

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<GuiResources> icons = ImmutableList.of(GuiResources.ICON_DONT_REPLACE, GuiResources.ICON_REPLACE_SOLID,
				GuiResources.ICON_REPLACE_ANY, GuiResources.ICON_REPLACE_EMPTY);
		List<String> toolTips = ImmutableList.of("Don't Replace Solid Blocks", "Replace Solid with Solid",
				"Replace Solid with Any", "Replace Solid with Empty");

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new GuiIndicator(x + 12 + i * 18, y + 96, ""));
			replaceLevelButtons.add(new SimiButton(x + 12 + i * 18, y + 101, icons.get(i)));
			replaceLevelButtons.get(i).setToolTip(toolTips.get(i));
		}
		widgets.addAll(replaceLevelButtons);
		widgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new SimiButton(x + 107, y + 101, GuiResources.ICON_SKIP_MISSING);
		skipMissingButton.setToolTip("Skip missing Blocks");
		skipMissingIndicator = new GuiIndicator(x + 107, y + 96, "");
		Collections.addAll(widgets, skipMissingButton, skipMissingIndicator);

		tick();
	}

	@Override
	public void tick() {

		SchematicannonTileEntity te = container.getTileEntity();
		for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++)
			replaceLevelIndicators.get(replaceMode).state = replaceMode <= te.replaceMode ? State.ON : State.OFF;

		skipMissingIndicator.state = te.skipMissing ? State.ON : State.OFF;

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
			if (w instanceof SimiButton)
				if (!((SimiButton) w).getToolTip().isEmpty()) {
					((SimiButton) w).setToolTip(((SimiButton) w).getToolTip().get(0));
					((SimiButton) w).getToolTip()
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
		GuiResources.PLAYER_INVENTORY.draw(this, guiLeft - 10, guiTop + 145);
		GuiResources.SCHEMATICANNON.draw(this, guiLeft + 20, guiTop);

		SchematicannonTileEntity te = container.getTileEntity();
		renderPrintingProgress(te.schematicProgress);
		renderFuelBar(te.fuelLevel);
		renderChecklistPrinterProgress(te.paperPrintingProgress);

		if (!te.inventory.getStackInSlot(0).isEmpty())
			renderBlueprintHighlight();
		
		renderCannon();

		font.drawString("Schematicannon", guiLeft + 80, guiTop + 10, GuiResources.FONT_COLOR);

		String msg = te.statusMsg;
		int stringWidth = font.getStringWidth(msg);
		if (stringWidth < 120)
			font.drawStringWithShadow(msg, guiLeft + 20 + 96 - stringWidth / 2, guiTop + 30, 0xCCDDFF);
		else 
			font.drawSplitString(msg, guiLeft + 20 + 45, guiTop + 24, 120, 0xCCDDFF);
		
		font.drawString("Placement Settings", guiLeft + 20 + 13, guiTop + 84, GuiResources.FONT_COLOR);
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
		GuiResources.SCHEMATICANNON_HIGHLIGHT.draw(this, guiLeft + 20 + 8, guiTop + 31);
	}

	protected void renderPrintingProgress(float progress) {
		GuiResources sprite = GuiResources.SCHEMATICANNON_PROGRESS;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 39, guiTop + 36, sprite.startX, sprite.startY, (int) (sprite.width * progress),
				sprite.height);
	}

	protected void renderChecklistPrinterProgress(float progress) {
		GuiResources sprite = GuiResources.SCHEMATICANNON_PROGRESS_2;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 222, guiTop + 42, sprite.startX, sprite.startY, sprite.width,
				(int) (sprite.height * progress));
	}

	protected void renderFuelBar(float amount) {
		GuiResources sprite = GuiResources.SCHEMATICANNON_FUEL;
		minecraft.getTextureManager().bindTexture(sprite.location);
		blit(guiLeft + 20 + 73, guiTop + 135, sprite.startX, sprite.startY, (int) (sprite.width * amount),
				sprite.height);
	}

	@Override
	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		int fuelX = guiLeft + 20 + 73, fuelY = guiTop + 135;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + GuiResources.SCHEMATICANNON_FUEL.width
				&& mouseY <= fuelY + GuiResources.SCHEMATICANNON_FUEL.height) {
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

		if (playButton.isHovered() && playButton.active) 
			sendOptionUpdate(Option.PLAY, true);
		if (pauseButton.isHovered() && pauseButton.active) 
			sendOptionUpdate(Option.PAUSE, true);
		if (resetButton.isHovered() && resetButton.active) 
			sendOptionUpdate(Option.STOP, true);


		return super.mouseClicked(x, y, button);
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		Packets.channel
				.sendToServer(PacketConfigureSchematicannon.setOption(container.getTileEntity().getPos(), option, set));
	}

}
