package com.simibubi.create.content.optics.aligner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AtomicDouble;
import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.behaviour.AbstractLightHandlingBehaviour;
import com.simibubi.create.content.optics.behaviour.LightHandlingbehaviourProperties;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class AlignerBehaviour extends AbstractLightHandlingBehaviour<AlignerTileEntity> {
	public static final BehaviourType<AlignerBehaviour> TYPE = new BehaviourType<>();
	@Nullable
	protected Beam collectedBeam = null;
	boolean updating = false;

	protected AlignerBehaviour(AlignerTileEntity te) {
		super(te, LightHandlingbehaviourProperties.create()
				.withScansBeacons(true)
				.withAbsorbsLight(true));
	}

	@Override
	public void updateBeams() {
		super.updateBeams();
		if (updating)
			return;
		updating = true;

		AtomicDouble r = new AtomicDouble();
		AtomicDouble g = new AtomicDouble();
		AtomicDouble b = new AtomicDouble();

		beams = beams.stream()
				.filter(Objects::nonNull)
				.filter(((Predicate<Beam>) Beam::isRemoved).negate())
				.peek(beam -> {
					float[] localColor = beam.getColorAt(getBlockPos());
					r.addAndGet(localColor[0] * localColor[0]);
					g.addAndGet(localColor[1] * localColor[1]);
					b.addAndGet(localColor[2] * localColor[2]);
				})
				.collect(Collectors.toSet());

		if (collectedBeam != null) {
			collectedBeam.onRemoved();
			collectedBeam = null;
		}

		if (!beams.isEmpty()) {
			collectedBeam = constructOutBeam(null, getFacingVec(), getBlockPos(), new float[]{(float) Math.sqrt(r.get() / beams.size()), (float) Math.sqrt(g.get() / beams.size()), (float) Math.sqrt(b.get() / beams.size())});
			if (collectedBeam != null && !collectedBeam.isEmpty()) {
				collectedBeam.addListener(this);
				collectedBeam.onCreated();
			}
		}

		updating = false;
	}

	@Nonnull
	@Override
	public Direction getBeamRotationAround() {
		return Direction.getFacingFromAxisDirection(getFacing().getAxis(), Direction.AxisDirection.POSITIVE);
	}

	private Direction getFacing() {
		return tileEntity.getBlockState()
				.get(BlockStateProperties.FACING);
	}

	private Vector3d getFacingVec() {
		return Vector3d.of(getFacing().getDirectionVec());
	}

	@Override
	public Stream<Beam> constructSubBeams(Beam beam) {
		Vector3d beamDir = beam.getDirection();
		if (!beam.isRemoved() && beamDir != null && AngleHelper.deg(Math.cos(beamDir.dotProduct(getFacingVec()))) < 60) {
			beams.add(beam);
			requestBeamUpdate();
		}

		return Stream.empty();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public Iterator<Beam> getRenderBeams() {
		return Iterators.concat(super.getRenderBeams(), collectedBeam == null ? Collections.emptyIterator() : Collections.singleton(collectedBeam)
				.iterator());
	}
}
