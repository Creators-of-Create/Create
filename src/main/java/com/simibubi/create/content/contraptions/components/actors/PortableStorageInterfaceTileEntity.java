package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class PortableStorageInterfaceTileEntity extends SmartTileEntity {

	public static final int ANIMATION = 4;
	protected int transferTimer;
	protected float distance;
	protected LerpedFloat connectionAnimation;
	protected boolean powered;
	protected Entity connectedEntity;

	public int keepAlive = 0;

	public PortableStorageInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		transferTimer = 0;
		connectionAnimation = LerpedFloat.linear()
			.startWithValue(0);
		powered = false;
	}

	public void startTransferringTo(Contraption contraption, float distance) {
		if (connectedEntity == contraption.entity)
			return;
		this.distance = Math.min(2, distance);
		connectedEntity = contraption.entity;
		startConnecting();
		notifyUpdate();
	}

	protected void stopTransferring() {
		connectedEntity = null;
		level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
	}

	public boolean canTransfer() {
		if (connectedEntity != null && !connectedEntity.isAlive())
			stopTransferring();
		return connectedEntity != null && isConnected();
	}

	@Override
	public void initialize() {
		super.initialize();
		powered = level.hasNeighborSignal(worldPosition);
		if (!powered)
			notifyContraptions();
	}

	protected abstract void invalidateCapability();

	@Override
	public void tick() {
		super.tick();
		boolean wasConnected = isConnected();
		int timeUnit = getTransferTimeout();
		int animation = ANIMATION;

		if (keepAlive > 0) {
			keepAlive--;
			if (keepAlive == 0 && !level.isClientSide) {
				stopTransferring();
				transferTimer = ANIMATION - 1;
				sendData();
				return;
			}
		}

		transferTimer = Math.min(transferTimer, ANIMATION * 2 + getTransferTimeout());

		boolean timerCanDecrement = transferTimer > ANIMATION || transferTimer > 0 && keepAlive == 0
			&& (isVirtual() || !level.isClientSide || transferTimer != ANIMATION);

		if (timerCanDecrement && (!isVirtual() || transferTimer != timeUnit)) {
			transferTimer--;
			if (transferTimer == ANIMATION - 1)
				sendData();
			if (transferTimer <= 0 || powered)
				stopTransferring();
		}

		boolean isConnected = isConnected();
		if (wasConnected != isConnected && !level.isClientSide)
			setChanged();

		float progress = 0;
		if (isConnected)
			progress = 1;
		else if (transferTimer >= timeUnit + animation)
			progress = Mth.lerp((transferTimer - timeUnit - animation) / (float) animation, 1, 0);
		else if (transferTimer < animation)
			progress = Mth.lerp(transferTimer / (float) animation, 0, 1);
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
		boolean poweredPreviously = powered;
		powered = compound.getBoolean("Powered");
		if (clientPacket && powered != poweredPreviously && !powered)
			notifyContraptions();
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
		if (!powered)
			notifyContraptions();
		if (powered)
			stopTransferring();
		sendData();
	}

	private void notifyContraptions() {
		level.getEntitiesOfClass(AbstractContraptionEntity.class, new AABB(worldPosition).inflate(3))
			.forEach(AbstractContraptionEntity::refreshPSIs);
	}

	public boolean isPowered() {
		return powered;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	public boolean isTransferring() {
		return transferTimer > ANIMATION;
	}

	boolean isConnected() {
		int timeUnit = getTransferTimeout();
		return transferTimer >= ANIMATION && transferTimer <= timeUnit + ANIMATION;
	}

	float getExtensionDistance(float partialTicks) {
		return (float) (Math.pow(connectionAnimation.getValue(partialTicks), 2) * distance / 2);
	}

	float getConnectionDistance() {
		return distance;
	}

	public void startConnecting() {
		transferTimer = getTransferTimeout() + ANIMATION * 2;
	}

	public void onContentTransferred() {
		int timeUnit = getTransferTimeout();
		transferTimer = timeUnit + ANIMATION;
		award(AllAdvancements.PSI);
		sendData();
	}

	protected Integer getTransferTimeout() {
		return AllConfigs.SERVER.logistics.psiTimeout.get();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		registerAwardables(behaviours, AllAdvancements.PSI);
	}

}
