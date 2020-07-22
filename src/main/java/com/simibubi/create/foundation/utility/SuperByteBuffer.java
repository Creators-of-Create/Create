package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.BufferBuilder.DrawState;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class SuperByteBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	public static final int FORMAT_LENGTH = DefaultVertexFormats.BLOCK.getSize();
	protected ByteBuffer original;
	protected ByteBuffer mutable;

	// Vertex Position
	private MatrixStack transforms;

	// Vertex Texture Coords
	private boolean shouldShiftUV;
	private boolean resetUV;
	private SpriteShiftEntry spriteShift;
	private float uTarget, vTarget;

	// Vertex Lighting
	private boolean shouldLight;
	private int packedLightCoords;
	private Matrix4f lightTransform;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private float sheetSize;

	public SuperByteBuffer(BufferBuilder buf) {
		Pair<DrawState, ByteBuffer> state = buf.popData();
		ByteBuffer original = state.getSecond();
		original.order(ByteOrder.nativeOrder()); // Vanilla bug, endianness does not carry over into sliced buffers
		this.original = original;
		this.mutable = GLAllocation.createDirectByteBuffer(state.getFirst()
			.getCount()
			* buf.getVertexFormat()
				.getSize());
		this.mutable.order(original.order());
		this.mutable.limit(original.limit());
		mutable.put(this.original);
		mutable.rewind();

		transforms = new MatrixStack();
	}

	public ByteBuffer build(MatrixStack view) {
		original.rewind();
		mutable.rewind();

		Matrix4f t = view.peek()
			.getModel()
			.copy();
		Matrix4f localTransforms = transforms.peek()
			.getModel();

		t.multiply(localTransforms);

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			Vector4f pos = new Vector4f(getX(original, vertex), getY(original, vertex), getZ(original, vertex), 1F);
			Vector4f lightPos = new Vector4f(pos.getX(), pos.getY(), pos.getZ(), pos.getW());

			pos.transform(t);
			lightPos.transform(localTransforms);
			putPos(mutable, vertex, pos.getX(), pos.getY(), pos.getZ());

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
					.getInterpolatedU((getUnInterpolatedU(spriteShift.getOriginal(), u) / sheetSize) + uTarget * 16);
				float targetV = spriteShift.getTarget()
					.getInterpolatedV((getUnInterpolatedV(spriteShift.getOriginal(), v) / sheetSize) + vTarget * 16);
				putUV(mutable, vertex, targetU, targetV);
			}

			if (resetUV)
				putUV(mutable, vertex, getU(original, vertex), getV(original, vertex));

			if (shouldLight) {
				int light = packedLightCoords;
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
					light = getLight(Minecraft.getInstance().world, lightPos);
				}
				putLight(mutable, vertex, light);
			}
		}

		transforms = new MatrixStack();
		shouldShiftUV = false;
		shouldColor = false;
		shouldLight = false;
		mutable.rewind();
		return mutable;
	}

	public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
		float f = sprite.getMaxU() - sprite.getMinU();
		return (u - sprite.getMinU()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
		float f = sprite.getMaxV() - sprite.getMinV();
		return (v - sprite.getMinV()) / f * 16.0F;
	}

	public void renderInto(MatrixStack input, IVertexBuilder buffer) {
		if (original.limit() == 0)
			return;
		if (!(buffer instanceof BufferBuilder)) {
			Matrix4f t = input.peek()
				.getModel()
				.copy();
			Matrix4f localTransforms = transforms.peek()
				.getModel();
			t.multiply(localTransforms);

			ByteBuffer m = mutable;
			for (int v = 0; v < vertexCount(m); v++) {
				Vector4f pos = new Vector4f(getX(original, v), getY(original, v), getZ(original, v), 1F);
				pos.transform(t);
				buffer.vertex(pos.getX(), pos.getY(), pos.getZ())
					.color(getR(m, v), getG(m, v), getB(m, v), getA(m, v))
					.texture(getU(m, v), getV(m, v))
					.light(getLight(m, v))
					.normal(getNX(m, v), getNY(m, v), getNZ(m, v))
					.endVertex();
			}
			transforms = new MatrixStack();
		} else
			((BufferBuilder) buffer).putBulkData(build(input));
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	public SuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		transforms.multiply(axis.getUnitVector()
			.getRadialQuaternion(radians));
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		return translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
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
		lightTransform = null;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		shouldLight = true;
		this.lightTransform = lightTransform;
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

	protected int getLight(ByteBuffer buffer, int index) {
		return buffer.getInt(getBufferPosition(index) + 24);
	}

	protected byte getNX(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 28);
	}

	protected byte getNY(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 29);
	}

	protected byte getNZ(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 30);
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
		buffer.putShort(getBufferPosition(index) + 24, (short) (packedLight & 0xFF));
		buffer.putShort(getBufferPosition(index) + 26, (short) ((packedLight >> 16) & 0xFF));
	}

	protected void putColor(ByteBuffer buffer, int index, byte r, byte g, byte b, byte a) {
		int bufferPosition = getBufferPosition(index);
		buffer.put(bufferPosition + 12, r);
		buffer.put(bufferPosition + 13, g);
		buffer.put(bufferPosition + 14, b);
		buffer.put(bufferPosition + 15, a);
	}

	private static int getLight(World world, Vector4f lightPos) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float sky = 0, block = 0;
		float offset = 1 / 8f;
		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.setPos(lightPos.getX() + xOffset, lightPos.getY() + yOffset, lightPos.getZ() + zOffset);
					sky += world.getLightLevel(LightType.SKY, pos) / 8f;
					block += world.getLightLevel(LightType.BLOCK, pos) / 8f;
				}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

}
