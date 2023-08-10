package com.simibubi.create.content.kinetics.steamEngine;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class SteamEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	protected ScrollOptionBehaviour<RotationDirection> movementDirection;

	public WeakReference<PoweredShaftBlockEntity> target;
	public WeakReference<FluidTankBlockEntity> source;

	public SteamEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		source = new WeakReference<>(null);
		target = new WeakReference<>(null);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
			CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new SteamEngineValueBox());
		movementDirection.onlyActiveWhen(() -> {
			PoweredShaftBlockEntity shaft = getShaft();
			return shaft == null || !shaft.hasSource();
		});
		movementDirection.withCallback($ -> onDirectionChanged());
		behaviours.add(movementDirection);

		registerAwardables(behaviours, AllAdvancements.STEAM_ENGINE);
	}

	private void onDirectionChanged() {}

	@Override
	public void tick() {
		super.tick();
		FluidTankBlockEntity tank = getTank();
		PoweredShaftBlockEntity shaft = getShaft();

		if (tank == null || shaft == null) {
			if (level.isClientSide())
				return;
			if (shaft == null)
				return;
			if (!shaft.getBlockPos()
				.subtract(worldPosition)
				.equals(shaft.enginePos))
				return;
			if (shaft.engineEfficiency == 0)
				return;
			Direction facing = SteamEngineBlock.getFacing(getBlockState());
			if (level.isLoaded(worldPosition.relative(facing.getOpposite())))
				shaft.update(worldPosition, 0, 0);
			return;
		}

		boolean verticalTarget = false;
		BlockState shaftState = shaft.getBlockState();
		Axis targetAxis = Axis.X;
		if (shaftState.getBlock()instanceof IRotate ir)
			targetAxis = ir.getRotationAxis(shaftState);
		verticalTarget = targetAxis == Axis.Y;

		BlockState blockState = getBlockState();
		if (!AllBlocks.STEAM_ENGINE.has(blockState))
			return;
		Direction facing = SteamEngineBlock.getFacing(blockState);
		if (facing.getAxis() == Axis.Y)
			facing = blockState.getValue(SteamEngineBlock.FACING);

		float efficiency = Mth.clamp(tank.boiler.getEngineEfficiency(tank.getTotalTankSize()), 0, 1);
		if (efficiency > 0)

			award(AllAdvancements.STEAM_ENGINE);

		int conveyedSpeedLevel =
			efficiency == 0 ? 1 : verticalTarget ? 1 : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
		if (targetAxis == Axis.Z)
			conveyedSpeedLevel *= -1;
		if (movementDirection.get() == RotationDirection.COUNTER_CLOCKWISE)
			conveyedSpeedLevel *= -1;

		float shaftSpeed = shaft.getTheoreticalSpeed();
		if (shaft.hasSource() && shaftSpeed != 0 && conveyedSpeedLevel != 0
			&& (shaftSpeed > 0) != (conveyedSpeedLevel > 0)) {
			movementDirection.setValue(1 - movementDirection.get()
				.ordinal());
			conveyedSpeedLevel *= -1;
		}

		shaft.update(worldPosition, conveyedSpeedLevel, efficiency);

		if (!level.isClientSide)
			return;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::spawnParticles);
	}

	@Override
	public void remove() {
		PoweredShaftBlockEntity shaft = getShaft();
		if (shaft != null)
			shaft.remove(worldPosition);
		super.remove();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	public PoweredShaftBlockEntity getShaft() {
		PoweredShaftBlockEntity shaft = target.get();
		if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy(worldPosition)) {
			if (shaft != null)
				target = new WeakReference<>(null);
			Direction facing = SteamEngineBlock.getFacing(getBlockState());
			BlockEntity anyShaftAt = level.getBlockEntity(worldPosition.relative(facing, 2));
			if (anyShaftAt instanceof PoweredShaftBlockEntity ps && ps.canBePoweredBy(worldPosition))
				target = new WeakReference<>(shaft = ps);
		}
		return shaft;
	}

	public FluidTankBlockEntity getTank() {
		FluidTankBlockEntity tank = source.get();
		if (tank == null || tank.isRemoved()) {
			if (tank != null)
				source = new WeakReference<>(null);
			Direction facing = SteamEngineBlock.getFacing(getBlockState());
			BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
			if (be instanceof FluidTankBlockEntity tankBe)
				source = new WeakReference<>(tank = tankBe);
		}
		if (tank == null)
			return null;
		return tank.getControllerBE();
	}

	float prevAngle = 0;

	@OnlyIn(Dist.CLIENT)
	private void spawnParticles() {
		Float targetAngle = getTargetAngle();
		PoweredShaftBlockEntity ste = target.get();
		if (ste == null)
			return;
		if (!ste.isPoweredBy(worldPosition) || ste.engineEfficiency == 0)
			return;
		if (targetAngle == null)
			return;

		float angle = AngleHelper.deg(targetAngle);
		angle += (angle < 0) ? -180 + 75 : 360 - 75;
		angle %= 360;

		PoweredShaftBlockEntity shaft = getShaft();
		if (shaft == null || shaft.getSpeed() == 0)
			return;

		if (angle >= 0 && !(prevAngle > 180 && angle < 180)) {
			prevAngle = angle;
			return;
		}
		if (angle < 0 && !(prevAngle < -180 && angle > -180)) {
			prevAngle = angle;
			return;
		}

		FluidTankBlockEntity sourceBE = source.get();
		if (sourceBE != null) {
			FluidTankBlockEntity controller = sourceBE.getControllerBE();
			if (controller != null && controller.boiler != null) {
				float volume = 3f / Math.max(2, controller.boiler.attachedEngines / 6);
				float pitch = 1.18f - level.random.nextFloat() * .25f;
				level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
					SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, volume, pitch, false);
				AllSoundEvents.STEAM.playAt(level, worldPosition, volume / 16, .8f, false);
			}
		}

		Direction facing = SteamEngineBlock.getFacing(getBlockState());

		Vec3 offset = VecHelper.rotate(new Vec3(0, 0, 1).add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1)
			.multiply(1, 1, 0)
			.normalize()
			.scale(.5f)), AngleHelper.verticalAngle(facing), Axis.X);
		offset = VecHelper.rotate(offset, AngleHelper.horizontalAngle(facing), Axis.Y);
		Vec3 v = offset.scale(.5f)
			.add(Vec3.atCenterOf(worldPosition));
		Vec3 m = offset.subtract(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(.75f));
		level.addParticle(new SteamJetParticleData(1), v.x, v.y, v.z, m.x, m.y, m.z);

		prevAngle = angle;
	}

	@Nullable
	@OnlyIn(Dist.CLIENT)
	public Float getTargetAngle() {
		float angle = 0;
		BlockState blockState = getBlockState();
		if (!AllBlocks.STEAM_ENGINE.has(blockState))
			return null;

		Direction facing = SteamEngineBlock.getFacing(blockState);
		PoweredShaftBlockEntity shaft = getShaft();
		Axis facingAxis = facing.getAxis();
		Axis axis = Axis.Y;

		if (shaft == null)
			return null;

		axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
		angle = KineticBlockEntityRenderer.getAngleForTe(shaft, shaft.getBlockPos(), axis);

		if (axis == facingAxis)
			return null;
		if (axis.isHorizontal() && (facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE))
			angle *= -1;
		if (axis == Axis.X && facing == Direction.DOWN)
			angle *= -1;
		return angle;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		PoweredShaftBlockEntity shaft = getShaft();
		return shaft == null ? false : shaft.addToEngineTooltip(tooltip, isPlayerSneaking);
	}

}
