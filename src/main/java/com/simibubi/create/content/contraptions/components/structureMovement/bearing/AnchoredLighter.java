package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;

public class AnchoredLighter extends ContraptionLighter<Contraption> {

    public AnchoredLighter(Contraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.from(contraption.bounds);
        bb.translate(contraption.anchor);
        return bb;
    }
}
