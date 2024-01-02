package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.light.TickingLightListener;
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

		Box contraptionBounds = getContraptionBounds();

		if (bounds.sameAs(contraptionBounds, 2)) {
			return false;
		}
		bounds.assign(contraptionBounds);
		growBoundsForEdgeData(bounds);

		lightVolume.move(bounds);

		return true;
	}

	@Override
    public MutableBox getContraptionBounds() {
        MutableBox bb = MutableBox.from(contraption.bounds);

        bb.translate(contraption.entity.blockPosition());

        return bb;
    }
}
