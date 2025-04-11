package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.gui.AllIcons;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicBoolean;

public class PackageFilterScreen extends AbstractFilterScreen<PackageFilterMenu> {

	private AddressEditBox addressBox;
	private IconButton useGlobPatternButton, useRegexButton;
	private boolean deferFocus;

	public PackageFilterScreen(PackageFilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, AllGuiTextures.PACKAGE_FILTER);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (deferFocus) {
			deferFocus = false;
			setFocused(addressBox);
		}
		addressBox.tick();
	}

	@Override
	protected void init() {
		AtomicBoolean regexState = new AtomicBoolean(menu.useRegex);
		setWindowOffset(-11, 7);
		super.init();

		int x = leftPos;
		int y = topPos;

		addressBox = new AddressEditBox(this, this.font, x + 44, y + 28, 129, 9, false);
		addressBox.setTextColor(0xffffff);
		addressBox.setValue(menu.address);
		addressBox.setResponder(this::onAddressEdited);
		addRenderableWidget(addressBox);

		useGlobPatternButton = new IconButton(x + 18, y + 28 + 36, AllIcons.I_GLOB_PATTERN);
		useGlobPatternButton.withCallback(() -> {
			useGlobPatternButton.green = true;
			useRegexButton.green = false;
			regexState.set(false);
			onRegexToggled(regexState);
		});
		useGlobPatternButton.setToolTip(CreateLang.translate("gui.package_filter.use_glob_patterns")
			.style(ChatFormatting.WHITE)
			.component());
		addRenderableWidget(useGlobPatternButton);

		useRegexButton = new IconButton(x + 18 + 18, y + 28 + 36, AllIcons.I_REGEX);
		useRegexButton.active = menu.usingRegex();
		useRegexButton.withCallback(() -> {
			useGlobPatternButton.green = false;
			useRegexButton.green = true;
			regexState.set(true);
			onRegexToggled(regexState);
		});
		useRegexButton.setToolTip(CreateLang.translate("gui.package_filter.use_regex")
			.style(ChatFormatting.GOLD)
			.component());
		addRenderableWidget(useRegexButton);

		setFocused(addressBox);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(leftPos + 16, topPos + 23, 0);
		GuiGameElement.of(PackageStyles.getDefaultBox())
			.render(graphics);
		ms.popPose();
	}

	public void onAddressEdited(String s) {
		menu.address = s;
		CompoundTag tag = new CompoundTag();
		tag.putString("Address", s);
		CatnipServices.NETWORK.sendToServer(new FilterScreenPacket(Option.UPDATE_ADDRESS, tag));
	}

	public void onRegexToggled(AtomicBoolean b) {
		menu.useRegex = b.get();
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("UseRegex", b.get());
		CatnipServices.NETWORK.sendToServer(new FilterScreenPacket(Option.UPDATE_MATCH_TYPE, tag));
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
			return true;
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == GLFW.GLFW_KEY_ENTER)
			setFocused(null);
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		return super.charTyped(pCodePoint, pModifiers);
	}

	@Override
	protected void contentsCleared() {
		addressBox.setValue("");
		deferFocus = true;
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		return false;
	}

}
