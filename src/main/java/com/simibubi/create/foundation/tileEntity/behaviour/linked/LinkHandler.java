package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import java.util.Arrays;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LinkHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		
		if (player.isShiftKeyDown() || player.isSpectator())
			return;

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		ItemStack heldItem = player.getItemInHand(hand);
		BlockRayTraceResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (AllItems.LINKED_CONTROLLER.isIn(heldItem))
			return;
		if (AllItems.WRENCH.isIn(heldItem))
			return;

		for (boolean first : Arrays.asList(false, true)) {
			if (behaviour.testHit(first, ray.getLocation())) {
				if (event.getSide() != LogicalSide.CLIENT) 
					behaviour.setFrequency(first, heldItem);
				event.setCanceled(true);
				event.setCancellationResult(ActionResultType.SUCCESS);
				world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
			}
		}
	}

}
