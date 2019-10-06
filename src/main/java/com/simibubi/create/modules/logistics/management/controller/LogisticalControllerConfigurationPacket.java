package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerTileEntity.Priority;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity.ShippingInventory;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class LogisticalControllerConfigurationPacket
		extends TileEntityConfigurationPacket<LogisticalInventoryControllerTileEntity> {

	String address;
	int filterAmount;
	Priority priority;
	boolean active;

	public LogisticalControllerConfigurationPacket(BlockPos pos, String address, int filterAmount,
			Priority priority, boolean active) {
		super(pos);
		this.address = address;
		this.filterAmount = filterAmount;
		this.priority = priority;
		this.active = active;
	}

	public LogisticalControllerConfigurationPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeString(address, 2048);
		buffer.writeInt(filterAmount);
		buffer.writeInt(priority.ordinal());
		buffer.writeBoolean(active);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		address = buffer.readString(2048);
		filterAmount = buffer.readInt();
		priority = Priority.values()[buffer.readInt()];
		active = buffer.readBoolean();
	}

	@Override
	protected void applySettings(LogisticalInventoryControllerTileEntity te) {
		if (!address.isEmpty()) {
			te.address = address;
			if (te.getNetwork() != null)
				te.getNetwork().reAdvertiseReceivers();
		}
		te.priority = priority;
		te.isActive = active;
		te.shippingInventory.ifPresent(inv -> {
			ShippingInventory sInv = (ShippingInventory) inv;
			sInv.filterAmount = filterAmount;
		});
	}

}
