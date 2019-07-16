package com.simibubi.create;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.block.SchematicTableContainer;
import com.simibubi.create.block.SchematicTableTileEntity;
import com.simibubi.create.item.ItemBlueprint;
import com.simibubi.create.networking.PacketSchematicUpload.DimensionPos;
import com.simibubi.create.utility.FilesHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class ServerSchematicLoader {

	public static final String PATH = "schematics/uploaded";
	private Map<String, OutputStream> activeDownloads;
	private Map<String, DimensionPos> activeTables;

	public ServerSchematicLoader() {
		activeDownloads = new HashMap<>();
		activeTables = new HashMap<>();
		FilesHelper.createFolderIfMissing("schematics");
		FilesHelper.createFolderIfMissing(PATH);
	}

	public void handleNewUpload(ServerPlayerEntity player, String schematic, DimensionPos dimPos) {
		String playerPath = PATH + "/" + player.getName().getFormattedText();
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;

		FilesHelper.createFolderIfMissing(playerPath);

		if (activeDownloads.containsKey(playerSchematicId))
			return;

		try {
			Files.deleteIfExists(Paths.get(PATH, playerSchematicId));
			OutputStream writer = Files.newOutputStream(Paths.get(PATH, playerSchematicId),
					StandardOpenOption.CREATE_NEW);
			Create.logger.info("Receiving New Schematic: " + playerSchematicId);
			activeDownloads.put(playerSchematicId, writer);
			activeTables.put(playerSchematicId, dimPos);

			SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) dimPos.world
					.getTileEntity(dimPos.pos);
			tileEntity.uploadingSchematic = schematic;
			tileEntity.uploadingProgress = 0;
			BlockState blockState = dimPos.world.getBlockState(dimPos.pos);
			dimPos.world.notifyBlockUpdate(dimPos.pos, blockState, blockState, 6);

			if (player.openContainer instanceof SchematicTableContainer) {
				SchematicTableContainer c = (SchematicTableContainer) player.openContainer;
				c.sendSchematicUpdate = true;
				c.detectAndSendChanges();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleWriteRequest(ServerPlayerEntity player, String schematic, byte[] data) {
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;
		if (activeDownloads.containsKey(playerSchematicId)) {

			try {
				activeDownloads.get(playerSchematicId).write(data);
				Create.logger.info("Writing to Schematic: " + playerSchematicId);
			} catch (IOException e) {
				e.printStackTrace();
				activeDownloads.remove(playerSchematicId);
				activeTables.remove(playerSchematicId);
			}
		}
	}

	public void handleFinishedUpload(ServerPlayerEntity player, String schematic) {
		String playerSchematicId = player.getName().getFormattedText() + "/" + schematic;

		if (activeDownloads.containsKey(playerSchematicId)) {
			try {
				activeDownloads.get(playerSchematicId).close();
				activeDownloads.remove(playerSchematicId);
				Create.logger.info("Finished receiving Schematic: " + playerSchematicId);

				DimensionPos dimpos = activeTables.remove(playerSchematicId);

				if (dimpos == null)
					return;

				BlockState blockState = dimpos.world.getBlockState(dimpos.pos);
				if (!AllBlocks.SCHEMATIC_TABLE.typeOf(blockState))
					return;

				SchematicTableTileEntity tileEntity = (SchematicTableTileEntity) dimpos.world.getTileEntity(dimpos.pos);

				tileEntity.inventory.setStackInSlot(0, ItemStack.EMPTY);
				tileEntity.inventory.setStackInSlot(1,
						ItemBlueprint.create(schematic, player.getName().getFormattedText()));
				tileEntity.uploadingSchematic = null;
				tileEntity.uploadingProgress = 0;
				dimpos.world.notifyBlockUpdate(dimpos.pos, blockState, blockState, 3);
				
				if (player.openContainer instanceof SchematicTableContainer) {
					SchematicTableContainer c = (SchematicTableContainer) player.openContainer;
					c.sendSchematicUpdate = true;
					c.detectAndSendChanges();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
