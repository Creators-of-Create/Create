package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.network.chat.MutableComponent;

public class CurrentFloorDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof ElevatorContactBlockEntity ecte))
			return EMPTY_LINE;
		return Components.literal(ecte.lastReportedCurrentFloor);
	}
	
	@Override
	protected String getTranslationKey() {
		return "current_floor";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return false;
	}

}
