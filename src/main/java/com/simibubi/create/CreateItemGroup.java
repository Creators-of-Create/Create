package com.simibubi.create;

import com.simibubi.create.foundation.item.IAddedByOther;
import com.tterrag.registrate.util.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class CreateItemGroup extends ItemGroup {

	public CreateItemGroup() {
		super(getGroupCountSafe(), Create.ID);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllBlocks.COGWHEEL.get());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fill(NonNullList<ItemStack> items) {
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	@OnlyIn(Dist.CLIENT)
	public void addBlocks(NonNullList<ItemStack> items) {
		for (AllBlocks block : AllBlocks.values()) {
			Block def = block.get();
			if (def == null)
				continue;
			if (!block.module.isEnabled())
				continue;
			if (def instanceof IAddedByOther)
				continue;

			Item item = def.asItem();
			if (item != Items.AIR) {
    			item.fillItemGroup(this, items);
    			for (RegistryEntry<? extends Block> alsoRegistered : block.alsoRegistered)
    				alsoRegistered.get().asItem().fillItemGroup(this, items);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		for (AllItems item : AllItems.values()) {
			if (item.get() == null)
				continue;
			if (!item.module.isEnabled())
				continue;
			IBakedModel model = itemRenderer.getItemModelWithOverrides(item.asStack(), Minecraft.getInstance().world, null);
			if (model.isGui3d() != specialItems)
				continue;
			if (item.get() instanceof IAddedByOther)
				continue;

			item.get().fillItemGroup(this, items);
		}
	}
}
