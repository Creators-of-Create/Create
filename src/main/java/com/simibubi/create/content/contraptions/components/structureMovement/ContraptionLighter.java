package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.GPULightVolume;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.ImmutableBox;
import com.jozufozu.flywheel.light.LightListener;
import com.jozufozu.flywheel.light.LightProvider;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.light.ListenerStatus;

import net.minecraft.world.level.LightLayer;

public abstract class ContraptionLighter<C extends Contraption> implements LightListener {
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
    public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changed) {
        lightVolume.onLightUpdate(world, type, changed);
    }

    @Override
    public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
        lightVolume.onLightPacket(world, chunkX, chunkZ);
    }

    protected void growBoundsForEdgeData() {
        // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.grow(2);
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
