package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.IControlContraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public abstract class LinearActuatorTileEntity extends KineticTileEntity implements IControlContraption {
	public float offset;
	public boolean running;
	protected boolean assembleNextTick;
	public ContraptionEntity movedContraption;
	protected boolean forceMove;

	// Custom position sync
	protected float clientOffsetDiff;

	public LinearActuatorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		setLazyTickRate(3);
	}

	@Override
	public void tick() {
		super.tick();
		boolean contraptionPresent = movedContraption != null;

		if (contraptionPresent)
			if (!movedContraption.isAlive())
				movedContraption = null;

		if (world.isRemote)
			clientOffsetDiff *= .75f;

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (getSpeed() == 0)
					disassembleConstruct();
				else
					sendData();
				return;
			}
			assembleConstruct();
			return;
		}

		if (!running)
			return;

		contraptionPresent = movedContraption != null;
		float movementSpeed = getMovementSpeed();
		float newOffset = offset + movementSpeed;
		if ((int) newOffset != (int) offset)
			visitNewPosition();

		if (!contraptionPresent || !movedContraption.isStalled())
			offset = newOffset;

		if (contraptionPresent)
			applyContraptionMotion();

		int extensionRange = getExtensionRange();
		if (offset <= 0 || offset >= extensionRange) {
			offset = offset <= 0 ? 0 : extensionRange;
			if (!world.isRemote)
				disassembleConstruct();
			return;
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !world.isRemote)
			sendData();
	}

	protected int getGridOffset(float offset) {
		return MathHelper.clamp((int) (offset + .5f), 0, getExtensionRange());
	}

	public float getInterpolatedOffset(float partialTicks) {
		float interpolatedOffset =
			MathHelper.clamp(offset + (partialTicks - .5f) * getMovementSpeed(), 0, getExtensionRange());
		return interpolatedOffset;
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	@Override
	public void remove() {
		this.removed = true;
		if (!world.isRemote)
			disassembleConstruct();
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putFloat("Offset", offset);
		return super.write(tag);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		if (forceMove) {
			compound.putBoolean("ForceMovement", forceMove);
			forceMove = false;
		}
		return super.writeToClient(compound);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		offset = tag.getFloat("Offset");
		super.read(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		float offsetBefore = offset;
		super.readClientUpdate(tag);
		if (running) {
			clientOffsetDiff = offset - offsetBefore;
			offset = offsetBefore;
		}

		if (tag.contains("ForceMovement"))
			if (movedContraption != null)
				applyContraptionPosition();
	}

	protected abstract void assembleConstruct();

	protected abstract void disassembleConstruct();

	protected abstract int getExtensionRange();

	protected abstract void visitNewPosition();

	protected abstract Vec3d toMotionVector(float speed);

	protected abstract Vec3d toPosition(float offset);

	protected void applyContraptionMotion() {
		if (movedContraption.isStalled())
			movedContraption.setMotion(Vec3d.ZERO);
		else
			movedContraption.setMotion(getMotionVector());
	}

	protected void applyContraptionPosition() {
		Vec3d vec = toPosition(offset);
		movedContraption.setPosition(vec.x, vec.y, vec.z);
	}

	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f + clientOffsetDiff / 2f;
		if (world.isRemote)
			movementSpeed *= ServerSpeedProvider.get();
		return movementSpeed;
	}

	public Vec3d getMotionVector() {
		return toMotionVector(getMovementSpeed());
	}

	@Override
	public void onStall() {
		if (!world.isRemote) {
			forceMove = true;
			sendData();
		}
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public void attach(ContraptionEntity contraption) {
		this.movedContraption = contraption;
		if (!world.isRemote)
			sendData();
	}

}