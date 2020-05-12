package com.simibubi.create.modules.palettes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateItemGroupBase;
import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class PalettesItemGroup extends CreateItemGroupBase {

	public PalettesItemGroup() {
		super("palettes");
	}

	@Override
	protected boolean shouldAdd(RegistryEntry<? extends Block> block) {
		Sections section = Create.registrate()
				.getSection(block);
		return section == Sections.PALETTES;
	}

	@Override
	protected boolean shouldAdd(AllItems item) {
		return item.section == Sections.PALETTES;
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllBlocks.IRON_GLASS.get());
	}

	
}
