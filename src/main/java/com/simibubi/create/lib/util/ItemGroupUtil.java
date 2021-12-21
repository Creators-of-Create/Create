package com.simibubi.create.lib.util;

import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.world.item.CreativeModeTab;

public class ItemGroupUtil {
	public static synchronized int getGroupCountSafe() {
		((ItemGroupExtensions) CreativeModeTab.TAB_BUILDING_BLOCKS).fabric_expandArray();
		return CreativeModeTab.TABS.length - 1;
	}
}
