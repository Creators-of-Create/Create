package com.simibubi.create;

import com.simibubi.create.item.ItemBlueprint;
import com.simibubi.create.item.ItemBlueprintAndQuill;
import com.simibubi.create.item.ItemWandSymmetry;
import com.simibubi.create.supertree.ItemTreeFertilizer;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllItems {
	
	TREE_FERTILIZER(new ItemTreeFertilizer(standardProperties())),
	SYMMETRY_WAND(new ItemWandSymmetry(standardProperties())),
	EMPTY_BLUEPRINT(new Item(standardProperties().maxStackSize(1))),
	BLUEPRINT_AND_QUILL(new ItemBlueprintAndQuill(standardProperties().maxStackSize(1))),
	BLUEPRINT(new ItemBlueprint(standardProperties()));

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

	@OnlyIn(Dist.CLIENT)
	public static void initColorHandlers() {
	}
	
}
