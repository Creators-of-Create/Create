package com.simibubi.create.foundation.command;

import java.util.Collections;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class AllCommands {

	public static final Predicate<CommandSourceStack> SOURCE_IS_PLAYER = cs -> cs.getEntity() instanceof Player;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

		LiteralCommandNode<CommandSourceStack> util = buildUtilityCommands();

		LiteralCommandNode<CommandSourceStack> createRoot = dispatcher.register(Commands.literal("create")
				.requires(cs -> cs.hasPermission(0))
				// general purpose
				.then(new ToggleDebugCommand().register())
				.then(FabulousWarningCommand.register())
				.then(OverlayConfigCommand.register())
				.then(DumpRailwaysCommand.register())
				.then(FixLightingCommand.register())
				.then(HighlightCommand.register())
				.then(KillTrainCommand.register())
				.then(PassengerCommand.register())
				.then(CouplingCommand.register())
				.then(ConfigCommand.register())
				.then(PonderCommand.register())
				.then(CloneCommand.register())
				.then(GlueCommand.register())

				// utility
				.then(util)
		);

		createRoot.addChild(buildRedirect("u", util));

		CommandNode<CommandSourceStack> c = dispatcher.findNode(Collections.singleton("c"));
		if (c != null)
			return;

		dispatcher.getRoot()
			.addChild(buildRedirect("c", createRoot));

	}

	private static LiteralCommandNode<CommandSourceStack> buildUtilityCommands() {

		return Commands.literal("util")
				.then(ReplaceInCommandBlocksCommand.register())
				.then(ClearBufferCacheCommand.register())
				.then(CameraDistanceCommand.register())
				.then(CameraAngleCommand.register())
				.then(FlySpeedCommand.register())
				.then(KillTPSCommand.register())
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
	public static LiteralCommandNode<CommandSourceStack> buildRedirect(final String alias, final LiteralCommandNode<CommandSourceStack> destination) {
		// Redirects only work for nodes with children, but break the top argument-less command.
		// Manually adding the root command after setting the redirect doesn't fix it.
		// See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
		LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder
				.<CommandSourceStack>literal(alias)
				.requires(destination.getRequirement())
				.forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
				.executes(destination.getCommand());
		for (CommandNode<CommandSourceStack> child : destination.getChildren()) {
			builder.then(child);
		}
		return builder.build();
	}

}
