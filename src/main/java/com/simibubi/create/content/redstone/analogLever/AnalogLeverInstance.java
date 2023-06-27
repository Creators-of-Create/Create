package com.simibubi.create.content.redstone.analogLever;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.Rotate;
import com.jozufozu.flywheel.util.transform.Translate;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class AnalogLeverInstance extends BlockEntityInstance<AnalogLeverBlockEntity> implements DynamicInstance {

	protected final ModelData handle;
	protected final ModelData indicator;

	final float rX;
	final float rY;

	public AnalogLeverInstance(MaterialManager materialManager, AnalogLeverBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		Material<ModelData> mat = getTransformMaterial();

		handle = mat.getModel(AllPartialModels.ANALOG_LEVER_HANDLE, blockState)
			.createInstance();
		indicator = mat.getModel(AllPartialModels.ANALOG_LEVER_INDICATOR, blockState)
			.createInstance();

		transform(indicator);

		AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
		rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));

		animateLever();
	}

	@Override
	public void beginFrame() {
		if (!blockEntity.clientState.settled())
			animateLever();
	}

	protected void animateLever() {
		float state = blockEntity.clientState.getValue(AnimationTickHolder.getPartialTicks());

		indicator.setColor(Color.mixColors(0x2C0300, 0xCD0000, state / 15f));

		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);

		transform(handle.loadIdentity()).translate(1 / 2f, 1 / 16f, 1 / 2f)
			.rotate(Direction.EAST, angle)
			.translate(-1 / 2f, -1 / 16f, -1 / 2f);
	}

	@Override
	public void remove() {
		handle.delete();
		indicator.delete();
	}

	@Override
	public void updateLight() {
		relight(pos, handle, indicator);
	}

	private <T extends Translate<T> & Rotate<T>> T transform(T msr) {
		return msr.translate(getInstancePosition())
			.centre()
			.rotate(Direction.UP, (float) (rY / 180 * Math.PI))
			.rotate(Direction.EAST, (float) (rX / 180 * Math.PI))
			.unCentre();
	}
}
