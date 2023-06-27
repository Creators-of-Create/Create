package com.simibubi.create.content.redstone.displayLink.source;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;

public class ComputerDisplaySource extends DisplaySource {

	@Override
	public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		List<MutableComponent> components = new ArrayList<>();
		ListTag tag = context.sourceConfig().getList("ComputerSourceList", Tag.TAG_STRING);

		for (int i = 0; i < tag.size(); i++) {
			components.add(Components.literal(tag.getString(i)));
		}

		return components;
	}

	@Override
	public boolean shouldPassiveReset() {
		return false;
	}

}
