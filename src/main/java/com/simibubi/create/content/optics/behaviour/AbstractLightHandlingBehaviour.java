package com.simibubi.create.content.optics.behaviour;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.content.optics.Beam;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractLightHandlingBehaviour<T extends SmartTileEntity & ILightHandler.ILightHandlerProvider> extends TileEntityBehaviour implements ILightHandler {
	protected final T handler;
	protected Set<Beam> beams;
	protected Beam beaconBeam = null;
	@Nullable
	protected BeaconTileEntity beacon;

	protected AbstractLightHandlingBehaviour(T te) {
		super(te);
		this.handler = te;
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
	public Iterator<Beam> getRenderBeams() {
		return beaconBeam == null ? Collections.emptyIterator() : Collections.singleton(beaconBeam)
				.iterator();
	}

	public Set<Beam> getBeams() {
		return beams;
	}
}
