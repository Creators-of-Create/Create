package com.simibubi.create.modules.logistics.management.index;

import static com.simibubi.create.ScreenResources.DISABLED_SLOT_FRAME;
import static com.simibubi.create.ScreenResources.DISABLED_SLOT_INNER;
import static com.simibubi.create.ScreenResources.INDEX_BOTTOM;
import static com.simibubi.create.ScreenResources.INDEX_BOTTOM_TRIM;
import static com.simibubi.create.ScreenResources.INDEX_MIDDLE;
import static com.simibubi.create.ScreenResources.INDEX_SCROLLER_BOTTOM;
import static com.simibubi.create.ScreenResources.INDEX_SCROLLER_MIDDLE;
import static com.simibubi.create.ScreenResources.INDEX_SCROLLER_TOP;
import static com.simibubi.create.ScreenResources.INDEX_SEARCH;
import static com.simibubi.create.ScreenResources.INDEX_SEARCH_OVERLAY;
import static com.simibubi.create.ScreenResources.INDEX_TAB;
import static com.simibubi.create.ScreenResources.INDEX_TAB_ACTIVE;
import static com.simibubi.create.ScreenResources.INDEX_TOP;
import static com.simibubi.create.ScreenResources.INDEX_TOP_TRIM;
import static com.simibubi.create.ScreenResources.I_CONFIRM;
import static com.simibubi.create.ScreenResources.SLOT_FRAME;
import static com.simibubi.create.ScreenResources.SLOT_INNER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.type.CountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class LogisticalIndexScreen extends AbstractSimiContainerScreen<LogisticalIndexContainer> {

	protected LogisticalIndexContainer container;
	protected IconButton orderButton;

	boolean searchActive = false;
	boolean searchHovered = false;
	InterpolatedChasingValue searchButtonOffset;
	TextFieldWidget searchTextField;
	String searchKey = "";

	TextFieldWidget receiverTextField;
	ScrollInput receiverScrollInput;
	List<String> receivers = new ArrayList<>();

	int cursorPos = 0;
	boolean cursorActive = false;
	InterpolatedChasingValue cursorLight;

	List<ItemStackEntry> displayedItems = new ArrayList<>();
	CountedItemsList order = new CountedItemsList();
	String initialTargetAddress;

	String title = Lang.translate("gui.index.title");
	String receiverScrollInputTitle = Lang.translate("gui.index.targetAddressSelect");
	String orderButtonTooltip = Lang.translate("gui.index.confirmOrder");
	String keyNumberInventories = "gui.index.numberIndexedInventories";
	ItemStack chestIcon = new ItemStack(Blocks.CHEST);

	public LogisticalIndexScreen(LogisticalIndexContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.container = container;
		cursorLight = new InterpolatedChasingValue().withSpeed(.25f);
		searchButtonOffset = new InterpolatedChasingValue().withSpeed(.5f);
		receivers = container.te.availableReceivers;
		initialTargetAddress = container.te.lastOrderAddress;
		buildDisplayedItems();
	}

	@Override
	protected void init() {
		int height = INDEX_TOP.height + INDEX_TOP_TRIM.height + INDEX_MIDDLE.height + INDEX_BOTTOM_TRIM.height
				+ INDEX_BOTTOM.height;
		int width = INDEX_MIDDLE.width;
		setWindowSize(width, height);
		super.init();
		widgets.clear();

		searchTextField = new TextFieldWidget(font, guiLeft + 23, guiTop + 8, 128, 8, "");
		searchTextField.setTextColor(0xFFFFFF);
		searchTextField.setDisabledTextColour(-1);
		searchTextField.setEnableBackgroundDrawing(false);
		searchTextField.setMaxStringLength(256);
		searchTextField.setResponder(this::onSearchKeyChanged);
		searchTextField.setFocused2(false);

		receiverTextField = new TextFieldWidget(font, guiLeft + 29, guiTop + 240, 116, 8, "");
		receiverTextField.setTextColor(0xFFFFFF);
		receiverTextField.setDisabledTextColour(-1);
		receiverTextField.setEnableBackgroundDrawing(false);
		receiverTextField.setMaxStringLength(256);
		receiverTextField.setResponder(this::onReceiverTextChanged);
		if (initialTargetAddress != null)
			receiverTextField.setText(initialTargetAddress);
		receiverTextField.setFocused2(false);

		receiverScrollInput = new SelectionScrollInput(guiLeft + 24, guiTop + 235, 126, 18).forOptions(receivers)
				.titled(receiverScrollInputTitle).calling(this::onReceiverScrollInputChanged);

		orderButton = new IconButton(guiLeft + 152, guiTop + 235, I_CONFIRM);
		orderButton.active = false;
		orderButton.setToolTip(orderButtonTooltip);

		widgets.add(receiverTextField);
		widgets.add(receiverScrollInput);
		widgets.add(orderButton);

		if (searchActive)
			widgets.add(searchTextField);
	}

	@Override
	public void tick() {
		super.tick();

		float targetOffset = 0.75f;
		if (searchTextField.isFocused())
			targetOffset = 1.5f;
		else if (searchHovered || searchActive)
			targetOffset = 1f;
		searchButtonOffset.target(targetOffset);
		searchButtonOffset.tick();

		if (cursorActive) {
			cursorLight.target(1);
			cursorLight.tick();
		} else {
			cursorLight.set(0);
		}

		if (!searchActive && searchTextField.isFocused())
			searchTextField.changeFocus(false);

		if (container.te.update) {
			buildDisplayedItems();
			((SelectionScrollInput) receiverScrollInput).forOptions(container.te.availableReceivers);
			container.te.update = false;
		}

	}

	private void updateSubmitButton() {
		orderButton.active = canSubmit();
	}

	public boolean canSubmit() {
		return !order.getFlattenedList().isEmpty()
				&& container.te.availableReceivers.contains(receiverTextField.getText());
	}

	private void onSearchKeyChanged(String newSearch) {
		if (searchKey.equals(newSearch))
			return;
		searchKey = new String(newSearch);
		buildDisplayedItems();
	}

	private void sendRequest() {
		String address = receiverTextField.getText();
		UUID id = container.te.getNetworkId();
		AllPackets.channel.sendToServer(new IndexOrderRequest(container.te.getPos(), address, order, id));
		order = new CountedItemsList();
		updateSubmitButton();
	}

	private void onReceiverTextChanged(String newSearch) {
		if (!receiverTextField.isFocused())
			return;
		receiverScrollInput.setState(0);
		receivers = container.te.availableReceivers.stream()
				.filter(str -> str.toLowerCase().startsWith(newSearch.toLowerCase())).collect(Collectors.toList());
		((SelectionScrollInput) receiverScrollInput).forOptions(receivers);
		receiverScrollInput.active = !receivers.isEmpty();
		if (receivers.isEmpty() || newSearch.isEmpty())
			receiverTextField.setSuggestion(null);
		else
			receiverTextField.setSuggestion(receivers.get(0).substring(newSearch.length()));
		updateSubmitButton();
	}

	private void onReceiverScrollInputChanged(int index) {
		if (receivers.isEmpty())
			return;
		String address = receivers.get(index);
		receiverTextField.setResponder(null);
		receiverTextField.setSuggestion(null);
		receiverTextField.setText(address);
		receiverTextField.setResponder(this::onReceiverTextChanged);
		updateSubmitButton();
	}

	public void buildDisplayedItems() {
		if (searchKey.isEmpty()) {
			displayedItems = container.allItems.get().getFlattenedList().stream().collect(Collectors.toList());
		} else {
			displayedItems = container.allItems
					.get().getFlattenedList().parallelStream().filter(entry -> entry.stack.getDisplayName()
							.getUnformattedComponentText().toLowerCase().startsWith(searchKey.toLowerCase()))
					.collect(Collectors.toList());
		}
		if (cursorActive)
			moveCursor(cursorPos);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (searchHovered && button == 0) {
			setSearchActive(!searchActive);
			return true;
		}

		if (orderButton.isHovered() && orderButton.active) {
			sendRequest();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public void mouseMoved(double xPos, double yPos) {
		cursorActive = false;
		super.mouseMoved(xPos, yPos);
	}

	private void setSearchActive(boolean searchActive) {
		this.searchActive = searchActive;
		if (searchActive) {
			cursorActive = false;
			searchTextField.setSelectionPos(0);
			searchTextField.changeFocus(true);
			widgets.add(searchTextField);
		} else {
			widgets.remove(searchTextField);
			onSearchKeyChanged("");
		}
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		InputMappings.Input mouseKey = InputMappings.getInputByCode(code, p_keyPressed_2_);
		boolean receiverFocused = receiverTextField.isFocused();
		boolean searchFocused = searchTextField.isFocused();
		boolean space = code == GLFW.GLFW_KEY_SPACE;
		boolean enter = code == GLFW.GLFW_KEY_ENTER;
		boolean writingText = receiverFocused || searchFocused;
		boolean closingScreen = this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey);

		if (closingScreen && writingText) {
			return true;
		}

		if (canSubmit() && hasShiftDown() && enter) {
			sendRequest();
			return true;
		}

		if (enter && searchActive && searchFocused) {
			searchTextField.changeFocus(false);
			searchTextField.setCursorPositionEnd();
			searchTextField.setSelectionPos(searchTextField.getCursorPosition());

			cursorActive = true;
			cursorPos = 0;

			if (searchTextField.getText().isEmpty())
				setSearchActive(false);

			return true;
		}

		if (enter && !writingText && cursorActive) {
			ItemStackEntry entry = displayedItems.get(cursorPos);
			if (!order.contains(entry.stack)) {
				if (order.getFlattenedList().size() > 4)
					return true;
				order.add(entry);
			} else
				order.remove(entry.stack);
			updateSubmitButton();
			return true;
		}

		if (space && !writingText) {
			setSearchActive(true);
			return true;
		}

		boolean up = code == GLFW.GLFW_KEY_UP;
		boolean left = code == GLFW.GLFW_KEY_LEFT;
		boolean down = code == GLFW.GLFW_KEY_DOWN;
		boolean right = code == GLFW.GLFW_KEY_RIGHT;
		boolean tab = code == GLFW.GLFW_KEY_TAB;

		if (!writingText && tab) {
			receiverTextField.setCursorPositionEnd();
			receiverTextField.setSelectionPos(0);
			receiverTextField.changeFocus(true);
		}

		if (receiverFocused) {
			if (enter) {
				receiverTextField.changeFocus(false);
			}
			if (tab) {
				if (receivers.isEmpty())
					return false;
				receiverTextField.setText(receivers.get(0));
				return true;
			}
			if (up || down) {
				receiverScrollInput.setState(receiverScrollInput.getState() + (up ? -1 : 1));
				receiverScrollInput.onChanged();
				return true;
			}
		}

		if (!writingText) {
			GameSettings keys = minecraft.gameSettings;
			boolean w = keys.keyBindForward.getKey().getKeyCode() == code || up;
			boolean a = keys.keyBindLeft.getKey().getKeyCode() == code || left;
			boolean s = keys.keyBindBack.getKey().getKeyCode() == code || down;
			boolean d = keys.keyBindRight.getKey().getKeyCode() == code || right;
			boolean any = w || a || s || d;

			if (any && !cursorActive) {
				cursorActive = true;
				return true;
			}

			if (any) {
				int offset = w ? -8 : a ? -1 : s ? 8 : d ? 1 : 0;
				if (hasShiftDown()) {
					offset *= 4;
					if (a || d) {
						int col = cursorPos % 8;
						offset = MathHelper.clamp(offset, -col, 7 - col);
					}
				}
				moveCursor(cursorPos + offset);
				return true;
			}

		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	protected void moveCursor(int slot) {
		int clamp = MathHelper.clamp(slot, 0, slotsVisible() - 1);
		if (cursorPos != clamp)
			cursorLight.set(0).set(0);
		cursorPos = clamp;
	}

	protected int slotsVisible() {
		return displayedItems.size();
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		applyNetworkColor();

		// Search bar
		searchHovered = (mouseX > guiLeft - 25 && mouseX < guiLeft && mouseY > guiTop + 6 && mouseY < guiTop + 31);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(searchButtonOffset.get(partialTicks) * -14, 0, 0);
		INDEX_SEARCH.draw(this, guiLeft - 5, guiTop + 8);
		GlStateManager.popMatrix();

		INDEX_MIDDLE.draw(this, guiLeft, guiTop + INDEX_TOP.height + 6);
		renderScrollbar();
		renderTabs();
		resetColor();
		renderSlots();
	}

	@Override
	public boolean charTyped(char character, int code) {
		if (!searchActive && !receiverTextField.isFocused())
			return false;
		if (character == ' ' && (searchTextField.getText().isEmpty()
				|| searchTextField.getSelectedText().equals(searchTextField.getText())))
			return false;
		return super.charTyped(character, code);
	}

	@Override
	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		resetColor();
		INDEX_TOP_TRIM.draw(this, guiLeft, guiTop + INDEX_TOP.height);
		INDEX_BOTTOM_TRIM.draw(this, guiLeft, guiTop + INDEX_TOP.height + INDEX_MIDDLE.height + 6);
		applyNetworkColor();
		INDEX_TOP.draw(this, guiLeft, guiTop);
		INDEX_BOTTOM.draw(this, guiLeft, guiTop + INDEX_TOP.height + INDEX_MIDDLE.height + 12);

		if (searchActive) {
			INDEX_SEARCH_OVERLAY.draw(this, guiLeft - 1, guiTop + 2);
			resetColor();

		} else {
			resetColor();
			int offset = (INDEX_TOP.width - font.getStringWidth(title)) / 2;
			font.drawStringWithShadow(title, guiLeft + offset, guiTop + 8,
					ColorHelper.mixColors(0xFFFFFF, container.te.getColor(), .25f));
		}

		orderButton.render(mouseX, mouseY, partialTicks);
		if (searchActive)
			searchTextField.render(mouseX, mouseY, partialTicks);
		receiverTextField.render(mouseX, mouseY, partialTicks);

		renderOrderedItems();
		ScreenElementRenderer.render3DItem(() -> {
			GlStateManager.translated(guiLeft + 6.5, guiTop + 235, 0);
			return chestIcon;
		});

		super.renderWindowForeground(mouseX, mouseY, partialTicks);
	}

	private void renderOrderedItems() {
		Collection<ItemStackEntry> flattenedList = order.getFlattenedList();

		int slotX = guiLeft + (getXSize() - flattenedList.size() * 18) / 2;
		int slotY = guiTop + 215;

		for (ItemStackEntry entry : flattenedList) {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableDepthTest();
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.0F, 0.0F, 32.0F);
			this.blitOffset = 200;
			this.itemRenderer.zLevel = 200.0F;
			net.minecraft.client.gui.FontRenderer font = entry.stack.getItem().getFontRenderer(entry.stack);
			if (font == null)
				font = this.font;
			this.itemRenderer.renderItemAndEffectIntoGUI(entry.stack, slotX, slotY);
			this.renderItemOverlayIntoGUI(font, entry.stack, slotX, slotY,
					entry.amount > 1 ? String.valueOf(entry.amount) : null, 0xFFFFFF);
			this.blitOffset = 0;
			this.itemRenderer.zLevel = 0.0F;
			GlStateManager.popMatrix();
			slotX += 18;
		}
	}

	private void renderSlots() {
		int slot = 0;
		for (ItemStackEntry entry : displayedItems) {
			resetColor();
			
			renderSlot(slot, entry.stack, entry.amount);
			slot++;
		}
	}

	private void renderSlot(int slot, ItemStack stack, int count) {
		int slotX = getSlotX(slot);
		int slotY = getSlotY(slot);

//		boolean ordered = order.contains(stack);
		boolean orderedFully = order.getItemCount(stack) == count;
		RenderHelper.disableStandardItemLighting();

		if (orderedFully) {
			DISABLED_SLOT_FRAME.draw(this, slotX, slotY);
			DISABLED_SLOT_INNER.draw(this, slotX, slotY);
		} else {
			SLOT_FRAME.draw(this, slotX, slotY);
			SLOT_INNER.draw(this, slotX, slotY);
		}

		if (cursorActive && slot == cursorPos) {
			renderCursor();
		}

		slotX++;
		slotY++;

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 32.0F);
		this.blitOffset = 200;
		this.itemRenderer.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null)
			font = this.font;
		this.itemRenderer.renderItemAndEffectIntoGUI(stack, slotX, slotY);

		String text = count > 1 ? String.valueOf(count) : null;
		int color = 0xFFFFFF;
		if (orderedFully) {
			color = ColorHelper.mixColors(container.te.getColor(), 0xFFFFFF, 0.5f);
			text = new StringTextComponent("\u2714").getFormattedText();
		}

		this.renderItemOverlayIntoGUI(font, stack, slotX, slotY, text, color);
		this.blitOffset = 0;
		this.itemRenderer.zLevel = 0.0F;
		GlStateManager.popMatrix();
	}

	public int getSlotY(int slot) {
		return guiTop + 28 + 18 * (slot / 8);
	}

	public int getSlotX(int slot) {
		return guiLeft + 15 + 18 * (slot % 8);
	}

	private void renderScrollbar() {
		INDEX_SCROLLER_TOP.draw(this, guiLeft + 173, guiTop + 31);
		INDEX_SCROLLER_MIDDLE.draw(this, guiLeft + 173, guiTop + 37);
		INDEX_SCROLLER_BOTTOM.draw(this, guiLeft + 173, guiTop + 43);
	}

	private void renderTabs() {
		INDEX_TAB.draw(this, guiLeft - 19, guiTop + 40);
		INDEX_TAB_ACTIVE.draw(this, guiLeft - 19, guiTop + 61);
		INDEX_TAB.draw(this, guiLeft - 19, guiTop + 82);
		INDEX_TAB.draw(this, guiLeft - 19, guiTop + 103);
	}

	public void resetColor() {
		ColorHelper.glResetColor();
	}

	public void applyNetworkColor() {
		ColorHelper.glColor(container.te.getColor());
	}

	private void renderCursor() {
		if (!cursorActive)
			return;
		int x = getSlotX(cursorPos);
		int y = getSlotY(cursorPos);

		float pt = minecraft.getRenderPartialTicks();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, cursorLight.get(pt));
		ScreenResources.SELECTED_SLOT_INNER.draw(this, x, y);
		resetColor();
	}

}
