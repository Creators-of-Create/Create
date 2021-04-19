package com.simibubi.create.content.curiosities.projector;

import javax.annotation.Nullable;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

public class ChromaticProjectorContainer extends Container {

	public ChromaticProjectorContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainerTypes.CHROMATIC_PROJECTOR.type, id);


	}

	public ChromaticProjectorContainer(int id, PlayerInventory inv, ChromaticProjectorTileEntity te) {
		super(AllContainerTypes.CHROMATIC_PROJECTOR.type, id);
	}

	public ChromaticProjectorContainer(@Nullable ContainerType<?> p_i50105_1_, int p_i50105_2_) {
		super(p_i50105_1_, p_i50105_2_);
	}

	@Override
	public boolean canInteractWith(PlayerEntity p_75145_1_) {
		return true;
	}
}
