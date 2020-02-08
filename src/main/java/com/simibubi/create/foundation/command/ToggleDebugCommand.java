package com.simibubi.create.foundation.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.AllPackets;
import com.simibubi.create.config.AllConfigs;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class ToggleDebugCommand {

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("toggleDebug")
				.requires(cs -> cs.hasPermissionLevel(0))
				.then(Commands.argument("value", BoolArgumentType.bool())
						.executes(ctx -> {
							boolean value = BoolArgumentType.getBool(ctx, "value");
							System.out.println("Command toggleDebug " + value);

							DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> AllConfigs.CLIENT.rainbowDebug.set(value));

							DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () ->
									AllPackets.channel.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) ctx.getSource().getEntity()),
											new ConfigureConfigPacket("rainbowDebug", String.valueOf(value))));

							ctx.getSource().sendFeedback(new StringTextComponent((value ? "enabled" : "disabled") + " rainbow debug"), true);

							return 1;
						}));
	}
}
