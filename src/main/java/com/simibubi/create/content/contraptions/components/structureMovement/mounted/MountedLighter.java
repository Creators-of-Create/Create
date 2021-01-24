package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.render.light.GridAlignedBB;

public class MountedLighter extends ContraptionLighter<MountedContraption> {
    public MountedLighter(MountedContraption contraption) {
        super(contraption);
    }

    @Override
    public void tick(RenderedContraption owner) {
        GridAlignedBB contraptionBounds = getContraptionBounds();

        if (!contraptionBounds.sameAs(bounds)) {
            lightVolume.move(contraption.entity.world, contraptionBoundsToVolume(contraptionBounds));
            bounds = contraptionBounds;
        }
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.fromAABB(contraption.bounds);

        bb.translate(contraption.entity.getPosition());

        return bb;
    }
}
