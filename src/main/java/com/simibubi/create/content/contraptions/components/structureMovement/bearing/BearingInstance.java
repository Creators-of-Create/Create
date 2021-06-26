package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.BackHalfShaftInstance;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class BearingInstance<B extends KineticTileEntity & IBearingTileEntity> extends BackHalfShaftInstance implements IDynamicInstance {
	final B bearing;

	final OrientedData topInstance;

	final Vector3f rotationAxis;
	final Quaternion blockOrientation;

	public BearingInstance(MaterialManager<?> modelManager, B tile) {
		super(modelManager, tile);
		this.bearing = tile;

		Direction facing = blockState.get(BlockStateProperties.FACING);
		rotationAxis = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector();

		blockOrientation = getBlockStateOrientation(facing);

		PartialModel top =
				bearing.isWoodenTop() ? AllBlockPartials.BEARING_TOP_WOODEN : AllBlockPartials.BEARING_TOP;

		topInstance = getOrientedMaterial().getModel(top, blockState).createInstance();

		topInstance.setPosition(getInstancePosition()).setRotation(blockOrientation);
	}

	@Override
	public void beginFrame() {

		float interpolatedAngle = bearing.getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1);
		Quaternion rot = rotationAxis.getDegreesQuaternion(interpolatedAngle);

		rot.multiply(blockOrientation);

		topInstance.setRotation(rot);
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, topInstance);
	}

	@Override
	public void remove() {
		super.remove();
		topInstance.delete();
	}

	static Quaternion getBlockStateOrientation(Direction facing) {
		Quaternion orientation;

		if (facing.getAxis().isHorizontal()) {
			orientation = Vector3f.POSITIVE_Y.getDegreesQuaternion(AngleHelper.horizontalAngle(facing.getOpposite()));
		} else {
			orientation = Quaternion.IDENTITY.copy();
		}

		orientation.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90 - AngleHelper.verticalAngle(facing)));
		return orientation;
	}
}
