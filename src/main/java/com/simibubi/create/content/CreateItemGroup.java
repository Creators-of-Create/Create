package com.simibubi.create.content;

import java.util.EnumSet;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.item.CreateItemGroupBase;

import net.minecraft.world.item.ItemStack;

public class CreateItemGroup extends CreateItemGroupBase {

	public CreateItemGroup() {
		super("base");
	}

	@Override
	protected EnumSet<AllSections> getSections() {
		return EnumSet.complementOf(EnumSet.of(AllSections.PALETTES));
	}

	@Override
	public ItemStack makeIcon() {
		return AllBlocks.COGWHEEL.asStack();
	}

}
