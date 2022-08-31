package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class FixLightingCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				CatnipServices.NETWORK.sendToPlayer(
						ctx.getSource().getPlayerOrException(),
						new ClientboundSimpleActionPacket("fixLighting", String.valueOf(true)));

				ctx.getSource()
					.sendSuccess(
						new TextComponent("Forge's experimental block rendering pipeline is now enabled."), true);

				return 1;
			});
	}
}
