package com.simibubi.create.content.palettes;

import java.util.EnumSet;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.item.CreateItemGroupBase;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class PalettesItemGroup extends CreateItemGroupBase {

	public PalettesItemGroup() {
		super("palettes");
	}

	@Override
	protected EnumSet<AllSections> getSections() {
		return EnumSet.of(AllSections.PALETTES);
	}

	@Override
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {}

	@Override
	public ItemStack makeIcon() {
		return new ItemStack(AllPaletteBlocks.ORNATE_IRON_WINDOW.get());
	}

}
