package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

public class EnchantPowerDisplaySource extends NumericSingleLineDisplaySource {

	protected static final RandomSource random = RandomSource.create();
	protected static final ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof EnchantmentTableBlockEntity))
			return ZERO.copy();

		BlockPos pos = context.getSourcePos();
		Level level = context.level();
		float enchantPower = 0;

		for(BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
			if (!EnchantmentTableBlock.isValidBookShelf(level, pos, offset))
				continue;

			enchantPower += level.getBlockState(pos.offset(offset)).getEnchantPowerBonus(level, pos.offset(offset));
		}


		int cost = EnchantmentHelper.getEnchantmentCost(random, 2, (int) enchantPower, stack);

		return Components.literal(String.valueOf(cost));
	}

	@Override
	protected String getTranslationKey() {
		return "max_enchant_level";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
