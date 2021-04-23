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

import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractLightHandlingBehaviour<T extends SmartTileEntity & ILightHandler.ILightHandlerProvider> extends TileEntityBehaviour implements ILightHandler {
	protected final T handler;
	private final LightHandlingBehaviourProperties properties;
	protected Set<Beam> beams;
	@Nullable
	protected Beam beaconBeam = null;
	@Nullable
	protected BeaconTileEntity beacon;
	boolean needsBeamUpdate = false;

	protected AbstractLightHandlingBehaviour(T te, LightHandlingBehaviourProperties properties) {
		super(te);
		this.handler = te;
		this.properties = properties;
		beams = new HashSet<>();
	}

	@Override
	public void tick() {
		super.tick();
		if (properties.scansBeacon && beacon != null && beacon.isRemoved())
			updateBeaconState();
		if (needsBeamUpdate && canUpdateFast())
			updateBeams();
	}


	protected void updateBeaconState() {
		beacon = BeaconHelper.getBeaconTE(getBlockPos(), getHandlerWorld())
				.orElse(null);

		if (beaconBeam != null) {
			beaconBeam.clear();
			beaconBeam = null;
			requestBeamUpdate();
		}

		if (beacon != null) {
			beaconBeam = constructOutBeam(null, VecHelper.UP, beacon.getPos(), DyeColor.WHITE.getColorComponentValues());
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
		if (properties.scansBeacon)
			updateBeaconState();
		if (canUpdateFast())
			requestBeamUpdate();
		else
			updateBeams();
	}

	public void requestBeamUpdate() {
		needsBeamUpdate = true;
	}

	@Override
	public void updateBeams() {
		needsBeamUpdate = false;
	}

	@Override
	public Iterator<Beam> getRenderBeams() {
		return beaconBeam == null ? Collections.emptyIterator() : Collections.singleton(beaconBeam)
				.iterator();
	}

	public Set<Beam> getBeams() {
		return beams;
	}

	@Override
	public void remove() {
		beams.forEach(Beam::onRemoved);
		if (beaconBeam != null)
			beaconBeam.onRemoved();
	}

	@Override
	public boolean absorbsLight() {
		return properties.absorbsLight;
	}

	@Override
	public int getMaxScanRange() {
		return properties.scanRange;
	}

	protected boolean canUpdateFast() {
		return true;
	}
}
