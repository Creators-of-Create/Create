package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.function.Predicate;

public class AllCommands {

	public static Predicate<CommandSource> sourceIsPlayer = (cs) -> cs.getEntity() instanceof PlayerEntity;

	public static void register(CommandDispatcher<CommandSource> dispatcher) {

		LiteralCommandNode<CommandSource> util = buildUtilityCommands();

		LiteralCommandNode<CommandSource> createRoot = dispatcher.register(Commands.literal("create")
				.requires(cs -> cs.hasPermissionLevel(0))
				//general purpose
				.then(new ToggleExperimentalRenderingCommand().register())
				.then(new ToggleDebugCommand().register())
				.then(OverlayConfigCommand.register())
				.then(FixLightingCommand.register())
				.then(HighlightCommand.register())
				.then(CouplingCommand.register())
				.then(CloneCommand.register())
				.then(PonderCommand.register())

				//utility
				.then(util)
		);

		createRoot.addChild(buildRedirect("u", util));

		CommandNode<CommandSource> c = dispatcher.findNode(Collections.singleton("c"));
		if (c != null)
			return;

		dispatcher.getRoot().addChild(buildRedirect("c", createRoot));

	}


	private static LiteralCommandNode<CommandSource> buildUtilityCommands() {

		return Commands.literal("util")
				.then(ReplaceInCommandBlocksCommand.register())
				.then(ClearBufferCacheCommand.register())
				.then(ChunkUtilCommand.register())
				.then(FlySpeedCommand.register())
				//.then(KillTPSCommand.register())
				.build();

	}

	/**
	 * *****
	 * https://github.com/VelocityPowered/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38
	 * *****
	 * <p>
	 * Returns a literal node that redirects its execution to
	 * the given destination node.
	 *
	 * @param alias       the command alias
	 * @param destination the destination node
	 *
	 * @return the built node
	 */
	public static LiteralCommandNode<CommandSource> buildRedirect(final String alias, final LiteralCommandNode<CommandSource> destination) {
		// Redirects only work for nodes with children, but break the top argument-less command.
		// Manually adding the root command after setting the redirect doesn't fix it.
		// See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
		LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder
				.<CommandSource>literal(alias)
				.requires(destination.getRequirement())
				.forward(
						destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
				.executes(destination.getCommand());
		for (CommandNode<CommandSource> child : destination.getChildren()) {
			builder.then(child);
		}
		return builder.build();
	}
}
