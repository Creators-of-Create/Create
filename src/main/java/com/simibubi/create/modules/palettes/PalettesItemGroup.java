package com.simibubi.create.modules.palettes;

import java.util.Collection;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.CreateItemGroupBase;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PalettesItemGroup extends CreateItemGroupBase {

	public PalettesItemGroup() {
		super("palettes");
	}

	@Override
	protected Collection<RegistryEntry<Block>> getBlocks() {
		return Create.palettesRegistrate().getAll(Block.class);
	}
	
	@Override
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllBlocks.IRON_GLASS.get());
	}

}
