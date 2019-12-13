package com.simibubi.create.modules.curiosities.deforester;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.modules.curiosities.tools.AllToolTiers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class DeforesterItem extends AxeItem {

	public DeforesterItem(Properties builder) {
		super(AllToolTiers.RADIANT, 10.0F, -3.1F, builder);
	}

	// Moved away from Item#onBlockDestroyed as it does not get called in Creative
	public static void destroyTree(ItemStack stack, IWorld worldIn, BlockState state, BlockPos pos,
			PlayerEntity player) {
		if (!state.isIn(BlockTags.LOGS) || player.isSneaking())
			return;
		Tree tree = TreeCutter.cutTree(worldIn, pos);
		if (tree == null)
			return;
		boolean dropBlock = !player.isCreative();
		World world = worldIn.getWorld();
		if (world == null)
			return;

		for (BlockPos log : tree.logs)
			BlockHelper.destroyBlock(world, log, 1 / 2f, item -> {
				if (dropBlock)
					Block.spawnAsEntity(world, log, item);
			});
		for (BlockPos leaf : tree.leaves)
			BlockHelper.destroyBlock(world, leaf, 1 / 8f, item -> {
				if (dropBlock)
					Block.spawnAsEntity(world, leaf, item);
			});
	}

	@SubscribeEvent
	public static void onBlockDestroyed(BlockEvent.BreakEvent event) {
		ItemStack heldItemMainhand = event.getPlayer().getHeldItemMainhand();
		if (!AllItems.DEFORESTER.typeOf(heldItemMainhand))
			return;
		destroyTree(heldItemMainhand, event.getWorld(), event.getState(), event.getPos(), event.getPlayer());
	}

}
