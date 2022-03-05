package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class PortableStorageInterfaceTileEntity extends SmartTileEntity implements CustomRenderBoundingBoxBlockEntity {

	protected int transferTimer;
	protected float distance;
	protected LerpedFloat connectionAnimation;
	protected boolean powered;
	protected Entity connectedEntity;

	public PortableStorageInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		transferTimer = 0;
		connectionAnimation = LerpedFloat.linear()
			.startWithValue(0);
		powered = false;
	}

	public void startTransferringTo(Contraption contraption, float distance) {
		this.distance = distance;
		connectedEntity = contraption.entity;
		startConnecting();
		notifyUpdate();
	}

	protected void stopTransferring() {
		connectedEntity = null;
	}

	public boolean canTransfer() {
		if (connectedEntity != null && !connectedEntity.isAlive())
			stopTransferring();
		return connectedEntity != null && isConnected();
	}

	protected abstract void invalidateCapability();

	@Override
	public void tick() {
		super.tick();
		boolean wasConnected = isConnected();
		int timeUnit = getTransferTimeout() / 2;

		if (transferTimer > 0 && (!isVirtual() || transferTimer != timeUnit)) {
			transferTimer--;
			if (transferTimer == 0 || powered)
				stopTransferring();
		}

		boolean isConnected = isConnected();
		if (wasConnected != isConnected && !level.isClientSide)
			setChanged();

		float progress = 0;
		if (isConnected)
			progress = 1;
		else if (transferTimer >= timeUnit * 3)
			progress = Mth.lerp((transferTimer - timeUnit * 3) / (float) timeUnit, 1, 0);
		else if (transferTimer < timeUnit)
			progress = Mth.lerp(transferTimer / (float) timeUnit, 0, 1);
		connectionAnimation.setValue(progress);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		invalidateCapability();
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		transferTimer = compound.getInt("Timer");
		distance = compound.getFloat("Distance");
		powered = compound.getBoolean("Powered");
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Timer", transferTimer);
		compound.putFloat("Distance", distance);
		compound.putBoolean("Powered", powered);
	}

	public void neighbourChanged() {
		boolean isBlockPowered = level.hasNeighborSignal(worldPosition);
		if (isBlockPowered == powered)
			return;
		powered = isBlockPowered;
		sendData();
	}

	public boolean isPowered() {
		return powered;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	public boolean isTransferring() {
		return transferTimer != 0;
	}

	boolean isConnected() {
		int timeUnit = getTransferTimeout() / 2;
		return transferTimer >= timeUnit && transferTimer <= timeUnit * 3;
	}

	float getExtensionDistance(float partialTicks) {
		return connectionAnimation.getValue(partialTicks) * distance / 2;
	}

	float getConnectionDistance() {
		return distance;
	}

	public void startConnecting() {
		transferTimer = getTransferTimeout() * 2;
	}

	public void onContentTransferred() {
		int timeUnit = getTransferTimeout() / 2;
		transferTimer = timeUnit * 3;
		sendData();
	}

	protected Integer getTransferTimeout() {
		return AllConfigs.SERVER.logistics.psiTimeout.get();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
