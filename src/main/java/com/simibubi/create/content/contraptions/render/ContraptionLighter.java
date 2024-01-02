package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.light.GPULightVolume;
import com.jozufozu.flywheel.lib.light.LightListener;
import com.jozufozu.flywheel.lib.light.LightUpdater;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

public abstract class ContraptionLighter<C extends Contraption> implements LightListener {
    protected final C contraption;
    public final GPULightVolume lightVolume;
	protected final LightUpdater lightUpdater;

	protected final MutableBox bounds;

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

	public abstract MutableBox getContraptionBounds();

	@Override
	public boolean isInvalid() {
		return lightVolume.isInvalid();
	}

	@Override
	public void onLightUpdate(LightLayer type, SectionPos pos) {
		lightVolume.onLightUpdate(type, pos);
	}

    protected static void growBoundsForEdgeData(MutableBox bounds) {
        // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.grow(2);
	}

	@Override
	public Box getVolume() {
		return bounds;
	}

	public void delete() {
		lightUpdater.removeListener(this);
		lightVolume.delete();
	}
}
