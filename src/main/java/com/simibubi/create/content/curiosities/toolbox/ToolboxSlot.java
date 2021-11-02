package com.simibubi.create.content.curiosities.toolbox;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

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
