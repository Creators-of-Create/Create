package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.render.light.GridAlignedBB;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class StabilizedLighter extends ContraptionLighter<StabilizedContraption> {
    public StabilizedLighter(StabilizedContraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.fromAABB(contraption.bounds);

        // TODO: this whole thing is a hack and is not generalizable
        Iterable<Entity> allEntities = ((ClientWorld) contraption.entity.world).getAllEntities();

        for (Entity entity : allEntities) {

            if (entity.getUniqueID() == contraption.parentID && entity instanceof AbstractContraptionEntity) {
                Contraption mountedOn = ((AbstractContraptionEntity) entity).getContraption();

                GridAlignedBB mountedBounds = GridAlignedBB.fromAABB(mountedOn.bounds);

                Vec3i dir = contraption.getFacing().getDirectionVec();

                int mulX = 1 - Math.abs(dir.getX());
                int mulY = 1 - Math.abs(dir.getY());
                int mulZ = 1 - Math.abs(dir.getZ());

                bb.minX -= mulX * mountedBounds.sizeX();
                bb.minY -= mulY * mountedBounds.sizeY();
                bb.minZ -= mulZ * mountedBounds.sizeZ();
                bb.maxX += mulX * mountedBounds.sizeX();
                bb.maxY += mulY * mountedBounds.sizeY();
                bb.maxZ += mulZ * mountedBounds.sizeZ();

                break;
            }
        }

        bb.translate(contraption.anchor);

        return bb;
    }
}
