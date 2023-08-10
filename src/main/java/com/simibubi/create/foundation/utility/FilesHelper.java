package com.simibubi.create.foundation.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.simibubi.create.Create;

import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.nbt.CompoundTag;

public class FilesHelper {

	public static void createFolderIfMissing(String name) {
		try {
			Files.createDirectories(Paths.get(name));
		} catch (IOException e) {
			Create.LOGGER.warn("Could not create Folder: {}", name);
		}
	}

	public static String findFirstValidFilename(String name, Path folderPath, String extension) {
		int index = 0;
		String filename;
		Path filepath;
		do {
			filename = slug(name) + ((index == 0) ? "" : "_" + index) + "." + extension;
			index++;
			filepath = folderPath.resolve(filename);
		} while (Files.exists(filepath));
		return filename;
	}

	public static String slug(String name) {
		return Lang.asId(name)
			.replaceAll("\\W+", "_");
	}

	public static boolean saveTagCompoundAsJson(CompoundTag compound, String path) {
		try {
			Files.deleteIfExists(Paths.get(path));
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE));
			writer.setIndent("  ");
			Streams.write(JsonParser.parseString(compound.toString()), writer);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean saveTagCompoundAsJsonCompact(CompoundTag compound, String path) {
		try {
			Files.deleteIfExists(Paths.get(path));
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE));
			Streams.write(JsonParser.parseString(compound.toString()), writer);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	private static JsonElement loadJson(InputStream inputStream) {
		try {
			JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
			reader.setLenient(true);
			JsonElement element = Streams.parse(reader);
			reader.close();
			inputStream.close();
			return element;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JsonElement loadJsonResource(String filepath) {
		return loadJson(ClassLoader.getSystemResourceAsStream(filepath));
	}

	public static JsonElement loadJson(String filepath) {
		try {
			return loadJson(Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
