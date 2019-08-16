package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;

public abstract class BufferManipulator {

	protected static final int FORMAT_LENGTH = DefaultVertexFormats.BLOCK.getSize();
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

	protected int vertexCount(ByteBuffer buffer) {
		return buffer.limit() / FORMAT_LENGTH;
	}
	
	protected int getBufferPosition(int vertexIndex) {
		return vertexIndex * FORMAT_LENGTH;
	}
	
	protected float getX(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index));
	}
	
	protected float getY(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 4);
	}
	
	protected float getZ(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 8);
	}
	
	protected byte getR(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 12);
	}
	
	protected byte getG(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 13);
	}
	
	protected byte getB(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 14);
	}
	
	protected byte getA(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 15);
	}

	protected void putPos(ByteBuffer buffer, int index, float x, float y, float z) {
		int pos = getBufferPosition(index);
		buffer.putFloat(pos, x);
		buffer.putFloat(pos + 4, y);
		buffer.putFloat(pos + 8, z);
	}

	protected float rotateX(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? x * cos + z * sin : axis == Axis.Z ? x * cos - y * sin : x;
	}

	protected float rotateY(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? y : axis == Axis.Z ? y * cos + x * sin : y * cos - z * sin;
	}

	protected float rotateZ(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? z * cos - x * sin : axis == Axis.Z ? z : z * cos + y * sin;
	}
	
	protected void putLight(ByteBuffer buffer, int index, int packedLight) {
		buffer.putInt(getBufferPosition(index) + 24, packedLight);
	}
	
	protected void putColor(ByteBuffer buffer, int index, byte r, byte g, byte b, byte a) {
		int bufferPosition = getBufferPosition(index);
		buffer.put(bufferPosition + 12, r);
		buffer.put(bufferPosition + 13, g);
		buffer.put(bufferPosition + 14, b);
		buffer.put(bufferPosition + 15, a);
	}

}
