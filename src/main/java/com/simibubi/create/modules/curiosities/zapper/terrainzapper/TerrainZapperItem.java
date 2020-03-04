package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import com.simibubi.create.modules.curiosities.zapper.ZapperItem;

import net.minecraft.item.ItemStack;

public class TerrainZapperItem extends ZapperItem {

	public TerrainZapperItem(Properties properties) {
		super(properties);
	}

	@Override
	protected void openHandgunGUI(ItemStack item, boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getCooldownDelay(ItemStack item) {
		return 2;
	}

}
