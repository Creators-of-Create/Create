package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;

public class PassengerCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("passenger")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("rider", EntityArgument.entity())
				.then(Commands.argument("vehicle", EntityArgument.entity())
					.executes(ctx -> {
						run(ctx.getSource(), EntityArgument.getEntity(ctx, "vehicle"),
							EntityArgument.getEntity(ctx, "rider"), 0);
						return 1;
					})
					.then(Commands.argument("seatIndex", IntegerArgumentType.integer(0))
						.executes(ctx -> {
							run(ctx.getSource(), EntityArgument.getEntity(ctx, "vehicle"),
								EntityArgument.getEntity(ctx, "rider"),
								IntegerArgumentType.getInteger(ctx, "seatIndex"));
							return 1;
						}))));
	}

	private static void run(CommandSourceStack source, Entity vehicle, Entity rider, int index) {
		if (vehicle == rider)
			return;
		if (rider instanceof CarriageContraptionEntity)
			return;
		if (rider instanceof ControlledContraptionEntity)
			return;
		
		if (vehicle instanceof AbstractContraptionEntity ace) {
			if (ace.getContraption()
				.getSeats()
				.size() > index)
				ace.addSittingPassenger(rider, index);
			return;
		}
		
		rider.startRiding(vehicle, true);
	}
}
