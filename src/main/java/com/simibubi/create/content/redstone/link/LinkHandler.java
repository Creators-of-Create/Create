package com.simibubi.create.content.redstone.link;

import java.util.Arrays;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LinkHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		Level world = event.getWorld();
		BlockPos pos = event.getPos();
		Player player = event.getPlayer();
		InteractionHand hand = event.getHand();

		if (player.isShiftKeyDown() || player.isSpectator())
			return;

		LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		ItemStack heldItem = player.getItemInHand(hand);
		BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (AllItems.LINKED_CONTROLLER.isIn(heldItem))
			return;
		if (AllItems.WRENCH.isIn(heldItem))
			return;

		boolean fakePlayer = player instanceof FakePlayer;
		boolean fakePlayerChoice = false;

		if (fakePlayer) {
			BlockState blockState = world.getBlockState(pos);
			Vec3 localHit = ray.getLocation()
				.subtract(Vec3.atLowerCornerOf(pos))
				.add(Vec3.atLowerCornerOf(ray.getDirection()
					.getNormal())
					.scale(.25f));
			fakePlayerChoice = localHit.distanceToSqr(behaviour.firstSlot.getLocalOffset(blockState)) > localHit
				.distanceToSqr(behaviour.secondSlot.getLocalOffset(blockState));
		}

		for (boolean first : Arrays.asList(false, true)) {
			if (behaviour.testHit(first, ray.getLocation()) || fakePlayer && fakePlayerChoice == first) {
				if (event.getSide() != LogicalSide.CLIENT)
					behaviour.setFrequency(first, heldItem);
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.SUCCESS);
				world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
			}
		}
	}

}
