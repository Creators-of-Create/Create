package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.IMovingListener;
import com.jozufozu.flywheel.light.LightProvider;
import com.jozufozu.flywheel.light.ImmutableBox;
import com.simibubi.create.foundation.config.AllConfigs;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> implements IMovingListener {
    public NonStationaryLighter(C contraption) {
        super(contraption);
    }

	@Override
	public boolean update(LightProvider provider) {
		if (getVolume().volume() > AllConfigs.CLIENT.maxContraptionLightVolume.get())
			return false;

		ImmutableBox contraptionBounds = getContraptionBounds();

		if (bounds.sameAs(contraptionBounds)) {
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
}
