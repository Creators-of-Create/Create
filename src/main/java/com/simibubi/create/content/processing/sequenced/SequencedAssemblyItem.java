package com.simibubi.create.content.processing.sequenced;

import com.simibubi.create.AllDataComponents;
import net.createmod.catnip.theme.Color;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SequencedAssemblyItem extends Item {

	public SequencedAssemblyItem(Properties p_i48487_1_) {
		super(p_i48487_1_.stacksTo(1));
	}

	public float getProgress(ItemStack stack) {
		if (!stack.has(AllDataComponents.SEQUENCED_ASSEMBLY))
			return 0;
		//noinspection DataFlowIssue
		return stack.get(AllDataComponents.SEQUENCED_ASSEMBLY).progress();
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(getProgress(stack) * 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return Color.mixColors(0xFF_FFC074, 0xFF_46FFE0, getProgress(stack));
	}

}
