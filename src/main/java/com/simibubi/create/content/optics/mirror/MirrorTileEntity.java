package com.simibubi.create.content.optics.mirror;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirrorTileEntity extends KineticTileEntity implements ILightHandler<MirrorTileEntity> {
	protected float angle;
	protected float clientAngleDiff;
	Map<Beam, Beam> beams;
	private float prevAngle;
	@Nullable
	private BeaconTileEntity beacon;
	private Beam beaconBeam = null;

	public MirrorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		beacon = null;
		beams = new HashMap<>();
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

		if (angle != prevAngle) {
			updateBeams();
		}

		if (beacon != null && beacon.isRemoved())
			updateBeaconState();
	}

	private void updateBeaconState() {
		BeaconTileEntity beaconBefore = beacon;
		beacon = BeaconHelper.getBeaconTE(pos, world)
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
		Map<Beam, Beam> newBeams = new HashMap<>();
		for (Map.Entry<Beam, Beam> entry : beams.entrySet()) {
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
	}

	private Vector3d getReflectionAngle(Vector3d inputAngle) {
		inputAngle = inputAngle.normalize();
		Vector3d normal = new Matrix3d().asIdentity()
				.asAxisRotation(getBlockState().get(BlockStateProperties.AXIS), AngleHelper.rad(angle))
				.transform(VecHelper.UP);
		return inputAngle.subtract(normal.scale(2 * inputAngle.dotProduct(normal)));
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBeaconState();
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

	@Nonnull
	@Override
	public Direction getBeamRotationAround() {
		return Direction.getFacingFromAxisDirection(getBlockState().get(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 256.0D;
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

		Vector3d outDir = getReflectionAngle(inDir).normalize();

		if (inDir.subtract(outDir)
				.normalize() == Vector3d.ZERO)
			return null;
		return constructOutBeam(beam, outDir);
	}
}
