package com.simibubi.create.content.curiosities.toolbox;

import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.SlotItemHandler;

public class ToolboxSlot extends SlotItemHandler {

	private ToolboxContainer toolboxMenu;

	public ToolboxSlot(ToolboxContainer container, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
		this.toolboxMenu = container;
	}

	@Override
	public boolean isActive() {
		return !toolboxMenu.renderPass && super.isActive();
	}

}
