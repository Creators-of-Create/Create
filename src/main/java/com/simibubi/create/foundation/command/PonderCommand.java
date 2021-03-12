package com.simibubi.create.foundation.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.PonderPacket;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collection;

public class PonderCommand {
	public static final SuggestionProvider<CommandSource> ALL_PONDERS = SuggestionProviders.register(new ResourceLocation("all_ponders"), (iSuggestionProviderCommandContext, builder) -> ISuggestionProvider.func_212476_a(PonderRegistry.all.keySet().stream(), builder));

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("ponder")
				.requires(cs -> cs.hasPermissionLevel(0))
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().asPlayer();

					AllPackets.channel.send(
							PacketDistributor.PLAYER.with(() -> player),
							new ConfigureConfigPacket(ConfigureConfigPacket.Actions.ponderIndex.name(), ""));

					return 1;
				})
			.then(Commands.argument("scene", ResourceLocationArgument.resourceLocation()).suggests(ALL_PONDERS)
			.executes(context -> openScene(ResourceLocationArgument.getResourceLocation(context, "scene"), ImmutableList.of(context.getSource().asPlayer())))
			.then(Commands.argument("targets", EntityArgument.players())
				.requires(cs -> cs.hasPermissionLevel(2))
				.executes(context -> openScene(ResourceLocationArgument.getResourceLocation(context, "scene"), EntityArgument.getPlayers(context, "targets")))));

	}

	private static int openScene(ResourceLocation scene, Collection<? extends ServerPlayerEntity> players) {
		for (ServerPlayerEntity player : players) {
			if (player instanceof FakePlayer)
				continue;
			AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new PonderPacket(scene));
		}
		return 1;
	}
}
