package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.IMovingListener;
import com.jozufozu.flywheel.light.LightProvider;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> implements IMovingListener {
	public NonStationaryLighter(C contraption) {
		super(contraption);
	}

	@Override
	public boolean update(LightProvider provider) {
		GridAlignedBB contraptionBounds = getContraptionBounds();

		if (!contraptionBounds.sameAs(bounds)) {
			return false;
		}
		bounds.assign(contraptionBounds);
		growBoundsForEdgeData();

		lightVolume.move(provider, bounds);
		return true;
	}

	@Override
	public GridAlignedBB getContraptionBounds() {
		GridAlignedBB bb = GridAlignedBB.from(contraption.bounds);
		bb.translate(contraption.entity.blockPosition());

		return bb;
	}

	protected void growBoundsForEdgeData() {
		bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
		bounds.setMinY(Math.max(bounds.getMinY(), 0));
		bounds.setMaxY(Math.min(bounds.getMaxY(), 255));
	}
}
