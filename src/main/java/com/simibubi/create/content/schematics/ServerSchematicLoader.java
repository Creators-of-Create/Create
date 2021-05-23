package com.simibubi.create.content.schematics;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CSchematics;
import com.simibubi.create.foundation.utility.FilesHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

public class ServerSchematicLoader {

	private Map<String, SchematicUploadEntry> activeUploads;

	public class SchematicUploadEntry {
		public World world;
		public BlockPos tablePos;
		public OutputStream stream;
		public long bytesUploaded;
		public long totalBytes;
		public int idleTime;

		public SchematicUploadEntry(OutputStream stream, long totalBytes, World world, BlockPos tablePos) {
			this.stream = stream;
			this.totalBytes = totalBytes;
			this.tablePos = tablePos;
			this.world = world;
			this.bytesUploaded = 0;
			this.idleTime = 0;
		}
	}

	public ServerSchematicLoader() {
		activeUploads = new HashMap<>();
	}

	public String getSchematicPath() {
		return "schematics/uploaded";
	}

	public void tick() {
		// Detect Timed out Uploads
		Set<String> deadEntries = new HashSet<>();
		for (String upload : activeUploads.keySet()) {
			SchematicUploadEntry entry = activeUploads.get(upload);

			if (entry.idleTime++ > getConfig().schematicIdleTimeout.get()) {
				Create.LOGGER.warn("Schematic Upload timed out: " + upload);
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

	public void handleNewUpload(ServerPlayerEntity player, String schematic, long size, BlockPos pos) {
		String playerPath = getSchematicPath() + "/" + player.getGameProfile().getName();
		String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;
		FilesHelper.createFolderIfMissing(playerPath);

		// Unsupported Format
		if (!schematic.endsWith(".nbt")) {
			Create.LOGGER.warn("Attempted Schematic Upload with non-supported Format: " + playerSchematicId);
			return;
		}

		Path playerSchematicsPath = Paths.get(getSchematicPath(), player.getGameProfile().getName()).toAbsolutePath();

		Path uploadPath = playerSchematicsPath.resolve(schematic).normalize();
		if (!uploadPath.startsWith(playerSchematicsPath)) {
			Create.LOGGER.warn("Attempted Schematic Upload with directory escape: {}", playerSchematicId);
			return;
		}

		// Too big
		if (!validateSchematicSizeOnServer(player, size))
			return;

		// Skip existing Uploads
		if (activeUploads.containsKey(playerSchematicId))
			return;

		try {
			// Validate Referenced Block
			SchematicTableTileEntity table = getTable(player.getEntityWorld(), pos);
			if (table == null)
				return;

			// Delete schematic with same name
			Files.deleteIfExists(uploadPath);

			// Too many Schematics
			long count;
			try (Stream<Path> list = Files.list(Paths.get(playerPath))) {
				count = list.count();
			}

			if (count >= getConfig().maxSchematics.get()) {
				Stream<Path> list2 = Files.list(Paths.get(playerPath));
				Optional<Path> lastFilePath = list2.filter(f -> !Files.isDirectory(f))
					.min(Comparator.comparingLong(f -> f.toFile()
						.lastModified()));
				list2.close();
				if (lastFilePath.isPresent()) {
					Files.deleteIfExists(lastFilePath.get());
				}
			}

			// Open Stream
			OutputStream writer = Files.newOutputStream(uploadPath);
			activeUploads.put(playerSchematicId, new SchematicUploadEntry(writer, size, player.getServerWorld(), pos));

			// Notify Tile Entity
			table.startUpload(schematic);

		} catch (IOException e) {
			Create.LOGGER.error("Exception Thrown when starting Upload: " + playerSchematicId);
			e.printStackTrace();
		}
	}

	protected boolean validateSchematicSizeOnServer(ServerPlayerEntity player, long size) {
		Integer maxFileSize = getConfig().maxTotalSchematicSize.get();
		if (size > maxFileSize * 1000) {
			player.sendMessage(new TranslationTextComponent("create.schematics.uploadTooLarge")
				.append(new StringTextComponent(" (" + size / 1000 + " KB).")), player.getUniqueID());
			player.sendMessage(new TranslationTextComponent("create.schematics.maxAllowedSize")
				.append(new StringTextComponent(" " + maxFileSize + " KB")), player.getUniqueID());
			return false;
		}
		return true;
	}

	public CSchematics getConfig() {
		return AllConfigs.SERVER.schematics;
	}

	public void handleWriteRequest(ServerPlayerEntity player, String schematic, byte[] data) {
		String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;

		if (activeUploads.containsKey(playerSchematicId)) {
			SchematicUploadEntry entry = activeUploads.get(playerSchematicId);
			entry.bytesUploaded += data.length;

			// Size Validations
			if (data.length > getConfig().maxSchematicPacketSize.get()) {
				Create.LOGGER.warn("Oversized Upload Packet received: " + playerSchematicId);
				cancelUpload(playerSchematicId);
				return;
			}

			if (entry.bytesUploaded > entry.totalBytes) {
				Create.LOGGER.warn("Received more data than Expected: " + playerSchematicId);
				cancelUpload(playerSchematicId);
				return;
			}

			try {
				entry.stream.write(data);
				entry.idleTime = 0;

				SchematicTableTileEntity table = getTable(entry.world, entry.tablePos);
				if (table == null)
					return;
				table.uploadingProgress = (float) ((double) entry.bytesUploaded / entry.totalBytes);
				table.sendUpdate = true;

			} catch (IOException e) {
				Create.LOGGER.error("Exception Thrown when uploading Schematic: " + playerSchematicId);
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
			Files.deleteIfExists(Paths.get(getSchematicPath(), playerSchematicId));
			Create.LOGGER.warn("Cancelled Schematic Upload: " + playerSchematicId);

		} catch (IOException e) {
			Create.LOGGER.error("Exception Thrown when cancelling Upload: " + playerSchematicId);
			e.printStackTrace();
		}

		BlockPos pos = entry.tablePos;
		if (pos == null)
			return;

		SchematicTableTileEntity table = getTable(entry.world, pos);
		if (table != null)
			table.finishUpload();
	}

	public SchematicTableTileEntity getTable(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof SchematicTableTileEntity))
			return null;
		SchematicTableTileEntity table = (SchematicTableTileEntity) te;
		return table;
	}

	public void handleFinishedUpload(ServerPlayerEntity player, String schematic) {
		String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;

		if (activeUploads.containsKey(playerSchematicId)) {
			try {
				activeUploads.get(playerSchematicId).stream.close();
				SchematicUploadEntry removed = activeUploads.remove(playerSchematicId);
				World world = removed.world;
				BlockPos pos = removed.tablePos;

				Create.LOGGER.info("New Schematic Uploaded: " + playerSchematicId);
				if (pos == null)
					return;

				BlockState blockState = world.getBlockState(pos);
				if (AllBlocks.SCHEMATIC_TABLE.get() != blockState.getBlock())
					return;

				SchematicTableTileEntity table = getTable(world, pos);
				if (table == null)
					return;
				table.finishUpload();
				table.inventory.setStackInSlot(1, SchematicItem.create(schematic, player.getGameProfile().getName()));

			} catch (IOException e) {
				Create.LOGGER.error("Exception Thrown when finishing Upload: " + playerSchematicId);
				e.printStackTrace();
			}
		}
	}

	public void handleInstantSchematic(ServerPlayerEntity player, String schematic, World world, BlockPos pos,
		BlockPos bounds) {
		String playerPath = getSchematicPath() + "/" + player.getGameProfile().getName();
		String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;
		FilesHelper.createFolderIfMissing(playerPath);

		// Unsupported Format
		if (!schematic.endsWith(".nbt")) {
			Create.LOGGER.warn("Attempted Schematic Upload with non-supported Format: {}", playerSchematicId);
			return;
		}

		Path schematicPath = Paths.get(getSchematicPath()).toAbsolutePath();

		Path path = schematicPath.resolve(playerSchematicId).normalize();
		if (!path.startsWith(schematicPath)) {
			Create.LOGGER.warn("Attempted Schematic Upload with directory escape: {}", playerSchematicId);
			return;
		}

		// Not holding S&Q
		if (!AllItems.SCHEMATIC_AND_QUILL.isIn(player.getHeldItemMainhand()))
			return;

		try {
			// Delete schematic with same name
			Files.deleteIfExists(path);

			// Too many Schematics
			long count;
			try (Stream<Path> list = Files.list(Paths.get(playerPath))) {
				count = list.count();
			}

			if (count >= getConfig().maxSchematics.get()) {
				Stream<Path> list2 = Files.list(Paths.get(playerPath));
				Optional<Path> lastFilePath = list2.filter(f -> !Files.isDirectory(f))
					.min(Comparator.comparingLong(f -> f.toFile()
						.lastModified()));
				list2.close();
				if (lastFilePath.isPresent())
					Files.deleteIfExists(lastFilePath.get());
			}

			Template t = new Template();
			t.takeBlocksFromWorld(world, pos, bounds, true, Blocks.AIR);

			try (OutputStream outputStream = Files.newOutputStream(path)) {
				CompoundNBT nbttagcompound = t.writeToNBT(new CompoundNBT());
				CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
				player.setHeldItem(Hand.MAIN_HAND, SchematicItem.create(schematic, player.getGameProfile().getName()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			Create.LOGGER.error("Exception Thrown in direct Schematic Upload: " + playerSchematicId);
			e.printStackTrace();
		}
	}

}
