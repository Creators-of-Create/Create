package com.simibubi.create.compat.computercraft;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

public class FallbackComputerBehaviour extends AbstractComputerBehaviour {

	public FallbackComputerBehaviour(SmartTileEntity te) {
		super(te);
	}

	@Override
	public boolean hasAttachedComputer() {
		return false;
	}
	
}
