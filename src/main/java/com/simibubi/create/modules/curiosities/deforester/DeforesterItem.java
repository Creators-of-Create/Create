package com.simibubi.create.modules.curiosities.deforester;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.curiosities.tools.AllToolTiers;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class DeforesterItem extends AxeItem implements IHaveCustomItemModel {

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

		Vec3d vec = player.getLookVec();
		for (BlockPos log : tree.logs)
			BlockHelper.destroyBlock(world, log, 1 / 2f, item -> {
				if (dropBlock) {
					dropItemFromCutTree(world, pos, vec, log, item);
					stack.damageItem(1, player, p -> p.sendBreakAnimation(Hand.MAIN_HAND));
				}
			});
		for (BlockPos leaf : tree.leaves)
			BlockHelper.destroyBlock(world, leaf, 1 / 8f, item -> {
				if (dropBlock)
					dropItemFromCutTree(world, pos, vec, leaf, item);
			});
	}

	@SubscribeEvent
	public static void onBlockDestroyed(BlockEvent.BreakEvent event) {
		ItemStack heldItemMainhand = event.getPlayer().getHeldItemMainhand();
		if (!AllItems.DEFORESTER.typeOf(heldItemMainhand))
			return;
		destroyTree(heldItemMainhand, event.getWorld(), event.getState(), event.getPos(), event.getPlayer());
	}

	public static void dropItemFromCutTree(World world, BlockPos breakingPos, Vec3d fallDirection, BlockPos pos,
			ItemStack stack) {
		float distance = (float) Math.sqrt(pos.distanceSq(breakingPos));
		Vec3d dropPos = VecHelper.getCenterOf(pos);
		ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
		entity.setMotion(fallDirection.scale(distance / 20f));
		world.addEntity(entity);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CustomRenderedItemModel createModel(IBakedModel original) {
		return new DeforesterModel(original);
	}

}
