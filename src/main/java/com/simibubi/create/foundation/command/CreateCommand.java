package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CreateCommand {

	public CreateCommand(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("create")
				.then(ToggleDebugCommand.register())
				.then(ClearBufferCacheCommand.register())
				//.then(KillTPSCommand.register()) //Commented out for release
				);
	}
}
