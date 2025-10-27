package com.simibubi.create.content.logistics.packagePort;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PackagePortScreen extends AbstractSimiContainerScreen<PackagePortMenu> {

	private boolean frogMode;
	private AllGuiTextures background;

	private EditBox addressBox;
	private IconButton confirmButton;
	private IconButton dontAcceptPackages;
	private IconButton acceptPackages;

	private ItemStack icon;

	private List<Rect2i> extraAreas = Collections.emptyList();

	public PackagePortScreen(PackagePortMenu container, Inventory inv, Component title) {
		super(container, inv, title);
		background = AllGuiTextures.FROGPORT_BG;
		frogMode = container.contentHolder instanceof FrogportBlockEntity;
		icon = new ItemStack(container.contentHolder.getBlockState()
			.getBlock()
			.asItem());
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight());
		super.init();
		clearWidgets();

		int x = getGuiLeft();
		int y = getGuiTop();

		Consumer<String> onTextChanged;
		onTextChanged = s -> addressBox.setX(nameBoxX(s, addressBox));
        addressBox = new EditBox(new NoShadowFontWrapper(font), x + 23, y - 11, background.getWidth() - 20, 10,
                Component.empty());
		addressBox.setBordered(false);
		addressBox.setMaxLength(25);
		addressBox.setTextColor(0x3D3C48);
		addressBox.setValue(menu.contentHolder.addressFilter);
		addressBox.setFocused(false);
		addressBox.mouseClicked(0, 0, 0);
		addressBox.setResponder(onTextChanged);
		addressBox.setX(nameBoxX(addressBox.getValue(), addressBox));
		addRenderableWidget(addressBox);

		confirmButton =
			new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.player.closeContainer());
		addRenderableWidget(confirmButton);

		acceptPackages = new IconButton(x + 37, y + background.getHeight() - 24, AllIcons.I_SEND_AND_RECEIVE);
		acceptPackages.withCallback(() -> {
			acceptPackages.green = true;
			dontAcceptPackages.green = false;
		});
		acceptPackages.green = menu.contentHolder.acceptsPackages;
		acceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_and_receive"));
		addRenderableWidget(acceptPackages);

		dontAcceptPackages = new IconButton(x + 37 + 18, y + background.getHeight() - 24, AllIcons.I_SEND_ONLY);
		dontAcceptPackages.withCallback(() -> {
			acceptPackages.green = false;
			dontAcceptPackages.green = true;
		});
		dontAcceptPackages.green = !menu.contentHolder.acceptsPackages;
		dontAcceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_only"));
		addRenderableWidget(dontAcceptPackages);

		containerTick();

		extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 50, 70, 60));
	}

	private int nameBoxX(String s, EditBox nameBox) {
		return getGuiLeft() + background.getWidth() / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
	}

	@Override
	protected void containerTick() {
		acceptPackages.visible = menu.contentHolder.target != null;
		dontAcceptPackages.visible = menu.contentHolder.target != null;
		super.containerTick();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
		int x = getGuiLeft();
		int y = getGuiTop();

		AllGuiTextures header = frogMode ? AllGuiTextures.FROGPORT_HEADER : AllGuiTextures.POSTBOX_HEADER;
		header.render(graphics, x, y - header.getHeight());
		background.render(graphics, x, y);

		String text = addressBox.getValue();
		if (!addressBox.isFocused()) {
			if (addressBox.getValue()
				.isEmpty()) {
				text = icon.getHoverName()
					.getString();
				graphics.drawString(font, text, nameBoxX(text, addressBox), y - 11, 0x3D3C48, false);
			}
			AllGuiTextures.FROGPORT_EDIT_NAME.render(graphics, nameBoxX(text, addressBox) + font.width(text) + 5,
				y - 14);
		}

		GuiGameElement.of(icon).<GuiGameElement
			.GuiRenderBuilder>at(x + background.getWidth() + 6, y + background.getHeight() - 56, -200)
			.scale(4)
			.render(graphics);

		int invX = leftPos + 30;
		int invY = topPos + 8 + imageHeight - AllGuiTextures.PLAYER_INVENTORY.getHeight();
		renderPlayerInventory(graphics, invX, invY);

		if (menu.contentHolder.target == null)
			return;

		x += 13;
		y += 58;
		AllGuiTextures.FROGPORT_SLOT.render(graphics, x, y);
		graphics.renderItem(menu.contentHolder.target.getIcon(), x + 1, y + 1);

		if (addressBox.isHovered()) {
			graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.package_port.catch_packages")
				.color(AbstractSimiWidget.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.package_port.catch_packages_empty")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.package_port.catch_packages_wildcard")
					.style(ChatFormatting.GRAY)
					.component()),
				pMouseX, pMouseY);
		}
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		boolean hitEnter = getFocused() instanceof EditBox
			&& (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER);

		if (hitEnter && addressBox.isFocused()) {
			addressBox.setFocused(false);
			return true;
		}

		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public void removed() {
		CatnipServices.NETWORK.sendToServer(new PackagePortConfigurationPacket(menu.contentHolder.getBlockPos(), addressBox.getValue(),
				acceptPackages.green));
		super.removed();
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
