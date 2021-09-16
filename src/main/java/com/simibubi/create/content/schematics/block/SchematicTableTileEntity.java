package com.simibubi.create.content.schematics.block;

import com.simibubi.create.foundation.gui.IInteractionChecker;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicTableTileEntity extends SyncedTileEntity implements BlockEntityTicker, MenuProvider, IInteractionChecker {

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
			setChanged();
		}
	}

	public SchematicTableTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		inventory = new SchematicTableInventory();
		uploadingSchematic = null;
		uploadingProgress = 0;
	}

	public void sendToContainer(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(getBlockPos());
		buffer.writeNbt(getUpdateTag());
	}

	@Override
	public void load(CompoundTag compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		readClientUpdate(getBlockState(), compound);
		super.load(compound);
	}

	@Override
	public void readClientUpdate(BlockState state, CompoundTag compound) {
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
	public CompoundTag save(CompoundTag compound) {
		compound.put("Inventory", inventory.serializeNBT());
		writeToClient(compound);
		return super.save(compound);
	}

	@Override
	public CompoundTag writeToClient(CompoundTag compound) {
		if (isUploading) {
			compound.putBoolean("Uploading", true);
			compound.putString("Schematic", uploadingSchematic);
			compound.putFloat("Progress", uploadingProgress);
		}
		return compound;
	}

	@Override
	public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_, BlockEntity p_155256_) {
		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
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
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return SchematicTableContainer.create(id, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return Lang.translate("gui.schematicTable.title");
	}

	@Override
	public boolean canPlayerUse(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this) {
			return false;
		}
		return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
	}

}
