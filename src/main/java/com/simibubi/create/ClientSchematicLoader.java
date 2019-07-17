package com.simibubi.create;

import static com.simibubi.create.ServerSchematicLoader.MAX_PACKET_SIZE;

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

import com.simibubi.create.networking.PacketSchematicUpload;
import com.simibubi.create.networking.Packets;
import com.simibubi.create.utility.FilesHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicLoader {

	public static final int PACKET_DELAY = 10;

	private List<String> availableSchematics;
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
			if (size > ServerSchematicLoader.MAX_SCHEMATIC_FILE_SIZE) {
				Minecraft.getInstance().player
						.sendMessage(new StringTextComponent("Your schematic is too large (" + size / 1024 + " KB)."));
				Minecraft.getInstance().player
						.sendMessage(new StringTextComponent("The maximum allowed schematic file size is: "
								+ ServerSchematicLoader.MAX_SCHEMATIC_FILE_SIZE / 1024 + " KB"));
				return;
			}

			in = Files.newInputStream(path, StandardOpenOption.READ);
			activeUploads.put(schematic, in);
			Packets.channel.sendToServer(PacketSchematicUpload.begin(schematic, size));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void continueUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			byte[] data = new byte[MAX_PACKET_SIZE];
			try {
				int status = activeUploads.get(schematic).read(data);
				if (status < MAX_PACKET_SIZE) {
					data = Arrays.copyOf(data, status);
				}

				if (Minecraft.getInstance().world != null)
					Packets.channel.sendToServer(PacketSchematicUpload.write(schematic, data));
				else {
					activeUploads.remove(schematic);
					return;
				}

				if (status < MAX_PACKET_SIZE)
					finishUpload(schematic);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void finishUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			Packets.channel.sendToServer(PacketSchematicUpload.finish(schematic));
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

						availableSchematics.add(path.getFileName().toString());
					});
		} catch (NoSuchFileException e) {
			// No Schematics created yet
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<String> getAvailableSchematics() {
		return availableSchematics;
	}

	public Path getPath(String name) {
		return Paths.get("schematics", name + ".nbt");
	}

}
