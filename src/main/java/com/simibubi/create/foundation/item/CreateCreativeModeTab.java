package com.simibubi.create.foundation.item;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class CreateCreativeModeTab extends CreativeModeTab {
	public CreateCreativeModeTab(String id) {
		super(Create.ID + "." + id);
	}

	@Override
	public void fillItemList(NonNullList<ItemStack> items) {
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	public void addBlocks(NonNullList<ItemStack> items) {
		for (Item item : ForgeRegistries.ITEMS) {
			if (item instanceof BlockItem) {
				item.fillItemCategory(this, items);
			}
		}
	}

	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		for (Item item : ForgeRegistries.ITEMS) {
			if (!(item instanceof BlockItem)) {
				ItemStack stack = new ItemStack(item);
				BakedModel model = itemRenderer.getModel(stack, null, null, 0);
				if (model.isGui3d() == specialItems) {
					item.fillItemCategory(this, items);
				}
			}
		}
	}
}
