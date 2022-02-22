package com.simibubi.create.content.logistics.block.depot;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Mth;

public class EjectorInstance extends ShaftInstance implements DynamicInstance {

	protected final EjectorTileEntity tile;

	protected final ModelData plate;

	private float lastProgress = Float.NaN;

	public EjectorInstance(MaterialManager dispatcher, EjectorTileEntity tile) {
		super(dispatcher, tile);
		this.tile = tile;

		plate = getTransformMaterial().getModel(AllBlockPartials.EJECTOR_TOP, blockState).createInstance();

		pivotPlate();
	}

	@Override
	public void beginFrame() {
		float lidProgress = getLidProgress();

		if (Mth.equal(lidProgress, lastProgress)) return;

		pivotPlate(lidProgress);
		lastProgress = lidProgress;
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, plate);
	}

	@Override
	public void remove() {
		super.remove();
		plate.delete();
	}

	private void pivotPlate() {
		pivotPlate(getLidProgress());
	}

	private float getLidProgress() {
		return tile.getLidProgress(AnimationTickHolder.getPartialTicks());
	}

	private void pivotPlate(float lidProgress) {
		float angle = lidProgress * 70;

		EjectorRenderer.applyLidAngle(tile, angle, plate.loadIdentity().translate(getInstancePosition()));
	}
}
