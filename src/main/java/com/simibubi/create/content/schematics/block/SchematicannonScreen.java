package com.simibubi.create.content.schematics.block;

import static net.minecraft.util.text.TextFormatting.BLUE;
import staticnet.minecraft.ChatFormattingg.DARK_PURPLE;
import static net.minecraft.util.text.TextFormattingimport com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

.GRAY;

import javanet.minecraft.ChatFormattinglections;
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

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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

	private List<Rect2i> extraAreas = Collections.emptyList();
	protected List<AbstractWidget> placementSettingWidgets;

	private final Component listPrinter = Lang.translate("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _showSettings = "gui.schematicannon.showOptions";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
	private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
	private final String _slotSchematic = "gui.schematicannon.slot.schematic";

	private final Component optionEnabled = Lang.translate("gui.schematicannon.optionEnabled");
	private final Component optionDisabled = Lang.translate("gui.schematicannon.optionDisabled");

	private final ItemStack renderedItem = AllBlocks.SCHEMATICANNON.asStack();

	private IconButton confirmButton;
	private IconButton showSettingsButton;
	private Indicator showSettingsIndicator;

	public SchematicannonScreen(SchematicannonContainer container, Inventory inventory,
								Component title) {
		super(container, inventory, title);
		placementSettingWidgets = new ArrayList<>();
	}

	@Override
	protected void init() {
		setWindowSize(BG_TOP.width, BG_TOP.height + BG_BOTTOM.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
		setWindowOffset(-10 + (width % 2 == 0 ? 0 : -1), 0);
		super.init();
		widgets.clear();

		int x = leftPos;
		int y = topPos;

		// Play Pause Stop
		playButton = new IconButton(x + 75, y + 86, AllIcons.I_PLAY);
		playIndicator = new Indicator(x + 75, y + 79, TextComponent.EMPTY);
		pauseButton = new IconButton(x + 93, y + 86, AllIcons.I_PAUSE);
		pauseIndicator = new Indicator(x + 93, y + 79, TextComponent.EMPTY);
		resetButton = new IconButton(x + 111, y + 86, AllIcons.I_STOP);
		resetIndicator = new Indicator(x + 111, y + 79, TextComponent.EMPTY);
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
			resetIndicator);

		confirmButton = new IconButton(x + 180, y + 117, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);
		showSettingsButton = new IconButton(x + 9, y + 117, AllIcons.I_PLACEMENT_SETTINGS);
		showSettingsButton.setToolTip(Lang.translate(_showSettings));
		widgets.add(showSettingsButton);
		showSettingsIndicator = new Indicator(x + 9, y + 111, TextComponent.EMPTY);
		widgets.add(showSettingsIndicator);

		extraAreas = ImmutableList.of(
			new Rect2i(x + BG_TOP.width, y + BG_TOP.height + BG_BOTTOM.height - 62, 84, 92)
		);

		tick();
	}

	private void initPlacementSettings() {
		widgets.removeAll(placementSettingWidgets);
		placementSettingWidgets.clear();

		if (placementSettingsHidden())
			return;

		int x = leftPos;
		int y = topPos;

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<AllIcons> icons = ImmutableList.of(AllIcons.I_DONT_REPLACE, AllIcons.I_REPLACE_SOLID,
			AllIcons.I_REPLACE_ANY, AllIcons.I_REPLACE_EMPTY);
		List<Component> toolTips = ImmutableList.of(Lang.translate("gui.schematicannon.option.dontReplaceSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithAny"),
			Lang.translate("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 33 + i * 18, y + 111, TextComponent.EMPTY));
			replaceLevelButtons.add(new IconButton(x + 33 + i * 18, y + 117, icons.get(i)));
			replaceLevelButtons.get(i)
				.setToolTip(toolTips.get(i));
		}
		placementSettingWidgets.addAll(replaceLevelButtons);
		placementSettingWidgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 111, y + 117, AllIcons.I_SKIP_MISSING);
		skipMissingButton.setToolTip(Lang.translate("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 111, y + 111, TextComponent.EMPTY);
		Collections.addAll(placementSettingWidgets, skipMissingButton, skipMissingIndicator);

		skipTilesButton = new IconButton(x + 129, y + 117, AllIcons.I_SKIP_TILES);
		skipTilesButton.setToolTip(Lang.translate("gui.schematicannon.option.skipTileEntities"));
		skipTilesIndicator = new Indicator(x + 129, y + 111, TextComponent.EMPTY);
		Collections.addAll(placementSettingWidgets, skipTilesButton, skipTilesIndicator);

		widgets.addAll(placementSettingWidgets);
	}

	protected boolean placementSettingsHidden() {
		return showSettingsIndicator.state == State.OFF;
	}

	@Override
	public void tick() {
		SchematicannonTileEntity te = menu.getTileEntity();

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

		for (AbstractWidget w : placementSettingWidgets)
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
		List<Component> tip = button.getToolTip();
		tip.add((enabled ? optionEnabled : optionDisabled).plainCopy().withStyle(BLUE));
		tip.addAll(TooltipHelper.cutTextComponent(Lang.translate("gui.schematicannon.option." + tooltipKey + ".description"),
			GRAY, GRAY));
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.width);
		int invY = topPos + BG_TOP.height + BG_BOTTOM.height + 2;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		BG_TOP.draw(ms, this, x, y);
		BG_BOTTOM.draw(ms, this, x, y + BG_TOP.height);

		SchematicannonTileEntity te = menu.getTileEntity();
		renderPrintingProgress(ms, x, y, te.schematicProgress);
		renderFuelBar(ms, x, y, te.fuelLevel);
		renderChecklistPrinterProgress(ms, x, y, te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0)
			.isEmpty())
			renderBlueprintHighlight(ms, x, y);

		GuiGameElement.of(renderedItem)
			.<GuiGameElement.GuiRenderBuilder>at(x + BG_TOP.width, y + BG_TOP.height + BG_BOTTOM.height - 48, -200)
			.scale(5)
			.render(ms);

		drawCenteredString(ms, font, title, x + (BG_TOP.width - 8) / 2, y + 3, 0xFFFFFF);

		Component msg = Lang.translate("schematicannon.status." + te.statusMsg);
		int stringWidth = font.width(msg);

		if (te.missingItem != null) {
			stringWidth += 16;
			GuiGameElement.of(te.missingItem)
				.<GuiGameElement.GuiRenderBuilder>at(x + 128, y + 49, 100)
				.scale(1)
				.render(ms);
		}

		font.drawShadow(ms, msg, x + 103 - stringWidth / 2, y + 53, 0xCCDDFF);
	}

	protected void renderBlueprintHighlight(PoseStack matrixStack, int x, int y) {
		AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.draw(matrixStack, this, x + 10, y + 60);
	}

	protected void renderPrintingProgress(PoseStack matrixStack, int x, int y, float progress) {
		progress = Math.min(progress, 1);
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
		sprite.bind();
		blit(matrixStack, x + 44, y + 64, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderChecklistPrinterProgress(PoseStack matrixStack, int x, int y, float progress) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
		sprite.bind();
		blit(matrixStack, x + 154, y + 20, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderFuelBar(PoseStack matrixStack, int x, int y, float amount) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
		if (menu.getTileEntity().hasCreativeCrate) {
			AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.draw(matrixStack, this, x + 36, y + 19);
			return;
		}
		sprite.bind();
		blit(matrixStack, x + 36, y + 19, sprite.startX, sprite.startY, (int) (sprite.width * amount),
			sprite.height);
	}

	@Override
	protected void renderWindowForeground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		SchematicannonTileEntity te = menu.getTileEntity();

		int x = leftPos;
		int y = topPos;

		int fuelX = x + 36, fuelY = y + 19;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.width
			&& mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.height) {
			List<Component> tooltip = getFuelLevelTooltip(te);
			renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
		}

		if (hoveredSlot != null && !hoveredSlot.hasItem()) {
			if (hoveredSlot.index == 0)
				renderComponentTooltip(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotSchematic), GRAY, ChatFormatting.BLUE),
					mouseX, mouseY);
			if (hoveredSlot.index == 2)
				renderComponentTooltip(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotListPrinter), GRAY, ChatFormatting.BLUE),
					mouseX, mouseY);
			if (hoveredSlot.index == 4)
				renderComponentTooltip(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotGunpowder), GRAY, ChatFormatting.BLUE),
					mouseX, mouseY);
		}

		if (te.missingItem != null) {
			int missingBlockX = x + 128, missingBlockY = y + 49;
			if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16
				&& mouseY <= missingBlockY + 16) {
				renderTooltip(matrixStack, te.missingItem, mouseX, mouseY);
			}
		}

		int paperX = x + 112, paperY = y + 19;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16)
			renderTooltip(matrixStack, listPrinter, mouseX, mouseY);

		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	protected List<Component> getFuelLevelTooltip(SchematicannonTileEntity te) {
		double fuelUsageRate = te.getFuelUsageRate();
		int shotsLeft = (int) (te.fuelLevel / fuelUsageRate);
		int shotsLeftWithItems = (int) (shotsLeft + te.inventory.getStackInSlot(4)
			.getCount() * (te.getFuelAddedByGunPowder() / fuelUsageRate));
		List<Component> tooltip = new ArrayList<>();

		if (te.hasCreativeCrate) {
			tooltip.add(Lang.translate(_gunpowderLevel, "" + 100));
			tooltip.add(new TextComponent("(").append(new TranslatableComponent(AllBlocks.CREATIVE_CRATE.get()
				.getDescriptionId())).append(")").withStyle(DARK_PURPLE));
			return tooltip;
		}

		int fillPercent = (int) (te.fuelLevel * 100);
		tooltip.add(Lang.translate(_gunpowderLevel, fillPercent));
		tooltip.add(Lang.translate(_shotsRemaining, new TextComponent(Integer.toString(shotsLeft)).withStyle(BLUE)).withStyle(GRAY));
		if (shotsLeftWithItems != shotsLeft)
			tooltip.add(Lang.translate(_shotsRemainingWithBackup, new TextComponent(Integer.toString(shotsLeftWithItems)).withStyle(BLUE)).withStyle(GRAY));

		return tooltip;
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (showSettingsButton.isHovered()) {
			showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
			initPlacementSettings();
		}

		if (confirmButton.isHovered()) {
			minecraft.player.closeContainer();
			return true;
		}

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
				if (!replaceLevelButtons.get(replaceMode)
					.isHovered())
					continue;
				if (menu.getTileEntity().replaceMode == replaceMode)
					continue;
				sendOptionUpdate(Option.values()[replaceMode], true);
			}
			if (skipMissingButton.isHovered())
				sendOptionUpdate(Option.SKIP_MISSING, !menu.getTileEntity().skipMissing);
			if (skipTilesButton.isHovered())
				sendOptionUpdate(Option.SKIP_TILES, !menu.getTileEntity().replaceTileEntities);
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
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		AllPackets.channel.sendToServer(new ConfigureSchematicannonPacket(option, set));
	}

}

