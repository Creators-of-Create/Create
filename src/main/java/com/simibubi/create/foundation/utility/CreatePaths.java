package com.simibubi.create.foundation.utility;

import java.nio.file.Path;

import net.neoforged.fml.loading.FMLPaths;

public class CreatePaths {
	// These are all absolute, so anything that is resolved via Path#resolve on these paths will also always be absolute
	public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
	public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get();
	public static final Path MODS_DIR = FMLPaths.MODSDIR.get();

	public static final Path SCHEMATICS_DIR = GAME_DIR.resolve("schematics");
	public static final Path UPLOADED_SCHEMATICS_DIR = SCHEMATICS_DIR.resolve("uploaded");

	private CreatePaths() {
	}
}
