package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class GlueCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("glue")
				.requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						//.then(Commands.argument("direction", EnumArgument.enumArgument(Direction.class))
								.executes(ctx -> {
									BlockPos pos = BlockPosArgument.getOrLoadBlockPos(ctx, "pos");

									ServerLevel world = ctx.getSource().getLevel();
									SuperGlueEntity entity = new SuperGlueEntity(world, pos, Direction.UP);

									entity.playPlaceSound();
									world.addFreshEntity(entity);

									return 1;
								}));

	}
}
