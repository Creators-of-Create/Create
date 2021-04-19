package com.simibubi.create.content.curiosities.projector;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.render.backend.effects.SphereFilterProgram;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ChromaticProjectorTileEntity extends TileEntity implements INamedContainerProvider {

	SphereFilterProgram.FilterSphere filter;

	public ChromaticProjectorTileEntity(TileEntityType<?> te) {
		super(te);
	}

	public void sendToContainer(PacketBuffer buffer) {
		buffer.writeBlockPos(getPos());
		buffer.writeCompoundTag(getUpdateTag());
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Chromatic Projector");
	}

	@Nullable
	@Override
	public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new ChromaticProjectorContainer(p_createMenu_1_, p_createMenu_2_, this);
	}
}
