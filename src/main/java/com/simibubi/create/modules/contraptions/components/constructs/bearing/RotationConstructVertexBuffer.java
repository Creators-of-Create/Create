package com.simibubi.create.modules.contraptions.components.constructs.bearing;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class RotationConstructVertexBuffer extends BufferManipulator {

	public RotationConstructVertexBuffer(ByteBuffer original) {
		super(original);
	}

	public ByteBuffer getTransformed(TileEntity te, float x, float y, float z, float angle, Axis axis) {
		original.rewind();
		mutable.rewind();

		float cos = MathHelper.cos(angle);
		float sin = MathHelper.sin(angle);

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			float xL = getX(original, vertex) -.5f;
			float yL = getY(original, vertex) -.5f;
			float zL = getZ(original, vertex) -.5f;

			float xL2 = rotateX(xL, yL, zL, sin, cos, axis) + .5f;
			float yL2 = rotateY(xL, yL, zL, sin, cos, axis) + .5f;
			float zL2 = rotateZ(xL, yL, zL, sin, cos, axis) + .5f;

			putPos(mutable, vertex, xL2 + x, yL2 + y, zL2 + z);
			BlockPos pos = new BlockPos(te.getPos().getX() + xL2, te.getPos().getY() + yL2, te.getPos().getZ() + zL2);
			putLight(mutable, vertex, te.getWorld().getCombinedLight(pos, 0));
		}

		return mutable;
	}

}
