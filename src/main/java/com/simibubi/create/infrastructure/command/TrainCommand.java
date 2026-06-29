package com.simibubi.create.infrastructure.command;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class TrainCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("train")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("remove")
				.then(Commands.argument("train", UuidArgument.uuid())
					.executes(ctx -> runDelete(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
				)
			).then(Commands.literal("tp")
				.then(Commands.argument("train", UuidArgument.uuid())
					.requires(CommandSourceStack::isPlayer)
					.executes(ctx -> runTeleport(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
				)
			);
	}

	private static int runDelete(CommandSourceStack source, UUID argument) {
		Train train = Create.RAILWAYS.trains.get(argument);
		if (train == null) {
			source.sendFailure(Component.literal("No Train with id " + argument.toString()
				.substring(0, 5) + "[...] was found"));
			return 0;
		}

		train.invalid = true;
		source.sendSuccess(() -> {
			return Component.literal("Train '").append(train.name)
				.append("' removed successfully");
		}, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int runTeleport(CommandSourceStack source, UUID argument) throws CommandSyntaxException {
		ServerPlayer serverPlayer = source.getPlayerOrException();
		GameType gameMode = serverPlayer.gameMode.getGameModeForPlayer();
		if (gameMode != GameType.CREATIVE && gameMode != GameType.SPECTATOR) {
			source.sendFailure(Component.literal("Can only teleport to train when in Creative or Spectator Mode!"));
			return 0;
		}

		Train train = Create.RAILWAYS.trains.get(argument);
		if (train == null) {
			source.sendFailure(Component.literal("No Train with id " + argument.toString()
				.substring(0, 5) + "[...] was found"));
			return 0;
		}

		List<ResourceKey<Level>> presentDimensions = train.getPresentDimensions();

		if (presentDimensions.isEmpty()) {
			source.sendFailure(Component.literal("Unable to teleport to Train. No valid location found"));
			return 0;
		}

		ResourceKey<Level> levelKey = presentDimensions.get(0);
		ServerLevel serverLevel = serverPlayer.getServer().getLevel(levelKey);
		Optional<BlockPos> positionInDimension = train.getPositionInDimension(levelKey);

		if (positionInDimension.isEmpty() || serverLevel == null) {
			source.sendFailure(Component.literal("Unable to teleport to Train. No valid location found"));
			return 0;
		}

		BlockPos pos = positionInDimension.get();

		serverPlayer.teleportTo(
			serverLevel,
			pos.getX(),
			pos.getY() + 5,
			pos.getZ(),
			serverPlayer.getViewYRot(0),
			serverPlayer.getViewXRot(0)
		);

		source.sendSuccess(() -> {
			return Component.literal("Teleported to Train '").append(train.name)
				.append("' successfully");
		}, true);
		return Command.SINGLE_SUCCESS;
	}

}
