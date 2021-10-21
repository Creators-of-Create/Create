package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerChunkProvider;

public class ChunkUtilCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("chunk")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("reload")
				.then(Commands.argument("pos", ColumnPosArgument.columnPos())
					.executes(ctx -> {
						// chunk reload <pos>
						ColumnPos columnPos = ColumnPosArgument.getColumnPos(ctx, "pos");
						ChunkPos chunkPos = new ChunkPos(columnPos.x >> 4, columnPos.z >> 4);
						ServerChunkProvider chunkProvider = ctx.getSource()
								.getLevel()
								.getChunkSource();

						boolean success = Create.CHUNK_UTIL.reloadChunk(chunkProvider, chunkPos);

						if (success) {
							ctx.getSource()
									.sendSuccess(new StringTextComponent("scheduled unload for chunk "
											+ chunkPos.toString() + ", might need to repeat command"), true);
							return 1;
						} else {
							ctx.getSource()
									.sendSuccess(
											new StringTextComponent(
										"unable to schedule unload, is chunk " + chunkPos.toString() + " loaded?"),
									true);
							return 0;
						}
					})))
			.then(Commands.literal("unload")
				.then(Commands.argument("pos", ColumnPosArgument.columnPos())
					.executes(ctx -> {
						// chunk unload <pos>
						ColumnPos columnPos = ColumnPosArgument.getColumnPos(ctx, "pos");
						ChunkPos chunkPos = new ChunkPos(columnPos.x >> 4, columnPos.z >> 4);
						ServerChunkProvider chunkProvider = ctx.getSource()
								.getLevel()
								.getChunkSource();

						boolean success = Create.CHUNK_UTIL.unloadChunk(chunkProvider, chunkPos);
						ctx.getSource()
								.sendSuccess(
										new StringTextComponent("added chunk " + chunkPos.toString() + " to unload list"),
										true);

						if (success) {
							ctx.getSource()
									.sendSuccess(new StringTextComponent("scheduled unload for chunk "
											+ chunkPos.toString() + ", might need to repeat command"), true);
							return 1;
						} else {
							ctx.getSource()
								.sendSuccess(
									new StringTextComponent(
										"unable to schedule unload, is chunk " + chunkPos.toString() + " loaded?"),
									true);
							return 0;
						}
					})))
			.then(Commands.literal("clear")
				.executes(ctx -> {
					// chunk clear
					int count = Create.CHUNK_UTIL.clear(ctx.getSource()
							.getLevel()
							.getChunkSource());
					ctx.getSource()
							.sendSuccess(new StringTextComponent("removed " + count + " entries from unload list"), false);

					return 1;
				}));

	}
}
