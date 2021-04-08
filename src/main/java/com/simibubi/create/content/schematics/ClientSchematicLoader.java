package com.simibubi.create.content.schematics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicLoader {

	public static final int PACKET_DELAY = 10;

	private List<ITextComponent> availableSchematics;
	private Map<String, InputStream> activeUploads;
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
		Path path = Paths.get("schematics", schematic);

		if (!Files.exists(path)) {
			Create.logger.fatal("Missing Schematic file: " + path.toString());
			return;
		}

		InputStream in;
		try {
			long size = Files.size(path);

			// Too big
			if (!validateSizeLimitation(size))
				return;

			in = Files.newInputStream(path, StandardOpenOption.READ);
			activeUploads.put(schematic, in);
			AllPackets.channel.sendToServer(SchematicUploadPacket.begin(schematic, size));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean validateSizeLimitation(long size) {
		if (Minecraft.getInstance().isSingleplayer())
			return true;
		Integer maxSize = AllConfigs.SERVER.schematics.maxTotalSchematicSize.get();
		if (size > maxSize * 1000) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player != null) {
				player.sendMessage(Lang.translate("schematics.uploadTooLarge").append(" (" + size / 1000 + " KB)."), player.getUniqueID());
				player.sendMessage(Lang.translate("schematics.maxAllowedSize").append(" " + maxSize + " KB"), player.getUniqueID());
			}
			return false;
		}
		return true;
	}

	private void continueUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			Integer maxPacketSize = AllConfigs.SERVER.schematics.maxSchematicPacketSize.get();
			byte[] data = new byte[maxPacketSize];
			try {
				int status = activeUploads.get(schematic).read(data);

				if (status != -1) {
					if (status < maxPacketSize)
						data = Arrays.copyOf(data, status);
					if (Minecraft.getInstance().world != null)
						AllPackets.channel.sendToServer(SchematicUploadPacket.write(schematic, data));
					else {
						activeUploads.remove(schematic);
						return;
					}
				}

				if (status < maxPacketSize)
					finishUpload(schematic);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void finishUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			AllPackets.channel.sendToServer(SchematicUploadPacket.finish(schematic));
			activeUploads.remove(schematic);
		}
	}

	public void refresh() {
		FilesHelper.createFolderIfMissing("schematics");
		availableSchematics.clear();

		try {
			Files.list(Paths.get("schematics/"))
					.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt")).forEach(path -> {
						if (Files.isDirectory(path))
							return;

						availableSchematics.add(new StringTextComponent(path.getFileName().toString()));
					});
		} catch (NoSuchFileException e) {
			// No Schematics created yet
		} catch (IOException e) {
			e.printStackTrace();
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

	public List<ITextComponent> getAvailableSchematics() {
		return availableSchematics;
	}

	public Path getPath(String name) {
		return Paths.get("schematics", name + ".nbt");
	}

}
