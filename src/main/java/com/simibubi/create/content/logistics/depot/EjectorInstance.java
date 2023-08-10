package com.simibubi.create.content.logistics.depot;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftInstance;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.minecraft.util.Mth;

public class EjectorInstance extends ShaftInstance<EjectorBlockEntity> implements DynamicInstance {

	protected final ModelData plate;

	private float lastProgress = Float.NaN;

	public EjectorInstance(MaterialManager dispatcher, EjectorBlockEntity blockEntity) {
		super(dispatcher, blockEntity);

		plate = getTransformMaterial().getModel(AllPartialModels.EJECTOR_TOP, blockState).createInstance();

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
		return blockEntity.getLidProgress(AnimationTickHolder.getPartialTicks());
	}

	private void pivotPlate(float lidProgress) {
		float angle = lidProgress * 70;

		//EjectorRenderer.applyLidAngle(blockEntity, angle, plate.loadIdentity().translate(getInstancePosition()));//TODO flw
	}
}
