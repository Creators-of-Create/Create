package com.simibubi.create.modules.logistics.management.controller;

import static com.simibubi.create.ScreenResources.I_PRIORITY_HIGH;
import static com.simibubi.create.ScreenResources.I_PRIORITY_LOW;
import static com.simibubi.create.ScreenResources.I_PRIORITY_VERY_HIGH;
import static com.simibubi.create.ScreenResources.I_PRIORITY_VERY_LOW;
import static com.simibubi.create.ScreenResources.LOGISTICAL_CONTROLLER;
import static com.simibubi.create.ScreenResources.LOGISTICAL_CONTROLLER_TRIM;
import static com.simibubi.create.ScreenResources.PLAYER_INVENTORY;
import static com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.TYPE;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity.Priority;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.Type;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class LogisticalInventoryControllerScreen
		extends AbstractSimiContainerScreen<LogisticalInventoryControllerContainer> {

	private BlockState controllerState;
	private BlockState controllerLightState;
	private ItemStack nameTagItem;
	private ItemStack boxItem;

	private Type controllerType;

	private ScrollInput priorityInput;
	private TextFieldWidget addressInput;
	private IconButton passiveModeButton;
	private IconButton activeModeButton;
	private Indicator passiveModeIndicator;
	private Indicator activeModeIndicator;
	private IconButton confirmButton;
	private ScrollInput filterAmountInput;

	private final List<ScreenResources> priorityIcons = Arrays.asList(I_PRIORITY_VERY_HIGH, I_PRIORITY_HIGH,
			I_PRIORITY_LOW, I_PRIORITY_VERY_LOW);
	private final List<String> priorityOptions = Lang.translatedOptions("logistics.priority", "highest", "high", "low",
			"lowest");

	private String priority = Lang.translate("logistics.priority");
	private String requestedItemCount = Lang.translate("gui.requester.requestedItemCount");
	private String activeMode = Lang.translate("gui.logistical_controller.active_mode");
	private String passiveMode = Lang.translate("gui.logistical_controller.passive_mode");
	private String storagePassiveModeOnly = Lang.translate("gui.storage.passiveModeOnly");
	private String title;

	public LogisticalInventoryControllerScreen(LogisticalInventoryControllerContainer container, PlayerInventory inv,
			ITextComponent title) {
		super(container, inv, title);
		this.title = I18n
				.format(LogisticalControllerBlock.getControllerTypeTranslationKey(container.te.getBlockState()));
		controllerState = container.te.getBlockState().with(BlockStateProperties.FACING, Direction.SOUTH);
		controllerType = controllerState.get(LogisticalControllerBlock.TYPE);
		controllerLightState = AllBlocks.LOGISTICAL_CONTROLLER_INDICATOR.get().getDefaultState()
				.with(FACING, controllerState.get(FACING)).with(TYPE, controllerState.get(TYPE));
		nameTagItem = new ItemStack(Items.NAME_TAG);
		boxItem = AllItems.CARDBOARD_BOX_1410.asStack();
	}

	@Override
	protected void init() {
		setWindowSize(256, 200);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		addressInput = new TextFieldWidget(font, x + 29, y + 62, 116, 8, "");
		addressInput.setTextColor(0xFFFFFF);
		addressInput.setDisabledTextColour(-1);
		addressInput.setText(container.te.address);
		addressInput.setEnableBackgroundDrawing(false);
		addressInput.setMaxStringLength(256);
		addressInput.setResponder(this::onAddressInputChanged);
		addressInput.setFocused2(false);

		priorityInput = new SelectionScrollInput(x + 49, y + 31, 18, 18).forOptions(priorityOptions).titled(priority)
				.setState(container.te.getPriority().ordinal());
		filterAmountInput = new ScrollInput(x + 85, y + 46, 15, 8).withRange(1, 1025).withShiftStep(64)
				.titled(requestedItemCount).setState(container.te.getInventory().filterAmount);

		passiveModeButton = new IconButton(x + 8, y + 31, ScreenResources.I_PASSIVE);
		passiveModeButton.setToolTip(passiveMode);
		if (controllerType == Type.STORAGE)
			passiveModeButton.getToolTip().add(TextFormatting.GOLD + storagePassiveModeOnly);
		passiveModeIndicator = new Indicator(x + 8, y + 26, "");
		activeModeButton = new IconButton(x + 26, y + 31, ScreenResources.I_ACTIVE);
		activeModeButton.setToolTip(activeMode);
		activeModeIndicator = new Indicator(x + 26, y + 26, "");
		setPassiveMode(!container.te.isActive);

		confirmButton = new IconButton(x + 152, y + 57, ScreenResources.I_CONFIRM);

		widgets.addAll(Arrays.asList(priorityInput, addressInput, passiveModeButton, passiveModeIndicator,
				activeModeButton, activeModeIndicator, confirmButton));
		if (controllerType == Type.REQUEST)
			widgets.add(filterAmountInput);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		LOGISTICAL_CONTROLLER_TRIM.draw(this, x, y);
		LOGISTICAL_CONTROLLER_TRIM.draw(this, x, y + 6 + LOGISTICAL_CONTROLLER.height);

		ColorHelper.glColor(container.te.getColor());
		LOGISTICAL_CONTROLLER.draw(this, x, y + 6);
		ColorHelper.glResetColor();

		ScreenResources.BIG_SLOT.draw(this, x + 79, y + 24);
		ScreenResources.SHIPPING_SLOT.draw(this, x + 134, y + 28);
		ScreenResources.RECEIVING_SLOT.draw(this, x + 134 + 18, y + 28);
		PLAYER_INVENTORY.draw(this, x + (getXSize() - PLAYER_INVENTORY.width) / 2, y + 100);

		ScreenResources priorityIcon = priorityIcons.get(priorityInput.getState());
		ColorHelper.glColor(0);
		priorityIcon.draw(this, x + 51, y + 33);
		ColorHelper.glResetColor();
		priorityIcon.draw(this, x + 50, y + 32);

		if (controllerType == Type.REQUEST) {
			ScreenResources.ITEM_COUNT_SCROLLAREA.draw(this, x + 81, y + 45);
			GlStateManager.pushMatrix();
			double scale = getItemCountTextScale();
			String text = "" + filterAmountInput.getState();
			GlStateManager.translated(x + 91.5, y + 53, 0);
			GlStateManager.scaled(scale, scale, 0);
			int guiScaleFactor = (int) minecraft.mainWindow.getGuiScaleFactor();
			GlStateManager.translated((-font.getStringWidth(text)) / 2,
					-font.FONT_HEIGHT + (guiScaleFactor > 1 ? 1 : 1.75f), 0);
			font.drawStringWithShadow(text, 0, 0, 0xFFFFFF);
			GlStateManager.popMatrix();
		}

		font.drawString(playerInventory.getDisplayName().getFormattedText(), x + 48, y + 106, 0x666666);
		font.drawStringWithShadow(title, x + (LOGISTICAL_CONTROLLER.width - font.getStringWidth(title)) / 2, y + 9,
				ColorHelper.mixColors(0xFFFFFF, container.te.getColor(), .25f));

		ScreenElementRenderer.renderBlock(() -> {
			transformRenderedBlocks();
			return controllerState;
		});

		ScreenElementRenderer.renderBlock(() -> {
			transformRenderedBlocks();
			return controllerLightState;
		}, container.te.getColor());

		ColorHelper.glResetColor();
		RenderHelper.enableGUIStandardItemLighting();
		itemRenderer.renderItemIntoGUI(nameTagItem, x + 7, y + 57);
		itemRenderer.renderItemIntoGUI(boxItem, x + 116, y + 32);
	}

	public void onAddressInputChanged(String s) {
		confirmButton.active = !s.isEmpty();
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		InputMappings.Input mouseKey = InputMappings.getInputByCode(code, p_keyPressed_2_);
		boolean closingScreen = this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey);
		boolean enter = code == GLFW.GLFW_KEY_ENTER;

		if (closingScreen && addressInput.isFocused())
			return true;
		if (enter && addressInput.isFocused())
			addressInput.changeFocus(false);

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.active && confirmButton.isHovered())
			minecraft.displayGuiScreen(null);
		if (activeModeButton.active && activeModeButton.isHovered())
			setPassiveMode(false);
		if (passiveModeButton.active && passiveModeButton.isHovered())
			setPassiveMode(true);

		return super.mouseClicked(x, y, button);
	}

	public void setPassiveMode(boolean passive) {
		if (controllerType == Type.STORAGE) {
			activeModeButton.active = passiveModeButton.active = false;
			activeModeIndicator.state = State.OFF;
			passiveModeIndicator.state = State.ON;
			return;
		}

		activeModeButton.active = passive;
		passiveModeButton.active = !passive;
		activeModeIndicator.state = passive ? State.OFF : State.ON;
		passiveModeIndicator.state = !passive ? State.OFF : State.ON;
	}

	@Override
	public void removed() {
		boolean active = !activeModeButton.active;
		Priority priorityOut = Priority.values()[priorityInput.getState()];
		String text = addressInput.getText();
		AllPackets.channel.sendToServer(new LogisticalControllerConfigurationPacket(container.te.getPos(), text,
				filterAmountInput.getState(), priorityOut, active));
		super.removed();
	}

	private void transformRenderedBlocks() {
		GlStateManager.translated(guiLeft + 205, guiTop + 70, 0);
		GlStateManager.rotatef(50, -.5f, 1, -.2f);
		GlStateManager.rotatef(190, 0, 1, 0);
		GlStateManager.scaled(1.25, 1.25, 1.25);
	}

}
