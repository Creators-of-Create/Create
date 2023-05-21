package com.simibubi.create.content.decoration.encasing;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CasingBlock extends Block implements IWrenchable {

	private boolean visible;

	public static CasingBlock deprecated(Properties p_i48440_1_) {
		return new CasingBlock(p_i48440_1_, false);
	}
	
	public CasingBlock(Properties p_i48440_1_) {
		this(p_i48440_1_, true);
	}

	public CasingBlock(Properties p_i48440_1_, boolean visible) {
		super(p_i48440_1_);
		this.visible = visible;
	}

	@Override
	public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
		if (visible)
			super.fillItemCategory(pCategory, pItems);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.FAIL;
	}

}
