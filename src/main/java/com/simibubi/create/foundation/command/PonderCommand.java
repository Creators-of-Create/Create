package com.simibubi.create.foundation.command;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.simibubi.create.foundation.networking.AllPackets;
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

public class PonderCommand {
	public static final SuggestionProvider<CommandSource> ITEM_PONDERS = SuggestionProviders.register(new ResourceLocation("all_ponders"), (iSuggestionProviderCommandContext, builder) -> ISuggestionProvider.suggestResource(PonderRegistry.all.keySet().stream(), builder));

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("ponder")
				.requires(cs -> cs.hasPermission(0))
				.executes(ctx -> openScene("index", ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("scene", ResourceLocationArgument.id())
						.suggests(ITEM_PONDERS)
						.executes(ctx -> openScene(ResourceLocationArgument.getId(ctx, "scene").toString(), ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("targets", EntityArgument.players())
								.requires(cs -> cs.hasPermission(2))
								.executes(ctx -> openScene(ResourceLocationArgument.getId(ctx, "scene").toString(), EntityArgument.getPlayers(ctx, "targets")))
						)
				);

	}

	private static int openScene(String sceneId, ServerPlayerEntity player) {
		return openScene(sceneId, ImmutableList.of(player));
	}

	private static int openScene(String sceneId, Collection<? extends ServerPlayerEntity> players) {
		for (ServerPlayerEntity player : players) {
			if (player instanceof FakePlayer)
				continue;

			AllPackets.channel.send(
					PacketDistributor.PLAYER.with(() -> player),
					new SConfigureConfigPacket(SConfigureConfigPacket.Actions.openPonder.name(), sceneId));
		}
		return Command.SINGLE_SUCCESS;
	}
}
