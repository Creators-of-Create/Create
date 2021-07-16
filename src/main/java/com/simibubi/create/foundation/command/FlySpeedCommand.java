package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FlySpeedCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("flySpeed")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("speed", FloatArgumentType.floatArg(0))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> sendFlySpeedUpdate(ctx, EntityArgument.getPlayer(ctx, "target"),
						FloatArgumentType.getFloat(ctx, "speed"))))
				.executes(ctx -> sendFlySpeedUpdate(ctx, ctx.getSource()
					.getPlayerOrException(), FloatArgumentType.getFloat(ctx, "speed"))))
			.then(Commands.literal("reset")
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> sendFlySpeedUpdate(ctx, EntityArgument.getPlayer(ctx, "target"), 0.05f)))
				.executes(ctx -> sendFlySpeedUpdate(ctx, ctx.getSource()
					.getPlayerOrException(), 0.05f))

			);
	}

	private static int sendFlySpeedUpdate(CommandContext<CommandSource> ctx, ServerPlayerEntity player, float speed) {
		SPlayerAbilitiesPacket packet = new SPlayerAbilitiesPacket(player.abilities);
		// packet.setFlySpeed(speed);
		ObfuscationReflectionHelper.setPrivateValue(SPlayerAbilitiesPacket.class, packet, speed, "flyingSpeed"); // flyingSpeed
		player.connection.send(packet);

		ctx.getSource()
			.sendSuccess(new StringTextComponent("Temporarily set " + player.getName()
				.getString() + "'s Flying Speed to: " + speed), true);

		return Command.SINGLE_SUCCESS;
	}

}
