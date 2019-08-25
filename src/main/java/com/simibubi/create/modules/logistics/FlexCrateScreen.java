package com.simibubi.create.modules.logistics;

import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class FlexCrateScreen extends AbstractSimiContainerScreen<FlexCrateContainer> {

	public FlexCrateScreen(FlexCrateContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		
	}

}
