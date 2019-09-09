package com.simibubi.create.modules.curiosities.tools;

import java.util.List;

import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;

public class ShadowSteelToolItem extends AbstractToolItem {

	public ShadowSteelToolItem(float attackDamageIn, float attackSpeedIn, Properties builder,
			AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.SHADOW_STEEL, builder, types);
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Purple;
		return new ItemDescription(color)
				.withSummary("The fast moving shadow material allows for quick riddance of blocks and entities.")
				.withBehaviour("When [Forge#5828] Fixed",
						TextFormatting.STRIKETHROUGH + "Broken blocks do not drop any items.")
				.createTabs();
	}

	@Override
	public void modifyDrops(List<ItemStack> drops, IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
		drops.clear();
	}

}
