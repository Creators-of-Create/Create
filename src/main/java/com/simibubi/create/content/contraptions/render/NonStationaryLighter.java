package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.light.TickingLightListener;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.infrastructure.config.AllConfigs;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> implements TickingLightListener {
    public NonStationaryLighter(C contraption) {
        super(contraption);
    }

	@Override
	public boolean tickLightListener() {
		if (getVolume().volume() > AllConfigs.client().maxContraptionLightVolume.get())
			return false;

		ImmutableBox contraptionBounds = getContraptionBounds();

		if (bounds.sameAs(contraptionBounds, 2)) {
			return false;
		}
		bounds.assign(contraptionBounds);
		growBoundsForEdgeData(bounds);

		lightVolume.move(bounds);

		return true;
	}

	@Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.from(contraption.bounds);

        bb.translate(contraption.entity.blockPosition());

        return bb;
    }
}
