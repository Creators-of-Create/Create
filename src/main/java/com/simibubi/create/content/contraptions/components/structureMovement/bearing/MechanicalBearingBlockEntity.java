package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MechanicalBearingBlockEntity extends GeneratingKineticBlockEntity
	implements IBearingBlockEntity, IDisplayAssemblyExceptions {

	protected ScrollOptionBehaviour<RotationMode> movementMode;
	protected ControlledContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;
	protected AssemblyException lastException;
	protected double sequencedAngleLimit;

	private float prevAngle;

	public MechanicalBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(3);
		sequencedAngleLimit = -1;
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	protected boolean syncSequenceContext() {
		return true;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(RotationMode.class,
			Lang.translateDirect("contraptions.movement_mode"), this, getMovementModeSlot());
		behaviours.add(movementMode);
		registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS);
	}

	@Override
	public void remove() {
		if (!level.isClientSide)
			disassemble();
		super.remove();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("Angle", angle);
		if (sequencedAngleLimit >= 0)
			compound.putDouble("SequencedAngleLimit", sequencedAngleLimit);
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		if (wasMoved) {
			super.read(compound, clientPacket);
			return;
		}

		float angleBefore = angle;
		running = compound.getBoolean("Running");
		angle = compound.getFloat("Angle");
		sequencedAngleLimit = compound.contains("SequencedAngleLimit") ? compound.getDouble("SequencedAngleLimit") : -1;
		lastException = AssemblyException.read(compound);
		super.read(compound, clientPacket);
		if (!clientPacket)
			return;
		if (running) {
			if (movedContraption == null || !movedContraption.isStalled()) {
				clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
				angle = angleBefore;
			}
		} else
			movedContraption = null;
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (isVirtual())
			return Mth.lerp(partialTicks + .5f, prevAngle, angle);
		if (movedContraption == null || movedContraption.isStalled() || !running)
			partialTicks = 0;
		float angularSpeed = getAngularSpeed();
		if (sequencedAngleLimit >= 0)
			angularSpeed = (float) Mth.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
		return Mth.lerp(partialTicks, angle, angle + angularSpeed);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
		sequencedAngleLimit = -1;

		if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
			if (!movedContraption.isStalled()) {
				angle = Math.round(angle);
				applyRotation();
			}
			movedContraption.getContraption()
				.stop(level);
		}

		if (!isWindmill() && sequenceContext != null
			&& sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE)
			sequencedAngleLimit = sequenceContext.getEffectiveValue(getTheoreticalSpeed());
	}

	public float getAngularSpeed() {
		float speed = convertToAngular(isWindmill() ? getGeneratedSpeed() : getSpeed());
		if (getSpeed() == 0)
			speed = 0;
		if (level.isClientSide) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	protected boolean isWindmill() {
		return false;
	}

	@Override
	public BlockPos getBlockPosition() {
		return worldPosition;
	}

	public void assemble() {
		if (!(level.getBlockState(worldPosition)
			.getBlock() instanceof BearingBlock))
			return;

		Direction direction = getBlockState().getValue(BearingBlock.FACING);
		BearingContraption contraption = new BearingContraption(isWindmill(), direction);
		try {
			if (!contraption.assemble(level, worldPosition))
				return;

			lastException = null;
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}

		if (isWindmill())
			award(AllAdvancements.WINDMILL);
		if (contraption.getSailBlocks() >= 16 * 8)
			award(AllAdvancements.WINDMILL_MAXED);

		contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
		movedContraption = ControlledContraptionEntity.create(level, this, contraption);
		BlockPos anchor = worldPosition.relative(direction);
		movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		movedContraption.setRotationAxis(direction.getAxis());
		level.addFreshEntity(movedContraption);

		AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

		if (contraption.containsBlockBreakers())
			award(AllAdvancements.CONTRAPTION_ACTORS);

		running = true;
		angle = 0;
		sendData();
		updateGeneratedRotation();
	}

	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		angle = 0;
		sequencedAngleLimit = -1;
		if (isWindmill())
			applyRotation();
		if (movedContraption != null) {
			movedContraption.disassemble();
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
		}

		movedContraption = null;
		running = false;
		updateGeneratedRotation();
		assembleNextTick = false;
		sendData();
	}

	@Override
	public void tick() {
		super.tick();

		prevAngle = angle;
		if (level.isClientSide)
			clientAngleDiff /= 2;

		if (!level.isClientSide && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE
					|| (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
				if (speed == 0 && (canDisassemble || movedContraption == null || movedContraption.getContraption()
					.getBlocks()
					.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption()
							.stop(level);
					disassemble();
					return;
				}
			} else {
				if (speed == 0 && !isWindmill())
					return;
				assemble();
			}
		}

		if (!running)
			return;

		if (!(movedContraption != null && movedContraption.isStalled())) {
			float angularSpeed = getAngularSpeed();
			if (sequencedAngleLimit >= 0) {
				angularSpeed = (float) Mth.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
				sequencedAngleLimit = Math.max(0, sequencedAngleLimit - Math.abs(angularSpeed));
			}
			float newAngle = angle + angularSpeed;
			angle = (float) (newAngle % 360);
		}

		applyRotation();
	}

	public boolean isNearInitialAngle() {
		return Math.abs(angle) < 22.5 || Math.abs(angle) > 360 - 22.5;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !level.isClientSide)
			sendData();
	}

	protected void applyRotation() {
		if (movedContraption == null)
			return;
		movedContraption.setAngle(angle);
		BlockState blockState = getBlockState();
		if (blockState.hasProperty(BlockStateProperties.FACING))
			movedContraption.setRotationAxis(blockState.getValue(BlockStateProperties.FACING)
				.getAxis());
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		BlockState blockState = getBlockState();
		if (!(contraption.getContraption() instanceof BearingContraption))
			return;
		if (!blockState.hasProperty(BearingBlock.FACING))
			return;

		this.movedContraption = contraption;
		setChanged();
		BlockPos anchor = worldPosition.relative(blockState.getValue(BearingBlock.FACING));
		movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		if (!level.isClientSide) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void onStall() {
		if (!level.isClientSide)
			sendData();
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		return movedContraption == contraption;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (isPlayerSneaking)
			return false;
		if (!isWindmill() && getSpeed() == 0)
			return false;
		if (running)
			return false;
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof BearingBlock))
			return false;

		BlockState attachedState = level.getBlockState(worldPosition.relative(state.getValue(BearingBlock.FACING)));
		if (attachedState.getMaterial()
			.isReplaceable())
			return false;
		TooltipHelper.addHint(tooltip, "hint.empty_bearing");
		return true;
	}

	public void setAngle(float forcedAngle) {
		angle = forcedAngle;
	}

}
