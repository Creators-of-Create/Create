package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.light.GPULightVolume;
import com.jozufozu.flywheel.light.LightListener;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.world.level.LightLayer;

public abstract class ContraptionLighter<C extends Contraption> implements LightListener {
    protected final C contraption;
    public final GPULightVolume lightVolume;
	protected final LightUpdater lightUpdater;

	protected final GridAlignedBB bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;
		lightUpdater = LightUpdater.get(contraption.entity.level());

		bounds = getContraptionBounds();
		growBoundsForEdgeData(bounds);

		lightVolume = new GPULightVolume(contraption.entity.level(), bounds);

		lightVolume.initialize();
		scheduleRebuild = true;

		lightUpdater.addListener(this);
	}

	public abstract GridAlignedBB getContraptionBounds();

	@Override
	public boolean isListenerInvalid() {
		return lightVolume.isListenerInvalid();
	}

	@Override
    public void onLightUpdate(LightLayer type, ImmutableBox changed) {
        lightVolume.onLightUpdate(type, changed);
    }

    @Override
    public void onLightPacket(int chunkX, int chunkZ) {
        lightVolume.onLightPacket(chunkX, chunkZ);
    }

    protected static void growBoundsForEdgeData(GridAlignedBB bounds) {
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
