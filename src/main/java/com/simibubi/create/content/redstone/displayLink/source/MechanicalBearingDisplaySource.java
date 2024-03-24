package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MechanicalBearingDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof MechanicalBearingBlockEntity epbe))
			return null;
		return new TextComponent(String.valueOf(epbe.getAngle()));
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
