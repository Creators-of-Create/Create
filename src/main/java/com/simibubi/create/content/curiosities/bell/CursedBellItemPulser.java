package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CursedBellItemPulser {

	public static final int DISTANCE = 2;
	public static final int RECHARGE_TICKS = 8;

	@SubscribeEvent
	public static void bellItemCreatesPulses(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		if (event.side != LogicalSide.SERVER)
			return;

		if (event.player.world.getGameTime() % RECHARGE_TICKS != 0)
			return;

		for (Hand hand : Hand.values()) {
			Item held = event.player.getHeldItem(hand).getItem();
			if (!(held instanceof BlockItem))
				continue;
			Block block = ((BlockItem) held).getBlock();
			if (!block.is(AllBlocks.CURSED_BELL.get()))
				continue;

			SoulPulseEffectHandler.sendPulsePacket(event.player.world, event.player.getBlockPos(), DISTANCE, false);
		}
	}

}
