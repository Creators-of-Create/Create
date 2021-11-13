package com.simibubi.create.lib.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;

public class Configs {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String PATH_TO_CONFIGS = FabricLoader.getInstance().getConfigDir().toString() + "\\create\\";
	static {
		if (!Files.exists(Paths.get(PATH_TO_CONFIGS))) {
			try {
				Files.createDirectory(Paths.get(PATH_TO_CONFIGS));
			} catch (IOException e) {
				LOGGER.fatal("There was an error creating Create config files!", e);
			}
		}
	}
}
