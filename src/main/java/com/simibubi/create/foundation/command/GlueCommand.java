package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class GlueCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("glue")
				.requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						//.then(Commands.argument("direction", EnumArgument.enumArgument(Direction.class))
								.executes(ctx -> {
									BlockPos pos = BlockPosArgument.getOrLoadBlockPos(ctx, "pos");

									ServerWorld world = ctx.getSource().getLevel();
									SuperGlueEntity entity = new SuperGlueEntity(world, pos, Direction.UP);

									entity.playPlaceSound();
									world.addFreshEntity(entity);

									return 1;
								}));

	}
}
