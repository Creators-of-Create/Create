package com.simibubi.create;

import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class CreateItemGroup extends CreateItemGroupBase {

	public CreateItemGroup() {
		super("base");
	}

	@Override
	protected boolean shouldAdd(RegistryEntry<? extends Block> block) {
		Sections section = Create.registrate()
				.getSection(block);
		return section != Sections.PALETTES;
	}

	@Override
	protected boolean shouldAdd(AllItems item) {
		return item.section != Sections.PALETTES;
	}

	@Override
	public ItemStack createIcon() {
		return AllBlocksNew.COGWHEEL.asStack();
	}

}
