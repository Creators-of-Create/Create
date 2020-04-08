package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;

import javax.vecmath.Matrix4f;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;

public class SuperByteBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	public static final int FORMAT_LENGTH = DefaultVertexFormats.BLOCK.getSize();
	protected ByteBuffer original;
	protected ByteBuffer mutable;

	// Vertex Position
	private Matrix4f transforms;
	private Matrix4f t;

	// Vertex Texture Coords
	private boolean shouldShiftUV;
	private boolean resetUV;
	private SpriteShiftEntry spriteShift;
	private float uTarget, vTarget;

	// Vertex Lighting
	private boolean shouldLight;
	private IVertexLighter vertexLighter;
	private float lightOffsetX, lightOffsetY, lightOffsetZ;
	private int packedLightCoords;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private float sheetSize;

	public SuperByteBuffer(ByteBuffer original) {
		original.rewind();
		this.original = original;

		this.mutable = GLAllocation.createDirectByteBuffer(original.capacity());
		this.mutable.order(original.order());
		this.mutable.limit(original.limit());
		mutable.put(this.original);
		mutable.rewind();

		t = new Matrix4f();
		transforms = new Matrix4f();
		transforms.setIdentity();
	}

	public ByteBuffer build() {
		original.rewind();
		mutable.rewind();
		float x, y, z = 0;
		float x2, y2, z2 = 0;

		Matrix4f t = transforms;
		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			x = getX(original, vertex);
			y = getY(original, vertex);
			z = getZ(original, vertex);

			x2 = t.m00 * x + t.m01 * y + t.m02 * z + t.m03;
			y2 = t.m10 * x + t.m11 * y + t.m12 * z + t.m13;
			z2 = t.m20 * x + t.m21 * y + t.m22 * z + t.m23;

			putPos(mutable, vertex, x2, y2, z2);

			if (shouldColor) {
				byte lumByte = getR(original, vertex);
				float lum = (lumByte < 0 ? 255 + lumByte : lumByte) / 256f;
				int r2 = (int) (r * lum);
				int g2 = (int) (g * lum);
				int b2 = (int) (b * lum);
				putColor(mutable, vertex, (byte) r2, (byte) g2, (byte) b2, (byte) a);
			}

			if (shouldShiftUV) {
				float u = getU(original, vertex);
				float v = getV(original, vertex);
				float targetU = spriteShift.getTarget()
						.getInterpolatedU((spriteShift.getOriginal().getUnInterpolatedU(u) / sheetSize) + uTarget * 16);
				float targetV = spriteShift.getTarget()
						.getInterpolatedV((spriteShift.getOriginal().getUnInterpolatedV(v) / sheetSize) + vTarget * 16);
				putUV(mutable, vertex, targetU, targetV);
			}

			if (resetUV)
				putUV(mutable, vertex, getU(original, vertex), getV(original, vertex));

			if (shouldLight) {
				if (vertexLighter != null)
					putLight(mutable, vertex,
							vertexLighter.getPackedLight(x2 + lightOffsetX, y2 + lightOffsetY, z2 + lightOffsetZ));
				else
					putLight(mutable, vertex, packedLightCoords);
			}
		}

		t.setIdentity();
		shouldShiftUV = false;
		shouldColor = false;
		shouldLight = false;
		return mutable;
	}

	public void renderInto(BufferBuilder buffer) {
		if (original.limit() == 0)
			return;
		buffer.putBulkData(build());
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.m03 += x;
		transforms.m13 += y;
		transforms.m23 += z;
		return this;
	}

	public SuperByteBuffer rotate(Axis axis, float angle) {
		if (angle == 0)
			return this;
		t.setIdentity();
		if (axis == Axis.X)
			t.rotX(angle);
		else if (axis == Axis.Y)
			t.rotY(angle);
		else
			t.rotZ(angle);
		transforms.mul(t, transforms);
		return this;
	}

	public SuperByteBuffer rotateCentered(Axis axis, float angle) {
		return translate(-.5f, -.5f, -.5f).rotate(axis, angle).translate(.5f, .5f, .5f);
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		shouldShiftUV = true;
		resetUV = false;
		spriteShift = entry;
		uTarget = 0;
		vTarget = 0;
		sheetSize = 1;
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		shouldShiftUV = true;
		resetUV = false;
		spriteShift = entry;
		this.uTarget = uTarget;
		this.vTarget = vTarget;
		this.sheetSize = sheetSize;
		return this;
	}

	public SuperByteBuffer dontShiftUV() {
		shouldShiftUV = false;
		resetUV = true;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		shouldLight = true;
		vertexLighter = null;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(IVertexLighter lighter) {
		shouldLight = true;
		vertexLighter = lighter;
		return this;
	}

	public SuperByteBuffer offsetLighting(double x, double y, double z) {
		lightOffsetX = (float) x;
		lightOffsetY = (float) y;
		lightOffsetZ = (float) z;
		return this;
	}

	public SuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
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

	protected float getU(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 16);
	}

	protected float getV(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 20);
	}

	protected void putPos(ByteBuffer buffer, int index, float x, float y, float z) {
		int pos = getBufferPosition(index);
		buffer.putFloat(pos, x);
		buffer.putFloat(pos + 4, y);
		buffer.putFloat(pos + 8, z);
	}

	protected void putUV(ByteBuffer buffer, int index, float u, float v) {
		int pos = getBufferPosition(index);
		buffer.putFloat(pos + 16, u);
		buffer.putFloat(pos + 20, v);
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
