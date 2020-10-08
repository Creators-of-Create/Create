package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class CouplingHandler {

	public static void forEachLoadedCoupling(World world, Consumer<Couple<MinecartController>> consumer) {
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

	public static void tryToCoupleCarts(@Nullable PlayerEntity player, World world, int cartId1, int cartId2) {
		Entity entity1 = world.getEntityByID(cartId1);
		Entity entity2 = world.getEntityByID(cartId2);

		if (!(entity1 instanceof AbstractMinecartEntity))
			return;
		if (!(entity2 instanceof AbstractMinecartEntity))
			return;

		String tooMany = "two_couplings_max";
		String unloaded = "unloaded";
		String noLoops = "no_loops";
		String tooFar = "too_far";

		int distanceTo = (int) entity1.getPositionVec()
			.distanceTo(entity2.getPositionVec());
		
		if (distanceTo < 2) {
			if (player == null)
				return; // dont allow train contraptions with <2 distance
			distanceTo = 2;
		}
		
		if (distanceTo > AllConfigs.SERVER.kinetics.maxCartCouplingLength.get()) {
			status(player, tooFar);
			return;
		}

		AbstractMinecartEntity cart1 = (AbstractMinecartEntity) entity1;
		AbstractMinecartEntity cart2 = (AbstractMinecartEntity) entity2;
		UUID mainID = cart1.getUniqueID();
		UUID connectedID = cart2.getUniqueID();
		MinecartController mainController = CapabilityMinecartController.getIfPresent(world, mainID);
		MinecartController connectedController = CapabilityMinecartController.getIfPresent(world, connectedID);

		if (mainController == null || connectedController == null) {
			status(player, unloaded);
			return;
		}
		if (mainController.isFullyCoupled() || connectedController.isFullyCoupled()) {
			status(player, tooMany);
			return;
		}

		if (mainController.isLeadingCoupling() && mainController.getCoupledCart(true)
			.equals(connectedID) || connectedController.isLeadingCoupling()
				&& connectedController.getCoupledCart(true)
					.equals(mainID))
			return;

		for (boolean main : Iterate.trueAndFalse) {
			MinecartController current = main ? mainController : connectedController;
			boolean forward = current.isLeadingCoupling();
			int safetyCount = 1000;

			while (true) {
				if (safetyCount-- <= 0) {
					Create.logger.warn("Infinite loop in coupling iteration");
					return;
				}

				current = getNextInCouplingChain(world, current, forward);
				if (current == null) {
					status(player, unloaded);
					return;
				}
				if (current == connectedController) {
					status(player, noLoops);
					return;
				}
				if (current == MinecartController.EMPTY)
					break;
			}
		}

		if (player != null) {
			for (Hand hand : Hand.values()) {
				if (player.isCreative())
					break;
				ItemStack heldItem = player.getHeldItem(hand);
				if (!AllItems.MINECART_COUPLING.isIn(heldItem))
					continue;
				heldItem.shrink(1);
				break;
			}
		}

		mainController.prepareForCoupling(true);
		connectedController.prepareForCoupling(false);

		mainController.coupleWith(true, connectedID, distanceTo);
		connectedController.coupleWith(false, mainID, distanceTo);
	}

	@Nullable
	/**
	 * MinecartController.EMPTY if none connected, null if not yet loaded
	 */
	public static MinecartController getNextInCouplingChain(World world, MinecartController controller,
		boolean forward) {
		UUID coupledCart = controller.getCoupledCart(forward);
		if (coupledCart == null)
			return MinecartController.empty();
		return CapabilityMinecartController.getIfPresent(world, coupledCart);
	}

	public static void status(PlayerEntity player, String key) {
		if (player == null)
			return;
		player.sendStatusMessage(new StringTextComponent(Lang.translate("minecart_coupling." + key)), true);
	}

}
