package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TranslationConstructVertexBuffer extends BufferManipulator {

	public TranslationConstructVertexBuffer(ByteBuffer original) {
		super(original);
	}

	public ByteBuffer getTransformed(TileEntity te, float x, float y, float z, Vec3d offset) {
		original.rewind();
		mutable.rewind();

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			float xL = getX(original, vertex);
			float yL = getY(original, vertex);
			float zL = getZ(original, vertex);
			putPos(mutable, vertex, xL + x, yL + y, zL + z);
			BlockPos pos = new BlockPos(offset.x + xL, offset.y + yL, offset.z + zL);
			putLight(mutable, vertex, te.getWorld().getCombinedLight(pos, 0));
		}

		return mutable;
	}

}
