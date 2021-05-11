package com.jozufozu.flywheel.util;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;

public class BakedQuadWrapper {
	private final FormatCache formatCache = new FormatCache();
	private BakedQuad quad;
	private int[] vertexData;

	public BakedQuadWrapper() {
	}

	public BakedQuadWrapper(BakedQuad quad) {
		this.quad = quad;
		this.vertexData = quad.getVertexData();
	}

	public void setQuad(BakedQuad quad) {
		this.quad = quad;
		this.vertexData = this.quad.getVertexData();
	}

	public static BakedQuadWrapper of(BakedQuad quad) {
		return new BakedQuadWrapper(quad);
	}

	public void refreshFormat() {
		formatCache.refresh();
	}

	public BakedQuad getQuad() {
		return quad;
	}

	public void clear() {
		quad = null;
		vertexData = null;
	}

	// Getters

	public float getPosX(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position]);
	}

	public float getPosY(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 1]);
	}

	public float getPosZ(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 2]);
	}

	public Vector3f getPos(int vertexIndex) {
		return new Vector3f(getPosX(vertexIndex), getPosY(vertexIndex), getPosZ(vertexIndex));
	}

	public void copyPos(int vertexIndex, Vector3f pos) {
		pos.set(getPosX(vertexIndex), getPosY(vertexIndex), getPosZ(vertexIndex));
	}

	public int getColor(int vertexIndex) {
		return vertexData[vertexIndex * formatCache.vertexSize + formatCache.color];
	}

	public float getTexU(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture]);
	}

	public float getTexV(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture + 1]);
	}

	public Vector2f getTex(int vertexIndex) {
		return new Vector2f(getTexU(vertexIndex), getTexV(vertexIndex));
	}

	public int getLight(int vertexIndex) {
		return vertexData[vertexIndex * formatCache.vertexSize + formatCache.light];
	}

	public float getNormalX(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal]);
	}

	public float getNormalY(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 1]);
	}

	public float getNormalZ(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 2]);
	}

	public Vector3f getNormal(int vertexIndex) {
		return new Vector3f(getNormalX(vertexIndex), getNormalY(vertexIndex), getNormalZ(vertexIndex));
	}

	public void copyNormal(int vertexIndex, Vector3f normal) {
		normal.set(getNormalX(vertexIndex), getNormalY(vertexIndex), getNormalZ(vertexIndex));
	}

	// Setters

	public void setPosX(int vertexIndex, float x) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position] = Float.floatToRawIntBits(x);
	}

	public void setPosY(int vertexIndex, float y) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 1] = Float.floatToRawIntBits(y);
	}

	public void setPosZ(int vertexIndex, float z) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 2] = Float.floatToRawIntBits(z);
	}

	public void setPos(int vertexIndex, float x, float y, float z) {
		setPosX(vertexIndex, x);
		setPosY(vertexIndex, y);
		setPosZ(vertexIndex, z);
	}

	public void setPos(int vertexIndex, Vector3f pos) {
		setPos(vertexIndex, pos.getX(), pos.getY(), pos.getZ());
	}

	public void setColor(int vertexIndex, int color) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.color] = color;
	}

	public void setTexU(int vertexIndex, float u) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture] = Float.floatToRawIntBits(u);
	}

	public void setTexV(int vertexIndex, float v) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture + 1] = Float.floatToRawIntBits(v);
	}

	public void setTex(int vertexIndex, float u, float v) {
		setTexU(vertexIndex, u);
		setTexV(vertexIndex, v);
	}

	public void setTex(int vertexIndex, Vector2f tex) {
		setTex(vertexIndex, tex.x, tex.y);
	}

	public void setLight(int vertexIndex, int light) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.light] = light;
	}

	public void setNormalX(int vertexIndex, float normalX) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal] = Float.floatToRawIntBits(normalX);
	}

	public void setNormalY(int vertexIndex, float normalY) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 1] = Float.floatToRawIntBits(normalY);
	}

	public void setNormalZ(int vertexIndex, float normalZ) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 2] = Float.floatToRawIntBits(normalZ);
	}

	public void setNormal(int vertexIndex, float normalX, float normalY, float normalZ) {
		setNormalX(vertexIndex, normalX);
		setNormalY(vertexIndex, normalY);
		setNormalZ(vertexIndex, normalZ);
	}

	public void setNormal(int vertexIndex, Vector3f normal) {
		setNormal(vertexIndex, normal.getX(), normal.getY(), normal.getZ());
	}

	private static class FormatCache {
		private static final VertexFormat FORMAT = DefaultVertexFormats.BLOCK;

		public FormatCache() {
			refresh();
		}

		// Integer size
		public int vertexSize;

		// Element integer offsets
		public int position;
		public int color;
		public int texture;
		public int light;
		public int normal;

		public void refresh() {
			vertexSize = FORMAT.getIntegerSize();
			for (int elementId = 0; elementId < FORMAT.getElements().size(); elementId++) {
				VertexFormatElement element = FORMAT.getElements().get(elementId);
				int intOffset = FORMAT.getOffset(elementId) / Integer.BYTES;
				if (element.getUsage() == VertexFormatElement.Usage.POSITION) {
					position = intOffset;
				} else if (element.getUsage() == VertexFormatElement.Usage.COLOR) {
					color = intOffset;
				} else if (element.getUsage() == VertexFormatElement.Usage.UV) {
					if (element.getIndex() == 0) {
						texture = intOffset;
					} else if (element.getIndex() == 2) {
						light = intOffset;
					}
				} else if (element.getUsage() == VertexFormatElement.Usage.NORMAL) {
					normal = intOffset;
				}
			}
		}
	}
}
