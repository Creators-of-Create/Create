package com.simibubi.create.content.curiosities.toolbox;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;

public class ToolboxSlot extends SlotItemHandler {

	private ToolboxContainer toolboxMenu;

	public ToolboxSlot(ToolboxContainer container, ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
		this.toolboxMenu = container;
	}

	@Override
	public boolean isActive() {
		return !toolboxMenu.renderPass && super.isActive();
	}

}
