package com.simibubi.create.compat.computercraft;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

public class FallbackComputerBehaviour extends AbstractComputerBehaviour {

	public FallbackComputerBehaviour(SmartBlockEntity te) {
		super(te);
	}

	@Override
	public boolean hasAttachedComputer() {
		return false;
	}
	
}
