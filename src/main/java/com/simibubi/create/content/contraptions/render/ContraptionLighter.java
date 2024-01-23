package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.light.GPULightVolume;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

public abstract class ContraptionLighter<C extends Contraption> {
    protected final C contraption;
    public final GPULightVolume lightVolume;

	protected final MutableBox bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;

		bounds = getContraptionBounds();
		growBoundsForEdgeData(bounds);

		lightVolume = new GPULightVolume(contraption.entity.level(), bounds);

		lightVolume.initialize();
		scheduleRebuild = true;
	}

	public abstract MutableBox getContraptionBounds();

	public boolean isInvalid() {
		return lightVolume.isInvalid();
	}

	public void onLightUpdate(LightLayer type, SectionPos pos) {
		lightVolume.onLightUpdate(type, pos);
	}

    protected static void growBoundsForEdgeData(MutableBox bounds) {
        // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.grow(2);
	}

	public Box getVolume() {
		return bounds;
	}

	public void delete() {
		lightVolume.delete();
	}
}
