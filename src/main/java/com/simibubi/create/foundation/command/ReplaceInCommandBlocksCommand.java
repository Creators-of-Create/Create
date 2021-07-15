package com.simibubi.create.foundation.command;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ReplaceInCommandBlocksCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("replaceInCommandBlocks")
			.requires(cs -> cs.hasPermission(0))
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

	private static void doReplace(CommandSource source, BlockPos from, BlockPos to, String toReplace,
		String replaceWith) {
		ServerWorld world = source.getLevel();
		MutableInt blocks = new MutableInt(0);
		BlockPos.betweenClosedStream(from, to)
			.forEach(pos -> {
				BlockState blockState = world.getBlockState(pos);
				if (!(blockState.getBlock() instanceof CommandBlockBlock))
					return;
				TileEntity tileEntity = world.getBlockEntity(pos);
				if (!(tileEntity instanceof CommandBlockTileEntity))
					return;
				CommandBlockTileEntity cb = (CommandBlockTileEntity) tileEntity;
				CommandBlockLogic commandBlockLogic = cb.getCommandBlock();
				String command = commandBlockLogic.getCommand();
				if (command.indexOf(toReplace) != -1)
					blocks.increment();
				commandBlockLogic.setCommand(command.replaceAll(toReplace, replaceWith));
				cb.setChanged();
				world.sendBlockUpdated(pos, blockState, blockState, 2);
			});
		int intValue = blocks.intValue();
		if (intValue == 0) {
			source.sendSuccess(new StringTextComponent("Couldn't find \"" + toReplace + "\" anywhere."), true);
			return;
		}
		source.sendSuccess(new StringTextComponent("Replaced occurrences in " + intValue + " blocks."), true);
	}

}
