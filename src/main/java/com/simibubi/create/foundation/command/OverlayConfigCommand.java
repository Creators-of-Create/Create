package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
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
						.sendSuccess(new TextComponent("reset overlay offset"), true);

						return 1;
					})
				)
				.executes(ctx -> {
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SimpleCreateActions.overlayScreen(""));

					DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
							CatnipServices.NETWORK.sendToPlayer(
									(Player) ctx.getSource().getEntity(),
									new ClientboundSimpleActionPacket("overlayScreen", "")));

					ctx.getSource()
							.sendSuccess(new TextComponent("window opened"), true);

				return Command.SINGLE_SUCCESS;
			});

	}
}
