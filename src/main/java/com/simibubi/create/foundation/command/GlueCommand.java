package com.simibubi.create.foundation.command;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GlueCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("glue").requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).executes(ctx -> {
					BoundingBox area = BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(ctx, "from"), BlockPosArgument.getLoadedBlockPos(ctx, "to"));

					List<SuperGlueEntity> newGlue = Lists.newArrayList();
					Direction[] directions = { Direction.EAST, Direction.SOUTH, Direction.UP };
					int[] areaMaxCoords = { area.maxX(), area.maxZ(), area.maxY() };
					ServerLevel world = ctx.getSource().getLevel();
					int gluePastes = 0;

					for (BlockPos blockPos : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {

						int[] blockCoords = { blockPos.getX(), blockPos.getZ(), blockPos.getY() };

						for (int j = 0; j < 3; j++) {
							if (!isAirBlock(world, blockPos) && !isTargetAirBlock(world, blockPos, directions[j]) && blockCoords[j] != areaMaxCoords[j]) {
								BlockPos blockPos1 = blockPos.relative(directions[j]);
								newGlue.add(new SuperGlueEntity(world, blockPos1, directions[j]));
							}
						}
					}

					for (SuperGlueEntity glue : newGlue) {
						if (glue.onValidSurface()) {
							world.addFreshEntity(glue);
							gluePastes++;
						}
					}

					ctx.getSource().sendSuccess(new TextComponent("Successfully applied glue " + gluePastes + " times"), true);
					return Command.SINGLE_SUCCESS;
				})));

	}

	private static boolean isAirBlock(ServerLevel world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() == Blocks.AIR;
	}

	private static boolean isTargetAirBlock(ServerLevel world, BlockPos pos, Direction direction) {
		return world.getBlockState(pos.relative(direction)).getBlock() == Blocks.AIR;
	}
}
