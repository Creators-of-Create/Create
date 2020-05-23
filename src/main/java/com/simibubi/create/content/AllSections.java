package com.simibubi.create.content;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum AllSections {

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

	private AllSections(Palette tooltipPalette) {
		this.tooltipPalette = tooltipPalette;
	}

	public Palette getTooltipPalette() {
		return tooltipPalette;
	}

	public static AllSections of(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof BlockItem)
			return ofBlock(((BlockItem) item).getBlock());
		return ofItem(item);
	}

	static AllSections ofItem(Item item) {
		for (AllItems allItems : AllItems.values())
			if (allItems.get() == item)
				return allItems.section;
		return UNASSIGNED;
	}

	static AllSections ofBlock(Block block) {
		return Create.registrate().getSection(block);
	}

}
