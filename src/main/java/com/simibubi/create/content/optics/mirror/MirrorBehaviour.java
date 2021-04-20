package com.simibubi.create.content.optics.mirror;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MirrorBehaviour extends TileEntityBehaviour implements ILightHandler {
	public static final BehaviourType<MirrorBehaviour> TYPE = new BehaviourType<>();
	private final MirrorTileEntity mirror;
	protected float angle;
	protected float clientAngleDiff;
	Map<Beam, Beam> beams;
	private float prevAngle;
	@Nullable
	private BeaconTileEntity beacon;
	private Beam beaconBeam = null;
	private boolean isUpdating = false;


	public MirrorBehaviour(MirrorTileEntity te) {
		super(te);
		mirror = te;
		beacon = null;
		beams = new HashMap<>();
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		nbt.putFloat("Angle", angle);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		angle = nbt.getFloat("Angle");
		super.read(nbt, clientPacket);
	}

	public float getInterpolatedAngle(float partialTicks) {
		if (tileEntity.isVirtual())
			return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
		if (mirror.movementMode.get() == RotationMode.ROTATE_LIMITED && Math.abs(angle) == 90)
			return angle;
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	public float getAngularSpeed() {
		float speed = mirror.getSpeed() * 3 / 10f;
		if (mirror.getSpeed() == 0)
			speed = 0;
		if (getHandlerWorld() != null && getHandlerWorld().isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	@Override
	public void tick() {
		super.tick();

		prevAngle = angle;
		if (getHandlerWorld() != null && getHandlerWorld().isRemote)
			clientAngleDiff /= 2;

		float angularSpeed = getAngularSpeed();
		float newAngle = angle + angularSpeed;
		angle = newAngle % 360;

		if (mirror.movementMode.get() == RotationMode.ROTATE_LIMITED)
			angle = MathHelper.clamp(angle, -90, 90);
		if (mirror.movementMode.get() == RotationMode.ROTATE_45 && angle == prevAngle) // don't snap while still rotating
			angle = 45F * Math.round(Math.round(angle) / 45F);


		if (angle != prevAngle) {
			updateBeams();
		}

		if (beacon != null && beacon.isRemoved())
			updateBeaconState();
	}

	private void updateBeaconState() {
		BeaconTileEntity beaconBefore = beacon;
		beacon = BeaconHelper.getBeaconTE(getBlockPos(), getHandlerWorld())
				.orElse(null);

		if (beaconBefore != null) {
			beaconBeam.clear();
			beaconBeam = null;
			updateBeams();
		}

		if (beacon != null) {
			beaconBeam = constructOutBeam(null, VecHelper.UP, beacon.getPos());
			if (beaconBeam != null && !beaconBeam.isEmpty()) {
				beaconBeam.addListener(this);
				beaconBeam.onCreated();
			}
		}
	}

	@Override
	public void updateBeams() {
		if (isUpdating)
			return;
		isUpdating = true;

		Map<Beam, Beam> newBeams = new HashMap<>();
		for (Map.Entry<Beam, Beam> entry : new HashSet<>(beams.entrySet())) {
			entry.getValue()
					.onRemoved();
			if (entry.getKey()
					.isRemoved())
				continue;
			Beam reflected = reflectBeam(entry.getKey());
			if (reflected != null && !reflected.isEmpty()) {
				newBeams.put(entry.getKey(), reflected);
				reflected.onCreated();
				entry.getKey()
						.addListener(this);
			}
		}
		beams = newBeams;
		isUpdating = false;
	}

	private Vector3d getReflectionAngle(Vector3d inputAngle) {
		inputAngle = inputAngle.normalize();
		Direction.Axis axis = mirror.getAxis();
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

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBeaconState();
		updateBeams();
	}

	@Override
	public TileEntity getTile() {
		return tileEntity;
	}

	@Nonnull
	@Override
	public Direction getBeamRotationAround() {
		return Direction.getFacingFromAxisDirection(mirror.getAxis(), Direction.AxisDirection.POSITIVE);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public Iterator<Beam> getRenderBeams() {
		Iterator<Beam> beaconIter = beaconBeam == null ? Collections.emptyIterator() : Collections.singleton(beaconBeam)
				.iterator();
		return Iterators.concat(beams.values()
				.iterator(), beaconIter);
	}

	@Override
	public Stream<Beam> constructSubBeams(Beam beam) {
		if (beams.keySet()
				.stream()
				.filter(((Predicate<Beam>) Beam::isRemoved).negate())
				.map(Beam::getDirection)
				.filter(Objects::nonNull)
				.anyMatch(b -> b.equals(beam.getDirection())))
			return Stream.empty();
		Beam reflected = reflectBeam(beam);
		if (reflected != null && !reflected.isEmpty()) {
			beams.put(beam, reflected);
			beam.addListener(this);
			return Stream.of(reflected);
		}
		return Stream.empty();
	}

	@Nullable
	private Beam reflectBeam(Beam beam) {
		Vector3d inDir = beam.getDirection();
		if (inDir == null)
			return null;

		return constructOutBeam(beam, getReflectionAngle(inDir).normalize());
	}
}
