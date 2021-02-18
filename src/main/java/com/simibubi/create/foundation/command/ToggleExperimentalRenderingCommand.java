package com.simibubi.create.foundation.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class ToggleExperimentalRenderingCommand {

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("experimentalRendering")
				.requires(cs -> cs.hasPermissionLevel(0))
				.then(Commands.argument("value", BoolArgumentType.bool())
						.executes(ctx -> {
							boolean value = BoolArgumentType.getBool(ctx, "value");
							//DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AllConfigs.CLIENT.rainbowDebug.set(value));
							DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConfigureConfigPacket.Actions.experimentalRendering.performAction(String.valueOf(value)));

							DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
									AllPackets.channel.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) ctx.getSource().getEntity()),
											new ConfigureConfigPacket(ConfigureConfigPacket.Actions.experimentalRendering.name(), String.valueOf(value))));

							ctx.getSource().sendFeedback(new StringTextComponent((value ? "enabled" : "disabled") + " experimental rendering"), true);

							return 1;
						}));
	}
}
