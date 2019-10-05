package com.simibubi.create;

import com.simibubi.create.foundation.block.IWithoutBlockItem;

import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public final class CreateItemGroup extends ItemGroup {

	public CreateItemGroup() {
		super(getGroupCountSafe(), Create.ID);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllBlocks.COGWHEEL.get());
	}
	
	@Override
	public void fill(NonNullList<ItemStack> items) {
		for (AllItems item : AllItems.values()) {
			if (item.get() == null)
				continue;
			if (!item.module.isEnabled())
				continue;
			
			item.get().fillItemGroup(this, items);
		}
		
		for (AllBlocks block : AllBlocks.values()) {
			if (block.get() == null)
				continue;
			if (!block.module.isEnabled())
				continue;
			if (block.get() instanceof IWithoutBlockItem)
				continue;
			
			block.get().asItem().fillItemGroup(this, items);
			for (Block alsoRegistered : block.alsoRegistered)
				alsoRegistered.asItem().fillItemGroup(this, items);
		}
	}
}
