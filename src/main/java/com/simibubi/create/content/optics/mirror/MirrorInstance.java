package com.simibubi.create.content.optics.mirror;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.foundation.render.backend.core.OrientedData;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class MirrorInstance extends KineticTileInstance<MirrorTileEntity> implements IDynamicInstance {
	final Vector3f rotationAxis;
	final OrientedData instance;

	public MirrorInstance(InstancedTileRenderer<?> modelManager, MirrorTileEntity tile) {
		super(modelManager, tile);

		rotationAxis = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis)
				.getUnitVector();
		instance = getOrientedMaterial().getModel(AllBlockPartials.MIRROR_PLANE, tile.getBlockState())
				.createInstance();
		instance.setPosition(getInstancePosition());
	}

	@Override
	public void beginFrame() {
		instance.setRotation(tile.getHandler()
				.getBufferedRotationQuaternion());
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, instance);
	}

	@Override
	public void remove() {
		instance.delete();
	}
}
