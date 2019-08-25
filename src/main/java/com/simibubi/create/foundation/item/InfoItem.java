package com.simibubi.create.foundation.item;

import java.util.List;

import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.TooltipHolder;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class InfoItem extends Item implements ITooltip {

	protected TooltipHolder info;

	public InfoItem(Properties properties) {
		super(properties);
		info = new TooltipHolder(this);
	}
	
	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

}
