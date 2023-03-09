package com.simibubi.create.content;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription.Palette;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public enum AllSections {

	/** Create's kinetic mechanisms */
	KINETICS(Palette.Red),

	/** Item transport and other Utility */
	LOGISTICS(Palette.Yellow),

	/** Tools for structure movement and replication */
	SCHEMATICS(Palette.Blue),

	/** Decorative blocks */
	PALETTES(Palette.Green),

	/** Helpful gadgets and other shenanigans */
	CURIOSITIES(Palette.Purple),

	/** Base materials, ingredients and tools */
	MATERIALS(Palette.Green),

	/** Fallback section */
	UNASSIGNED(Palette.Gray)

	;

	private final Palette tooltipPalette;

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
		return Create.REGISTRATE
			.getSection(item);
	}

	static AllSections ofBlock(Block block) {
		return Create.REGISTRATE
			.getSection(block);
	}

}
