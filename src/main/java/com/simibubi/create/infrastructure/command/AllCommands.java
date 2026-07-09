package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.createmod.catnip.impl.command.CatnipCommands;
import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionProviderCheck;

public class AllCommands {
	// Client Commands

	public static void registerClient(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> util = buildClientUtilityCommands();

		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("create")
			.requires(hasPermission(0))
			// general purpose
			.then(ToggleDebugCommand.register())
			.then(FabulousWarningCommand.register())
			.then(OverlayConfigCommand.register())
			.then(FixLightingCommand.register())

			// utility
			.then(util);

		LiteralCommandNode<CommandSourceStack> createRoot = dispatcher.register(root);
		createRoot.addChild(CatnipCommands.buildRedirect("u", util));
		CatnipCommands.createOrAddToShortcut(dispatcher, "c", createRoot);
	}

	private static LiteralCommandNode<CommandSourceStack> buildClientUtilityCommands() {
		return Commands.literal("util")
			.then(ClearBufferCacheCommand.register())
			.then(CameraDistanceCommand.register())
			.then(CameraAngleCommand.register())
			.build();
	}

	// Server Commands

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> util = buildUtilityCommands();

		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("create")
			.requires(hasPermission(0))
			// general purpose
			.then(DumpRailwaysCommand.register())
			.then(DebugInfoCommand.register())
			.then(HighlightCommand.register())
			.then(PassengerCommand.register())
			.then(CouplingCommand.register())
			.then(CloneCommand.register())
			.then(TrainCommand.register())
			.then(GlueCommand.register())

			// utility
			.then(util);

		if (CatnipServices.PLATFORM.isDevelopmentEnvironment() && CatnipServices.PLATFORM.getEnv().isClient())
			root.then(CreateTestCommand.register());

		LiteralCommandNode<CommandSourceStack> createRoot = dispatcher.register(root);
		createRoot.addChild(CatnipCommands.buildRedirect("u", util));
		CatnipCommands.createOrAddToShortcut(dispatcher, "c", createRoot);
	}

	private static LiteralCommandNode<CommandSourceStack> buildUtilityCommands() {
		return Commands.literal("util")
			.then(ReplaceInCommandBlocksCommand.register())
			//.then(DebugValueCommand.register())
			//.then(KillTPSCommand.register())
			//.then(DebugHatsCommand.register())
			.build();

	}

	public static PermissionProviderCheck<CommandSourceStack> hasPermission(int level) {
		return switch (level) {
			case 0 -> Commands.hasPermission(Commands.LEVEL_ALL);
			case 1 -> Commands.hasPermission(Commands.LEVEL_MODERATORS);
			case 2 -> Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
			case 3 -> Commands.hasPermission(Commands.LEVEL_ADMINS);
			default -> Commands.hasPermission(Commands.LEVEL_OWNERS);
		};
	}
}
