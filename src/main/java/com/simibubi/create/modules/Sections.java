package com.simibubi.create.modules;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum Sections {

	/** Create's kinetic mechanisms */
	KINETICS(Palette.Red),

	/** Item transport and other Utility */
	LOGISTICS(Palette.Yellow),

	/** Helpful gadgets and other shenanigans */
	CURIOSITIES(Palette.Purple),

	/** Tools for strucuture movement and replication */
	SCHEMATICS(Palette.Blue),

	/** Decorative blocks */
	PALETTES(Palette.Green),

	/** Base materials, ingredients and tools */
	MATERIALS(Palette.Green),

	/** Fallback section */
	UNASSIGNED(Palette.Gray)

	;

	private Palette tooltipPalette;

	private Sections(Palette tooltipPalette) {
		this.tooltipPalette = tooltipPalette;
	}

	public Palette getTooltipPalette() {
		return tooltipPalette;
	}

	public static Sections of(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof BlockItem)
			return ofBlock(((BlockItem) item).getBlock());
		return ofItem(item);
	}

	static Sections ofItem(Item item) {
		for (AllItems allItems : AllItems.values())
			if (allItems.get() == item)
				return allItems.section;
		return UNASSIGNED;
	}

	static Sections ofBlock(Block block) {
		for (AllBlocks allBlocks : AllBlocks.values())
			if (allBlocks.get() == block)
				return allBlocks.section;
		return Create.registrate().getSection(block);
	}

}
