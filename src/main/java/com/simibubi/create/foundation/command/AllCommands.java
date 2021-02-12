package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
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

		LiteralCommandNode<CommandSource> createRoot = dispatcher.register(Commands.literal("create")
						//general purpose
						.then(ToggleDebugCommand.register())
						.then(OverlayConfigCommand.register())
						.then(FixLightingCommand.register())
						.then(ReplaceInCommandBlocksCommand.register())
						.then(HighlightCommand.register())

						//dev-util
						//Comment out for release
						.then(ClearBufferCacheCommand.register())
						.then(ChunkUtilCommand.register())
						//.then(KillTPSCommand.register())
		);

		CommandNode<CommandSource> c = dispatcher.findNode(Collections.singleton("c"));
		if (c != null)
			return;

		dispatcher.register(Commands.literal("c")
				.redirect(createRoot)
		);
	}
}
