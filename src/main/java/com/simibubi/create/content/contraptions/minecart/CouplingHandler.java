package com.simibubi.create.content.contraptions.minecart;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CouplingHandler {

	@SubscribeEvent
	public static void preventEntitiesFromMoutingOccupiedCart(EntityMountEvent event) {
		Entity e = event.getEntityBeingMounted();
		LazyOptional<MinecartController> optional = e.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (!optional.isPresent())
			return;
		if (event.getEntityMounting() instanceof AbstractContraptionEntity)
			return;
		MinecartController controller = optional.orElse(null);
		if (controller.isCoupledThroughContraption()) {
			event.setCanceled(true);
			event.setResult(Result.DENY);
		}
	}

	public static void forEachLoadedCoupling(Level world, Consumer<Couple<MinecartController>> consumer) {
		if (world == null)
			return;
		Set<UUID> cartsWithCoupling = CapabilityMinecartController.loadedMinecartsWithCoupling.get(world);
		if (cartsWithCoupling == null)
			return;
		cartsWithCoupling.forEach(id -> {
			MinecartController controller = CapabilityMinecartController.getIfPresent(world, id);
			if (controller == null)
				return;
			if (!controller.isLeadingCoupling())
				return;
			UUID coupledCart = controller.getCoupledCart(true);
			MinecartController coupledController = CapabilityMinecartController.getIfPresent(world, coupledCart);
			if (coupledController == null)
				return;
			consumer.accept(Couple.create(controller, coupledController));
		});
	}

	public static boolean tryToCoupleCarts(@Nullable Player player, Level world, int cartId1, int cartId2) {
		Entity entity1 = world.getEntity(cartId1);
		Entity entity2 = world.getEntity(cartId2);

		if (!(entity1 instanceof AbstractMinecart))
			return false;
		if (!(entity2 instanceof AbstractMinecart))
			return false;

		String tooMany = "two_couplings_max";
		String unloaded = "unloaded";
		String noLoops = "no_loops";
		String tooFar = "too_far";

		int distanceTo = (int) entity1.position()
			.distanceTo(entity2.position());
		boolean contraptionCoupling = player == null;

		if (distanceTo < 2) {
			if (contraptionCoupling)
				return false; // dont allow train contraptions with <2 distance
			distanceTo = 2;
		}

		if (distanceTo > AllConfigs.server().kinetics.maxCartCouplingLength.get()) {
			status(player, tooFar);
			return false;
		}

		AbstractMinecart cart1 = (AbstractMinecart) entity1;
		AbstractMinecart cart2 = (AbstractMinecart) entity2;
		UUID mainID = cart1.getUUID();
		UUID connectedID = cart2.getUUID();
		MinecartController mainController = CapabilityMinecartController.getIfPresent(world, mainID);
		MinecartController connectedController = CapabilityMinecartController.getIfPresent(world, connectedID);

		if (mainController == null || connectedController == null) {
			status(player, unloaded);
			return false;
		}
		if (mainController.isFullyCoupled() || connectedController.isFullyCoupled()) {
			status(player, tooMany);
			return false;
		}

		if (mainController.isLeadingCoupling() && mainController.getCoupledCart(true)
			.equals(connectedID) || connectedController.isLeadingCoupling()
				&& connectedController.getCoupledCart(true)
					.equals(mainID))
			return false;

		for (boolean main : Iterate.trueAndFalse) {
			MinecartController current = main ? mainController : connectedController;
			boolean forward = current.isLeadingCoupling();
			int safetyCount = 1000;

			while (true) {
				if (safetyCount-- <= 0) {
					Create.LOGGER.warn("Infinite loop in coupling iteration");
					return false;
				}

				current = getNextInCouplingChain(world, current, forward);
				if (current == null) {
					status(player, unloaded);
					return false;
				}
				if (current == connectedController) {
					status(player, noLoops);
					return false;
				}
				if (current == MinecartController.EMPTY)
					break;
			}
		}

		if (!contraptionCoupling) {
			for (InteractionHand hand : InteractionHand.values()) {
				if (player.isCreative())
					break;
				ItemStack heldItem = player.getItemInHand(hand);
				if (!AllItems.MINECART_COUPLING.isIn(heldItem))
					continue;
				heldItem.shrink(1);
				break;
			}
		}

		mainController.prepareForCoupling(true);
		connectedController.prepareForCoupling(false);

		mainController.coupleWith(true, connectedID, distanceTo, contraptionCoupling);
		connectedController.coupleWith(false, mainID, distanceTo, contraptionCoupling);
		return true;
	}

	@Nullable
	/**
	 * MinecartController.EMPTY if none connected, null if not yet loaded
	 */
	public static MinecartController getNextInCouplingChain(Level world, MinecartController controller,
		boolean forward) {
		UUID coupledCart = controller.getCoupledCart(forward);
		if (coupledCart == null)
			return MinecartController.empty();
		return CapabilityMinecartController.getIfPresent(world, coupledCart);
	}

	public static void status(Player player, String key) {
		if (player == null)
			return;
		player.displayClientMessage(CreateLang.translateDirect("minecart_coupling." + key), true);
	}

}
