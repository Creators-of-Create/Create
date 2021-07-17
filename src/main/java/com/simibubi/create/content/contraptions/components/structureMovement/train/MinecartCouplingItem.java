package com.simibubi.create.content.contraptions.components.structureMovement.train;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MinecartCouplingItem extends Item {

	public MinecartCouplingItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void handleInteractionWithMinecart(PlayerInteractEvent.EntityInteract event) {
		Entity interacted = event.getTarget();
		if (!(interacted instanceof AbstractMinecartEntity))
			return;
		AbstractMinecartEntity minecart = (AbstractMinecartEntity) interacted;
		PlayerEntity player = event.getPlayer();
		if (player == null)
			return;
		LazyOptional<MinecartController> capability =
			minecart.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (!capability.isPresent())
			return;
		MinecartController controller = capability.orElse(null);

		ItemStack heldItem = player.getItemInHand(event.getHand());
		if (AllItems.MINECART_COUPLING.isIn(heldItem)) {
			if (!onCouplingInteractOnMinecart(event, minecart, player, controller))
				return;
		} else if (AllItems.WRENCH.isIn(heldItem)) {
			if (!onWrenchInteractOnMinecart(event, minecart, player, controller))
				return;
		} else
			return;

		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
	}

	protected static boolean onCouplingInteractOnMinecart(PlayerInteractEvent.EntityInteract event,
		AbstractMinecartEntity minecart, PlayerEntity player, MinecartController controller) {
		World world = event.getWorld();
		if (controller.isFullyCoupled()) {
			if (!world.isClientSide)
				CouplingHandler.status(player, "two_couplings_max");
			return true;
		}
		if (world != null && world.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> cartClicked(player, minecart));
		return true;
	}

	private static boolean onWrenchInteractOnMinecart(EntityInteract event, AbstractMinecartEntity minecart,
		PlayerEntity player, MinecartController controller) {
		int couplings = (controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
		if (couplings == 0)
			return false;
		if (event.getWorld().isClientSide)
			return true;

		for (boolean forward : Iterate.trueAndFalse) {
			if (controller.hasContraptionCoupling(forward))
				couplings--;
		}

		CouplingHandler.status(player, "removed");
		controller.decouple();
		if (!player.isCreative())
			player.inventory.placeItemBackInInventory(event.getWorld(),
				new ItemStack(AllItems.MINECART_COUPLING.get(), couplings));
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private static void cartClicked(PlayerEntity player, AbstractMinecartEntity interacted) {
		CouplingHandlerClient.onCartClicked(player, (AbstractMinecartEntity) interacted);
	}

}
