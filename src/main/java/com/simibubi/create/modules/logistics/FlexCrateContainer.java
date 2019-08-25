package com.simibubi.create.modules.logistics;

import com.simibubi.create.AllContainers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class FlexCrateContainer extends Container {

	private FlexCrateTileEntity te;

	public FlexCrateContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainers.SchematicTable.type, id);
		ClientWorld world = Minecraft.getInstance().world;
		this.te = (FlexCrateTileEntity) world.getTileEntity(extraData.readBlockPos());
		this.te.handleUpdateTag(extraData.readCompoundTag());
		init();
	}

	public FlexCrateContainer(int id, PlayerInventory inv, FlexCrateTileEntity te) {
		super(AllContainers.SchematicTable.type, id);
		this.te = te;
		init();
	}

	private void init() {
		
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

}
