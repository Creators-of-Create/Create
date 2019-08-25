package com.simibubi.create.foundation.block;

import java.util.List;

import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.TooltipHolder;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class InfoBlock extends Block implements ITooltip {

	protected TooltipHolder info;
	
	public InfoBlock(Properties properties) {
		super(properties);
		info = new TooltipHolder(this);
	}
	
	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}
	
}
