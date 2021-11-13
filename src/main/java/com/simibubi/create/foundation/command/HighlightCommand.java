package com.simibubi.create.foundation.command;

import java.util.Collection;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HighlightCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("highlight")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.then(Commands.argument("players", EntityArgument.players())
					.executes(ctx -> {
						Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
						BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

						for (ServerPlayer p : players) {
							AllPackets.channel.sendToClient(new HighlightPacket(pos), p);
						}

						return players.size();
					}))
				// .requires(AllCommands.sourceIsPlayer)
				.executes(ctx -> {
					BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

					AllPackets.channel.sendToClient(new HighlightPacket(pos), (ServerPlayer) ctx.getSource().getEntity());

					return Command.SINGLE_SUCCESS;
				}))
			// .requires(AllCommands.sourceIsPlayer)
			.executes(ctx -> {
				ServerPlayer player = ctx.getSource()
					.getPlayerOrException();
				return highlightAssemblyExceptionFor(player, ctx.getSource());
			});

	}

	private static void sendMissMessage(CommandSourceStack source) {
		source.sendSuccess(
			new TextComponent("Try looking at a Block that has failed to assemble a Contraption and try again."),
			true);
	}

	private static int highlightAssemblyExceptionFor(ServerPlayer player, CommandSourceStack source) {
		double distance = player.getAttribute(ReachEntityAttributes.REACH)
			.getValue();
		Vec3 start = player.getEyePosition(1);
		Vec3 look = player.getViewVector(1);
		Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
		Level world = player.level;

		BlockHitResult ray = world.clip(
			new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (ray.getType() == HitResult.Type.MISS) {
			sendMissMessage(source);
			return 0;
		}

		BlockPos pos = ray.getBlockPos();
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof IDisplayAssemblyExceptions)) {
			sendMissMessage(source);
			return 0;
		}

		IDisplayAssemblyExceptions display = (IDisplayAssemblyExceptions) te;
		AssemblyException exception = display.getLastAssemblyException();
		if (exception == null) {
			sendMissMessage(source);
			return 0;
		}

		if (!exception.hasPosition()) {
			source.sendSuccess(new TextComponent("Can't highlight a specific position for this issue"), true);
			return Command.SINGLE_SUCCESS;
		}

		BlockPos p = exception.getPosition();
		String command = "/create highlight " + p.getX() + " " + p.getY() + " " + p.getZ();
		return player.server.getCommands()
			.performCommand(source, command);
	}
}
