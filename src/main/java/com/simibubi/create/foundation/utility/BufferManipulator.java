package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public abstract class BufferManipulator {

	public static final int FORMAT_LENGTH = DefaultVertexFormats.BLOCK.getSize();
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

	protected static int vertexCount(ByteBuffer buffer) {
		return buffer.limit() / FORMAT_LENGTH;
	}

	protected static int getBufferPosition(int vertexIndex) {
		return vertexIndex * FORMAT_LENGTH;
	}

	protected static float getX(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index));
	}

	protected static float getY(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 4);
	}

	protected static float getZ(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 8);
	}

	protected static byte getR(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 12);
	}

	protected static byte getG(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 13);
	}

	protected static byte getB(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 14);
	}

	protected static byte getA(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 15);
	}

	protected static void putPos(ByteBuffer buffer, int index, float x, float y, float z) {
		int pos = getBufferPosition(index);
		buffer.putFloat(pos, x);
		buffer.putFloat(pos + 4, y);
		buffer.putFloat(pos + 8, z);
	}

	protected static float rotateX(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? x * cos + z * sin : axis == Axis.Z ? x * cos - y * sin : x;
	}

	protected static float rotateY(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? y : axis == Axis.Z ? y * cos + x * sin : y * cos - z * sin;
	}

	protected static float rotateZ(float x, float y, float z, float sin, float cos, Axis axis) {
		return axis == Axis.Y ? z * cos - x * sin : axis == Axis.Z ? z : z * cos + y * sin;
	}

	protected static void putLight(ByteBuffer buffer, int index, int packedLight) {
		buffer.putInt(getBufferPosition(index) + 24, packedLight);
	}

	protected static void putColor(ByteBuffer buffer, int index, byte r, byte g, byte b, byte a) {
		int bufferPosition = getBufferPosition(index);
		buffer.put(bufferPosition + 12, r);
		buffer.put(bufferPosition + 13, g);
		buffer.put(bufferPosition + 14, b);
		buffer.put(bufferPosition + 15, a);
	}

	public ByteBuffer getTranslated(float xIn, float yIn, float zIn, int packedLightCoords) {
		original.rewind();
		mutable.rewind();

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			putPos(mutable, vertex, getX(original, vertex) + xIn, getY(original, vertex) + yIn,
					getZ(original, vertex) + zIn);
			putLight(mutable, vertex, packedLightCoords);
		}

		return mutable;
	}

	public static ByteBuffer remanipulateBuffer(ByteBuffer buffer, float x, float y, float z, float xOrigin,
			float yOrigin, float zOrigin, float yaw, float pitch) {
		buffer.rewind();

		float cosYaw = MathHelper.cos(yaw);
		float sinYaw = MathHelper.sin(yaw);
		float cosPitch = MathHelper.cos(pitch);
		float sinPitch = MathHelper.sin(pitch);

		for (int vertex = 0; vertex < vertexCount(buffer); vertex++) {
			float xL = getX(buffer, vertex) - xOrigin;
			float yL = getY(buffer, vertex) - yOrigin;
			float zL = getZ(buffer, vertex) - zOrigin;

			float xL2 = rotateX(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float yL2 = rotateY(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float zL2 = rotateZ(xL, yL, zL, sinPitch, cosPitch, Axis.X);

			xL = rotateX(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			yL = rotateY(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			zL = rotateZ(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);

			float xPos = xL + x + xOrigin;
			float yPos = yL + y + yOrigin;
			float zPos = zL + z + zOrigin;
			putPos(buffer, vertex, xPos, yPos, zPos);
		}

		return buffer;
	}

	public static ByteBuffer recolorBuffer(ByteBuffer buffer, int color) {
		buffer.rewind();

		boolean defaultColor = color == -1;
		int b = defaultColor ? 128 : color & 0xFF;
		int g = defaultColor ? 128 : (color >> 8) & 0xFF;
		int r = defaultColor ? 128 : (color >> 16) & 0xFF;

		for (int vertex = 0; vertex < vertexCount(buffer); vertex++) {
			float lum = 1;

			int r2 = (int) (r * lum);
			int g2 = (int) (g * lum);
			int b2 = (int) (b * lum);
			putColor(buffer, vertex, (byte) r2, (byte) g2, (byte) b2, (byte) 255);
		}

		return buffer;
	}

}
