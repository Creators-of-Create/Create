package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IControlContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class LinearActuatorTileEntity extends KineticTileEntity implements IControlContraption, IDisplayAssemblyExceptions {

	public float offset;
	public boolean running;
	public boolean assembleNextTick;
	public AbstractContraptionEntity movedContraption;
	protected boolean forceMove;
	protected ScrollOptionBehaviour<MovementMode> movementMode;
	protected boolean waitingForSpeedChange;
	protected AssemblyException lastException;

	// Custom position sync
	protected float clientOffsetDiff;

	public LinearActuatorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		setLazyTickRate(3);
		forceMove = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(MovementMode.class, Lang.translate("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		movementMode.withCallback(t -> waitingForSpeedChange = false);
		behaviours.add(movementMode);
	}

	@Override
	public void tick() {
		super.tick();

		if (movedContraption != null) {
			if (!movedContraption.isAlive())
				movedContraption = null;
		}

		if (world.isRemote)
			clientOffsetDiff *= .75f;

		if (waitingForSpeedChange && movedContraption != null) {
			if (world.isRemote) {
				float syncSpeed = clientOffsetDiff / 2f;
				offset += syncSpeed;
				movedContraption.setContraptionMotion(toMotionVector(syncSpeed));
				return;
			}
			movedContraption.setContraptionMotion(Vec3d.ZERO);
			return;
		}

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (getSpeed() == 0)
					tryDisassemble();
				else
					sendData();
				return;
			} else {
				if (getSpeed() != 0)
					try {
						assemble();
						lastException = null;
					} catch (AssemblyException e) {
						lastException = e;
					}
				sendData();
			}
			return;
		}

		if (!running)
			return;

		boolean contraptionPresent = movedContraption != null;
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
			if (!world.isRemote) {
				applyContraptionMotion();
				applyContraptionPosition();
				tryDisassemble();
				if (waitingForSpeedChange) {
					forceMove = true;
					sendData();
				}
			}
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
		waitingForSpeedChange = false;
	}

	@Override
	public void remove() {
		this.removed = true;
		if (!world.isRemote)
			disassemble();
		super.remove();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putBoolean("Waiting", waitingForSpeedChange);
		compound.putFloat("Offset", offset);
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);

		if (clientPacket && forceMove) {
			compound.putBoolean("ForceMovement", forceMove);
			forceMove = false;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		boolean forceMovement = compound.contains("ForceMovement");
		float offsetBefore = offset;

		running = compound.getBoolean("Running");
		waitingForSpeedChange = compound.getBoolean("Waiting");
		offset = compound.getFloat("Offset");
		lastException = AssemblyException.read(compound);
		super.read(compound, clientPacket);

		if (!clientPacket)
			return;
		if (forceMovement)
			applyContraptionPosition();
		else if (running) {
			clientOffsetDiff = offset - offsetBefore;
			offset = offsetBefore;
		}
		if (!running)
			movedContraption = null;
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	public abstract void disassemble();

	protected abstract void assemble() throws AssemblyException;

	protected abstract int getExtensionRange();

	protected abstract int getInitialOffset();

	protected abstract ValueBoxTransform getMovementModeSlot();

	protected abstract Vec3d toMotionVector(float speed);

	protected abstract Vec3d toPosition(float offset);

	protected void visitNewPosition() {}

	protected void tryDisassemble() {
		if (removed) {
			disassemble();
			return;
		}
		if (movementMode.get() == MovementMode.MOVE_NEVER_PLACE) {
			waitingForSpeedChange = true;
			return;
		}
		int initial = getInitialOffset();
		if ((int) (offset + .5f) != initial && movementMode.get() == MovementMode.MOVE_PLACE_RETURNED) {
			waitingForSpeedChange = true;
			return;
		}
		disassemble();
	}

	@Override
	public void collided() {
		if (world.isRemote) {
			waitingForSpeedChange = true;
			return;
		}
		offset = getGridOffset(offset - getMovementSpeed());
		applyContraptionPosition();
		tryDisassemble();
	}

	protected void applyContraptionMotion() {
		if (movedContraption == null)
			return;
		if (movedContraption.isStalled()) {
			movedContraption.setContraptionMotion(Vec3d.ZERO);
			return;
		}
		movedContraption.setContraptionMotion(getMotionVector());
	}

	protected void applyContraptionPosition() {
		if (movedContraption == null)
			return;
		Vec3d vec = toPosition(offset);
		movedContraption.setPosition(vec.x, vec.y, vec.z);
		if (getSpeed() == 0 || waitingForSpeedChange)
			movedContraption.setContraptionMotion(Vec3d.ZERO);
	}

	public float getMovementSpeed() {
		float movementSpeed = MathHelper.clamp(getSpeed() / 512f, -.49f, .49f) + clientOffsetDiff / 2f;
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
	public void attach(ControlledContraptionEntity contraption) {
		this.movedContraption = contraption;
		if (!world.isRemote) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		return movedContraption == contraption;
	}

	@Override
	public BlockPos getBlockPosition() {
		return pos;
	}
}