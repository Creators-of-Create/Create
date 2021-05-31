package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity
	implements IBearingTileEntity, IDisplayAssemblyExceptions {

	protected ScrollOptionBehaviour<RotationMode> movementMode;
	protected ControlledContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;
	protected AssemblyException lastException;

	private float prevAngle;

	public MechanicalBearingTileEntity(TileEntityType<? extends MechanicalBearingTileEntity> type) {
		super(type);
		setLazyTickRate(3);
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(RotationMode.class, Lang.translate("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		behaviours.add(movementMode);
	}

	@Override
	public void remove() {
		if (!world.isRemote)
			disassemble();
		super.remove();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("Angle", angle);
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		if (wasMoved) {
			super.fromTag(state, compound, clientPacket);
			return;
		}

		float angleBefore = angle;
		running = compound.getBoolean("Running");
		angle = compound.getFloat("Angle");
		lastException = AssemblyException.read(compound);
		super.fromTag(state, compound, clientPacket);
		if (!clientPacket)
			return;
		if (running) {
			clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
			angle = angleBefore;
		} else
			movedContraption = null;
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (isVirtual())
			return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
		if (movedContraption == null || movedContraption.isStalled() || !running)
			partialTicks = 0;
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;

		if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
			movedContraption.getContraption()
				.stop(world);
		}
	}

	public float getAngularSpeed() {
		float speed = convertToAngular(isWindmill() ? getGeneratedSpeed() : getSpeed());
		if (getSpeed() == 0)
			speed = 0;
		if (world.isRemote) {
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
		return pos;
	}

	public void assemble() {
		if (!(world.getBlockState(pos)
			.getBlock() instanceof BearingBlock))
			return;

		Direction direction = getBlockState().get(FACING);
		BearingContraption contraption = new BearingContraption(isWindmill(), direction);
		try {
			if (!contraption.assemble(world, pos))
				return;

			lastException = null;
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}

		if (isWindmill())
			AllTriggers.triggerForNearbyPlayers(AllTriggers.WINDMILL, world, pos, 5);
		if (contraption.getSailBlocks() >= 16 * 8)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.MAXED_WINDMILL, world, pos, 5);

		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		movedContraption = ControlledContraptionEntity.create(world, this, contraption);
		BlockPos anchor = pos.offset(direction);
		movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
		movedContraption.setRotationAxis(direction.getAxis());
		world.addEntity(movedContraption);

		AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(world, pos);

		running = true;
		angle = 0;
		sendData();
		updateGeneratedRotation();
	}

	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		angle = 0;
		if (isWindmill())
			applyRotation();
		if (movedContraption != null) {
			movedContraption.disassemble();
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(world, pos);
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
		if (world.isRemote)
			clientAngleDiff /= 2;

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE
					|| (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
				if (speed == 0 && (canDisassemble || movedContraption == null || movedContraption.getContraption()
					.getBlocks()
					.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption()
							.stop(world);
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
			float newAngle = angle + angularSpeed;
			angle = (float) (newAngle % 360);
		}

		applyRotation();
	}

	public boolean isNearInitialAngle() {
		return Math.abs(angle) < 45 || Math.abs(angle) > 7 * 45;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !world.isRemote)
			sendData();
	}

	protected void applyRotation() {
		if (movedContraption == null)
			return;
		movedContraption.setAngle(angle);
		BlockState blockState = getBlockState();
		if (blockState.contains(BlockStateProperties.FACING))
			movedContraption.setRotationAxis(blockState.get(BlockStateProperties.FACING)
				.getAxis());
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		BlockState blockState = getBlockState();
		if (!(contraption.getContraption() instanceof BearingContraption))
			return;
		if (!blockState.contains(FACING))
			return;

		this.movedContraption = contraption;
		markDirty();
		BlockPos anchor = pos.offset(blockState.get(FACING));
		movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
		if (!world.isRemote) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void onStall() {
		if (!world.isRemote)
			sendData();
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public void collided() {}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		return movedContraption == contraption;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean addToTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
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

		BlockState attachedState = world.getBlockState(pos.offset(state.get(BearingBlock.FACING)));
		if (attachedState.getMaterial()
			.isReplaceable())
			return false;
		TooltipHelper.addHint(tooltip, "hint.empty_bearing");
		return true;
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}

	public void setAngle(float forcedAngle) {
		angle = forcedAngle;
	}

}
