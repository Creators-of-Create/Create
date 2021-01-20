package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.render.light.GridAlignedBB;

public class BearingLighter extends ContraptionLighter<BearingContraption> {

    public BearingLighter(BearingContraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB localBounds = GridAlignedBB.fromAABB(contraption.bounds);
        localBounds.rotate45(contraption.getFacing().getAxis());
        localBounds.translate(contraption.anchor);
        return localBounds;
    }
}
