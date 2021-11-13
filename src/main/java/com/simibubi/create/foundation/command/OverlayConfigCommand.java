package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.api.EnvType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class OverlayConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("overlay")
				.requires(cs -> cs.hasPermission(0))
				.then(Commands.literal("reset")
					.executes(ctx -> {
						DistExecutor.unsafeRunWhenOn(EnvType.CLIENT, () -> () -> SConfigureConfigPacket.Actions.overlayReset.performAction(""));

						DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
								AllPackets.channel.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayer) ctx.getSource().getEntity()),
										new SConfigureConfigPacket(SConfigureConfigPacket.Actions.overlayReset.name(), "")));

					ctx.getSource()
						.sendSuccess(new TextComponent("reset overlay offset"), true);

						return 1;
					})
				)
				.executes(ctx -> {
					DistExecutor.unsafeRunWhenOn(EnvType.CLIENT, () -> () -> SConfigureConfigPacket.Actions.overlayScreen.performAction(""));

					DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
							AllPackets.channel.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayer) ctx.getSource().getEntity()),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.overlayScreen.name(), "")));

					ctx.getSource()
							.sendSuccess(new TextComponent("window opened"), true);

				return 1;
			});

	}
}
