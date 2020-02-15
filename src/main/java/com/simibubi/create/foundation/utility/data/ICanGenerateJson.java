package com.simibubi.create.foundation.utility.data;

import net.minecraft.data.DirectoryCache;

import java.nio.file.Path;

public interface ICanGenerateJson {

	//path points to the resource1s base folder
	void generate(Path path, DirectoryCache cache);
}
