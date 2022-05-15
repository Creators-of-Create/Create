package com.simibubi.create.foundation.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entity;
import static net.minecraft.commands.arguments.EntityArgument.getEntity;

import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.common.util.LazyOptional;

public class CouplingCommand {

	public static final SimpleCommandExceptionType ONLY_MINECARTS_ALLOWED =
		new SimpleCommandExceptionType(new TextComponent("Only Minecarts can be coupled"));
	public static final SimpleCommandExceptionType SAME_DIMENSION =
		new SimpleCommandExceptionType(new TextComponent("Minecarts have to be in the same Dimension"));

	public static ArgumentBuilder<CommandSourceStack, ?> register() {

		return literal("coupling")
			.requires(cs -> cs.hasPermission(2))
			.then(literal("add")
				.then(argument("cart1", entity())
					.then(argument("cart2", entity())
						.executes(ctx -> {
							Entity cart1 = getEntity(ctx, "cart1");
							if (!(cart1 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							Entity cart2 = getEntity(ctx, "cart2");
							if (!(cart2 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							if (!cart1.getCommandSenderWorld()
								.equals(cart2.getCommandSenderWorld()))
								throw SAME_DIMENSION.create();

							Entity source = ctx.getSource()
								.getEntity();

							CouplingHandler.tryToCoupleCarts(
								source instanceof Player ? (Player) source : null, cart1.getCommandSenderWorld(),
								cart1.getId(), cart2.getId());

							return Command.SINGLE_SUCCESS;
						}))))
			.then(literal("remove")
				.then(argument("cart1", entity())
					.then(argument("cart2", entity())
						.executes(ctx -> {
							Entity cart1 = getEntity(ctx, "cart1");
							if (!(cart1 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							Entity cart2 = getEntity(ctx, "cart2");
							if (!(cart2 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							LazyOptional<MinecartController> cart1Capability =
								cart1.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
							if (!cart1Capability.isPresent()) {
								ctx.getSource()
									.sendSuccess(new TextComponent("Minecart has no Couplings Attached"), true);
								return 0;
							}

							MinecartController cart1Controller = cart1Capability.orElse(null);

							int cart1Couplings = (cart1Controller.isConnectedToCoupling() ? 1 : 0)
								+ (cart1Controller.isLeadingCoupling() ? 1 : 0);
							if (cart1Couplings == 0) {
								ctx.getSource()
									.sendSuccess(new TextComponent("Minecart has no Couplings Attached"), true);
								return 0;
							}

							for (boolean bool : Iterate.trueAndFalse) {
								UUID coupledCart = cart1Controller.getCoupledCart(bool);
								if (coupledCart == null)
									continue;

								if (coupledCart != cart2.getUUID())
									continue;

								MinecartController cart2Controller =
									CapabilityMinecartController.getIfPresent(cart1.getCommandSenderWorld(), coupledCart);
								if (cart2Controller == null)
									return 0;

								cart1Controller.removeConnection(bool);
								cart2Controller.removeConnection(!bool);
								return Command.SINGLE_SUCCESS;
							}

							ctx.getSource()
								.sendSuccess(new TextComponent("The specified Carts are not coupled"), true);

							return 0;
						}))))
			.then(literal("removeAll")
				.then(argument("cart", entity())
					.executes(ctx -> {
						Entity cart = getEntity(ctx, "cart");
						if (!(cart instanceof AbstractMinecart))
							throw ONLY_MINECARTS_ALLOWED.create();

						LazyOptional<MinecartController> capability =
							cart.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
						if (!capability.isPresent()) {
							ctx.getSource()
								.sendSuccess(new TextComponent("Minecart has no Couplings Attached"), true);
							return 0;
						}

						MinecartController controller = capability.orElse(null);

						int couplings =
							(controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
						if (couplings == 0) {
							ctx.getSource()
								.sendSuccess(new TextComponent("Minecart has no Couplings Attached"), true);
							return 0;
						}

						controller.decouple();

						ctx.getSource()
							.sendSuccess(
								new TextComponent("Removed " + couplings + " couplings from the Minecart"), true);

						return couplings;
					})));
	}

}
