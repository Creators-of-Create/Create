package com.simibubi.create.content.schematics.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.CreatePaths;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicLoader {

	public static final int PACKET_DELAY = 10;

	private final List<Component> availableSchematics;
	private final Map<String, InputStream> activeUploads;
	private int packetCycle;

	public ClientSchematicLoader() {
		availableSchematics = new ArrayList<>();
		activeUploads = new HashMap<>();
		refresh();
	}

	public void tick() {
		if (activeUploads.isEmpty())
			return;
		if (packetCycle-- > 0)
			return;
		packetCycle = PACKET_DELAY;

		for (String schematic : new HashSet<>(activeUploads.keySet())) {
			continueUpload(schematic);
		}
	}

	public void startNewUpload(String schematic) {
		Path path = CreatePaths.SCHEMATICS_DIR.resolve(schematic);

		if (!Files.exists(path)) {
			Create.LOGGER.error("Missing Schematic file: {}", path);
			return;
		}

		InputStream in;
		try {
			long size = Files.size(path);

			// Too big
			if (!validateSizeLimitation(size))
				return;

			// Validate if the file is encoded in a GZIP compatible format
			if (!isGZIPEncoded(path.toFile())) {
				LocalPlayer player = Minecraft.getInstance().player;
				if (player != null)
					player.displayClientMessage(CreateLang.translateDirect("schematics.wrongFormat"), false);
				return;
			}

			in = Files.newInputStream(path, StandardOpenOption.READ);
			activeUploads.put(schematic, in);
			CatnipServices.NETWORK.sendToServer(SchematicUploadPacket.begin(schematic, size));
		} catch (IOException e) {
			Create.LOGGER.error("Encountered an error while starting schematic upload", e);
		}
	}

	public static boolean validateSizeLimitation(long size) {
		if (Minecraft.getInstance().hasSingleplayerServer())
			return true;
		long maxSize = AllConfigs.server().schematics.maxTotalSchematicSize.get();
		if (size > maxSize * 1000) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				player.displayClientMessage(CreateLang.translateDirect("schematics.uploadTooLarge").append(" (" + size / 1000 + " KB)."), false);
				player.displayClientMessage(CreateLang.translateDirect("schematics.maxAllowedSize").append(" " + maxSize + " KB"), false);
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks if a given file is GZIP-encoded by checking its header,
	 * of which the first two bytes should contain the magic number (0x1F, 0x8B)
	 */
	public static boolean isGZIPEncoded(File file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] bytes = new byte[2];
			if (fis.read(bytes) != 2)
				return false;

			int byte1 = bytes[0] & 0xFF;
			int byte2 = bytes[1] & 0xFF;

			return byte1 == 0x1F && byte2 == 0x8B;
		} catch (IOException exception) {
			return false;
		}
	}

	private void continueUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			int maxPacketSize = AllConfigs.server().schematics.maxSchematicPacketSize.get();
			byte[] data = new byte[maxPacketSize];
			try {
				int status = activeUploads.get(schematic).read(data);

				if (status != -1) {
					if (status < maxPacketSize)
						data = Arrays.copyOf(data, status);
					if (Minecraft.getInstance().level != null)
						CatnipServices.NETWORK.sendToServer(SchematicUploadPacket.write(schematic, data));
					else {
						//noinspection resource
						activeUploads.remove(schematic);
						return;
					}
				}

				if (status < maxPacketSize)
					finishUpload(schematic);
			} catch (IOException e) {
				Create.LOGGER.error("Encountered a error while uploading schematic", e);
			}
		}
	}

	private void finishUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			CatnipServices.NETWORK.sendToServer(SchematicUploadPacket.finish(schematic));
			//noinspection resource
			activeUploads.remove(schematic);
		}
	}

	public void refresh() {
		FilesHelper.createFolderIfMissing(CreatePaths.SCHEMATICS_DIR);
		availableSchematics.clear();

		try (Stream<Path> paths = Files.list(CreatePaths.SCHEMATICS_DIR)) {
			paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
				.forEach(path -> {
					if (Files.isDirectory(path))
						return;

					availableSchematics.add(Component.literal(path.getFileName().toString()));
				});
		} catch (NoSuchFileException ignored) {
			// No Schematics created yet
		} catch (IOException e) {
			Create.LOGGER.error("Failed to refresh schematics", e);
		}

		availableSchematics.sort((aT, bT) -> {
			String a = aT.getString();
			String b = bT.getString();
			if (a.endsWith(".nbt"))
				a = a.substring(0, a.length() - 4);
			if (b.endsWith(".nbt"))
				b = b.substring(0, b.length() - 4);
			int aLength = a.length();
			int bLength = b.length();
			int minSize = Math.min(aLength, bLength);
			char aChar, bChar;
			boolean aNumber, bNumber;
			boolean asNumeric = false;
			int lastNumericCompare = 0;
			for (int i = 0; i < minSize; i++) {
				aChar = a.charAt(i);
				bChar = b.charAt(i);
				aNumber = aChar >= '0' && aChar <= '9';
				bNumber = bChar >= '0' && bChar <= '9';
				if (asNumeric)
					if (aNumber && bNumber) {
						if (lastNumericCompare == 0)
							lastNumericCompare = aChar - bChar;
					} else if (aNumber)
						return 1;
					else if (bNumber)
						return -1;
					else if (lastNumericCompare == 0) {
						if (aChar != bChar)
							return aChar - bChar;
						asNumeric = false;
					} else
						return lastNumericCompare;
				else if (aNumber && bNumber) {
					asNumeric = true;
					if (lastNumericCompare == 0)
						lastNumericCompare = aChar - bChar;
				} else if (aChar != bChar)
					return aChar - bChar;
			}
			if (asNumeric)
				if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
					return 1; // a has bigger size, thus b is smaller
				else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
					return -1; // b has bigger size, thus a is smaller
				else if (lastNumericCompare == 0)
					return aLength - bLength;
				else
					return lastNumericCompare;
			else
				return aLength - bLength;
		});
	}

	public List<Component> getAvailableSchematics() {
		return availableSchematics;
	}

}
