package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class SpeedControllerTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	public ScrollValueBehaviour targetSpeed;
	public AbstractComputerBehaviour computerBehaviour;

	boolean hasBracket;

	public SpeedControllerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		hasBracket = false;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBracket();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

		targetSpeed =
			new ScrollValueBehaviour(Lang.translateDirect("generic.speed"), this, new ControllerValueBoxTransform());
		targetSpeed.between(-max, max);
		targetSpeed.value = DEFAULT_SPEED;
		targetSpeed.moveText(new Vec3(9, 0, 10));
		targetSpeed.withUnit(i -> Lang.translateDirect("generic.unit.rpm"));
		targetSpeed.withCallback(i -> this.updateTargetRotation());
		targetSpeed.withStepFunction(CreativeMotorTileEntity::step);
		behaviours.add(targetSpeed);
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));

		registerAwardables(behaviours, AllAdvancements.SPEED_CONTROLLER);
	}

	private void updateTargetRotation() {
		if (hasNetwork())
			getOrCreateNetwork().remove(this);
		RotationPropagator.handleRemoved(level, worldPosition, this);
		removeSource();
		attachKinetics();

		if (isCogwheelPresent() && getSpeed() != 0)
			award(AllAdvancements.SPEED_CONTROLLER);
	}

	public static float getConveyedSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
		boolean targetingController) {
		if (!(speedControllerIn instanceof SpeedControllerTileEntity))
			return 0;

		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();
		float desiredOutputSpeed = getDesiredOutputSpeed(cogWheel, speedControllerIn, targetingController);

		float compareSpeed = targetingController ? speed : wheelSpeed;
		if (desiredOutputSpeed >= 0 && compareSpeed >= 0)
			return Math.max(desiredOutputSpeed, compareSpeed);
		if (desiredOutputSpeed < 0 && compareSpeed < 0)
			return Math.min(desiredOutputSpeed, compareSpeed);

		return desiredOutputSpeed;
	}

	public static float getDesiredOutputSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
		boolean targetingController) {
		SpeedControllerTileEntity speedController = (SpeedControllerTileEntity) speedControllerIn;
		float targetSpeed = speedController.targetSpeed.getValue();
		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();

		if (targetSpeed == 0)
			return 0;
		if (targetingController && wheelSpeed == 0)
			return 0;
		if (!speedController.hasSource()) {
			if (targetingController)
				return targetSpeed;
			return 0;
		}

		boolean wheelPowersController = speedController.source.equals(cogWheel.getBlockPos());

		if (wheelPowersController) {
			if (targetingController)
				return targetSpeed;
			return wheelSpeed;
		}

		if (targetingController)
			return speed;
		return targetSpeed;
	}

	public void updateBracket() {
		if (level != null && level.isClientSide)
			hasBracket = isCogwheelPresent();
	}

	private boolean isCogwheelPresent() {
		BlockState stateAbove = level.getBlockState(worldPosition.above());
		return ICogWheel.isDedicatedCogWheel(stateAbove.getBlock()) && ICogWheel.isLargeCog(stateAbove)
			&& stateAbove.getValue(CogWheelBlock.AXIS).isHorizontal();
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		computerBehaviour.removePeripheral();
	}

	private class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 11f, 16);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			if (direction.getAxis()
				.isVertical())
				return false;
			return state.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
		}

		@Override
		protected float getScale() {
			return 0.275f;
		}

	}

}
