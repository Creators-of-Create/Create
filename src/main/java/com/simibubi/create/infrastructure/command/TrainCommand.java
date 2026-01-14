package com.simibubi.create.infrastructure.command;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;

import net.minecraft.ChatFormatting;
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
			).then(Commands.argument("train", UuidArgument.uuid())
				.then(Commands.literal("schedule")
					.executes(ctx -> runSchedule(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
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

	private static int runSchedule(CommandSourceStack source, UUID argument) {
		Train train = Create.RAILWAYS.trains.get(argument);
		if (train == null) {
			source.sendFailure(Component.literal("No Train with id " + argument.toString()
				.substring(0, 5) + "[...] was found"));
			return 0;
		}

		Schedule schedule = train.runtime.getSchedule();
		if (schedule == null) {
			source.sendFailure(Component.literal("Train '").append(train.name)
				.append("' has no schedule"));
			return 0;
		}

		// Print schedule header
		source.sendSuccess(() -> {
			return Component.literal("").append(Component.literal("─────< Schedule for Train '")
				.withStyle(ChatFormatting.WHITE))
				.append(train.name)
				.append(Component.literal("' >─────").withStyle(ChatFormatting.WHITE));
		}, false);

		// Print cyclic status
		source.sendSuccess(() -> {
			return Component.literal("Cyclic: " + (schedule.cyclic ? "Yes" : "No"))
				.withStyle(ChatFormatting.GRAY);
		}, false);

		// Print current entry
		source.sendSuccess(() -> {
			return Component.literal("Current Entry: " + train.runtime.currentEntry + " / " + (schedule.entries.size() - 1))
				.withStyle(ChatFormatting.GRAY);
		}, false);

		// Print state
		source.sendSuccess(() -> {
			return Component.literal("State: " + train.runtime.state.name())
				.withStyle(ChatFormatting.GRAY);
		}, false);

		source.sendSuccess(() -> Component.literal(""), false);

		// Print each schedule entry
		for (int i = 0; i < schedule.entries.size(); i++) {
			final int index = i;
			boolean isCurrent = i == train.runtime.currentEntry;
			ScheduleEntry entry = schedule.entries.get(i);
			ScheduleInstruction instruction = entry.instruction;

			// Entry header
			source.sendSuccess(() -> {
				Component prefix = Component.literal((isCurrent ? "→ " : "  ") + "Entry " + index + ": ")
					.withStyle(isCurrent ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
				Component summary = instruction.getSummary().getSecond();
				return prefix.copy().append(summary);
			}, false);

			// Print conditions
			if (instruction.supportsConditions() && !entry.conditions.isEmpty()) {
				for (int columnIndex = 0; columnIndex < entry.conditions.size(); columnIndex++) {
					List<ScheduleWaitCondition> column = entry.conditions.get(columnIndex);
					final int col = columnIndex;
					
					source.sendSuccess(() -> {
						return Component.literal("  Condition Group " + col + ":")
							.withStyle(ChatFormatting.DARK_GRAY);
					}, false);
					
					for (int condIndex = 0; condIndex < column.size(); condIndex++) {
						ScheduleWaitCondition condition = column.get(condIndex);
						source.sendSuccess(() -> {
							Component summary = condition.getSummary().getSecond();
							return Component.literal("    - ").withStyle(ChatFormatting.DARK_GRAY)
								.append(summary);
						}, false);
					}
				}
			}

			// Add spacing between entries
			if (i < schedule.entries.size() - 1) {
				source.sendSuccess(() -> Component.literal(""), false);
			}
		}

		// Print footer
		source.sendSuccess(() -> {
			return Component.literal("─────────────────────────────────")
				.withStyle(ChatFormatting.WHITE);
		}, false);

		return Command.SINGLE_SUCCESS;
	}

}
