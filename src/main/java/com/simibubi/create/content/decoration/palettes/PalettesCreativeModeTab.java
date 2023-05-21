package com.simibubi.create.content.decoration.palettes;

import com.simibubi.create.infrastructure.item.CreateCreativeModeTab;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class PalettesCreativeModeTab extends CreateCreativeModeTab {
	public PalettesCreativeModeTab() {
		super("palettes");
	}

	@Override
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
	}

	@Override
	public ItemStack makeIcon() {
		return AllPaletteBlocks.ORNATE_IRON_WINDOW.asStack();
	}
}
