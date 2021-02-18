package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PortableStorageInterfaceTileEntity extends SmartTileEntity {

	protected int transferTimer;
	protected float distance;
	protected LerpedFloat connectionAnimation;
	protected boolean powered;

	public PortableStorageInterfaceTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		transferTimer = 0;
		connectionAnimation = LerpedFloat.linear()
			.startWithValue(0);
		powered = false;
	}

	public void startTransferringTo(Contraption contraption, float distance) {
		this.distance = distance;
		startConnecting();
		notifyUpdate();
	}

	protected abstract void stopTransferring();

	protected abstract void invalidateCapability();

	@Override
	public void tick() {
		super.tick();
		boolean wasConnected = isConnected();

		if (transferTimer > 0) {
			transferTimer--;
			if (transferTimer == 0 || powered)
				stopTransferring();
		}

		boolean isConnected = isConnected();
		if (wasConnected != isConnected && !world.isRemote)
			markDirty();

		float progress = 0;
		int timeUnit = getTransferTimeout() / 2;
		if (isConnected)
			progress = 1;
		else if (transferTimer >= timeUnit * 3)
			progress = MathHelper.lerp((transferTimer - timeUnit * 3) / (float) timeUnit, 1, 0);
		else if (transferTimer < timeUnit)
			progress = MathHelper.lerp(transferTimer / (float) timeUnit, 0, 1);
		connectionAnimation.setValue(progress);
	}

	@Override
	public void remove() {
		super.remove();
		invalidateCapability();
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		transferTimer = compound.getInt("Timer");
		distance = compound.getFloat("Distance");
		powered = compound.getBoolean("Powered");
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Timer", transferTimer);
		compound.putFloat("Distance", distance);
		compound.putBoolean("Powered", powered);
	}

	public void neighbourChanged() {
		boolean isBlockPowered = world.isBlockPowered(pos);
		if (isBlockPowered == powered)
			return;
		powered = isBlockPowered;
		sendData();
	}
	
	public boolean isPowered() {
		return powered;
	}

	protected AxisAlignedBB cachedBoundingBox;
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = super.getRenderBoundingBox().grow(2);
		}
		return cachedBoundingBox;
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
