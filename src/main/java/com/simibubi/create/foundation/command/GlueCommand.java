package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.foundation.utility.GlueHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.blocks.BlockPredicateArgument.blockPredicate;
import static net.minecraft.commands.arguments.blocks.BlockPredicateArgument.getBlockPredicate;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos;
import static net.minecraft.commands.arguments.coordinates.SwizzleArgument.getSwizzle;
import static net.minecraft.commands.arguments.coordinates.SwizzleArgument.swizzle;

public class GlueCommand {

	public static final int GLUE_LIMIT = 8192;

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return literal("glue")
				.requires(cs -> cs.hasPermission(2))
				.then(literal("fill")
						.then(argument("pos1", blockPos()).then(argument("pos2", blockPos())
								.executes(ctx -> fillCommand(ctx, EnumSet.allOf(Direction.Axis.class)))
								.then(argument("axes", swizzle())//enumArgument(Direction.Axis.class))
										.executes(ctx -> fillCommand(ctx, getSwizzle(ctx, "axes")))))))
				.then(literal("clear")
						.then(argument("pos1", blockPos()).then(argument("pos2", blockPos())
								.executes(ctx -> clearCommand(ctx, EnumSet.allOf(Direction.Axis.class)))
								.then(argument("axes", swizzle())
										.executes(ctx -> clearCommand(ctx, getSwizzle(ctx, "axes")))))))
				.then(literal("floodfill")
						.then(argument("source", blockPos())
								.executes(ctx -> floodfillCommand(ctx, GLUE_LIMIT, null))
								.then(argument("mask", blockPredicate())
										.executes(ctx -> floodfillCommand(ctx, GLUE_LIMIT, getBlockPredicate(ctx, "mask"))))
								.then(argument("limit", integer(1, GLUE_LIMIT))
										.executes(ctx -> floodfillCommand(ctx, ctx.getArgument("limit", Integer.class), null))
										.then(argument("mask", blockPredicate())
												.executes(ctx -> floodfillCommand(ctx, ctx.getArgument("limit", Integer.class), getBlockPredicate(ctx, "mask")))))))
				;
	}

	private static int fillCommand(CommandContext<CommandSourceStack> ctx, @NotNull EnumSet<Direction.Axis> axes) throws CommandSyntaxException {
		int cleared = GlueHelper.fill(ctx.getSource().getLevel(), getLoadedBlockPos(ctx, "pos1"), getLoadedBlockPos(ctx, "pos2"), axes);
		ctx.getSource().sendSuccess(new TextComponent("Successfully applied " + cleared + " patches of glue."), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int clearCommand(CommandContext<CommandSourceStack> ctx, @NotNull EnumSet<Direction.Axis> axes) throws CommandSyntaxException {
		int cleared = GlueHelper.clear(ctx.getSource().getLevel(), getLoadedBlockPos(ctx, "pos1"), getLoadedBlockPos(ctx, "pos2"), axes);
		ctx.getSource().sendSuccess(new TextComponent("Successfully cleared " + cleared + " patches of glue."), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int floodfillCommand(CommandContext<CommandSourceStack> ctx, int limit, @Nullable Predicate<BlockInWorld> mask) throws CommandSyntaxException {
		int propagated = GlueHelper.floodfill(ctx.getSource().getLevel(), getLoadedBlockPos(ctx, "source"), limit, mask);
		ctx.getSource().sendSuccess(new TextComponent("Successfully propagated " + propagated + "(limited to " + limit + ") patches of glue."), true);
		return Command.SINGLE_SUCCESS;
	}
}
