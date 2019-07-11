package com.simibubi.create;

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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicLoader {

	public static final int PACKET_DELAY = 10;
	public static final int PACKET_SIZE = 500;
	
	private List<String> availableSchematics;
	private Map<String, InputStream> activeUploads;
	private Map<String, ReadProgress> progress;
	private int packetCycle;
	
	public ClientSchematicLoader() {
		availableSchematics = new ArrayList<>();
		activeUploads = new HashMap<>();
		progress = new HashMap<>();
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
	
	public float getProgress(String schematic) {
		if (progress.containsKey(schematic)) {
			return progress.get(schematic).getProgress();
		}
		
		return 0;
	}

	public void startNewUpload(String schematic) {
		Path path = Paths.get("schematics", schematic);

		if (!Files.exists(path)) {
			Create.logger.fatal("Missing Schematic file: " + path.toString());
			return;
		}

		InputStream in;
		try {
			in = Files.newInputStream(path, StandardOpenOption.READ);
			activeUploads.put(schematic, in);
			ReadProgress tracker = new ReadProgress();
			tracker.length = Files.size(path);
			progress.put(schematic, tracker);
			Packets.channel.sendToServer(PacketSchematicUpload.begin(schematic));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void continueUpload(String schematic) {
		if (activeUploads.containsKey(schematic)) {
			byte[] data = new byte[PACKET_SIZE];
			try {
				int status = activeUploads.get(schematic).read(data);
				
				progress.get(schematic).progress += status;
				
				if (status < PACKET_SIZE) {
					data = Arrays.copyOf(data, status);
				}
				
				Packets.channel.sendToServer(PacketSchematicUpload.write(schematic, data));
				
				if (status < PACKET_SIZE)
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
					.forEach(path -> {
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
	
	public static class ReadProgress {
		public long length;
		public long progress;
		public float getProgress() {
			return (float) (progress * 1d / length);
		}
	}

}
