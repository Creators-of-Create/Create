package com.simibubi.create.content.contraptions.wrench;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class WrenchItem extends Item {

	public WrenchItem(Properties properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		if (player == null || !player.isAllowEdit())
			return super.onItemUse(context);

		BlockState state = context.getWorld()
			.getBlockState(context.getPos());
		if (!(state.getBlock() instanceof IWrenchable))
			return super.onItemUse(context);
		IWrenchable actor = (IWrenchable) state.getBlock();

		if (player.isSneaking())
			return actor.onSneakWrenched(state, context);
		return actor.onWrenched(state, context);
	}
}
