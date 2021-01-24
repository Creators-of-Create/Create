package com.simibubi.create.foundation.render.light;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;

import static com.simibubi.create.foundation.render.RenderMath.nextPowerOf2;

public abstract class ContraptionLighter<C extends Contraption> {
    protected final C contraption;
    public final LightVolume lightVolume;

    protected GridAlignedBB bounds;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;

        bounds = getContraptionBounds();

        lightVolume = new LightVolume(contraptionBoundsToVolume(bounds));

        lightVolume.initialize(contraption.entity.world);
    }

    protected GridAlignedBB contraptionBoundsToVolume(GridAlignedBB bounds) {
        bounds = bounds.copy();
        bounds.grow(1); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.minY = Math.max(bounds.minY, 0);
        bounds.maxY = Math.min(bounds.maxY, 255);

        return bounds;
    }

    public void tick(RenderedContraption owner) {}

    public abstract GridAlignedBB getContraptionBounds();
}
