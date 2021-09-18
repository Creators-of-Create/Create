package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PulleyLighter extends ContraptionLighter<PulleyContraption> {
    public PulleyLighter(PulleyContraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {

        GridAlignedBB bounds = GridAlignedBB.from(contraption.bounds);

        Level world = contraption.entity.level;

        BlockPos.MutableBlockPos pos = contraption.anchor.mutable();
        while (!AllBlocks.ROPE_PULLEY.has(world.getBlockState(pos)) && pos.getY() < 256) {
            pos.move(0, 1, 0);
        }

        bounds.translate(pos);
        bounds.setMinY(1); // the super constructor will take care of making this 0

        return bounds;
    }
}
