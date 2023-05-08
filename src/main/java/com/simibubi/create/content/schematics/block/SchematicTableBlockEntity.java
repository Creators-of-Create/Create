package com.simibubi.create.content.schematics.block;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicTableBlockEntity extends SmartBlockEntity implements MenuProvider, IInteractionChecker {

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

	public SchematicTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inventory = new SchematicTableInventory();
		uploadingSchematic = null;
		uploadingProgress = 0;
	}

	public void sendToMenu(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(getBlockPos());
		buffer.writeNbt(getUpdateTag());
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		super.read(compound, clientPacket);
		
		if (!clientPacket)
			return;
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
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());
		super.write(compound, clientPacket);
		
		if (clientPacket && isUploading) {
			compound.putBoolean("Uploading", true);
			compound.putString("Schematic", uploadingSchematic);
			compound.putFloat("Progress", uploadingProgress);
		}
	}
	
	@Override
	public void tick() {
		// Update Client block entity
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
		return SchematicTableMenu.create(id, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return Lang.translateDirect("gui.schematicTable.title");
	}

	@Override
	public boolean canPlayerUse(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this) {
			return false;
		}
		return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

}
