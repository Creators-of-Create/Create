package com.simibubi.create.content.schematics.block;

import static net.minecraft.util.text.TextFormatting.DARK_PURPLE;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonContainer> {

	private static final AllGuiTextures BG_BOTTOM = AllGuiTextures.SCHEMATICANNON_BOTTOM;
	private static final AllGuiTextures BG_TOP = AllGuiTextures.SCHEMATICANNON_TOP;

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
	protected List<Widget> placementSettingWidgets;

	private final ITextComponent title = Lang.translate("gui.schematicannon.title");
	private final ITextComponent listPrinter = Lang.translate("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _showSettings = "gui.schematicannon.showOptions";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
	private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
	private final String _slotSchematic = "gui.schematicannon.slot.schematic";

	private final ITextComponent optionEnabled = Lang.translate("gui.schematicannon.optionEnabled");
	private final ITextComponent optionDisabled = Lang.translate("gui.schematicannon.optionDisabled");

	private final ItemStack renderedItem = AllBlocks.SCHEMATICANNON.asStack();

	private IconButton confirmButton;
	private IconButton showSettingsButton;
	private Indicator showSettingsIndicator;

	public SchematicannonScreen(SchematicannonContainer container, PlayerInventory inventory,
		ITextComponent p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
		placementSettingWidgets = new ArrayList<>();
	}

	@Override
	protected void init() {
		setWindowSize(BG_TOP.width + 50, BG_BOTTOM.height + BG_TOP.height + 80);
		super.init();

		int x = guiLeft + 20;
		int y = guiTop;

		widgets.clear();

		// Play Pause Stop
		playButton = new IconButton(x + 75, y + 86, AllIcons.I_PLAY);
		playIndicator = new Indicator(x + 75, y + 79, StringTextComponent.EMPTY);
		pauseButton = new IconButton(x + 93, y + 86, AllIcons.I_PAUSE);
		pauseIndicator = new Indicator(x + 93, y + 79, StringTextComponent.EMPTY);
		resetButton = new IconButton(x + 111, y + 86, AllIcons.I_STOP);
		resetIndicator = new Indicator(x + 111, y + 79, StringTextComponent.EMPTY);
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
			resetIndicator);

		extraAreas = new ArrayList<>();
		extraAreas.add(new Rectangle2d(guiLeft + 240, guiTop + 88, 84, 113));

		confirmButton = new IconButton(x + 180, guiTop + 117, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);
		showSettingsButton = new IconButton(guiLeft + 29, guiTop + 117, AllIcons.I_PLACEMENT_SETTINGS);
		showSettingsButton.setToolTip(Lang.translate(_showSettings));
		widgets.add(showSettingsButton);
		showSettingsIndicator = new Indicator(guiLeft + 29, guiTop + 111, StringTextComponent.EMPTY);
		widgets.add(showSettingsIndicator);

		tick();
	}

	private void initPlacementSettings() {
		widgets.removeAll(placementSettingWidgets);
		placementSettingWidgets.clear();

		if (placementSettingsHidden())
			return;

		int x = guiLeft + 20;
		int y = guiTop;

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		/*
<<<<<<< HEAD
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
=======*/
		List<AllIcons> icons = ImmutableList.of(AllIcons.I_DONT_REPLACE, AllIcons.I_REPLACE_SOLID,
			AllIcons.I_REPLACE_ANY, AllIcons.I_REPLACE_EMPTY);
		List<ITextComponent> toolTips = ImmutableList.of(Lang.translate("gui.schematicannon.option.dontReplaceSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithAny"),
			Lang.translate("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 33 + i * 18, y + 111, StringTextComponent.EMPTY));
			replaceLevelButtons.add(new IconButton(x + 33 + i * 18, y + 117, icons.get(i)));
			replaceLevelButtons.get(i)
				.setToolTip(toolTips.get(i));
		}
		placementSettingWidgets.addAll(replaceLevelButtons);
		placementSettingWidgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 111, y + 117, AllIcons.I_SKIP_MISSING);
		skipMissingButton.setToolTip(Lang.translate("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 111, y + 111, StringTextComponent.EMPTY);
		Collections.addAll(placementSettingWidgets, skipMissingButton, skipMissingIndicator);

		skipTilesButton = new IconButton(x + 129, y + 117, AllIcons.I_SKIP_TILES);
		skipTilesButton.setToolTip(Lang.translate("gui.schematicannon.option.skipTileEntities"));
		skipTilesIndicator = new Indicator(x + 129, y + 111, StringTextComponent.EMPTY);
		Collections.addAll(placementSettingWidgets, skipTilesButton, skipTilesIndicator);

		widgets.addAll(placementSettingWidgets);
	}

	protected boolean placementSettingsHidden() {
		return showSettingsIndicator.state == State.OFF;
	}

	@Override
	public void tick() {
		SchematicannonTileEntity te = container.getTileEntity();

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++)
				replaceLevelIndicators.get(replaceMode).state = replaceMode == te.replaceMode ? State.ON : State.OFF;
			skipMissingIndicator.state = te.skipMissing ? State.ON : State.OFF;
			skipTilesIndicator.state = !te.replaceTileEntities ? State.ON : State.OFF;
		}

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
		if (placementSettingsHidden())
			return;

		for (Widget w : placementSettingWidgets)
			if (w instanceof IconButton) {
				IconButton button = (IconButton) w;
				if (!button.getToolTip()
					.isEmpty()) {
					button.setToolTip(button.getToolTip()
						.get(0));
					button.getToolTip()
						.add(TooltipHelper.holdShift(Palette.Blue, hasShiftDown()));
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
		BG_TOP.draw(matrixStack, this, guiLeft + 20, guiTop);
		BG_BOTTOM.draw(matrixStack, this, guiLeft + 20, guiTop + BG_TOP.height);

		SchematicannonTileEntity te = container.getTileEntity();
		renderPrintingProgress(matrixStack, te.schematicProgress);
		renderFuelBar(matrixStack, te.fuelLevel);
		renderChecklistPrinterProgress(matrixStack, te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0).isEmpty())
			renderBlueprintHighlight(matrixStack);

		GuiGameElement.of(renderedItem)
			.at(guiLeft + 230, guiTop + 110)
				.scale(5)
				.render(matrixStack);


		textRenderer.draw(matrixStack, title, guiLeft + 80, guiTop + 10, AllGuiTextures.FONT_COLOR);

		IFormattableTextComponent msg = Lang.translate("schematicannon.status." + te.statusMsg);
		int stringWidth = textRenderer.getWidth(msg);

		if (te.missingItem != null) {
			stringWidth += 15;
			itemRenderer.renderItemIntoGUI(te.missingItem, guiLeft + 150, guiTop + 46);
		}

		textRenderer.drawWithShadow(matrixStack, msg, guiLeft + 20 + 102 - stringWidth / 2, guiTop + 50, 0xCCDDFF);
		textRenderer.draw(matrixStack, playerInventory.getDisplayName(), guiLeft - 10 + 7, guiTop + 145 + 6, 0x666666);
	}

	protected void renderBlueprintHighlight(MatrixStack matrixStack) {
		AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.draw(matrixStack, this, guiLeft + 20 + 10, guiTop + 60);
	}

	protected void renderPrintingProgress(MatrixStack matrixStack, float progress) {
		progress = Math.min(progress, 1);
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
		client.getTextureManager().bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 44, guiTop + 64, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderChecklistPrinterProgress(MatrixStack matrixStack, float progress) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
		client.getTextureManager().bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 154, guiTop + 20, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderFuelBar(MatrixStack matrixStack, float amount) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
		if (container.getTileEntity().hasCreativeCrate) {
			AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.draw(matrixStack, this, guiLeft + 20 + 36, guiTop + 19);
			return;
		}
		client.getTextureManager()
			.bindTexture(sprite.location);
		drawTexture(matrixStack, guiLeft + 20 + 36, guiTop + 19, sprite.startX, sprite.startY, (int) (sprite.width * amount),
			sprite.height);
	}

	@Override
	protected void renderWindowForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		SchematicannonTileEntity te = container.getTileEntity();

		int fuelX = guiLeft + 20 + 36, fuelY = guiTop + 19;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.width
			&& mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.height) {
			List<ITextComponent> tooltip = getFuelLevelTooltip(te);
			renderTooltip(matrixStack, tooltip, mouseX, mouseY);
		}

		if (hoveredSlot != null && !hoveredSlot.getHasStack()) {
			if (hoveredSlot.getSlotIndex() == 0)
				renderTooltip(matrixStack,
					TooltipHelper.cutStringToComponents(Lang.translate(_slotSchematic), TextFormatting.GRAY, TextFormatting.BLUE),
					mouseX, mouseY);
			if (hoveredSlot.getSlotIndex() == 2)
				renderTooltip(matrixStack,
					TooltipHelper.cutStringToComponents(Lang.translate(_slotListPrinter), TextFormatting.GRAY, TextFormatting.BLUE),
					mouseX, mouseY);
			if (hoveredSlot.getSlotIndex() == 4)
				renderTooltip(matrixStack,
					TooltipHelper.cutStringToComponents(Lang.translate(_slotGunpowder), TextFormatting.GRAY, TextFormatting.BLUE),
					mouseX, mouseY);
		}

		if (te.missingItem != null) {
			int missingBlockX = guiLeft + 150, missingBlockY = guiTop + 46;
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

	protected List<ITextComponent> getFuelLevelTooltip(SchematicannonTileEntity te) {
		double fuelUsageRate = te.getFuelUsageRate();
		int shotsLeft = (int) (te.fuelLevel / fuelUsageRate);
		int shotsLeftWithItems = (int) (shotsLeft + te.inventory.getStackInSlot(4)
			.getCount() * (te.getFuelAddedByGunPowder() / fuelUsageRate));
		List<ITextComponent> tooltip = new ArrayList<>();

		if (te.hasCreativeCrate) {
			tooltip.add(Lang.translate(_gunpowderLevel, "" + 100));
			tooltip.add(new StringTextComponent("(").append( new TranslationTextComponent(AllBlocks.CREATIVE_CRATE.get()
				.getTranslationKey())).append(")").formatted(DARK_PURPLE));
			return tooltip;
		}

		float f = te.fuelLevel * 100;
		tooltip.add(Lang.translate(_gunpowderLevel, "" + (int) f));
		tooltip.add(Lang.translate(_shotsRemaining, "" + TextFormatting.BLUE + shotsLeft).formatted(GRAY));
		if (shotsLeftWithItems != shotsLeft)
			tooltip
				.add(Lang.translate(_shotsRemainingWithBackup, "" + TextFormatting.BLUE + shotsLeftWithItems).formatted(GRAY));
		return tooltip;
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (showSettingsButton.isHovered()) {
			showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
			initPlacementSettings();
		}

		if (confirmButton.isHovered()) {
			Minecraft.getInstance().player.closeScreen();
			return true;
		}

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
				if (!replaceLevelButtons.get(replaceMode)
					.isHovered())
					continue;
				if (container.getTileEntity().replaceMode == replaceMode)
					continue;
				sendOptionUpdate(Option.values()[replaceMode], true);
			}
			if (skipMissingButton.isHovered())
				sendOptionUpdate(Option.SKIP_MISSING, !container.getTileEntity().skipMissing);
			if (skipTilesButton.isHovered())
				sendOptionUpdate(Option.SKIP_TILES, !container.getTileEntity().replaceTileEntities);
		}

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
		AllPackets.channel.sendToServer(ConfigureSchematicannonPacket.setOption(container.getTileEntity()
			.getPos(), option, set));
	}

}
