package com.simibubi.create.content.contraptions.components.structureMovement.train;

import com.simibubi.create.AllItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MinecartCouplingItem extends Item {

	public MinecartCouplingItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@SubscribeEvent
	public static void couplingItemCanBeUsedOnMinecarts(PlayerInteractEvent.EntityInteract event) {
		Entity interacted = event.getTarget();
		if (!(interacted instanceof AbstractMinecartEntity))
			return;
		AbstractMinecartEntity minecart = (AbstractMinecartEntity) interacted;
		PlayerEntity player = event.getPlayer();
		if (player == null)
			return;
		ItemStack heldItem = player.getHeldItem(event.getHand());
		if (!AllItems.MINECART_COUPLING.isIn(heldItem))
			return;
		
		World world = event.getWorld();
		if (MinecartCouplingSerializer.getCouplingData(minecart).size() < 2) {
			if (world != null && world.isRemote)
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> cartClicked(player, minecart));
		}
		
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
	}

	@OnlyIn(Dist.CLIENT)
	private static void cartClicked(PlayerEntity player, AbstractMinecartEntity interacted) {
		ClientMinecartCouplingHandler.onCartClicked(player, (AbstractMinecartEntity) interacted);
	}

}
