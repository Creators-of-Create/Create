package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import java.util.List;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;
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
	public boolean assembleNextTick;
	public ContraptionEntity movedContraption;
	protected boolean forceMove;
	protected ScrollOptionBehaviour<MovementMode> movementMode;
	protected boolean waitingForSpeedChange;

	// Custom position sync
	protected float clientOffsetDiff;

	public LinearActuatorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		setLazyTickRate(3);
		forceMove = true;
	}

//	@Override
//	public void initialize() {
//		super.initialize();
//		if (!world.isRemote)
//			
//	}

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
		boolean contraptionPresent = movedContraption != null;

		if (contraptionPresent)
			if (!movedContraption.isAlive())
				movedContraption = null;

		if (world.isRemote)
			clientOffsetDiff *= .75f;

		if (waitingForSpeedChange) {
			movedContraption.setContraptionMotion(Vec3d.ZERO);
//			movedContraption.setMotion(Vec3d.ZERO);
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
			if (!world.isRemote) {
				applyContraptionMotion();
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
			disassembleConstruct();
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putBoolean("Waiting", waitingForSpeedChange);
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
		waitingForSpeedChange = tag.getBoolean("Waiting");
		offset = tag.getFloat("Offset");
		super.read(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		boolean forceMovement = tag.contains("ForceMovement");
		float offsetBefore = offset;
		super.readClientUpdate(tag);

		if (forceMovement) {
			if (movedContraption != null) {
				applyContraptionPosition();
			}
		} else {
			if (running) {
				clientOffsetDiff = offset - offsetBefore;
				offset = offsetBefore;
			}
		}

		if (!running)
			movedContraption = null;

	}

	protected abstract void assembleConstruct();

	protected abstract void disassembleConstruct();

	protected abstract int getExtensionRange();

	protected abstract int getInitialOffset();

	protected abstract ValueBoxTransform getMovementModeSlot();

	protected abstract void visitNewPosition();

	protected abstract Vec3d toMotionVector(float speed);

	protected abstract Vec3d toPosition(float offset);

	protected void tryDisassemble() {
		if (removed) {
			disassembleConstruct();
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
		disassembleConstruct();
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
		Vec3d vec = toPosition(offset);
		movedContraption.setPosition(vec.x, vec.y, vec.z);
		if (getSpeed() == 0 || waitingForSpeedChange)
			movedContraption.setContraptionMotion(Vec3d.ZERO);
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