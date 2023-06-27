package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

public class OverlayConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("overlay")
				.requires(cs -> cs.hasPermission(0))
				.then(Commands.literal("reset")
					.executes(ctx -> {
						DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SConfigureConfigPacket.Actions.overlayReset.performAction(""));

						DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
								AllPackets.getChannel().send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayer) ctx.getSource().getEntity()),
										new SConfigureConfigPacket(SConfigureConfigPacket.Actions.overlayReset.name(), "")));

					ctx.getSource()
						.sendSuccess(Components.literal("reset overlay offset"), true);

						return 1;
					})
				)
				.executes(ctx -> {
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SConfigureConfigPacket.Actions.overlayScreen.performAction(""));

					DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
							AllPackets.getChannel().send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayer) ctx.getSource().getEntity()),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.overlayScreen.name(), "")));

					ctx.getSource()
							.sendSuccess(Components.literal("window opened"), true);

				return 1;
			});

	}
}
