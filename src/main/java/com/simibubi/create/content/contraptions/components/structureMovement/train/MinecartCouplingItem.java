package com.simibubi.create.content.contraptions.components.structureMovement.train;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.lib.util.LazyOptional;
import com.simibubi.create.lib.util.MinecartAndRailUtil;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class MinecartCouplingItem extends Item {

	public MinecartCouplingItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	public static InteractionResult handleInteractionWithMinecart(Player player, Level world, InteractionHand hand, Entity interacted, @Nullable EntityHitResult hitResult) {
		if (player.isSpectator()) // forge checks this, fabric does not
			return InteractionResult.PASS;
		if (!(interacted instanceof AbstractMinecart))
			return InteractionResult.PASS;
		AbstractMinecart minecart = (AbstractMinecart) interacted;
		if (player == null)
			return InteractionResult.PASS;
		LazyOptional<MinecartController> capability =
			LazyOptional.ofObject(MinecartAndRailUtil.getController(minecart));
		if (!capability.isPresent())
			return InteractionResult.PASS;
		MinecartController controller = capability.orElse(null);

		ItemStack heldItem = player.getItemInHand(hand);
		if (AllItems.MINECART_COUPLING.isIn(heldItem)) {
			if (!onCouplingInteractOnMinecart(player.level, minecart, player, controller))
				return InteractionResult.PASS;
		} else if (AllItems.WRENCH.isIn(heldItem)) {
			if (!onWrenchInteractOnMinecart(player.level, minecart, player, controller))
				return InteractionResult.PASS;
		} else
			return InteractionResult.PASS;

		return InteractionResult.SUCCESS;
	}

	protected static boolean onCouplingInteractOnMinecart(Level world,
		AbstractMinecart minecart, Player player, MinecartController controller) {
		if (controller.isFullyCoupled()) {
			if (world.isClientSide) // fabric: on forge this only runs on server, here we only run
				// on client to avoid an incorrect message due to differences in timing across loaders.
				// on forge, the process is client -> server -> packet -> couple
				// on fabric, the process is client -> packet -> couple -> server
				CouplingHandler.status(player, "two_couplings_max");
			return true;
		}
		if (world != null && world.isClientSide)
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> cartClicked(player, minecart));
		return true;
	}

	private static boolean onWrenchInteractOnMinecart(Level world, AbstractMinecart minecart, Player player,
		MinecartController controller) {
		int couplings = (controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
		if (couplings == 0)
			return false;
		if (world.isClientSide)
			return true;

		for (boolean forward : Iterate.trueAndFalse) {
			if (controller.hasContraptionCoupling(forward))
				couplings--;
		}

		CouplingHandler.status(player, "removed");
		controller.decouple();
		if (!player.isCreative())
			player.getInventory()
				.placeItemBackInInventory(new ItemStack(AllItems.MINECART_COUPLING.get(), couplings));
		return true;
	}

	@Environment(EnvType.CLIENT)
	private static void cartClicked(Player player, AbstractMinecart interacted) {
		CouplingHandlerClient.onCartClicked(player, (AbstractMinecart) interacted);
	}

}
