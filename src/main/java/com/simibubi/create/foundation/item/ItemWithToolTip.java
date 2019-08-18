package com.simibubi.create.foundation.item;

import java.util.List;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ItemWithToolTip extends Item {
	
	protected TooltipCache tooltip;

	public ItemWithToolTip(Properties properties) {
		super(properties);
		tooltip = new TooltipCache();
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ItemDescription itemDescription = this.tooltip.getOrCreate(this::getDescription);
		itemDescription.addInformation(tooltip);
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	protected abstract ItemDescription getDescription();
	
	protected String h(String s, Palette palette) {
		return ItemDescription.hightlight(s, palette);
	}
	
}
