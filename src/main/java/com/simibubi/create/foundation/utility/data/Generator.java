package com.simibubi.create.foundation.utility.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.simibubi.create.AllSoundEvents;

import net.minecraft.data.DirectoryCache;

public class Generator {

	/*
	 * this can probably be called by some gradle task or so but im not know how, so for now i just added a main below and execute from there when we need to generate jsons
	 **/
	public static void generateJsonFiles(){
		Path base = Paths.get("src/main/resources");
		DirectoryCache cache;
		try {

			cache = new DirectoryCache(base, "cache");

			for (ICanGenerateJson gen:
					new ICanGenerateJson[]{AllSoundEvents.CUCKOO_CREEPER}) {
				gen.generate(base, cache);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		generateJsonFiles();
	}
}
