package com.simibubi.create.foundation.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public interface IWithContainer<T extends TileEntity, C extends AbstractTileEntityContainer<T>> extends INamedContainerProvider {

	public IContainerFactory<T, C> getContainerFactory();

	@SuppressWarnings("unchecked")
	@Override
	default Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return getContainerFactory().create(id, inv, ((T) this));
	}

	@Override
	default ITextComponent getDisplayName() {
		return new StringTextComponent(((TileEntity) this).getType().getRegistryName().toString());
	}

	public interface IContainerFactory<T extends TileEntity, C extends AbstractTileEntityContainer<? extends TileEntity>> {
		public C create(int id, PlayerInventory inv, T te);
	}
	
	default void sendToContainer(PacketBuffer buffer) {
		buffer.writeBlockPos(((TileEntity) this).getPos());
		buffer.writeCompoundTag(((TileEntity) this).getUpdateTag());
	}

}
