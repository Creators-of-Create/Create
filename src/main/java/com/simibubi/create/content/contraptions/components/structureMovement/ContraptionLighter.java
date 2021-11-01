package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.*;

import net.minecraft.world.LightType;

public abstract class ContraptionLighter<C extends Contraption> implements ILightUpdateListener {
    protected final C contraption;
    public final GPULightVolume lightVolume;
	protected final LightUpdater lightUpdater;

	protected final GridAlignedBB bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;
		lightUpdater = LightUpdater.get(contraption.entity.level);

		bounds = getContraptionBounds();
		growBoundsForEdgeData();

		lightVolume = new GPULightVolume(bounds);

		lightVolume.initialize(lightUpdater.getProvider());
		scheduleRebuild = true;

		lightUpdater.addListener(this);
	}

	public abstract GridAlignedBB getContraptionBounds();

	@Override
	public ListenerStatus status() {
		return ListenerStatus.OKAY;
	}

	@Override
    public void onLightUpdate(LightProvider world, LightType type, ImmutableBox changed) {
        lightVolume.onLightUpdate(world, type, changed);
    }

    @Override
    public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
        lightVolume.onLightPacket(world, chunkX, chunkZ);
    }

    protected void growBoundsForEdgeData() {
        bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.setMinY(Math.max(bounds.getMinY(), 0));
        bounds.setMaxY(Math.min(bounds.getMaxY(), 255));
	}

	@Override
	public ImmutableBox getVolume() {
		return bounds;
	}

	public void delete() {
		lightUpdater.removeListener(this);
		lightVolume.delete();
	}
}
