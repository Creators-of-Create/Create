package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

public class PortableStorageInterfaceTileEntity extends SmartTileEntity {

	protected int transferTimer;
	protected float distance;
	protected LazyOptional<IItemHandlerModifiable> capability;
	protected LerpedFloat connectionAnimation;

	public PortableStorageInterfaceTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		transferTimer = 0;
		capability = LazyOptional.empty();
		connectionAnimation = LerpedFloat.linear().startWithValue(0);
	}

	public void startTransferringTo(Contraption contraption, float distance) {
		capability.invalidate();
		capability = LazyOptional.of(() -> new InterfaceItemHandler(contraption.inventory));
		this.distance = distance;
		startConnecting();
		notifyUpdate();
	}

	@Override
	public void tick() {
		super.tick();
		boolean wasConnected = isConnected();
		
		if (transferTimer > 0) {
			transferTimer--;
			if (transferTimer == 0)
				capability.invalidate();
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
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		transferTimer = compound.getInt("Timer");
		distance = compound.getFloat("Distance");
	}
	
	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Timer", transferTimer);
		compound.putFloat("Distance", distance);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().grow(2);
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

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return capability.cast();
		return super.getCapability(cap, side);
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

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(IItemHandlerModifiable wrapped) {
			super(wrapped);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (!isConnected())
				return ItemStack.EMPTY;
			ItemStack extractItem = super.extractItem(slot, amount, simulate);
			if (!simulate && !extractItem.isEmpty())
				onContentTransferred();
			return extractItem;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (!isConnected())
				return stack;
			ItemStack insertItem = super.insertItem(slot, stack, simulate);
			if (!simulate && !insertItem.equals(stack, false))
				onContentTransferred();
			return insertItem;
		}

	}

}
