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

public class DebugValueCommand { //fixme this is *very* temporary

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

	// same side - other side
	// n(ormal)-n(ormal)
	// u(pside down)-u(pside down)
	// nu works with special handling, now to get un to work!
	// 1 works for: nn   n
	//-1 works for: uu   u
	public static double tmpPortalOffset(boolean leadingUpsideDown, boolean trailingUpsideDown, boolean isLeading) {
		double portalOffset = 0.0;
		if (!leadingUpsideDown && !trailingUpsideDown) {       // nn
			return 1.0;
		} else if (leadingUpsideDown && trailingUpsideDown) {  // uu
			return -1.0;
		} else if (leadingUpsideDown && !trailingUpsideDown) { // un
			if (isLeading) {
				return -1.0;
			} else {
				return -1.0;
			}
		} else if (!leadingUpsideDown && trailingUpsideDown) { // nu
			if (isLeading) {
				return 1.0;
			} else {
				return 1.0;
			}
		}
		Create.LOGGER.error("Theoretically unreachable code just got reached. HALP me please");
		return 0.0; // this is actually unreachable but yay
		/*if (!leadingUpsideDown) { // leading up
			portalOffset = 1.0;
		} else if (trailingUpsideDown) { // leading down, trailing down
			portalOffset = -1.0;
		} else { // leading down, trailing up - ahh
			portalOffset = DebugValueCommand.value;
		}
		return portalOffset;*/
	}

}
