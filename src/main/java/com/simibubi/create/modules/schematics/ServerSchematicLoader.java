package com.simibubi.create.modules.schematics;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.type.DimensionPos;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.modules.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.modules.schematics.item.SchematicItem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class ServerSchematicLoader {

	public static final String PATH = "schematics/uploaded";
	public static final int IDLE_TIMEOUT = 600;
	public static final int MAX_PACKET_SIZE = 1024;
	public static final int MAX_SCHEMATICS_PER_PLAYER = 10;
	public static final int MAX_SCHEMATIC_FILE_SIZE = 256 * 1024; // 256 kiB

	private Map<String, SchematicUploadEntry> activeUploads;

	public class SchematicUploadEntry {
		public OutputStream stream;
		public long bytesUploaded;
		public long totalBytes;
		public DimensionPos tablePos;
		public int idleTime;

		public SchematicUploadEntry(OutputStream stream, long totalBytes, DimensionPos tablePos) {
			this.stream = stream;
			this.totalBytes = totalBytes;
			this.tablePos = tablePos;
			this.bytesUploaded = 0;
			this.idleTime = 0;
		}
	}

	public ServerSchematicLoader() {
		activeUploads = new HashMap<>();
		FilesHelper.createFolderIfMissing("schematics");
		FilesHelper.createFolderIfMissing(PATH);
	}

	public void tick() {
		// Detect Timed out Uploads
		Set<String> deadEntries = new HashSet<>();
		for (String upload : activeUploads.keySet()) {
			SchematicUploadEntry entry = activeUploads.get(upload);

			if (entry.idleTime++ > IDLE_TIMEOUT) {
				Create.logger.warn("Schematic Upload timed out: " + upload);
				deadEntries.add(upload);
			}

		}

		// Remove Timed out Uploads
		deadEntries.forEach(this::cancelUpload);
	}

	public void shutdown() {
		// Close open streams
		new HashSet<>(activeUploads.keySet()).forEach(this::cancelUpload);
	}

	public void handleNewUpload(ServerPlayerEntity player, String schematic, long size, DimensionPos dimPos) {
		String playerPath = PATH + "/" + player.getName().getFormattedText();
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;
		FilesHelper.createFolderIfMissing(playerPath);

		// Unsupported Format
		if (!schematic.endsWith(".nbt")) {
			Create.logger.warn("Attempted Schematic Upload with non-supported Format: " + playerSchematicId);
		}

		// Too big
		if (size > MAX_SCHEMATIC_FILE_SIZE) {
			player.sendMessage(new StringTextComponent("Your schematic is too large (" + size/1024 + " KB)."));
			player.sendMessage(new StringTextComponent(
					"The maximum allowed schematic file size is: " + MAX_SCHEMATIC_FILE_SIZE/1024 + " KB"));
			return;
		}

		// Skip existing Uploads
		if (activeUploads.containsKey(playerSchematicId))
			return;

		try {
			// Validate Referenced Block
			BlockState blockState = dimPos.world.getBlockState(dimPos.pos);
			if (!AllBlocks.SCHEMATIC_TABLE.typeOf(blockState))
				return;

			// Delete schematic with same name
			Files.deleteIfExists(Paths.get(PATH, playerSchematicId));

			// Too many Schematics
			Stream<Path> list = Files.list(Paths.get(playerPath));
			if (list.count() >= MAX_SCHEMATICS_PER_PLAYER) {
				Stream<Path> list2 = Files.list(Paths.get(playerPath));
				Optional<Path> lastFilePath = list2.filter(f -> !Files.isDirectory(f))
						.min(Comparator.comparingLong(f -> f.toFile().lastModified()));
				list2.close();
				if (lastFilePath.isPresent()) {
					Files.deleteIfExists(lastFilePath.get());
				}
			}
			list.close();

			// Open Stream
			OutputStream writer = Files.newOutputStream(Paths.get(PATH, playerSchematicId),
					StandardOpenOption.CREATE_NEW);
			activeUploads.put(playerSchematicId, new SchematicUploadEntry(writer, size, dimPos));

			// Notify Tile Entity
			SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) dimPos.world.getTileEntity(dimPos.pos);
			tileEntity.startUpload(schematic);

		} catch (IOException e) {
			Create.logger.error("Exception Thrown when starting Upload: " + playerSchematicId);
			e.printStackTrace();
		}
	}

	public void handleWriteRequest(ServerPlayerEntity player, String schematic, byte[] data) {
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;

		if (activeUploads.containsKey(playerSchematicId)) {
			SchematicUploadEntry entry = activeUploads.get(playerSchematicId);
			entry.bytesUploaded += data.length;

			// Size Validations
			if (data.length > MAX_PACKET_SIZE) {
				Create.logger.warn("Oversized Upload Packet received: " + playerSchematicId);
				cancelUpload(playerSchematicId);
				return;
			}

			if (entry.bytesUploaded > entry.totalBytes) {
				Create.logger.warn("Received more data than Expected: " + playerSchematicId);
				cancelUpload(playerSchematicId);
				return;
			}

			try {
				entry.stream.write(data);
				entry.idleTime = 0;
				BlockState blockState = entry.tablePos.world.getBlockState(entry.tablePos.pos);
				if (!AllBlocks.SCHEMATIC_TABLE.typeOf(blockState))
					return;

				SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) entry.tablePos.world
						.getTileEntity(entry.tablePos.pos);
				tileEntity.uploadingProgress = (float) ((double) entry.bytesUploaded / entry.totalBytes);
				tileEntity.sendUpdate = true;

			} catch (IOException e) {
				Create.logger.error("Exception Thrown when uploading Schematic: " + playerSchematicId);
				e.printStackTrace();
				cancelUpload(playerSchematicId);
			}
		}
	}

	protected void cancelUpload(String playerSchematicId) {
		if (!activeUploads.containsKey(playerSchematicId))
			return;

		SchematicUploadEntry entry = activeUploads.remove(playerSchematicId);
		try {
			entry.stream.close();
			Files.deleteIfExists(Paths.get(PATH, playerSchematicId));
			Create.logger.warn("Cancelled Schematic Upload: " + playerSchematicId);

		} catch (IOException e) {
			Create.logger.error("Exception Thrown when cancelling Upload: " + playerSchematicId);
			e.printStackTrace();
		}

		DimensionPos dimpos = entry.tablePos;
		if (dimpos == null)
			return;
		
		BlockState blockState = dimpos.world.getBlockState(dimpos.pos);
		if (!AllBlocks.SCHEMATIC_TABLE.typeOf(blockState))
			return;
		
		SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) dimpos.world.getTileEntity(dimpos.pos);
		tileEntity.finishUpload();
	}

	public void handleFinishedUpload(ServerPlayerEntity player, String schematic) {
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;

		if (activeUploads.containsKey(playerSchematicId)) {
			try {
				activeUploads.get(playerSchematicId).stream.close();
				DimensionPos dimpos = activeUploads.remove(playerSchematicId).tablePos;
				Create.logger.info("New Schematic Uploaded: " + playerSchematicId);

				if (dimpos == null)
					return;

				BlockState blockState = dimpos.world.getBlockState(dimpos.pos);
				if (!AllBlocks.SCHEMATIC_TABLE.typeOf(blockState))
					return;

				SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) dimpos.world.getTileEntity(dimpos.pos);
				tileEntity.finishUpload();
				tileEntity.inventory.setStackInSlot(0, ItemStack.EMPTY);
				tileEntity.inventory.setStackInSlot(1,
						SchematicItem.create(schematic, player.getName().getFormattedText()));

			} catch (IOException e) {
				Create.logger.error("Exception Thrown when finishing Upload: " + playerSchematicId);
				e.printStackTrace();
			}
		}

	}
}
