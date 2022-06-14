package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.BackHalfShaftInstance;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BearingInstance<B extends KineticTileEntity & IBearingTileEntity> extends BackHalfShaftInstance implements DynamicInstance {
	final B bearing;

	final OrientedData topInstance;

	final Vector3f rotationAxis;
	final Quaternion blockOrientation;

	public BearingInstance(MaterialManager modelManager, B tile) {
		super(modelManager, tile);
		this.bearing = tile;

		Direction facing = blockState.getValue(BlockStateProperties.FACING);
		rotationAxis = Direction.get(Direction.AxisDirection.POSITIVE, axis).step();

		blockOrientation = getBlockStateOrientation(facing);

		PartialModel top =
				bearing.isWoodenTop() ? AllBlockPartials.BEARING_TOP_WOODEN : AllBlockPartials.BEARING_TOP;

		topInstance = getOrientedMaterial().getModel(top, blockState).createInstance();

		topInstance.setPosition(getInstancePosition()).setRotation(blockOrientation);
	}

	@Override
	public void beginFrame() {

		float interpolatedAngle = bearing.getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1);
		Quaternion rot = rotationAxis.rotationDegrees(interpolatedAngle);

		rot.mul(blockOrientation);

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
			orientation = Vector3f.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
		} else {
			orientation = Quaternion.ONE.copy();
		}

		orientation.mul(Vector3f.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
		return orientation;
	}
}
