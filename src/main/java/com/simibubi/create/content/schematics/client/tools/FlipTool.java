package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class FlipTool extends PlacementToolBase {

	private AABBOutline outline = new AABBOutline(new AxisAlignedBB(BlockPos.ZERO));

	@Override
	public void init() {
		super.init();
		renderSelectedFace = false;
	}

	@Override
	public boolean handleRightClick() {
		mirror();
		return true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		mirror();
		return true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	private void mirror() {
		if (schematicSelected && selectedFace.getAxis()
			.isHorizontal()) {
			schematicHandler.getTransformation()
				.flip(selectedFace.getAxis());
			schematicHandler.markDirty();
		}
	}

	@Override
	public void renderOnSchematic(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		if (!schematicSelected || !selectedFace.getAxis()
			.isHorizontal()) {
			super.renderOnSchematic(ms, buffer);
			return;
		}

		Direction facing = selectedFace.getClockWise();
		AxisAlignedBB bounds = schematicHandler.getBounds();

		Vector3d directionVec = Vector3d.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, facing.getAxis())
			.getNormal());
		Vector3d boundsSize = new Vector3d(bounds.getXsize(), bounds.getYsize(), bounds.getZsize());
		Vector3d vec = boundsSize.multiply(directionVec);
		bounds = bounds.contract(vec.x, vec.y, vec.z)
			.inflate(1 - directionVec.x, 1 - directionVec.y, 1 - directionVec.z);
		bounds = bounds.move(directionVec.scale(.5f)
			.multiply(boundsSize));
		
		outline.setBounds(bounds);
		AllSpecialTextures tex = AllSpecialTextures.CHECKERED;
		outline.getParams()
			.lineWidth(1 / 16f)
			.disableNormals()
			.colored(0xdddddd)
			.withFaceTextures(tex, tex);
		outline.render(ms, buffer, AnimationTickHolder.getPartialTicks());
		
		super.renderOnSchematic(ms, buffer);
	}

}
