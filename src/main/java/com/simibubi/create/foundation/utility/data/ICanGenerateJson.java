package com.simibubi.create.foundation.utility.data;

import java.nio.file.Path;

import net.minecraft.data.DirectoryCache;

public interface ICanGenerateJson {

	//path points to the resource1s base folder
	void generate(Path path, DirectoryCache cache);
}
