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
			player.sendMessage(new StringTextComponent(
					Lang.translate("schematics.uploadTooLarge") + " (" + size / 1000 + " KB)."), player.getUniqueID());
			player.sendMessage(
					new StringTextComponent(Lang.translate("schematics.maxAllowedSize") + " " + maxSize + " KB"), player.getUniqueID());
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

						availableSchematics.add(ITextComponent.of(path.getFileName().toString()));
					});
		} catch (NoSuchFileException e) {
			// No Schematics created yet
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<ITextComponent> getAvailableSchematics() {
		return availableSchematics;
	}

	public Path getPath(String name) {
		return Paths.get("schematics", name + ".nbt");
	}

}
