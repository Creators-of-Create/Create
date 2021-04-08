package com.simibubi.create.content.curiosities.tools;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(bus = Bus.FORGE)
public class DeforesterItem extends AxeItem {
	private static boolean deforesting = false; // required as to not run into "recursions" over forge events on tree cutting

	public DeforesterItem(Properties builder) {
		super(AllToolTiers.RADIANT, 5.0F, -3.1F, builder);
	}

	// Moved away from Item#onBlockDestroyed as it does not get called in Creative
	public static void destroyTree(IWorld iWorld, BlockState state, BlockPos pos,
								   PlayerEntity player) {

		if (deforesting ||!(state.isIn(BlockTags.LOGS) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state)) || player.isSneaking() || !(iWorld instanceof  World))
			return;
		World worldIn = (World) iWorld;
		Vector3d vec = player.getLookVec();

		deforesting = true;
		TreeCutter.findTree(worldIn, pos).destroyBlocks(worldIn, player, (dropPos, item) -> dropItemFromCutTree(worldIn, pos, vec, dropPos, item));
		deforesting = false;
	}

	@SubscribeEvent
	public static void onBlockDestroyed(BlockEvent.BreakEvent event) {
		ItemStack heldItemMainhand = event.getPlayer().getHeldItemMainhand();
		if (!AllItems.DEFORESTER.isIn(heldItemMainhand))
			return;
		destroyTree(event.getWorld(), event.getState(), event.getPos(), event.getPlayer());
	}

	public static void dropItemFromCutTree(World world, BlockPos breakingPos, Vector3d fallDirection, BlockPos pos,
			ItemStack stack) {
		float distance = (float) Math.sqrt(pos.distanceSq(breakingPos));
		Vector3d dropPos = VecHelper.getCenterOf(pos);
		ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
		entity.setMotion(fallDirection.scale(distance / 20f));
		world.addEntity(entity);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
		if (!state.isIn(BlockTags.LEAVES))
			super.onBlockDestroyed(stack, world, state, pos, entity);
		return true;
	}
}
