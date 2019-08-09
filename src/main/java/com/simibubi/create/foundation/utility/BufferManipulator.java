package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class BufferManipulator {
	
	protected ByteBuffer original;
	protected ByteBuffer mutable;

	public BufferManipulator(ByteBuffer original) {
		original.rewind();
		this.original = original;

		this.mutable = GLAllocation.createDirectByteBuffer(original.capacity());
		this.mutable.order(original.order());
		this.mutable.limit(original.limit());
		mutable.put(this.original);
		mutable.rewind();
	}
	
	protected void forEachVertex(ByteBuffer buffer, Consumer<Integer> consumer) {
		final int formatLength = DefaultVertexFormats.BLOCK.getSize();
		for (int i = 0; i < buffer.limit() / formatLength; i++) {
			final int position = i * formatLength;
			consumer.accept(position);
		}
	}
	
	protected Vec3d getPos(ByteBuffer buffer, int vertex) {
		return new Vec3d(buffer.getFloat(vertex), buffer.getFloat(vertex + 4), buffer.getFloat(vertex + 8));
	}

	protected void putPos(ByteBuffer buffer, int vertex, Vec3d pos) {
		buffer.putFloat(vertex, (float) pos.x);
		buffer.putFloat(vertex + 4, (float) pos.y);
		buffer.putFloat(vertex + 8, (float) pos.z);
	}

	protected Vec3d rotatePos(Vec3d pos, float angle, Axis axis) {
		return rotatePos(pos, MathHelper.sin(angle), MathHelper.cos(angle), axis);
	}

	protected Vec3d rotatePos(Vec3d pos, float sin, float cos, Axis axis) {
		final float x = (float) pos.x;
		final float y = (float) pos.y;
		final float z = (float) pos.z;

		if (axis == Axis.X)
			return new Vec3d(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vec3d(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vec3d(x * cos - y * sin, y * cos + x * sin, z);

		return pos;
	}
	
}
