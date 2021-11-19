package com.simibubi.create.foundation.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Iterate;

import com.simibubi.create.lib.utility.LazyOptional;

import com.simibubi.create.lib.utility.MinecartAndRailUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public class CouplingCommand {

	public static final SimpleCommandExceptionType ONLY_MINECARTS_ALLOWED =
		new SimpleCommandExceptionType(new TextComponent("Only Minecarts can be coupled"));
	public static final SimpleCommandExceptionType SAME_DIMENSION =
		new SimpleCommandExceptionType(new TextComponent("Minecarts have to be in the same Dimension"));
	public static final DynamicCommandExceptionType TWO_CARTS =
		new DynamicCommandExceptionType(a -> new TextComponent(
			"Your selector targeted " + a + " entities. You can only couple 2 Minecarts at a time."));

	public static ArgumentBuilder<CommandSourceStack, ?> register() {

		return Commands.literal("coupling")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("add")
				.then(Commands.argument("cart1", EntityArgument.entity())
					.then(Commands.argument("cart2", EntityArgument.entity())
						.executes(ctx -> {
							Entity cart1 = EntityArgument.getEntity(ctx, "cart1");
							if (!(cart1 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							Entity cart2 = EntityArgument.getEntity(ctx, "cart2");
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
						})))
				.then(Commands.argument("carts", EntityArgument.entities())
					.executes(ctx -> {
						Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "carts");
						if (entities.size() != 2)
							throw TWO_CARTS.create(entities.size());

						ArrayList<? extends Entity> eList = Lists.newArrayList(entities);
						Entity cart1 = eList.get(0);
						if (!(cart1 instanceof AbstractMinecart))
							throw ONLY_MINECARTS_ALLOWED.create();

						Entity cart2 = eList.get(1);
						if (!(cart2 instanceof AbstractMinecart))
							throw ONLY_MINECARTS_ALLOWED.create();

						if (!cart1.getCommandSenderWorld()
							.equals(cart2.getCommandSenderWorld()))
							throw SAME_DIMENSION.create();

						Entity source = ctx.getSource()
							.getEntity();

						CouplingHandler.tryToCoupleCarts(source instanceof Player ? (Player) source : null,
							cart1.getCommandSenderWorld(), cart1.getId(), cart2.getId());

						return Command.SINGLE_SUCCESS;
					})))
			.then(Commands.literal("remove")
				.then(Commands.argument("cart1", EntityArgument.entity())
					.then(Commands.argument("cart2", EntityArgument.entity())
						.executes(ctx -> {
							Entity cart1 = EntityArgument.getEntity(ctx, "cart1");
							if (!(cart1 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							Entity cart2 = EntityArgument.getEntity(ctx, "cart2");
							if (!(cart2 instanceof AbstractMinecart))
								throw ONLY_MINECARTS_ALLOWED.create();

							LazyOptional<MinecartController> cart1Capability =
									MinecartAndRailUtil.getControllerLazy((AbstractMinecart) cart1);
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
			.then(Commands.literal("removeAll")
				.then(Commands.argument("cart", EntityArgument.entity())
					.executes(ctx -> {
						Entity cart = EntityArgument.getEntity(ctx, "cart");
						if (!(cart instanceof AbstractMinecart))
							throw ONLY_MINECARTS_ALLOWED.create();

						LazyOptional<MinecartController> capability =
							MinecartAndRailUtil.getControllerLazy((AbstractMinecart) cart);
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
