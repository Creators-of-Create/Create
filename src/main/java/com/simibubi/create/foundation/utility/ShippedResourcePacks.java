package com.simibubi.create.foundation.utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ShippedResourcePacks {

	public static void extractFiles(String... packs) {
		FilesHelper.createFolderIfMissing("resourcepacks");

		for (String name : packs) {
			InputStream folderInJar = ShippedResourcePacks.class.getResourceAsStream("/opt_in/" + name + ".zip");

			try {
				Files.copy(folderInJar, Paths.get("resourcepacks/" + name + ".zip"));
			} catch (FileAlreadyExistsException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				folderInJar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
