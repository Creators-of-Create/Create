package com.simibubi.create.modules.economy;

import java.util.UUID;

import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ShopShelfTileEntity extends SyncedTileEntity implements INamedContainerProvider {

	private UUID owner;

	public ShopShelfTileEntity() {
		super(null);
//		super(AllTileEntities.SHOP_SHELF.type);
	}

	@Override
	public void read(CompoundNBT compound) {
		if (compound.contains("Owner"))
			setOwner(NBTUtil.readUniqueId(compound.getCompound("Owner")));
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (getOwner() != null)
			compound.put("Owner", NBTUtil.writeUniqueId(getOwner()));
		return super.write(compound);
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}
	
	public void sendToContainer(PacketBuffer buffer) {
		buffer.writeUniqueId(getOwner());
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new ShopShelfContainer(id, inventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName().toString());
	}

}
