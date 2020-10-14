package com.simibubi.create.content.contraptions.components.crank;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockPartials;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
public class ValveHandleBlock extends HandCrankBlock {
	private final boolean inCreativeTab;

	public static ValveHandleBlock copper(Properties properties) {
		return new ValveHandleBlock(properties, true);
	}
	
	public static ValveHandleBlock dyed(Properties properties) {
		return new ValveHandleBlock(properties, false);
	}
	
	private ValveHandleBlock(Properties properties, boolean inCreativeTab) {
		super(properties);
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.SEARCH && !inCreativeTab)
			return;
		super.fillItemGroup(group, p_149666_2_);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public AllBlockPartials getRenderedHandle() {
		return null;
	}
	
	@Override
	public int getRotationSpeed() {
		return 16;
	}

}
