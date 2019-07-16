package com.simibubi.create.block;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicTableTileEntity extends TileEntitySynced implements ITickableTileEntity, INamedContainerProvider {

	public SchematicTableInventory inventory;
	public String uploadingSchematic;
	public float uploadingProgress;
	
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
	
	public SchematicTableTileEntity() {
		this(AllTileEntities.SchematicTable.type);
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
	public void read(CompoundNBT compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		if (compound.contains("Schematic")) {
			uploadingSchematic = compound.getString("Schematic");
			uploadingProgress = compound.getFloat("Progress");
		} else {
			uploadingSchematic = null;
			uploadingProgress = 0;
		}
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Inventory", inventory.serializeNBT());
		if (uploadingSchematic != null) {
			compound.putString("Schematic", uploadingSchematic);
			compound.putFloat("Progress", uploadingProgress);
		}
		return super.write(compound);
	}

	@Override
	public void tick() {
		
	}

	@Override
	public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new SchematicTableContainer(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName().toString());
	}

}
