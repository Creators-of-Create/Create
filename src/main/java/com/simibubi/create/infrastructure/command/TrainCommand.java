package com.simibubi.create.infrastructure.command;

import java.util.ArrayList;
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
import com.simibubi.create.content.trains.schedule.destination.DeliverPackagesInstruction;
import com.simibubi.create.content.trains.schedule.destination.FetchPackagesInstruction;
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
import net.minecraft.world.phys.Vec3;

public class TrainCommand {

	// duplicate of DumpRailwaysCommand.java, should really be unified
	private static final int white = ChatFormatting.WHITE.getColor();
	private static final int blue = 0xaac8e0;
	//private static final int blue = 0xD3DEDC;
	private static final int darkBlue = 0x88a5b7;
	//private static final int darkBlue = 0x92A9BD;
	private static final int darkerBlue = 0x6b8694;
	private static final int darkestBlue = 0x536b75;
	private static final int bright = 0xFFEFEF;
	private static final int orange = 0xFFAD60;
	//custom additions
	private static final int green = 0xb5fb99;
	private static final int red = 0xe08894;

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("train")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("remove")
				.then(Commands.argument("train", UuidArgument.uuid())
					.executes(ctx -> runDelete(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
				)
				.then(Commands.literal("nearest")
					.executes(ctx -> runDeleteNearest(ctx.getSource()))
				)
			).then(Commands.literal("tp")
				.then(Commands.argument("train", UuidArgument.uuid())
					.requires(CommandSourceStack::isPlayer)
					.executes(ctx -> runTeleport(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
				)
				.then(Commands.literal("nearest")
					.requires(CommandSourceStack::isPlayer)
					.executes(ctx -> runTeleportNearest(ctx.getSource()))
				)
			).then(Commands.literal("schedule")
				.then(Commands.argument("train", UuidArgument.uuid())
					.executes(ctx -> runSchedule(ctx.getSource(), UuidArgument.getUuid(ctx, "train")))
				)
				.then(Commands.literal("nearest")
					.executes(ctx -> runScheduleNearest(ctx.getSource()))
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

	private static int runDeleteNearest(CommandSourceStack source) {
		Train train = findNearestTrain(source);
		if (train == null) {
			source.sendFailure(Component.literal("No trains found nearby"));
			return 0;
		}
		return runDelete(source, train.id);
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

	private static int runTeleportNearest(CommandSourceStack source) throws CommandSyntaxException {
		Train train = findNearestTrain(source);
		if (train == null) {
			source.sendFailure(Component.literal("No trains found nearby"));
			return 0;
		}
		return runTeleport(source, train.id);
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

		// Build the message
		List<Component> message = buildScheduleMessage(train, schedule);

		// Send each line to chat
		for (Component line : message) {
			source.sendSuccess(() -> line, true);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int runScheduleNearest(CommandSourceStack source) {
		Train train = findNearestTrain(source);
		if (train == null) {
			source.sendFailure(Component.literal("No trains found nearby"));
			return 0;
		}
		return runSchedule(source, train.id);
	}

	private static Train findNearestTrain(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		Vec3 position = source.getPosition();
		return Create.RAILWAYS.trains.values()
			.stream()
			.min((t1, t2) -> Float.compare(
				t1.distanceToLocationSqr(level, position),
				t2.distanceToLocationSqr(level, position)))
			.orElse(null);
	}

	private static List<Component> buildScheduleMessage(Train train, Schedule schedule) {
		List<Component> message = new ArrayList<>();

		// Add schedule header
		message.add(Component.literal("").append(Component.literal("-+---<< Schedule for Train '"))
			.append(train.name)
			.append(Component.literal("' >>---+-").withColor(white)));
		int headerLength = message.get(0).getString().length();

		// Add cyclic status
		message.add(Component.literal("Cyclic: ")
			.append(Component.literal(schedule.cyclic ? "Yes" : "No")
				.withColor(schedule.cyclic ? green : red))
			.withColor(blue));

		// Add current entry
		message.add(Component.literal("Current Entry: " + train.runtime.currentEntry + " / " + (schedule.entries.size() - 1))
			.withColor(blue));

		// Add state
		message.add(Component.literal("State: " + train.runtime.state.name())
			.withColor(blue));

		message.add(Component.literal("")); // Blank line

		// Add each schedule entry
		for (int i = 0; i < schedule.entries.size(); i++) {
			boolean isCurrent = i == train.runtime.currentEntry;
			ScheduleEntry entry = schedule.entries.get(i);
			ScheduleInstruction instruction = entry.instruction;
			// Build entry summary with proper name (translated title + content)
			Component title = Component.translatable("create.schedule.instruction." + instruction.getId().getPath());

			Component summary;
			// Special handling for Retrieve Package so information is not lost.
			if (instruction instanceof FetchPackagesInstruction retrieve) {
				Component target = Component.nullToEmpty(retrieve.getFilter());
				summary = title.copy().append(Component.literal(": ")).append(target);
			}
			else if (instruction instanceof DeliverPackagesInstruction){
				summary = title; //Deliver instructions have no content
			}
			else{
				Component content = instruction.getSummary().getSecond();
				summary = title.copy().append(Component.literal(": ")).append(content);
			}

			// Entry header with [ACTIVE] at the end
			Component prefix = Component.literal((isCurrent ? "-> " : "") + "Entry " + i + ": ")
				.withColor(isCurrent ? orange : white);
			Component active = isCurrent ? Component.literal(" [ACTIVE]").withColor(orange) : Component.literal("");
			message.add(prefix.copy().append(summary).append(active));

			// Add conditions with simple indented structure
			boolean hasConditions = instruction.supportsConditions() && !entry.conditions.isEmpty();
			if (hasConditions) {
				for (int columnIndex = 0; columnIndex < entry.conditions.size(); columnIndex++) {
					message.add(Component.literal("  Condition Group " + columnIndex + ":")
						.withColor(blue));
					List<ScheduleWaitCondition> column = entry.conditions.get(columnIndex);
					for (ScheduleWaitCondition condition : column) {
						// Use getTitleAs to get full condition details
						List<Component> conditionTitle = condition.getTitleAs("condition");
						Component conditionLine = Component.literal("    - ").withColor(darkBlue);
						for (Component titlePart : conditionTitle) {
							conditionLine = conditionLine.copy().append(titlePart);
						}
						message.add(conditionLine);
					}
				}
			}
			if (i < schedule.entries.size() - 1) {
				message.add(Component.literal("")); // Blank line between entries
			}
		}

		// Add footer (length cropped due to non-monospace font, best guess)
		String footer = "-".repeat(Math.max(headerLength-6, 5));
		message.add(Component.literal(footer)
			.withColor(white));


		return message;
	}

}
