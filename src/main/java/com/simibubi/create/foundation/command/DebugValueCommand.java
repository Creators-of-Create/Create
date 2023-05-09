package com.simibubi.create.foundation.command;

import com.mojang.brigadier.arguments.FloatArgumentType;

import com.simibubi.create.Create;

import net.minecraft.SharedConstants;

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

public class DebugValueCommand {

	public static float value = 0;

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("debugValue")
			.requires(cs -> cs.hasPermission(4))
			.then(Commands.argument("value", FloatArgumentType.floatArg())
					.executes((ctx) -> {
						value = FloatArgumentType.getFloat(ctx, "value");
						ctx.getSource().sendSuccess(Components.literal("Set value to: "+value), true);
						return 1;
					}));

	}
}
