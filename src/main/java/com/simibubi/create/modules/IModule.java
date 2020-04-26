package com.simibubi.create.modules;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.config.CServer;
import com.simibubi.create.foundation.item.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IModule {

	public static boolean isActive(String module) {
		if (module.equals("materials"))
			return true;

		CServer conf = AllConfigs.SERVER;
		switch (module) {
		case "contraptions":
			return true;
		case "palettes":
			return conf.enablePalettes.get();
		case "curiosities":
			return conf.enableCuriosities.get();
		case "logistics":
			return conf.enableLogistics.get();
		case "schematics":
			return conf.enableSchematics.get();
		default:
			return false;
		}
	}
	
	public default Palette getToolTipColor() {
		String module = getModuleName();
		
		if (module.equals("materials"))
			return Palette.Purple;
		
		switch (module) {
		case "contraptions":
			return Palette.Red;
		case "palettes":
			return Palette.Green;
		case "curiosities":
			return Palette.Purple;
		case "logistics":
			return Palette.Yellow;
		case "schematics":
			return Palette.Blue;
		default:
			return Palette.Purple;
		}
	}
	
	public static IModule of(String name) {
		return () -> name;
	}
	
	public static IModule of(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof BlockItem)
			return ofBlock(((BlockItem) item).getBlock());
		return ofItem(item);
	}
	
	static IModule ofItem(Item item) {
		for (AllItems allItems : AllItems.values()) {
			if (allItems.get() == item)
				return allItems.module;
		}
		return null;
	}
	
	static IModule ofBlock(Block block) {
		for (AllBlocks allBlocks : AllBlocks.values()) {
			if (allBlocks.get() == block)
				return allBlocks.module;
		}
		return Create.registrate().getModule(block);
	}

	public default boolean isEnabled() {
		return isActive(getModuleName());
	}

	public String getModuleName();

}
