package com.simibubi.create.content.logistics.tableCloth;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class TableClothBlockItem extends BlockItem {

	public TableClothBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		return pStack.has(AllDataComponents.AUTO_REQUEST_DATA);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay,
		Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltipComponents, tooltipFlag);
		if (!isFoil(stack))
			return;

		tooltipComponents.accept(CreateLang.translate("table_cloth.shop_configured")
			.style(ChatFormatting.GOLD)
			.component());

		List<Component> requesterTooltip = new java.util.ArrayList<>();
		RedstoneRequesterBlock.appendRequesterTooltip(stack, requesterTooltip);
		requesterTooltip.forEach(tooltipComponents);
	}

}
