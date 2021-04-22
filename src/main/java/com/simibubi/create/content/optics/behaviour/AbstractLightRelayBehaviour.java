package com.simibubi.create.content.optics.behaviour;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

public abstract class AbstractLightRelayBehaviour<T extends SmartTileEntity & ILightHandler.ILightHandlerProvider> extends AbstractLightHandlingBehaviour<T> {
	private boolean isUpdating;

	protected AbstractLightRelayBehaviour(T te, LightHandlingbehaviourProperties properties) {
		super(te, properties);
		isUpdating = false;
	}

	@Override
	public void updateBeams() {
		if (isUpdating)
			return;
		isUpdating = true;

		Set<Beam> oldBeams = new HashSet<>(beams);
		beams.clear();
		for (Beam child : oldBeams) {
			if (child.isNew()) {
				beams.add(child);
				continue;
			}
			Beam parent = child.getParent();
			child.onRemoved();
			if (parent != null && !parent.isRemoved())
				constructSubBeams(parent).forEach(Beam::onCreated);
		}
		isUpdating = false;
	}

	@Override
	public Stream<Beam> constructSubBeams(Beam beam) {
		if (beams.stream()
				.map(Beam::getParent)
				.filter(Objects::nonNull)
				.filter(((Predicate<Beam>) Beam::isRemoved).negate())
				.map(Beam::getDirection)
				.filter(Objects::nonNull)
				.anyMatch(b -> b.equals(beam.getDirection())))
			return Stream.empty();
		return constructSubBeams(beam, beams);
	}

	public Stream<Beam> constructSubBeams(Beam beam, Set<Beam> beamListing) {
		return safeConstructSubBeamsFor(beam)
				.filter(Objects::nonNull)
				.filter(((Predicate<Beam>) Beam::isEmpty).negate())
				.peek(beamListing::add);
	}

	@Override
	public Iterator<Beam> getRenderBeams() {
		return Iterators.concat(beams.iterator(), super.getRenderBeams());
	}

	protected abstract Stream<Beam> safeConstructSubBeamsFor(Beam beam);
}
