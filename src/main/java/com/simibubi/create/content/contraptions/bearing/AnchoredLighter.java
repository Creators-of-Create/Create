package com.simibubi.create.content.contraptions.bearing;

import com.jozufozu.flywheel.lib.box.MutableBox;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;

public class AnchoredLighter extends ContraptionLighter<Contraption> {

    public AnchoredLighter(Contraption contraption) {
        super(contraption);
    }

    @Override
    public MutableBox getContraptionBounds() {
        MutableBox bb = MutableBox.from(contraption.bounds);
        bb.translate(contraption.anchor);
        return bb;
    }
}
