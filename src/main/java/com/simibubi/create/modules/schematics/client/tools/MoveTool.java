package com.simibubi.create.modules.schematics.client.tools;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.schematics.client.SchematicTransformation;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class MoveTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (!schematicSelected)
			return;

		renderSelectedFace = selectedFace.getAxis().isHorizontal();
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		if (!schematicSelected || !selectedFace.getAxis().isHorizontal())
			return true;

		SchematicTransformation transformation = schematicHandler.getTransformation();
		Vec3d vec = new Vec3d(selectedFace.getDirectionVec()).scale(-Math.signum(delta));
		vec = vec.mul(transformation.getMirrorModifier(Axis.X), 1, transformation.getMirrorModifier(Axis.Z));
		vec = VecHelper.rotate(vec, transformation.getRotationTarget(), Axis.Y);
		transformation.move((float) vec.x, 0, (float) vec.z);
		schematicHandler.markDirty();
		
		return true;
	}

}
