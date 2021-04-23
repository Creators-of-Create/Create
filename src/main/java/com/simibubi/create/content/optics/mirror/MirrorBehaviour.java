package com.simibubi.create.content.optics.mirror;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.behaviour.AbstractRotatedLightRelayBehaviour;
import com.simibubi.create.content.optics.behaviour.LightHandlingBehaviourProperties;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class MirrorBehaviour extends AbstractRotatedLightRelayBehaviour<MirrorTileEntity> {
	public static final BehaviourType<MirrorBehaviour> TYPE = new BehaviourType<>();

	@OnlyIn(Dist.CLIENT)
	@Nullable
	Quaternion bufferedRotationQuaternion = null;

	public MirrorBehaviour(MirrorTileEntity te) {
		super(te, LightHandlingBehaviourProperties.create()
				.withAbsorbsLight(true)
				.withScansBeacons(true));
	}

	private Vector3d getReflectionAngle(Vector3d inputAngle) {
		inputAngle = inputAngle.normalize();
		Direction.Axis axis = handler.getAxis();
		Vector3d normal;
		if (axis.isHorizontal())
			normal = new Matrix3d().asIdentity()
					.asAxisRotation(axis, AngleHelper.rad(angle))
					.transform(VecHelper.UP);
		else
			normal = new Matrix3d().asIdentity()
					.asAxisRotation(axis, AngleHelper.rad(-angle))
					.transform(VecHelper.SOUTH);

		return inputAngle.subtract(normal.scale(2 * inputAngle.dotProduct(normal)));
	}


	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public Quaternion getBufferedRotationQuaternion() {
		if (bufferedRotationQuaternion == null) {
			bufferedRotationQuaternion = getMirrorAxis().getUnitVector()
					.getDegreesQuaternion(getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1));
			bufferedRotationQuaternion.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(AngleHelper.horizontalAngle(getMirrorAxis())));
			bufferedRotationQuaternion.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(AngleHelper.verticalAngle(getMirrorAxis())));
		}
		return bufferedRotationQuaternion;
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		bufferedRotationQuaternion = null;
	}

	@Nonnull
	public Direction getMirrorAxis() {
		return Direction.getFacingFromAxisDirection(getAxis(), Direction.AxisDirection.POSITIVE);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	protected Stream<Beam> safeConstructSubBeamsFor(Beam beam) {
		Vector3d inDir = beam.getDirection();
		if (inDir == null)
			return Stream.empty();

		return Stream.of(constructOutBeam(beam, getReflectionAngle(inDir).normalize()));
	}

	@Override
	protected void onAngleChanged() {
		super.onAngleChanged();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bufferedRotationQuaternion = null);
	}
}
