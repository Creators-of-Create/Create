package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IControlContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class LinearActuatorTileEntity extends KineticTileEntity
	implements IControlContraption, IDisplayAssemblyExceptions {

	public float offset;
	public boolean running;
	public boolean assembleNextTick;
	public boolean needsContraption;
	public AbstractContraptionEntity movedContraption;
	protected boolean forceMove;
	protected ScrollOptionBehaviour<MovementMode> movementMode;
	protected boolean waitingForSpeedChange;
	protected AssemblyException lastException;

	// Custom position sync
	protected float clientOffsetDiff;

	public LinearActuatorTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		setLazyTickRate(3);
		forceMove = true;
		needsContraption = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(MovementMode.class, Lang.translateDirect("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		movementMode.withCallback(t -> waitingForSpeedChange = false);
		behaviours.add(movementMode);
		registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS);
	}

	@Override
	public void tick() {
		super.tick();

		if (movedContraption != null) {
			if (!movedContraption.isAlive())
				movedContraption = null;
		}

		if (level.isClientSide)
			clientOffsetDiff *= .75f;

		if (waitingForSpeedChange) {
			if (movedContraption != null) {
				if (level.isClientSide) {
					float syncSpeed = clientOffsetDiff / 2f;
					offset += syncSpeed;
					movedContraption.setContraptionMotion(toMotionVector(syncSpeed));
					return;
				}
				movedContraption.setContraptionMotion(Vec3.ZERO);
			}
			return;
		}

		if (!level.isClientSide && assembleNextTick) {
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
		if (needsContraption && !contraptionPresent)
			return;

		float movementSpeed = getMovementSpeed();
		float newOffset = offset + movementSpeed;
		if ((int) newOffset != (int) offset)
			visitNewPosition();

		if (contraptionPresent) {
			if (moveAndCollideContraption()) {
				movedContraption.setContraptionMotion(Vec3.ZERO);
				offset = getGridOffset(offset);
				resetContraptionToOffset();
				collided();
				return;
			}
		}

		if (!contraptionPresent || !movedContraption.isStalled())
			offset = newOffset;

		int extensionRange = getExtensionRange();
		if (offset <= 0 || offset >= extensionRange) {
			offset = offset <= 0 ? 0 : extensionRange;
			if (!level.isClientSide) {
				moveAndCollideContraption();
				resetContraptionToOffset();
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
		if (movedContraption != null && !level.isClientSide)
			sendData();
	}

	protected int getGridOffset(float offset) {
		return Mth.clamp((int) (offset + .5f), 0, getExtensionRange());
	}

	public float getInterpolatedOffset(float partialTicks) {
		float interpolatedOffset =
			Mth.clamp(offset + (partialTicks - .5f) * getMovementSpeed(), 0, getExtensionRange());
		return interpolatedOffset;
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
		waitingForSpeedChange = false;

		if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
			movedContraption.getContraption()
				.stop(level);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		this.remove = true;
		if (!level.isClientSide)
			disassemble();
		super.setRemovedNotDueToChunkUnload();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
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
	protected void read(CompoundTag compound, boolean clientPacket) {
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
			resetContraptionToOffset();
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

	protected abstract Vec3 toMotionVector(float speed);

	protected abstract Vec3 toPosition(float offset);

	protected void visitNewPosition() {}

	protected void tryDisassemble() {
		if (remove) {
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

	protected boolean moveAndCollideContraption() {
		if (movedContraption == null)
			return false;
		if (movedContraption.isStalled()) {
			movedContraption.setContraptionMotion(Vec3.ZERO);
			return false;
		}

		Vec3 motion = getMotionVector();
		movedContraption.setContraptionMotion(getMotionVector());
		movedContraption.move(motion.x, motion.y, motion.z);
		return ContraptionCollider.collideBlocks(movedContraption);
	}

	protected void collided() {
		if (level.isClientSide) {
			waitingForSpeedChange = true;
			return;
		}
		offset = getGridOffset(offset - getMovementSpeed());
		resetContraptionToOffset();
		tryDisassemble();
	}

	protected void resetContraptionToOffset() {
		if (movedContraption == null)
			return;
		Vec3 vec = toPosition(offset);
		movedContraption.setPos(vec.x, vec.y, vec.z);
		if (getSpeed() == 0 || waitingForSpeedChange)
			movedContraption.setContraptionMotion(Vec3.ZERO);
	}

	public float getMovementSpeed() {
		float movementSpeed = Mth.clamp(convertToLinear(getSpeed()), -.49f, .49f) + clientOffsetDiff / 2f;
		if (level.isClientSide)
			movementSpeed *= ServerSpeedProvider.get();
		return movementSpeed;
	}

	public Vec3 getMotionVector() {
		return toMotionVector(getMovementSpeed());
	}

	@Override
	public void onStall() {
		if (!level.isClientSide) {
			forceMove = true;
			sendData();
		}
	}

	public void onLengthBroken() {
		offset = 0;
		sendData();
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		this.movedContraption = contraption;
		if (!level.isClientSide) {
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
		return worldPosition;
	}
}
