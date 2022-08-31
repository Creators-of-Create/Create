package com.simibubi.create.foundation.command;

import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.createmod.catnip.command.CatnipCommands;
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
				.then(CloneCommand.register())
				.then(GlueCommand.register())

				// utility
				.then(util)
		);

		createRoot.addChild(CatnipCommands.buildRedirect("u", util));

		//add all of Create's commands to /c if it already exists, otherwise create the shortcut
		CatnipCommands.createOrAddToShortcut(dispatcher, "c", createRoot);
	}

	private static LiteralCommandNode<CommandSourceStack> buildUtilityCommands() {

		return Commands.literal("util")
				.then(ReplaceInCommandBlocksCommand.register())
				.then(ClearBufferCacheCommand.register())
				.then(CameraDistanceCommand.register())
				.then(CameraAngleCommand.register())
				//.then(KillTPSCommand.register())
				.build();

	}

}
