package com.simibubi.create.content.schematics.cannon;

import static net.minecraft.ChatFormatting.BLUE;
import static net.minecraft.ChatFormatting.DARK_PURPLE;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.schematics.cannon.ConfigureSchematicannonPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Indicator.State;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonMenu> {

	private static final AllGuiTextures BG_BOTTOM = AllGuiTextures.SCHEMATICANNON_BOTTOM;
	private static final AllGuiTextures BG_TOP = AllGuiTextures.SCHEMATICANNON_TOP;

	private final Component listPrinter = Lang.translateDirect("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _showSettings = "gui.schematicannon.showOptions";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
	private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
	private final String _slotSchematic = "gui.schematicannon.slot.schematic";

	private final Component optionEnabled = Lang.translateDirect("gui.schematicannon.optionEnabled");
	private final Component optionDisabled = Lang.translateDirect("gui.schematicannon.optionDisabled");

	protected Vector<Indicator> replaceLevelIndicators;
	protected Vector<IconButton> replaceLevelButtons;

	protected IconButton skipMissingButton;
	protected Indicator skipMissingIndicator;
	protected IconButton skipBlockEntitiesButton;
	protected Indicator skipBlockEntitiesIndicator;

	protected IconButton playButton;
	protected Indicator playIndicator;
	protected IconButton pauseButton;
	protected Indicator pauseIndicator;
	protected IconButton resetButton;
	protected Indicator resetIndicator;

	private IconButton confirmButton;
	private IconButton showSettingsButton;
	private Indicator showSettingsIndicator;

	protected List<AbstractWidget> placementSettingWidgets;

	private final ItemStack renderedItem = AllBlocks.SCHEMATICANNON.asStack();

	private List<Rect2i> extraAreas = Collections.emptyList();

	public SchematicannonScreen(SchematicannonMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		placementSettingWidgets = new ArrayList<>();
	}

	@Override
	protected void init() {
		setWindowSize(BG_TOP.width, BG_TOP.height + BG_BOTTOM.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
		setWindowOffset(-11, 0);
		super.init();

		int x = leftPos;
		int y = topPos;

		// Play Pause Stop
		playButton = new IconButton(x + 75, y + 86, AllIcons.I_PLAY);
		playButton.withCallback(() -> {
			sendOptionUpdate(Option.PLAY, true);
		});
		playIndicator = new Indicator(x + 75, y + 79, Components.immutableEmpty());
		pauseButton = new IconButton(x + 93, y + 86, AllIcons.I_PAUSE);
		pauseButton.withCallback(() -> {
			sendOptionUpdate(Option.PAUSE, true);
		});
		pauseIndicator = new Indicator(x + 93, y + 79, Components.immutableEmpty());
		resetButton = new IconButton(x + 111, y + 86, AllIcons.I_STOP);
		resetButton.withCallback(() -> {
			sendOptionUpdate(Option.STOP, true);
		});
		resetIndicator = new Indicator(x + 111, y + 79, Components.immutableEmpty());
		resetIndicator.state = State.RED;
		addRenderableWidgets(playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
			resetIndicator);

		confirmButton = new IconButton(x + 180, y + 117, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			minecraft.player.closeContainer();
		});
		addRenderableWidget(confirmButton);
		showSettingsButton = new IconButton(x + 9, y + 117, AllIcons.I_PLACEMENT_SETTINGS);
		showSettingsButton.withCallback(() -> {
			showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
			initPlacementSettings();
		});
		showSettingsButton.setToolTip(Lang.translateDirect(_showSettings));
		addRenderableWidget(showSettingsButton);
		showSettingsIndicator = new Indicator(x + 9, y + 111, Components.immutableEmpty());
		addRenderableWidget(showSettingsIndicator);

		extraAreas = ImmutableList.of(new Rect2i(x + BG_TOP.width, y + BG_TOP.height + BG_BOTTOM.height - 62, 84, 92));

		tick();
	}

	private void initPlacementSettings() {
		removeWidgets(placementSettingWidgets);
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
		List<Component> toolTips = ImmutableList.of(Lang.translateDirect("gui.schematicannon.option.dontReplaceSolid"),
			Lang.translateDirect("gui.schematicannon.option.replaceWithSolid"),
			Lang.translateDirect("gui.schematicannon.option.replaceWithAny"),
			Lang.translateDirect("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 33 + i * 18, y + 111, Components.immutableEmpty()));
			IconButton replaceLevelButton = new IconButton(x + 33 + i * 18, y + 117, icons.get(i));
			int replaceMode = i;
			replaceLevelButton.withCallback(() -> {
				if (menu.contentHolder.replaceMode != replaceMode)
					sendOptionUpdate(Option.values()[replaceMode], true);
			});
			replaceLevelButton.setToolTip(toolTips.get(i));
			replaceLevelButtons.add(replaceLevelButton);
		}
		placementSettingWidgets.addAll(replaceLevelButtons);
		placementSettingWidgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 111, y + 117, AllIcons.I_SKIP_MISSING);
		skipMissingButton.withCallback(() -> {
			sendOptionUpdate(Option.SKIP_MISSING, !menu.contentHolder.skipMissing);
		});
		skipMissingButton.setToolTip(Lang.translateDirect("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 111, y + 111, Components.immutableEmpty());
		Collections.addAll(placementSettingWidgets, skipMissingButton, skipMissingIndicator);

		skipBlockEntitiesButton = new IconButton(x + 129, y + 117, AllIcons.I_SKIP_BLOCK_ENTITIES);
		skipBlockEntitiesButton.withCallback(() -> {
			sendOptionUpdate(Option.SKIP_BLOCK_ENTITIES, !menu.contentHolder.replaceBlockEntities);
		});
		skipBlockEntitiesButton.setToolTip(Lang.translateDirect("gui.schematicannon.option.skipBlockEntities"));
		skipBlockEntitiesIndicator = new Indicator(x + 129, y + 111, Components.immutableEmpty());
		Collections.addAll(placementSettingWidgets, skipBlockEntitiesButton, skipBlockEntitiesIndicator);

		addRenderableWidgets(placementSettingWidgets);
	}

	protected boolean placementSettingsHidden() {
		return showSettingsIndicator.state == State.OFF;
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		SchematicannonBlockEntity be = menu.contentHolder;

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
				replaceLevelButtons.get(replaceMode).active = replaceMode != be.replaceMode;
				replaceLevelIndicators.get(replaceMode).state = replaceMode == be.replaceMode ? State.ON : State.OFF;
			}
			skipMissingIndicator.state = be.skipMissing ? State.ON : State.OFF;
			skipBlockEntitiesIndicator.state = !be.replaceBlockEntities ? State.ON : State.OFF;
		}

		playIndicator.state = State.OFF;
		pauseIndicator.state = State.OFF;
		resetIndicator.state = State.OFF;

		switch (be.state) {
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
						.add(TooltipHelper.holdShift(Palette.BLUE, hasShiftDown()));
				}
			}

		if (hasShiftDown()) {
			fillToolTip(skipMissingButton, skipMissingIndicator, "skipMissing");
			fillToolTip(skipBlockEntitiesButton, skipBlockEntitiesIndicator, "skipBlockEntities");
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
		tip.add((enabled ? optionEnabled : optionDisabled).plainCopy()
			.withStyle(BLUE));
		tip.addAll(TooltipHelper
			.cutTextComponent(Lang.translateDirect("gui.schematicannon.option." + tooltipKey + ".description"), Palette.ALL_GRAY));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.width);
		int invY = topPos + BG_TOP.height + BG_BOTTOM.height + 2;
		renderPlayerInventory(graphics, invX, invY);

		int x = leftPos;
		int y = topPos;

		BG_TOP.render(graphics, x, y);
		BG_BOTTOM.render(graphics, x, y + BG_TOP.height);

		SchematicannonBlockEntity be = menu.contentHolder;
		renderPrintingProgress(graphics, x, y, be.schematicProgress);
		renderFuelBar(graphics, x, y, be.fuelLevel);
		renderChecklistPrinterProgress(graphics, x, y, be.bookPrintingProgress);

		if (!be.inventory.getStackInSlot(0)
			.isEmpty())
			renderBlueprintHighlight(graphics, x, y);

		GuiGameElement.of(renderedItem).<GuiGameElement
			.GuiRenderBuilder>at(x + BG_TOP.width, y + BG_TOP.height + BG_BOTTOM.height - 48, -200)
			.scale(5)
			.render(graphics);

		graphics.drawCenteredString(font, title, x + (BG_TOP.width - 8) / 2, y + 3, 0xFFFFFF);

		Component msg = Lang.translateDirect("schematicannon.status." + be.statusMsg);
		int stringWidth = font.width(msg);

		if (be.missingItem != null) {
			stringWidth += 16;
			GuiGameElement.of(be.missingItem).<GuiGameElement
				.GuiRenderBuilder>at(x + 128, y + 49, 100)
				.scale(1)
				.render(graphics);
		}

		graphics.drawString(font, msg, x + 103 - stringWidth / 2, y + 53, 0xCCDDFF);

		if ("schematicErrored".equals(be.statusMsg))
			graphics.drawString(font, Lang.translateDirect("schematicannon.status.schematicErroredCheckLogs"),
				x + 103 - stringWidth / 2, y + 65, 0xCCDDFF);
	}

	protected void renderBlueprintHighlight(GuiGraphics graphics, int x, int y) {
		AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.render(graphics, x + 10, y + 60);
	}

	protected void renderPrintingProgress(GuiGraphics graphics, int x, int y, float progress) {
		progress = Math.min(progress, 1);
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
		graphics.blit(sprite.location, x + 44, y + 64, sprite.startX, sprite.startY, (int) (sprite.width * progress), sprite.height);
	}

	protected void renderChecklistPrinterProgress(GuiGraphics graphics, int x, int y, float progress) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
		graphics.blit(sprite.location, x + 154, y + 20, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderFuelBar(GuiGraphics graphics, int x, int y, float amount) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
		if (menu.contentHolder.hasCreativeCrate) {
			AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.render(graphics, x + 36, y + 19);
			return;
		}
		graphics.blit(sprite.location, x + 36, y + 19, sprite.startX, sprite.startY, (int) (sprite.width * amount), sprite.height);
	}

	@Override
	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		SchematicannonBlockEntity be = menu.contentHolder;

		int x = leftPos;
		int y = topPos;

		int fuelX = x + 36, fuelY = y + 19;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.width
			&& mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.height) {
			List<Component> tooltip = getFuelLevelTooltip(be);
			graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
		}

		if (hoveredSlot != null && !hoveredSlot.hasItem()) {
			if (hoveredSlot.index == 0)
				graphics.renderComponentTooltip(font,
					TooltipHelper.cutTextComponent(Lang.translateDirect(_slotSchematic), Palette.GRAY_AND_BLUE), mouseX,
					mouseY);
			if (hoveredSlot.index == 2)
				graphics.renderComponentTooltip(font,
					TooltipHelper.cutTextComponent(Lang.translateDirect(_slotListPrinter), Palette.GRAY_AND_BLUE),
					mouseX, mouseY);
			if (hoveredSlot.index == 4)
				graphics.renderComponentTooltip(font,
					TooltipHelper.cutTextComponent(Lang.translateDirect(_slotGunpowder), Palette.GRAY_AND_BLUE), mouseX,
					mouseY);
		}

		if (be.missingItem != null) {
			int missingBlockX = x + 128, missingBlockY = y + 49;
			if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16
				&& mouseY <= missingBlockY + 16) {
				graphics.renderTooltip(font, be.missingItem, mouseX, mouseY);
			}
		}

		int paperX = x + 112, paperY = y + 19;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16)
			graphics.renderTooltip(font, listPrinter, mouseX, mouseY);

		super.renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	protected List<Component> getFuelLevelTooltip(SchematicannonBlockEntity be) {
		double fuelUsageRate = be.getFuelUsageRate();
		int shotsLeft = (int) (be.fuelLevel / fuelUsageRate);
		int shotsLeftWithItems = (int) (shotsLeft + be.inventory.getStackInSlot(4)
			.getCount() * (be.getFuelAddedByGunPowder() / fuelUsageRate));
		List<Component> tooltip = new ArrayList<>();

		if (be.hasCreativeCrate) {
			tooltip.add(Lang.translateDirect(_gunpowderLevel, "" + 100));
			tooltip.add(Components.literal("(").append(AllBlocks.CREATIVE_CRATE.get()
				.getName())
				.append(")")
				.withStyle(DARK_PURPLE));
			return tooltip;
		}

		int fillPercent = (int) (be.fuelLevel * 100);
		tooltip.add(Lang.translateDirect(_gunpowderLevel, fillPercent));
		tooltip.add(Lang.translateDirect(_shotsRemaining, Components.literal(Integer.toString(shotsLeft)).withStyle(BLUE))
			.withStyle(GRAY));
		if (shotsLeftWithItems != shotsLeft)
			tooltip.add(Lang
				.translateDirect(_shotsRemainingWithBackup,
					Components.literal(Integer.toString(shotsLeftWithItems)).withStyle(BLUE))
				.withStyle(GRAY));

		return tooltip;
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		AllPackets.getChannel().sendToServer(new ConfigureSchematicannonPacket(option, set));
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
