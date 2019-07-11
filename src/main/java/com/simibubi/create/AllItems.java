package com.simibubi.create;

import com.simibubi.create.item.ItemWandSymmetry;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllItems {
	
	SYMMETRY_WAND(new ItemWandSymmetry(standardProperties())),
	EMPTY_BLUEPRINT(new Item(standardProperties().maxStackSize(1))),
	BLUEPRINT(new Item(standardProperties().maxStackSize(1)));

	public Item item;

	private AllItems(Item item) {
		this.item = item;
		this.item.setRegistryName(Create.ID, this.name().toLowerCase());
	}
	
	public static Properties standardProperties() {
		return new Properties().group(Create.creativeTab);
	}
	
	public static void registerItems(IForgeRegistry<Item> iForgeRegistry) {
		for (AllItems item : values()) {
			iForgeRegistry.register(item.get());
		}
	}
	
	public Item get() {
		return item;
	}
	
	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	public static void initColorHandlers() {
	}
	
}
