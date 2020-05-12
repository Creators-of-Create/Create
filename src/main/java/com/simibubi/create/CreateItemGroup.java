package com.simibubi.create;

import java.util.Collection;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class CreateItemGroup extends CreateItemGroupBase {

	public CreateItemGroup() {
		super("base");
	}

	@Override
	protected Collection<RegistryEntry<Block>> getBlocks() {
		return Create.registrate().getAll(Block.class);
	}

	@Override
	public ItemStack createIcon() {
		return AllBlocksNew.COGWHEEL.asStack();
	}

}
