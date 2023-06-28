package com.simibubi.create.infrastructure.command;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceInCommandBlocksCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("replaceInCommandBlocks")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("begin", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.then(Commands.argument("toReplace", StringArgumentType.string())
						.then(Commands.argument("replaceWith", StringArgumentType.string())
							.executes(ctx -> {
								doReplace(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "begin"),
									BlockPosArgument.getLoadedBlockPos(ctx, "end"),
									StringArgumentType.getString(ctx, "toReplace"),
									StringArgumentType.getString(ctx, "replaceWith"));
								return 1;
							})))));

	}

	private static void doReplace(CommandSourceStack source, BlockPos from, BlockPos to, String toReplace,
		String replaceWith) {
		ServerLevel world = source.getLevel();
		MutableInt blocks = new MutableInt(0);
		BlockPos.betweenClosedStream(from, to)
			.forEach(pos -> {
				BlockState blockState = world.getBlockState(pos);
				if (!(blockState.getBlock() instanceof CommandBlock))
					return;
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (!(blockEntity instanceof CommandBlockEntity))
					return;
				CommandBlockEntity cb = (CommandBlockEntity) blockEntity;
				BaseCommandBlock commandBlockLogic = cb.getCommandBlock();
				String command = commandBlockLogic.getCommand();
				if (command.indexOf(toReplace) != -1)
					blocks.increment();
				commandBlockLogic.setCommand(command.replaceAll(toReplace, replaceWith));
				cb.setChanged();
				world.sendBlockUpdated(pos, blockState, blockState, 2);
			});
		int intValue = blocks.intValue();
		if (intValue == 0) {
			source.sendSuccess(() -> Components.literal("Couldn't find \"" + toReplace + "\" anywhere."), true);
			return;
		}
		source.sendSuccess(() -> Components.literal("Replaced occurrences in " + intValue + " blocks."), true);
	}

}
