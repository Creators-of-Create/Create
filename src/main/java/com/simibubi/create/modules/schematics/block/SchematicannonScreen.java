package com.simibubi.create.modules.schematics.block;

import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.modules.schematics.packet.ConfigureSchematicannonPacket.Option;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
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

	private List<Rectangle2d> extraAreas;

	private final String title = Lang.translate("gui.schematicannon.title");
	private final String settingsTitle = Lang.translate("gui.schematicannon.settingsTitle");
	private final String listPrinter = Lang.translate("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final String optionEnabled = Lang.translate("gui.schematicannon.optionEnabled");
	private final String optionDisabled = Lang.translate("gui.schematicannon.optionDisabled");

	public SchematicannonScreen(SchematicannonContainer container, PlayerInventory inventory,
			ITextComponent p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
	}

	@Override
	protected void init() {
		setWindowSize(ScreenResources.SCHEMATICANNON_BG.width + 50, ScreenResources.SCHEMATICANNON_BG.height + 80);
		super.init();

		int x = guiLeft + 20;
		int y = guiTop;

		widgets.clear();

		// Play Pause Stop
		playButton = new IconButton(x + 70, y + 55, ScreenResources.I_PLAY);
		playIndicator = new Indicator(x + 70, y + 50, "");
		pauseButton = new IconButton(x + 88, y + 55, ScreenResources.I_PAUSE);
		pauseIndicator = new Indicator(x + 88, y + 50, "");
		resetButton = new IconButton(x + 106, y + 55, ScreenResources.I_STOP);
		resetIndicator = new Indicator(x + 106, y + 50, "");
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
				resetIndicator);

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<ScreenResources> icons = ImmutableList.of(ScreenResources.I_DONT_REPLACE,
				ScreenResources.I_REPLACE_SOLID, ScreenResources.I_REPLACE_ANY,
				ScreenResources.I_REPLACE_EMPTY);
		List<String> toolTips = ImmutableList.of(Lang.translate("gui.schematicannon.option.dontReplaceSolid"),
				Lang.translate("gui.schematicannon.option.replaceWithSolid"),
				Lang.translate("gui.schematicannon.option.replaceWithAny"),
				Lang.translate("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 16 + i * 18, y + 96, ""));
			replaceLevelButtons.add(new IconButton(x + 16 + i * 18, y + 101, icons.get(i)));
			replaceLevelButtons.get(i).setToolTip(toolTips.get(i));
		}
		widgets.addAll(replaceLevelButtons);
		widgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 106, y + 101, ScreenResources.I_SKIP_MISSING);
		skipMissingButton.setToolTip(Lang.translate("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 106, y + 96, "");
		Collections.addAll(widgets, skipMissingButton, skipMissingIndicator);

		skipTilesButton = new IconButton(x + 124, y + 101, ScreenResources.I_SKIP_TILES);
		skipTilesButton.setToolTip(Lang.translate("gui.schematicannon.option.skipTileEntities"));
		skipTilesIndicator = new Indicator(x + 124, y + 96, "");
		Collections.addAll(widgets, skipTilesButton, skipTilesIndicator);

		extraAreas = new ArrayList<>();
		extraAreas.add(new Rectangle2d(guiLeft + 240, guiTop + 88, 84, 113));

		tick();
	}

	@Override
	public void tick() {

		SchematicannonTileEntity te = container.getTileEntity();
		replaceLevelIndicators.get(0).state = te.replaceMode == 0 ? State.ON : State.OFF;
		for (int replaceMode = 1; replaceMode < replaceLevelButtons.size(); replaceMode++)
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
			if (w instanceof IconButton) {
				IconButton button = (IconButton) w;
				if (!button.getToolTip().isEmpty()) {
					button.setToolTip(button.getToolTip().get(0));
					button.getToolTip().add(TooltipHelper.holdShift(Palette.Blue, hasShiftDown()));
				}
			}

		if (hasShiftDown()) {
			fillToolTip(skipMissingButton, skipMissingIndicator, "skipMissing");
			fillToolTip(skipTilesButton, skipTilesIndicator, "skipTileEntities");
			fillToolTip(replaceLevelButtons.get(0), replaceLevelIndicators.get(0), "dontReplaceSolid");
			fillToolTip(replaceLevelButtons.get(1), replaceLevelIndicators.get(1), "replaceWithSolid");
			fillToolTip(replaceLevelButtons.get(2), replaceLevelIndicators.get(2), "replaceWithAny");
			fillToolTip(replaceLevelButtons.get(3), replaceLevelIndicators.get(3), "replaceWithEmpty");
		}
	}

	private void fillToolTip(IconButton button, Indicator indicator, String tooltipKey) {
		if (!button.isHovered())
			return;
		boolean enabled = indicator.state == State.ON;
		List<String> tip = button.getToolTip();
		tip.add(TextFormatting.BLUE + (enabled ? optionEnabled : optionDisabled));
		tip.addAll(TooltipHelper.cutString(Lang.translate("gui.schematicannon.option." + tooltipKey + ".description"),
				GRAY, GRAY));
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		ScreenResources.PLAYER_INVENTORY.draw(this, guiLeft - 10, guiTop + 145);
		ScreenResources.SCHEMATICANNON_BG.draw(this, guiLeft + 20, guiTop);

		SchematicannonTileEntity te = container.getTileEntity();
		renderPrintingProgress(te.schematicProgress);
		renderFuelBar(te.fuelLevel);
		renderChecklistPrinterProgress(te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0).isEmpty())
			renderBlueprintHighlight();

		renderCannon();

		font.drawString(title, guiLeft + 80, guiTop + 10, ScreenResources.FONT_COLOR);

		String msg = Lang.translate("schematicannon.status." + te.statusMsg);
		int stringWidth = font.getStringWidth(msg);

		if (te.missingBlock != null) {
			stringWidth += 15;
			itemRenderer.renderItemIntoGUI(new ItemStack(BlockItem.BLOCK_TO_ITEM.get(te.missingBlock.getBlock())),
					guiLeft + 145, guiTop + 25);
		}

		font.drawStringWithShadow(msg, guiLeft + 20 + 96 - stringWidth / 2, guiTop + 30, 0xCCDDFF);

		font.drawString(settingsTitle, guiLeft + 20 + 13, guiTop + 84, ScreenResources.FONT_COLOR);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), guiLeft - 10 + 7, guiTop + 145 + 6,
				0x666666);

		//to see or debug the bounds of the extra area uncomment the following lines
		//Rectangle2d r = extraAreas.get(0);
		//fill(r.getX() + r.getWidth(), r.getY() + r.getHeight(), r.getX(), r.getY(), 0xd3d3d3d3);
	}

	protected void renderCannon() {
		RenderSystem.pushMatrix();

		RenderSystem.enableBlend();
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		RenderSystem.translated(guiLeft + 240, guiTop + 120, 200);
		RenderSystem.scaled(5, 5, 5);

		itemRenderer.renderItemIntoGUI(new ItemStack(AllBlocks.SCHEMATICANNON.get()), 0, 0);

		RenderSystem.popMatrix();
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
		SchematicannonTileEntity te = container.getTileEntity();
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + ScreenResources.SCHEMATICANNON_FUEL.width
				&& mouseY <= fuelY + ScreenResources.SCHEMATICANNON_FUEL.height) {
			container.getTileEntity();
			double fuelUsageRate = te.getFuelUsageRate();
			int shotsLeft = (int) (te.fuelLevel / fuelUsageRate);
			int shotsLeftWithItems = (int) (shotsLeft
					+ te.inventory.getStackInSlot(4).getCount() * (te.getFuelAddedByGunPowder() / fuelUsageRate));
			renderTooltip(ImmutableList.of(Lang.translate(_gunpowderLevel, "" + (int) (te.fuelLevel * 100)),
					GRAY + Lang.translate(_shotsRemaining, "" + TextFormatting.BLUE + shotsLeft),
					GRAY + Lang.translate(_shotsRemainingWithBackup, "" + TextFormatting.BLUE + shotsLeftWithItems)),
					mouseX, mouseY);
		}

		if (te.missingBlock != null) {
			int missingBlockX = guiLeft + 145, missingBlockY = guiTop + 25;
			if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16
					&& mouseY <= missingBlockY + 16) {
				renderTooltip(new ItemStack(BlockItem.BLOCK_TO_ITEM.get(te.missingBlock.getBlock())), mouseX, mouseY);
			}
		}

		int paperX = guiLeft + 20 + 202, paperY = guiTop + 20;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16) {
			renderTooltip(listPrinter, mouseX, mouseY);
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

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		AllPackets.channel
				.sendToServer(ConfigureSchematicannonPacket.setOption(container.getTileEntity().getPos(), option, set));
	}

}
