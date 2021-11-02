package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;

public class ChunkUtilCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("chunk")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("reload")
				.then(Commands.argument("pos", ColumnPosArgument.columnPos())
					.executes(ctx -> {
						// chunk reload <pos>
						ColumnPos columnPos = ColumnPosArgument.getColumnPos(ctx, "pos");
						ChunkPos chunkPos = new ChunkPos(columnPos.x >> 4, columnPos.z >> 4);
						ServerChunkCache chunkProvider = ctx.getSource()
								.getLevel()
								.getChunkSource();

						boolean success = Create.CHUNK_UTIL.reloadChunk(chunkProvider, chunkPos);

						if (success) {
							ctx.getSource()
									.sendSuccess(new TextComponent("scheduled unload for chunk "
											+ chunkPos.toString() + ", might need to repeat command"), true);
							return 1;
						} else {
							ctx.getSource()
									.sendSuccess(
											new TextComponent(
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
						ServerChunkCache chunkProvider = ctx.getSource()
								.getLevel()
								.getChunkSource();

						boolean success = Create.CHUNK_UTIL.unloadChunk(chunkProvider, chunkPos);
						ctx.getSource()
								.sendSuccess(
										new TextComponent("added chunk " + chunkPos.toString() + " to unload list"),
										true);

						if (success) {
							ctx.getSource()
									.sendSuccess(new TextComponent("scheduled unload for chunk "
											+ chunkPos.toString() + ", might need to repeat command"), true);
							return 1;
						} else {
							ctx.getSource()
								.sendSuccess(
									new TextComponent(
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
							.sendSuccess(new TextComponent("removed " + count + " entries from unload list"), false);

					return 1;
				}));

	}
}
