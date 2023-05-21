package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.outliner.AABBOutline;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FlipTool extends PlacementToolBase {

	private AABBOutline outline = new AABBOutline(new AABB(BlockPos.ZERO));

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
	public void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer) {
		if (!schematicSelected || !selectedFace.getAxis()
			.isHorizontal()) {
			super.renderOnSchematic(ms, buffer);
			return;
		}

		Direction facing = selectedFace.getClockWise();
		AABB bounds = schematicHandler.getBounds();

		Vec3 directionVec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, facing.getAxis())
			.getNormal());
		Vec3 boundsSize = new Vec3(bounds.getXsize(), bounds.getYsize(), bounds.getZsize());
		Vec3 vec = boundsSize.multiply(directionVec);
		bounds = bounds.contract(vec.x, vec.y, vec.z)
			.inflate(1 - directionVec.x, 1 - directionVec.y, 1 - directionVec.z);
		bounds = bounds.move(directionVec.scale(.5f)
			.multiply(boundsSize));
		
		outline.setBounds(bounds);
		AllSpecialTextures tex = AllSpecialTextures.CHECKERED;
		outline.getParams()
			.lineWidth(1 / 16f)
			.disableLineNormals()
			.colored(0xdddddd)
			.withFaceTextures(tex, tex);
		outline.render(ms, buffer, Vec3.ZERO, AnimationTickHolder.getPartialTicks());
		
		super.renderOnSchematic(ms, buffer);
	}

}
