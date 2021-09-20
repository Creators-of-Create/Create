package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.BasicProvider;
import com.jozufozu.flywheel.light.GPULightVolume;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.ILightUpdateListener;
import com.jozufozu.flywheel.light.ImmutableBox;
import com.jozufozu.flywheel.light.LightProvider;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.light.LightVolume;
import com.jozufozu.flywheel.light.ListenerStatus;
import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public abstract class ContraptionLighter<C extends Contraption> implements ILightUpdateListener {
    protected final C contraption;
    public final GPULightVolume lightVolume;
	protected final LightUpdater lightUpdater;

	protected GridAlignedBB bounds;

    protected boolean scheduleRebuild;

    protected ContraptionLighter(C contraption) {
        this.contraption = contraption;
		this.lightUpdater = LightUpdater.get(contraption.entity.level);

        bounds = getContraptionBounds();

        lightVolume = new GPULightVolume(bounds);

        lightVolume.initialize(BasicProvider.get(contraption.entity.level));
        scheduleRebuild = true;

		lightUpdater.addListener(this);
    }

    public void tick(RenderedContraption owner) {
        if (scheduleRebuild) {
            lightVolume.initialize(BasicProvider.get(owner.contraption.entity.level));
            scheduleRebuild = false;
        }
    }

    public abstract GridAlignedBB getContraptionBounds();

	@Override
	public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
		ILightUpdateListener.super.onLightPacket(world, chunkX, chunkZ);
	}

	@Override
	public ImmutableBox getVolume() {
		return lightVolume;
	}

	@Override
	public ListenerStatus status() {
		return ListenerStatus.OKAY;
	}

	@Override
	public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changed) {
		lightVolume.onLightUpdate(world, type, changed);
	}

    protected GridAlignedBB contraptionBoundsToVolume(GridAlignedBB bounds) {
        bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.setMinY(Math.max(bounds.getMinY(), 0));
        bounds.setMaxY(Math.min(bounds.getMaxY(), 255));

        return bounds;
    }

	public GridAlignedBB getBounds() {
		return bounds;
	}
}
