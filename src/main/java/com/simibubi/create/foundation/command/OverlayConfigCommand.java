package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class OverlayConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("overlay")
				.requires(cs -> cs.hasPermission(0))
				.then(Commands.literal("reset")
					.executes(ctx -> {
						DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SimpleCreateActions.overlayReset(""));

						DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
								CatnipServices.NETWORK.sendToPlayer(
										(Player) ctx.getSource().getEntity(),
										new ClientboundSimpleActionPacket("overlayReset", "")));

					ctx.getSource()
						.sendSuccess(Components.literal("reset overlay offset"), true);

						return Command.SINGLE_SUCCESS;
					})
				)
				.executes(ctx -> {
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SimpleCreateActions.overlayScreen(""));

					DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
							CatnipServices.NETWORK.sendToPlayer(
									(Player) ctx.getSource().getEntity(),
									new ClientboundSimpleActionPacket("overlayScreen", "")));

					ctx.getSource()
							.sendSuccess(Components.literal("window opened"), true);

				return Command.SINGLE_SUCCESS;
			});

	}
}
