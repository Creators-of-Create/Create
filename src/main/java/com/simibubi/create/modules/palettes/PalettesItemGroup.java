package com.simibubi.create.modules.palettes;

import java.util.EnumSet;

import com.simibubi.create.CreateItemGroupBase;
import com.simibubi.create.modules.Sections;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PalettesItemGroup extends CreateItemGroupBase {

	public PalettesItemGroup() {
		super("palettes");
	}

	@Override
	protected EnumSet<Sections> getSections() {
		return EnumSet.of(Sections.PALETTES);
	}

	@Override
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllPaletteBlocks.ORNATE_IRON_WINDOW.get());
	}

}
