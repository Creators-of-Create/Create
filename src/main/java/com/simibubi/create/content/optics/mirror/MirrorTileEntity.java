package com.simibubi.create.content.optics.mirror;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.BeamSegment;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MirrorTileEntity extends KineticTileEntity implements ILightHandler<MirrorTileEntity> {
	public final List<BeamSegment> beam;
	protected float angle;
	protected float clientAngleDiff;
	private float prevAngle;
	private Optional<BeaconTileEntity> beacon;
	private float[] initialColor = DyeColor.WHITE.getColorComponentValues();

	public MirrorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		beacon = Optional.empty();
		beam = new ArrayList<>();
		setLazyTickRate(20);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("Angle", angle);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);

		angle = compound.getFloat("Angle");
		super.fromTag(state, compound, clientPacket);
	}

	public float getInterpolatedAngle(float partialTicks) {
		if (isVirtual())
			return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	public float getAngularSpeed() {
		float speed = getSpeed() * 3 / 10f;
		if (getSpeed() == 0)
			speed = 0;
		if (world != null && world.isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	@Override
	public void tick() {
		super.tick();

		prevAngle = angle;
		if (world != null && world.isRemote)
			clientAngleDiff /= 2;

		float angularSpeed = getAngularSpeed();
		float newAngle = angle + angularSpeed;
		angle = newAngle % 360;

		if (angle != prevAngle)
			updateBeams();
	}

	private void updateBeams() {
		beacon = BeaconHelper.getBeaconTE(pos, world);
		beam.clear();
		beam.addAll(constructOutBeam(getReflectionAngle(VecHelper.UP)));
	}

	private Vector3d getReflectionAngle(Vector3d inputAngle) {
		inputAngle = inputAngle.normalize();
		Vector3d normal = new Matrix3d().asIdentity()
				.asAxisRotation(getAxis(), AngleHelper.rad(angle))
				.transform(inputAngle);
		return VecHelper.step(inputAngle.subtract(normal.scale(2 * inputAngle.dotProduct(normal))));
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBeams();
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}

	@Override
	public MirrorTileEntity getTile() {
		return this;
	}

	@Override
	public void setColor(float[] initialColor) {
		this.initialColor = initialColor;
	}

	@Override
	public float[] getSegmentStartColor() {
		return initialColor;
	}

	@Nonnull
	@Override
	public Direction getBeamRotationAround() {
		return Direction.getFacingFromAxisDirection(getAxis(), Direction.AxisDirection.POSITIVE);
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float forcedAngle) {
		angle = forcedAngle;
	}

	private Direction.Axis getAxis() {
		return getBlockState().get(BlockStateProperties.AXIS);
	}
}
