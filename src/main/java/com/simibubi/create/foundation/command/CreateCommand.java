package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CreateCommand {

	public CreateCommand(CommandDispatcher<CommandSource> dispatcher) {
		// KillTPSCommand.register(dispatcher); Commented out for release
		addCreateCommand(dispatcher, ToggleDebugCommand.register());
		addCreateCommand(dispatcher, ClearBufferCacheCommand.register());
	}

	public void addCreateCommand(CommandDispatcher<CommandSource> dispatcher,
			ArgumentBuilder<CommandSource, ?> register) {
		dispatcher.register(Commands.literal("create").then(register));
	}
}
