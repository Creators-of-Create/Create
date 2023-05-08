package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkContraption.HandType;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ClockworkBearingBlockEntity extends KineticBlockEntity
	implements IBearingBlockEntity, IDisplayAssemblyExceptions {

	protected ControlledContraptionEntity hourHand;
	protected ControlledContraptionEntity minuteHand;
	protected float hourAngle;
	protected float minuteAngle;
	protected float clientHourAngleDiff;
	protected float clientMinuteAngleDiff;

	protected boolean running;
	protected boolean assembleNextTick;
	protected AssemblyException lastException;
	protected ScrollOptionBehaviour<ClockHands> operationMode;

	private float prevForcedAngle;

	public ClockworkBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(3);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		operationMode = new ScrollOptionBehaviour<>(ClockHands.class,
			Lang.translateDirect("contraptions.clockwork.clock_hands"), this, getMovementModeSlot());
		behaviours.add(operationMode);
		registerAwardables(behaviours, AllAdvancements.CLOCKWORK_BEARING);
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide) {
			prevForcedAngle = hourAngle;
			clientMinuteAngleDiff /= 2;
			clientHourAngleDiff /= 2;
		}

		if (!level.isClientSide && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = true;
				if (speed == 0 && (canDisassemble || hourHand == null || hourHand.getContraption()
					.getBlocks()
					.isEmpty())) {
					if (hourHand != null)
						hourHand.getContraption()
							.stop(level);
					if (minuteHand != null)
						minuteHand.getContraption()
							.stop(level);
					disassemble();
				}
				return;
			} else
				assemble();
			return;
		}

		if (!running)
			return;

		if (!(hourHand != null && hourHand.isStalled())) {
			float newAngle = hourAngle + getHourArmSpeed();
			hourAngle = (float) (newAngle % 360);
		}

		if (!(minuteHand != null && minuteHand.isStalled())) {
			float newAngle = minuteAngle + getMinuteArmSpeed();
			minuteAngle = (float) (newAngle % 360);
		}

		applyRotations();
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	protected void applyRotations() {
		BlockState blockState = getBlockState();
		Axis axis = Axis.X;

		if (blockState.hasProperty(BlockStateProperties.FACING))
			axis = blockState.getValue(BlockStateProperties.FACING)
				.getAxis();

		if (hourHand != null) {
			hourHand.setAngle(hourAngle);
			hourHand.setRotationAxis(axis);
		}
		if (minuteHand != null) {
			minuteHand.setAngle(minuteAngle);
			minuteHand.setRotationAxis(axis);
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (hourHand != null && !level.isClientSide)
			sendData();
	}

	public float getHourArmSpeed() {
		float speed = getAngularSpeed() / 2f;

		if (speed != 0) {
			ClockHands mode = ClockHands.values()[operationMode.getValue()];
			float hourTarget = mode == ClockHands.HOUR_FIRST ? getHourTarget(false)
				: mode == ClockHands.MINUTE_FIRST ? getMinuteTarget() : getHourTarget(true);
			float shortestAngleDiff = AngleHelper.getShortestAngleDiff(hourAngle, hourTarget);
			if (shortestAngleDiff < 0) {
				speed = Math.max(speed, shortestAngleDiff);
			} else {
				speed = Math.min(-speed, shortestAngleDiff);
			}
		}

		return speed + clientHourAngleDiff / 3f;
	}

	public float getMinuteArmSpeed() {
		float speed = getAngularSpeed();

		if (speed != 0) {
			ClockHands mode = ClockHands.values()[operationMode.getValue()];
			float minuteTarget = mode == ClockHands.MINUTE_FIRST ? getHourTarget(false) : getMinuteTarget();
			float shortestAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngle, minuteTarget);
			if (shortestAngleDiff < 0) {
				speed = Math.max(speed, shortestAngleDiff);
			} else {
				speed = Math.min(-speed, shortestAngleDiff);
			}
		}

		return speed + clientMinuteAngleDiff / 3f;
	}

	protected float getHourTarget(boolean cycle24) {
		boolean isNatural = level.dimensionType()
			.natural();
		int dayTime = (int) ((level.getDayTime() * (isNatural ? 1 : 24)) % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int offset = getBlockState().getValue(ClockworkBearingBlock.FACING)
			.getAxisDirection()
			.getStep();
		float hourTarget = (float) (offset * -360 / (cycle24 ? 24f : 12f) * (hours % (cycle24 ? 24 : 12)));
		return hourTarget;
	}

	protected float getMinuteTarget() {
		boolean isNatural = level.dimensionType()
			.natural();
		int dayTime = (int) ((level.getDayTime() * (isNatural ? 1 : 24)) % 24000);
		int minutes = (dayTime % 1000) * 60 / 1000;
		int offset = getBlockState().getValue(ClockworkBearingBlock.FACING)
			.getAxisDirection()
			.getStep();
		float minuteTarget = (float) (offset * -360 / 60f * (minutes));
		return minuteTarget;
	}

	public float getAngularSpeed() {
		float speed = -Math.abs(getSpeed() * 3 / 10f);
		if (level.isClientSide)
			speed *= ServerSpeedProvider.get();
		return speed;
	}

	public void assemble() {
		if (!(level.getBlockState(worldPosition)
			.getBlock() instanceof ClockworkBearingBlock))
			return;

		Direction direction = getBlockState().getValue(BlockStateProperties.FACING);

		// Collect Construct
		Pair<ClockworkContraption, ClockworkContraption> contraption;
		try {
			contraption = ClockworkContraption.assembleClockworkAt(level, worldPosition, direction);
			lastException = null;
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}
		if (contraption == null)
			return;
		if (contraption.getLeft() == null)
			return;
		if (contraption.getLeft()
			.getBlocks()
			.isEmpty())
			return;
		BlockPos anchor = worldPosition.relative(direction);

		contraption.getLeft()
			.removeBlocksFromWorld(level, BlockPos.ZERO);
		hourHand = ControlledContraptionEntity.create(level, this, contraption.getLeft());
		hourHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		hourHand.setRotationAxis(direction.getAxis());
		level.addFreshEntity(hourHand);
		
		if (contraption.getLeft()
			.containsBlockBreakers())
			award(AllAdvancements.CONTRAPTION_ACTORS);

		if (contraption.getRight() != null) {
			anchor = worldPosition.relative(direction, contraption.getRight().offset + 1);
			contraption.getRight()
				.removeBlocksFromWorld(level, BlockPos.ZERO);
			minuteHand = ControlledContraptionEntity.create(level, this, contraption.getRight());
			minuteHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
			minuteHand.setRotationAxis(direction.getAxis());
			level.addFreshEntity(minuteHand);
			
			if (contraption.getRight()
				.containsBlockBreakers())
				award(AllAdvancements.CONTRAPTION_ACTORS);
		}
		
		award(AllAdvancements.CLOCKWORK_BEARING);

		// Run
		running = true;
		hourAngle = 0;
		minuteAngle = 0;
		sendData();
	}

	public void disassemble() {
		if (!running && hourHand == null && minuteHand == null)
			return;

		hourAngle = 0;
		minuteAngle = 0;
		applyRotations();

		if (hourHand != null) {
			hourHand.disassemble();
		}
		if (minuteHand != null)
			minuteHand.disassemble();

		hourHand = null;
		minuteHand = null;
		running = false;
		sendData();
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		if (!(contraption.getContraption() instanceof ClockworkContraption))
			return;

		ClockworkContraption cc = (ClockworkContraption) contraption.getContraption();
		setChanged();
		Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
		BlockPos anchor = worldPosition.relative(facing, cc.offset + 1);
		if (cc.handType == HandType.HOUR) {
			this.hourHand = contraption;
			hourHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		} else {
			this.minuteHand = contraption;
			minuteHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		}
		if (!level.isClientSide) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("HourAngle", hourAngle);
		compound.putFloat("MinuteAngle", minuteAngle);
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		float hourAngleBefore = hourAngle;
		float minuteAngleBefore = minuteAngle;

		running = compound.getBoolean("Running");
		hourAngle = compound.getFloat("HourAngle");
		minuteAngle = compound.getFloat("MinuteAngle");
		lastException = AssemblyException.read(compound);
		super.read(compound, clientPacket);

		if (!clientPacket)
			return;

		if (running) {
			clientHourAngleDiff = AngleHelper.getShortestAngleDiff(hourAngleBefore, hourAngle);
			clientMinuteAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngleBefore, minuteAngle);
			hourAngle = hourAngleBefore;
			minuteAngle = minuteAngleBefore;
		} else {
			hourHand = null;
			minuteHand = null;
		}
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (isVirtual())
			return Mth.lerp(partialTicks, prevForcedAngle, hourAngle);
		if (hourHand == null || hourHand.isStalled())
			partialTicks = 0;
		return Mth.lerp(partialTicks, hourAngle, hourAngle + getHourArmSpeed());
	}

	@Override
	public void onStall() {
		if (!level.isClientSide)
			sendData();
	}

	@Override
	public void remove() {
		if (!level.isClientSide)
			disassemble();
		super.remove();
	}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		if (!(contraption.getContraption() instanceof ClockworkContraption))
			return false;
		ClockworkContraption cc = (ClockworkContraption) contraption.getContraption();
		if (cc.handType == HandType.HOUR)
			return this.hourHand == contraption;
		else
			return this.minuteHand == contraption;
	}

	public boolean isRunning() {
		return running;
	}

	static enum ClockHands implements INamedIconOptions {

		HOUR_FIRST(AllIcons.I_HOUR_HAND_FIRST),
		MINUTE_FIRST(AllIcons.I_MINUTE_HAND_FIRST),
		HOUR_FIRST_24(AllIcons.I_HOUR_HAND_FIRST_24),

		;

		private String translationKey;
		private AllIcons icon;

		private ClockHands(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.clockwork." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

	@Override
	public BlockPos getBlockPosition() {
		return worldPosition;
	}

	public void setAngle(float forcedAngle) {
		hourAngle = forcedAngle;
	}
}
