package com.simibubi.create.content.contraptions.pulley;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.processing.burner.ScrollInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;

public class HosePulleyVisual extends AbstractPulleyVisual<HosePulleyBlockEntity> {
	public HosePulleyVisual(VisualizationContext dispatcher, HosePulleyBlockEntity blockEntity, float partialTick) {
		super(dispatcher, blockEntity, partialTick);
	}

	@Override
	protected Instancer<TransformedInstance> getRopeModel() {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE));
	}

	@Override
	protected Instancer<TransformedInstance> getMagnetModel() {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_MAGNET));
	}

	@Override
	protected Instancer<TransformedInstance> getHalfMagnetModel() {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_HALF_MAGNET));
	}

	@Override
	protected Instancer<ScrollInstance> getCoilModel() {
		return instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(AllPartialModels.HOSE_COIL));
	}

	@Override
	protected Instancer<TransformedInstance> getHalfRopeModel() {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_HALF));
	}

	@Override
	protected float getOffset(float pt) {
		return blockEntity.getInterpolatedOffset(pt);
	}

	@Override
	protected boolean isRunning() {
		return true;
	}
	
	@Override
	protected SpriteShiftEntry getCoilAnimation() {
		return AllSpriteShifts.HOSE_PULLEY_COIL;
	}
	
}
