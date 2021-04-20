package com.simibubi.create.content.optics.behaviour;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.content.optics.ILightHandlerProvider;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class LightHandlingBehaviour<T extends SmartTileEntity & ILightHandlerProvider> extends TileEntityBehaviour implements ILightHandler {
	protected final T handler;
	public Set<Beam> beams;
	private boolean isUpdating;
	@Nullable
	private BeaconTileEntity beacon;
	private Beam beaconBeam = null;

	public LightHandlingBehaviour(T te) {
		super(te);
		handler = te;
		isUpdating = false;
		beams = new HashSet<>();
	}

	@Override
	public void tick() {
		super.tick();
		if (beacon != null && beacon.isRemoved())
			updateBeaconState();
	}

	protected void updateBeaconState() {
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
	public TileEntity getTile() {
		return tileEntity;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBeaconState();
		updateBeams();
	}

	@Override
	public void updateBeams() {
		if (isUpdating)
			return;
		isUpdating = true;

		Set<Beam> newBeams = new HashSet<>();
		for (Beam child : new HashSet<>(beams)) {
			Beam parent = child.getParent();

			child.onRemoved();
			if (parent == null || parent.isRemoved())
				continue;
			constructSubBeams(parent, newBeams).forEach(Beam::onCreated);
		}
		beams = newBeams;
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

	protected abstract Stream<Beam> safeConstructSubBeamsFor(Beam beam);

	@Override
	public Iterator<Beam> getRenderBeams() {
		Iterator<Beam> beaconIter = beaconBeam == null ? Collections.emptyIterator() : Collections.singleton(beaconBeam)
				.iterator();
		return Iterators.concat(beams.iterator(), beaconIter);
	}
}
