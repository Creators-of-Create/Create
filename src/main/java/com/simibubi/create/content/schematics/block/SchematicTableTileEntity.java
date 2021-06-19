package com.simibubi.create.content.schematics.block;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicTableTileEntity extends SyncedTileEntity implements ITickableTileEntity, INamedContainerProvider {

	public SchematicTableInventory inventory;
	public boolean isUploading;
	public String uploadingSchematic;
	public float uploadingProgress;
	public boolean sendUpdate;

	public class SchematicTableInventory extends ItemStackHandler {
		public SchematicTableInventory() {
			super(2);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			markDirty();
		}
	}

	public SchematicTableTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		inventory = new SchematicTableInventory();
		uploadingSchematic = null;
		uploadingProgress = 0;
	}

	public void sendToContainer(PacketBuffer buffer) {
		buffer.writeBlockPos(getPos());
		buffer.writeCompoundTag(getUpdateTag());
	}

	@Override
	public void fromTag(BlockState state, CompoundNBT compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		readClientUpdate(state, compound);
		super.fromTag(state, compound);
	}

	@Override
	public void readClientUpdate(BlockState state, CompoundNBT compound) {
		if (compound.contains("Uploading")) {
			isUploading = true;
			uploadingSchematic = compound.getString("Schematic");
			uploadingProgress = compound.getFloat("Progress");
		} else {
			isUploading = false;
			uploadingSchematic = null;
			uploadingProgress = 0;
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Inventory", inventory.serializeNBT());
		writeToClient(compound);
		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		if (isUploading) {
			compound.putBoolean("Uploading", true);
			compound.putString("Schematic", uploadingSchematic);
			compound.putFloat("Progress", uploadingProgress);
		}
		return compound;
	}

	@Override
	public void tick() {
		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 6);
		}
	}
	
	public void startUpload(String schematic) {
		isUploading = true;
		uploadingProgress = 0;
		uploadingSchematic = schematic;
		sendUpdate = true;
		inventory.setStackInSlot(0, ItemStack.EMPTY);
	}
	
	public void finishUpload() {
		isUploading = false;
		uploadingProgress = 0;
		uploadingSchematic = null;
		sendUpdate = true;
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return SchematicTableContainer.create(id, inv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return Lang.translate("gui.schematicTable.title");
	}

}
