package com.simibubi.create.foundation.utility;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.event.EventHooks;

public abstract class AbstractBlockBreakQueue {
	protected Consumer<BlockPos> makeCallbackFor(Level world, float effectChance, ItemStack toDamage,
		@Nullable Player playerEntity, BiConsumer<BlockPos, ItemStack> drop) {
		return pos -> {
			ItemStack usedTool = toDamage.copy();
			BlockHelper.destroyBlockAs(world, pos, playerEntity, toDamage, effectChance,
				stack -> drop.accept(pos, stack));
			if (toDamage.isEmpty() && !usedTool.isEmpty())
				EventHooks.onPlayerDestroyItem(playerEntity, usedTool, InteractionHand.MAIN_HAND);
		};
	}

	public void destroyBlocks(Level world, @Nullable LivingEntity entity, BiConsumer<BlockPos, ItemStack> drop) {
		Player playerEntity = entity instanceof Player ? ((Player) entity) : null;
		ItemStack toDamage =
			playerEntity != null && !playerEntity.isCreative() ? playerEntity.getMainHandItem() : ItemStack.EMPTY;
		destroyBlocks(world, toDamage, playerEntity, drop);
	}

	public abstract void destroyBlocks(Level world, ItemStack toDamage, @Nullable Player playerEntity,
		BiConsumer<BlockPos, ItemStack> drop);
}
