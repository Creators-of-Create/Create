package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class GlueCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("glue")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("from", BlockPosArgument.blockPos())
				.then(Commands.argument("to", BlockPosArgument.blockPos())
					.executes(ctx -> {
						BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
						BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");

						ServerLevel world = ctx.getSource()
							.getLevel();

						SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(from, to));
						entity.playPlaceSound();
						world.addFreshEntity(entity);
						return 1;
					})));

	}
}
