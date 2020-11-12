package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class FixLightingCommand {

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermissionLevel(0))
			.executes(ctx -> {
				AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) ctx.getSource()
					.getEntity()),
					new ConfigureConfigPacket(ConfigureConfigPacket.Actions.fixLighting.name(), String.valueOf(true)));

				ctx.getSource()
					.sendFeedback(
						new StringTextComponent("Forge's experimental block rendering pipeline is now enabled."), true);

				return 1;
			});
	}
}
