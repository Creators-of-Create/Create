package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * Examples:
 * /create config client - to open Create's ConfigGui with the client config already selected
 * /create config "botania:common" - to open Create's ConfigGui with Botania's common config already selected
 * /create config "create:client.client.rainbowDebug" set false - to disable Create's rainbow debug for the sender
 */
public class ConfigCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("config")
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().asPlayer();
					AllPackets.channel.send(
							PacketDistributor.PLAYER.with(() -> player),
							new SConfigureConfigPacket(SConfigureConfigPacket.Actions.configScreen.name(), "")
					);

					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("path", StringArgumentType.string())
						.executes(ctx -> {
							ServerPlayerEntity player = ctx.getSource().asPlayer();
							AllPackets.channel.send(
									PacketDistributor.PLAYER.with(() -> player),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.configScreen.name(), StringArgumentType.getString(ctx, "path"))
							);

							return Command.SINGLE_SUCCESS;
						})
						.then(Commands.literal("set")
								.requires(cs -> cs.hasPermissionLevel(2))
								.then(Commands.argument("value", StringArgumentType.string())
										.executes(ctx -> {
											String path = StringArgumentType.getString(ctx, "path");
											String value = StringArgumentType.getString(ctx, "value");


											ConfigHelper.ConfigPath configPath;
											try {
												configPath = ConfigHelper.ConfigPath.parse(path);
											} catch (IllegalArgumentException e) {
												ctx.getSource().sendErrorMessage(new StringTextComponent(e.getMessage()));
												return 0;
											}

											if (configPath.getType() == ModConfig.Type.CLIENT) {
												ServerPlayerEntity player = ctx.getSource().asPlayer();
												AllPackets.channel.send(
														PacketDistributor.PLAYER.with(() -> player),
														new SConfigureConfigPacket("SET" + path, value)
												);

												return Command.SINGLE_SUCCESS;
											}

											try {
												ConfigHelper.setConfigValue(configPath, value);
												ctx.getSource().sendFeedback(new StringTextComponent("Great Success!"), false);
												return Command.SINGLE_SUCCESS;
											} catch (ConfigHelper.InvalidValueException e) {
												ctx.getSource().sendErrorMessage(new StringTextComponent("Config could not be set the the specified value!"));
												return 0;
											} catch (Exception e) {
												ctx.getSource().sendErrorMessage(new StringTextComponent("Something went wrong while trying to set config value. Check the server logs for more information"));
												Create.LOGGER.warn("Exception during server-side config value set:", e);
												return 0;
											}
										})
								)
						)
				);
	}

}
