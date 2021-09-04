package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.*;

import net.minecraft.world.LightType;

public abstract class ContraptionLighter<C extends Contraption> implements ILightUpdateListener {
    protected final C contraption;
    public final LightVolume lightVolume;
	protected final LightUpdater lightUpdater;

	protected GridAlignedBB bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;
		lightUpdater = LightUpdater.get(contraption.entity.level);

		bounds = getContraptionBounds();

		lightVolume = new LightVolume(contraptionBoundsToVolume(bounds.copy()));

		lightVolume.initialize(contraption.entity.level);
		scheduleRebuild = true;

		lightUpdater.addListener(this);

		lightVolume.initialize(this.contraption.entity.level);
	}

	public abstract GridAlignedBB getContraptionBounds();

	@Override
	public ListenerStatus status() {
		return ListenerStatus.OKAY;
	}

	@Override
    public void onLightUpdate(LightProvider world, LightType type, GridAlignedBB changed) {
        lightVolume.notifyLightUpdate(world, type, changed);
    }

    @Override
    public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
        lightVolume.notifyLightPacket(world, chunkX, chunkZ);
    }

    protected GridAlignedBB contraptionBoundsToVolume(GridAlignedBB bounds) {
        bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.minY = Math.max(bounds.minY, 0);
        bounds.maxY = Math.min(bounds.maxY, 255);

        return bounds;
    }

	@Override
	public GridAlignedBB getVolume() {
		return bounds;
	}
}
