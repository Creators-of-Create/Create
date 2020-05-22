package com.simibubi.create;

import java.util.EnumSet;

import com.simibubi.create.modules.Sections;

import net.minecraft.item.ItemStack;

public class CreateItemGroup extends CreateItemGroupBase {

	public CreateItemGroup() {
		super("base");
	}

	@Override
	protected EnumSet<Sections> getSections() {
		return EnumSet.complementOf(EnumSet.of(Sections.PALETTES));
	}

	@Override
	public ItemStack createIcon() {
		return AllBlocks.COGWHEEL.asStack();
	}

}
