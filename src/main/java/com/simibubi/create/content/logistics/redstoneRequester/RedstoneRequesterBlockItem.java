package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class RedstoneRequesterBlockItem extends LogisticallyLinkedBlockItem {

	public RedstoneRequesterBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay,
		Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		if (!isTuned(stack))
			return;

		if (!stack.has(AllDataComponents.AUTO_REQUEST_DATA)) {
			super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltipComponents, tooltipFlag);
			return;
		}

		tooltipComponents.accept(CreateLang.translate("logistically_linked.tooltip")
			.style(ChatFormatting.GOLD)
			.component());
		List<Component> requesterTooltip = new java.util.ArrayList<>();
		RedstoneRequesterBlock.appendRequesterTooltip(stack, requesterTooltip);
		requesterTooltip.forEach(tooltipComponents);
	}

}
