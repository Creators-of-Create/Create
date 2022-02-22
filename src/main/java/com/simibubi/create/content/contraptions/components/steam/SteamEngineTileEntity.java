package com.simibubi.create.content.contraptions.components.steam;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankConnectivityHandler;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Debug;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class SteamEngineTileEntity extends SmartTileEntity {

	public WeakReference<PoweredShaftTileEntity> target;
	public WeakReference<FluidTankTileEntity> source;

	public SteamEngineTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		source = new WeakReference<>(null);
		target = new WeakReference<>(null);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void tick() {
		super.tick();
		FluidTankTileEntity tank = getTank();
		PoweredShaftTileEntity shaft = getShaft();
		if (tank == null || shaft == null)
			return;

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

		int score = Math.max(0, tank.boiler.engineScore);
		int conveyedSpeedLevel =
			verticalTarget ? score : (int) GeneratingKineticTileEntity.convertToDirection(score, facing);
		if (targetAxis == Axis.Z)
			conveyedSpeedLevel *= -1;
		shaft.update(worldPosition, conveyedSpeedLevel);

		if (!level.isClientSide)
			return;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::spawnParticles);
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		PoweredShaftTileEntity shaft = getShaft();
		if (shaft != null)
			shaft.remove(worldPosition);
		super.setRemovedNotDueToChunkUnload();
	}

	@Override
	public AABB getRenderBoundingBox() {
		return super.getRenderBoundingBox().inflate(2);
	}

	public PoweredShaftTileEntity getShaft() {
		PoweredShaftTileEntity shaft = target.get();
		if (shaft == null || shaft.isRemoved()) {
			if (shaft != null)
				target = new WeakReference<>(null);
			Direction facing = SteamEngineBlock.getFacing(getBlockState());
			BlockEntity anyShaftAt = level.getBlockEntity(worldPosition.relative(facing, 2));
			if (anyShaftAt instanceof PoweredShaftTileEntity ps)
				target = new WeakReference<>(shaft = ps);
		}
		return shaft;
	}

	public FluidTankTileEntity getTank() {
		FluidTankTileEntity tank = source.get();
		if (tank == null || tank.isRemoved()) {
			if (tank != null)
				source = new WeakReference<>(null);
			Direction facing = SteamEngineBlock.getFacing(getBlockState());
			FluidTankTileEntity anyTankAt =
				FluidTankConnectivityHandler.anyTankAt(level, worldPosition.relative(facing.getOpposite()));
			if (anyTankAt != null)
				source = new WeakReference<>(tank = anyTankAt);
		}
		if (tank == null)
			return null;
		return tank.getControllerTE();
	}

	float prevAngle = 0;

	@OnlyIn(Dist.CLIENT)
	private void spawnParticles() {
		Float targetAngle = getTargetAngle();
		if (targetAngle == null)
			return;

		float angle = AngleHelper.deg(targetAngle);
		angle += (angle < 0) ? -180 + 75 : 360 - 75;
		angle %= 360;

		PoweredShaftTileEntity shaft = getShaft();
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

		Direction facing = SteamEngineBlock.getFacing(getBlockState());

		for (int i = 0; i < 2; i++) {
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
		}

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
		PoweredShaftTileEntity shaft = getShaft();
		Axis facingAxis = facing.getAxis();
		Axis axis = Axis.Y;

		if (shaft == null)
			return null;

		axis = KineticTileEntityRenderer.getRotationAxisOf(shaft);
		angle = KineticTileEntityRenderer.getAngleForTe(shaft, shaft.getBlockPos(), axis);

		if (axis == facingAxis)
			return null;
		if (axis.isHorizontal() && (facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE))
			angle *= -1;
		if (axis == Axis.X && facing == Direction.DOWN)
			angle *= -1;
		return angle;
	}

}
