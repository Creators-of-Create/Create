package com.simibubi.create.content.logistics.block.depot;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.MathHelper;

public class EjectorInstance extends ShaftInstance implements IDynamicInstance {

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

		if (MathHelper.equal(lidProgress, lastProgress)) return;

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

		MatrixStack ms = new MatrixStack();

		EjectorRenderer.applyLidAngle(tile, angle, MatrixTransformStack.of(ms).translate(getInstancePosition()));

		plate.setTransform(ms);
	}
}
