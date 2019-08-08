package com.simibubi.create.modules.shopping;

import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class ShopShelfScreen extends AbstractSimiContainerScreen<ShopShelfContainer> {

	public ShopShelfScreen(ShopShelfContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		renderTooltip("Hi", mouseX, mouseY);
	}

}
