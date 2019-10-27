package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ContraptionVertexBuffer extends BufferManipulator {

	public ContraptionVertexBuffer(ByteBuffer original) {
		super(original);
	}

	public ByteBuffer getTranslated(World world, float x, float y, float z, Vec3d offset) {
		original.rewind();
		mutable.rewind();

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			float xL = getX(original, vertex);
			float yL = getY(original, vertex);
			float zL = getZ(original, vertex);
			putPos(mutable, vertex, xL + x + (float) offset.x, yL + y + (float) offset.y, zL + z + (float) offset.z);
			BlockPos pos = new BlockPos(offset.x + xL, offset.y + yL, offset.z + zL);
			putLight(mutable, vertex, world.getCombinedLight(pos, 0));
		}

		return mutable;
	}

	public ByteBuffer getTranslatedAndRotated(World world, float x, float y, float z, float yaw, float pitch,
			Vec3d offset, Vec3d rotationOffset) {
		original.rewind();
		mutable.rewind();

		float cosYaw = MathHelper.cos(yaw);
		float sinYaw = MathHelper.sin(yaw);
		float cosPitch = MathHelper.cos(pitch);
		float sinPitch = MathHelper.sin(pitch);

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			float xL = getX(original, vertex) - (float) rotationOffset.x;
			float yL = getY(original, vertex) - (float) rotationOffset.y;
			float zL = getZ(original, vertex) - (float) rotationOffset.z;

			float xL2 = rotateX(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float yL2 = rotateY(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float zL2 = rotateZ(xL, yL, zL, sinPitch, cosPitch, Axis.X);
//
			xL = rotateX(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			yL = rotateY(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			zL = rotateZ(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);

			float xPos = xL + x + (float) (offset.x + rotationOffset.x);
			float yPos = yL + y + (float) (offset.y + rotationOffset.y);
			float zPos = zL + z + (float) (offset.z + rotationOffset.z);
			putPos(mutable, vertex, xPos, yPos, zPos);
			BlockPos pos = new BlockPos(xL + rotationOffset.x - .5f, yL + rotationOffset.y - .5f,
					zL + rotationOffset.z - .5f);
			putLight(mutable, vertex, world.getCombinedLight(pos, 15));
		}

		return mutable;
	}

}
