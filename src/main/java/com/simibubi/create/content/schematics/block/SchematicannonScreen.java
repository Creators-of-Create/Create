package com.simibubi.create.content.schematics.block;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.ConfigureSchematicannonPacket.Option;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static net.minecraft.util.text.TextFormatting.GRAY;

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

	private final ITextComponent title = Lang.translate("gui.schematicannon.title");
	private final ITextComponent settingsTitle = Lang.translate("gui.schematicannon.settingsTitle");
	private final ITextComponent listPrinter = Lang.translate("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final ITextComponent optionEnabled = Lang.translate("gui.schematicannon.optionEnabled");
	private final ITextComponent optionDisabled = Lang.translate("gui.schematicannon.optionDisabled");

	private final ItemStack renderedItem = AllBlocks.SCHEMATICANNON.asStack();

	public SchematicannonScreen(SchematicannonContainer container, PlayerInventory inventory,
			ITextComponent p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
	}

	@Override
	protected void init() {
		setWindowSize(AllGuiTextures.SCHEMATICANNON_BG.width + 50, AllGuiTextures.SCHEMATICANNON_BG.height + 80);
		super.init();

		int x = guiLeft + 20;
		int y = guiTop;

		widgets.clear();

		// Play Pause Stop
		playButton = new IconButton(x + 70, y + 55, AllIcons.I_PLAY);
		playIndicator = new Indicator(x + 70, y + 50, StringTextComponent.EMPTY);
		pauseButton = new IconButton(x + 88, y + 55, AllIcons.I_PAUSE);
		pauseIndicator = new Indicator(x + 88, y + 50, StringTextComponent.EMPTY);
		resetButton = new IconButton(x + 106, y + 55, AllIcons.I_STOP);
		resetIndicator = new Indicator(x + 106, y + 50, StringTextComponent.EMPTY);
		resetIndicator.state = State.RED;
		Collections
				.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton, resetIndicator);

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<AllIcons> icons = ImmutableList
				.of(AllIcons.I_DONT_REPLACE, AllIcons.I_REPLACE_SOLID, AllIcons.I_REPLACE_ANY,
						AllIcons.I_REPLACE_EMPTY);
		List<ITextComponent> toolTips = ImmutableList
				.of(Lang.translate("gui.schematicannon.option.dontReplaceSolid"),
						Lang.translate("gui.schematicannon.option.replaceWithSolid"),
						Lang.translate("gui.schematicannon.option.replaceWithAny"),
						Lang.translate("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 16 + i * 18, y + 96, StringTextComponent.EMPTY));
			replaceLevelButtons.add(new IconButton(x + 16 + i * 18, y + 101, icons.get(i)));
			replaceLevelButtons.get(i).setToolTip(toolTips.get(i));
		}
		widgets.addAll(replaceLevelButtons);
		widgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 106, y + 101, AllIcons.I_SKIP_MISSING);
		skipMissingButton.setToolTip(Lang.translate("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 106, y + 96, StringTextComponent.EMPTY);
		Collections.addAll(widgets, skipMissingButton, skipMissingIndicator);

		skipTilesButton = new IconButton(x + 124, y + 101, AllIcons.I_SKIP_TILES);
		skipTilesButton.setToolTip(Lang.translate("gui.schematicannon.option.skipTileEntities"));
		skipTilesIndicator = new Indicator(x + 124, y + 96, StringTextComponent.EMPTY);
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
		List<ITextComponent> tip = button.getToolTip();
		tip.add((enabled ? optionEnabled : optionDisabled).copy().formatted(TextFormatting.BLUE));
		TooltipHelper.cutString(Lang.translate("gui.schematicannon.option." + tooltipKey + ".description"), GRAY, GRAY).forEach(s -> tip.add(new StringTextComponent(s).formatted(GRAY)));
	}

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.PLAYER_INVENTORY.draw(matrixStack, this, guiLeft - 10, guiTop + 145);
		AllGuiTextures.SCHEMATICANNON_BG.draw(matrixStack, this, guiLeft + 20, guiTop);

		SchematicannonTileEntity te = container.getTileEntity();
		renderPrintingProgress(matrixStack, te.schematicProgress);
		renderFuelBar(matrixStack, te.fuelLevel);
		renderChecklistPrinterProgress(matrixStack, te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0).isEmpty())
			renderBlueprintHighlight(matrixStack);

		GuiGameElement.of(renderedItem)
				.at(guiLeft + 240, guiTop + 120)
				.scale(5)
				.render();


		textRenderer.draw(matrixStack, title, guiLeft + 80, guiTop + 10, AllGuiTextures.FONT_COLOR);

		IFormattableTextComponent msg = Lang.translate("schematicannon.status." + te.statusMsg);
		int stringWidth = textRenderer.getWidth(msg);

		if (te.missingItem != null) {
			stringWidth += 15;
			itemRenderer.renderItemIntoGUI(te.missingItem, guiLeft + 145, guiTop + 25);
		}

		textRenderer.drawWithShadow(matrixStack, msg, guiLeft + 20 + 96 - stringWidth / 2, guiTop + 30, 0xCCDDFF);

		textRenderer.draw(matrixStack, settingsTitle, guiLeft + 20 + 13, guiTop + 84, AllGuiTextures.FONT_COLOR);
		textRenderer
				.draw(matrixStack, playerInventory.getDisplayName(), guiLeft - 10 + 7, guiTop + 145 + 6,
						0x666666);

		// to see or debug the bounds of the extra area uncomment the following lines
		// Rectangle2d r = extraAreas.get(0);
		// fill(r.getX() + r.getWidth(), r.getY() + r.getHeight(), r.getX(), r.getY(),
		// 0xd3d3d3d3);
	}

	protected void renderBlueprintHighlight(MatrixStack matrixStack) {
		AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.draw(matrixStack, this, guiLeft + 20 + 8, guiTop + 31);
	}

	protected void renderPrintingProgress(MatrixStack matrixStack, float progress) {
		progress = Math.min(progress, 1);
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
		client.getTextureManager().bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 39, guiTop + 36, sprite.startX, sprite.startY, (int) (sprite.width * progress),
				sprite.height);
	}

	protected void renderChecklistPrinterProgress(MatrixStack matrixStack, float progress) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS_2;
		client.getTextureManager().bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 222, guiTop + 42, sprite.startX, sprite.startY, sprite.width,
				(int) (sprite.height * progress));
	}

	protected void renderFuelBar(MatrixStack matrixStack, float amount) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
		if (container.getTileEntity().hasCreativeCrate) {
			AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.draw(matrixStack, this, guiLeft + 20 + 73, guiTop + 135);
			return;
		}
		client.getTextureManager().bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 73, guiTop + 135, sprite.startX, sprite.startY, (int) (sprite.width * amount),
				sprite.height);
	}

	@Override
	protected void renderWindowForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int fuelX = guiLeft + 20 + 73, fuelY = guiTop + 135;
		SchematicannonTileEntity te = container.getTileEntity();
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.width
				&& mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.height) {
			container.getTileEntity();

			double fuelUsageRate = te.getFuelUsageRate();
			int shotsLeft = (int) (te.fuelLevel / fuelUsageRate);
			int shotsLeftWithItems = (int) (shotsLeft
					+ te.inventory.getStackInSlot(4).getCount() * (te.getFuelAddedByGunPowder() / fuelUsageRate));

			List<ITextComponent> tooltip = new ArrayList<>();
			float f = te.hasCreativeCrate ? 100 : te.fuelLevel * 100;
			tooltip.add(Lang.translate(_gunpowderLevel, "" + (int) f));
			if (!te.hasCreativeCrate)
				tooltip.add(Lang.translate(_shotsRemaining, "" + TextFormatting.BLUE + shotsLeft).formatted(GRAY));
			if (shotsLeftWithItems != shotsLeft)
				tooltip
						.add(Lang.translate(_shotsRemainingWithBackup, "" + TextFormatting.BLUE + shotsLeftWithItems).formatted(GRAY));

			renderTooltip(matrixStack, tooltip, mouseX, mouseY);
		}

		if (te.missingItem != null) {
			int missingBlockX = guiLeft + 145, missingBlockY = guiTop + 25;
			if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16
					&& mouseY <= missingBlockY + 16) {
				renderTooltip(matrixStack, te.missingItem, mouseX, mouseY);
			}
		}

		int paperX = guiLeft + 20 + 202, paperY = guiTop + 20;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16) {
			renderTooltip(matrixStack, listPrinter, mouseX, mouseY);
		}

		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
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
