package com.simibubi.create;

import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.item.IAddedByOther;

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
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	public void addBlocks(NonNullList<ItemStack> items) {
		for (AllBlocks block : AllBlocks.values()) {
			Block def = block.get();
			if (def == null)
				continue;
			if (!block.module.isEnabled())
				continue;
			if (def instanceof IHaveNoBlockItem && !((IHaveNoBlockItem) def).hasBlockItem())
				continue;
			if (def instanceof IAddedByOther)
				continue;

			def.asItem().fillItemGroup(this, items);
			for (Block alsoRegistered : block.alsoRegistered)
				alsoRegistered.asItem().fillItemGroup(this, items);
		}
	}

	public void addItems(NonNullList<ItemStack> items, boolean prioritized) {
		for (AllItems item : AllItems.values()) {
			if (item.get() == null)
				continue;
			if (!item.module.isEnabled())
				continue;
			if (item.firstInCreativeTab != prioritized)
				continue;
			if (item.get() instanceof IAddedByOther)
				continue;

			item.get().fillItemGroup(this, items);
		}
	}
}
