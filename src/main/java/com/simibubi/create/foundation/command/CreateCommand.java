package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;

public class CreateCommand {

	public CreateCommand(CommandDispatcher<CommandSource> dispatcher){

		KillTPSCommand.register(dispatcher);
	}
}
