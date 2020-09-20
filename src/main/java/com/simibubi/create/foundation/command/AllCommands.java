package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class AllCommands {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("create")
				//general purpose
				.then(ToggleDebugCommand.register())
				.then(OverlayConfigCommand.register())

				//dev-util
				//Comment out for release
				.then(ClearBufferCacheCommand.register())
				.then(ChunkUtilCommand.register())
		//      .then(KillTPSCommand.register())
		);
	}
}
