package com.simibubi.create.modules.logistics.management.base;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class LogisticalActorTileEntity extends SyncedTileEntity
		implements Comparable<LogisticalActorTileEntity>, ITickableTileEntity {

	public static final int COOLDOWN = 20;

	public Priority priority = Priority.LOW;
	public String address = "";
	
	protected LogisticalNetwork network;
	protected UUID networkId;
	protected boolean initialize;
	protected boolean checkTasks;
	protected int taskCooldown;

	public LogisticalActorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		initialize = true;
	}

	@Override
	public void tick() {
		if (initialize) {
			initialize = false;
			initialize();
			return;
		}

		if (taskCooldown > 0)
			taskCooldown--;
	}

	protected void initialize() {
		if (networkId != null)
			handleAdded();
	}

	@Override
	public void remove() {
		if (networkId != null)
			handleRemoved();
		super.remove();
	}

	public void notifyTaskUpdate() {
		checkTasks = true;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (networkId != null)
			compound.putUniqueId("NetworkID", networkId);
		compound.putString("Address", address);
		compound.putInt("Priority", priority.ordinal());
		return super.write(compound);
	}

	public UUID getNetworkId() {
		return networkId;
	}

	@Override
	public void read(CompoundNBT compound) {
		if (compound.contains("NetworkIDLeast"))
			networkId = compound.getUniqueId("NetworkID");
		address = compound.getString("Address");
		priority = Priority.values()[compound.getInt("Priority")];
		super.read(compound);
	}

	public int getColor() {
		return colorFromUUID(networkId);
	}

	public <T> LazyOptional<T> getCasingCapability(Capability<T> cap, Direction side) {
		return LazyOptional.empty();
	}

	public void setNetworkId(UUID uniqueId) {
		if (getNetwork() != null)
			handleRemoved();
		networkId = uniqueId;
		handleAdded();
		markDirty();
		sendData();
	}

	public void handleAdded() {
		if (world.isRemote)
			return;
		if (getNetwork() != null)
			return;
		network = Create.logisticalNetworkHandler.handleAdded(this);
	}

	public void handleRemoved() {
		if (world.isRemote)
			return;
		Create.logisticalNetworkHandler.handleRemoved(this);
		network = null;
	}

	public boolean isSupplier() {
		return false;
	}

	public boolean isReceiver() {
		return false;
	}

	@Override
	public int compareTo(LogisticalActorTileEntity o) {
		return this.priority.compareTo(o.priority);
	}

	public LogisticalNetwork getNetwork() {
		return network;
	}

	public Priority getPriority() {
		return priority;
	}

	public static enum Priority {
		HIGHEST, HIGH, LOWEST, LOW;
	}

}
