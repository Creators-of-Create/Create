package com.jozufozu.flywheel.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.core.model.ElementBuffer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.math.MathHelper;

public class QuadConverter {

	private static QuadConverter INSTANCE = new QuadConverter();

	public static QuadConverter getInstance() {
		return INSTANCE;
	}

	private final Int2ObjectMap<CachedEbo> quads2Tris;

	public QuadConverter() {
		quads2Tris = new Int2ObjectOpenHashMap<>();
	}

	public ElementBuffer getEboForNQuads(int quads) {
		quads2Tris.values().removeIf(CachedEbo::noReferences);

		CachedEbo ebo = quads2Tris.computeIfAbsent(quads, quadCount -> {
			int triangleCount = quadCount * 2;
			int indexCount = triangleCount * 3;

			GlNumericType type;

			int bitWidth = MathHelper.log2(indexCount);
			if (bitWidth <= 8) {
				type = GlNumericType.UBYTE;
			} else if (bitWidth <= 16) {
				type = GlNumericType.USHORT;
			} else {
				type = GlNumericType.UINT;
			}
			ByteBuffer indices = ByteBuffer.allocate(indexCount * type.getByteWidth());
			indices.order(ByteOrder.nativeOrder());

			for (int i = 0; i < quadCount; i++) {
				int qStart = 4 * i;
				// triangle 1
				type.castAndBuffer(indices, qStart);
				type.castAndBuffer(indices, qStart + 1);
				type.castAndBuffer(indices, qStart + 2);
				// triangle 2
				type.castAndBuffer(indices, qStart);
				type.castAndBuffer(indices, qStart + 2);
				type.castAndBuffer(indices, qStart + 3);
			}

			indices.flip();

			return new CachedEbo(indices, indexCount, type);
		});

		ebo.refCount++;

		return ebo;
	}

	private class CachedEbo extends ElementBuffer {
		int refCount = 1;

		public CachedEbo(ByteBuffer indices, int elementCount, GlNumericType indexType) {
			super(indices, elementCount, indexType);
		}

		@Override
		public void delete() {
			refCount--;

			if (refCount == 0)
				super.delete();
		}

		public boolean noReferences() {
			return refCount == 0;
		}
	}
}
