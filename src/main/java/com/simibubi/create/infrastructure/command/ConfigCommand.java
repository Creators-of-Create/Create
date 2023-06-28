package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.PacketDistributor;

/**
 * Examples:
 * /create config client - to open Create's ConfigGui with the client config already selected
 * /create config "botania:common" - to open Create's ConfigGui with Botania's common config already selected
 * /create config "create:client.client.rainbowDebug" set false - to disable Create's rainbow debug for the sender
 */
public class ConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("config")
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					AllPackets.getChannel().send(
							PacketDistributor.PLAYER.with(() -> player),
							new SConfigureConfigPacket(SConfigureConfigPacket.Actions.configScreen.name(), "")
					);

					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("path", StringArgumentType.string())
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							AllPackets.getChannel().send(
									PacketDistributor.PLAYER.with(() -> player),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.configScreen.name(), StringArgumentType.getString(ctx, "path"))
							);

							return Command.SINGLE_SUCCESS;
						})
						.then(Commands.literal("set")
								.requires(cs -> cs.hasPermission(2))
								.then(Commands.argument("value", StringArgumentType.string())
										.executes(ctx -> {
											String path = StringArgumentType.getString(ctx, "path");
											String value = StringArgumentType.getString(ctx, "value");


											ConfigHelper.ConfigPath configPath;
											try {
												configPath = ConfigHelper.ConfigPath.parse(path);
											} catch (IllegalArgumentException e) {
												ctx.getSource().sendFailure(Components.literal(e.getMessage()));
												return 0;
											}

											if (configPath.getType() == ModConfig.Type.CLIENT) {
												ServerPlayer player = ctx.getSource().getPlayerOrException();
												AllPackets.getChannel().send(
														PacketDistributor.PLAYER.with(() -> player),
														new SConfigureConfigPacket("SET" + path, value)
												);

												return Command.SINGLE_SUCCESS;
											}

											try {
												ConfigHelper.setConfigValue(configPath, value);
												ctx.getSource().sendSuccess(() -> Components.literal("Great Success!"), false);
												return Command.SINGLE_SUCCESS;
											} catch (ConfigHelper.InvalidValueException e) {
												ctx.getSource().sendFailure(Components.literal("Config could not be set the the specified value!"));
												return 0;
											} catch (Exception e) {
												ctx.getSource().sendFailure(Components.literal("Something went wrong while trying to set config value. Check the server logs for more information"));
												Create.LOGGER.warn("Exception during server-side config value set:", e);
												return 0;
											}
										})
								)
						)
				);
	}

}
