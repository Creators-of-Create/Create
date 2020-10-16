package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class DeforesterItem extends AxeItem {

	public DeforesterItem(Properties builder) {
		super(AllToolTiers.RADIANT, 5.0F, -3.1F, builder);
	}

	// Moved away from Item#onBlockDestroyed as it does not get called in Creative
	public static void destroyTree(ItemStack stack, IWorld iWorld, BlockState state, BlockPos pos,
			PlayerEntity player) {
		if (!(state.isIn(BlockTags.LOGS) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state)) || player.isSneaking() || !(iWorld instanceof  World))
			return;
		World worldIn = (World) iWorld;
		Tree tree = TreeCutter.cutTree(worldIn, pos);
		if (tree == null)
			return;
		boolean dropBlock = !player.isCreative();

		Vector3d vec = player.getLookVec();
		for (BlockPos log : tree.logs)
			BlockHelper.destroyBlock(worldIn, log, 1 / 2f, item -> {
				if (dropBlock) {
					dropItemFromCutTree(worldIn, pos, vec, log, item);
					stack.damageItem(1, player, p -> p.sendBreakAnimation(Hand.MAIN_HAND));
				}
			});
		for (BlockPos leaf : tree.leaves)
			BlockHelper.destroyBlock(worldIn, leaf, 1 / 8f, item -> {
				if (dropBlock)
					dropItemFromCutTree(worldIn, pos, vec, leaf, item);
			});
	}

	@SubscribeEvent
	public static void onBlockDestroyed(BlockEvent.BreakEvent event) {
		ItemStack heldItemMainhand = event.getPlayer().getHeldItemMainhand();
		if (!AllItems.DEFORESTER.isIn(heldItemMainhand))
			return;
		destroyTree(heldItemMainhand, event.getWorld(), event.getState(), event.getPos(), event.getPlayer());
	}

	public static void dropItemFromCutTree(World world, BlockPos breakingPos, Vector3d fallDirection, BlockPos pos,
			ItemStack stack) {
		float distance = (float) Math.sqrt(pos.distanceSq(breakingPos));
		Vector3d dropPos = VecHelper.getCenterOf(pos);
		ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
		entity.setMotion(fallDirection.scale(distance / 20f));
		world.addEntity(entity);
	}

}
