package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

import net.minecraft.world.level.block.state.BlockState;

public class SingleRotatingInstance<T extends KineticBlockEntity> extends KineticBlockEntityInstance<T> {

	protected RotatingData rotatingModel;

	public SingleRotatingInstance(MaterialManager materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public void init() {
		rotatingModel = setup(getModel().createInstance());
	}

	@Override
	public void update() {
		updateRotation(rotatingModel);
	}

	@Override
	public void updateLight() {
		relight(pos, rotatingModel);
	}

	@Override
	public void remove() {
		rotatingModel.delete();
	}

	protected BlockState getRenderedBlockState() {
		return blockState;
	}

	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(getRenderedBlockState());
	}
}
