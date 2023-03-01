package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.List;

import net.minecraft.network.chat.Component;

public record ValueSettingsBoard(Component title, int maxValue, int milestoneInterval, List<Component> rows,
	ValueSettingsFormatter formatter) {
}
