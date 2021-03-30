package com.simibubi.create.content.contraptions.components.structureMovement;

import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;
import com.simibubi.create.foundation.render.backend.light.LightUpdateListener;
import com.simibubi.create.foundation.render.backend.light.LightUpdater;
import com.simibubi.create.foundation.render.backend.light.LightVolume;

public abstract class ContraptionLighter<C extends Contraption> implements LightUpdateListener {
    protected final C contraption;
    public final LightVolume lightVolume;

    protected GridAlignedBB bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;

        bounds = getContraptionBounds();

        lightVolume = new LightVolume(contraptionBoundsToVolume(bounds.copy()));

        lightVolume.initialize(contraption.entity.world);
        scheduleRebuild = true;

        startListening();
    }

    public void tick(RenderedContraption owner) {
        if (scheduleRebuild) {
            lightVolume.initialize(owner.contraption.entity.world);
            scheduleRebuild = false;
        }
    }

    public abstract GridAlignedBB getContraptionBounds();

    @Override
    public boolean onLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changed) {
        lightVolume.notifyLightUpdate(world, type, changed);
        return false;
    }

    @Override
    public boolean onLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {
        lightVolume.notifyLightPacket(world, chunkX, chunkZ);
        return false;
    }

    protected void startListening() {
        LightUpdater.getInstance().startListening(bounds, this);
    }

    protected GridAlignedBB contraptionBoundsToVolume(GridAlignedBB bounds) {
        bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.minY = Math.max(bounds.minY, 0);
        bounds.maxY = Math.min(bounds.maxY, 255);

        return bounds;
    }
}
