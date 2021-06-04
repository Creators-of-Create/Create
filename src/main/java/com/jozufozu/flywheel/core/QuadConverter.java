package com.jozufozu.flywheel.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A class to manage EBOs that index quads as triangles.
 */
@Mod.EventBusSubscriber
public class QuadConverter {

	public static final int STARTING_CAPACITY = 42;

	private static QuadConverter INSTANCE;

	@Nonnull
	public static QuadConverter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new QuadConverter(STARTING_CAPACITY); // 255 / 6 = 42
		}

		return INSTANCE;
	}

	@Nullable
	public static QuadConverter getNullable() {
		return INSTANCE;
	}

	Map<GlNumericType, GlBuffer> ebos;
	int[] capacities;

	public QuadConverter(int initialCapacity) {
		this.ebos = new EnumMap<>(GlNumericType.class);
		initCapacities();

		fillBuffer(initialCapacity);
	}

	public ElementBuffer quads2Tris(int quads) {
		int indexCount = quads * 6;
		GlNumericType type = getSmallestIndexType(indexCount);

		if (quads > getCapacity(type)) {
			fillBuffer(quads, indexCount, type);
		}

		return new ElementBuffer(getBuffer(type), indexCount, type);
	}

	private void initCapacities() {
		this.capacities = new int[GlNumericType.values().length];
	}

	private int getCapacity(GlNumericType type) {
		return capacities[type.ordinal()];
	}

	private void updateCapacity(GlNumericType type, int capacity) {
		if (getCapacity(type) < capacity) {
			capacities[type.ordinal()] = capacity;
		}
	}

	public void free() {
		ebos.values().forEach(GlBuffer::delete);
		ebos.clear();
		initCapacities();
	}

	private void fillBuffer(int quads) {
		int indexCount = quads * 6;

		fillBuffer(quads, indexCount, getSmallestIndexType(indexCount));
	}

	private void fillBuffer(int quads, int indexCount, GlNumericType type) {
		MemoryStack stack = MemoryStack.stackPush();
		int bytes = indexCount * type.getByteWidth();

		ByteBuffer indices;
		if (bytes > stack.getSize()) {
			indices = MemoryUtil.memAlloc(bytes); // not enough space on the preallocated stack
		} else {
			stack.push();
			indices = stack.malloc(bytes);
		}

		indices.order(ByteOrder.nativeOrder());

		fillBuffer(indices, type, quads);

		GlBuffer buffer = getBuffer(type);

		buffer.bind();
		buffer.upload(indices);
		buffer.unbind();

		if (bytes > stack.getSize()) {
			MemoryUtil.memFree(indices);
		} else {
			stack.pop();
		}

		updateCapacity(type, quads);
	}

	private void fillBuffer(ByteBuffer indices, GlNumericType type, int quads) {
		for (int i = 0, max = 4 * quads; i < max; i += 4) {
			// triangle a
			type.castAndBuffer(indices, i);
			type.castAndBuffer(indices, i + 1);
			type.castAndBuffer(indices, i + 2);
			// triangle b
			type.castAndBuffer(indices, i);
			type.castAndBuffer(indices, i + 2);
			type.castAndBuffer(indices, i + 3);
		}
		indices.flip();
	}

	private GlBuffer getBuffer(GlNumericType type) {
		return ebos.computeIfAbsent(type, $ -> new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER));
	}

	/**
	 * Given the needed number of indices, what is the smallest bit width type that can index everything? <br>
	 *
	 * <pre>
	 * | indexCount   | type  |
	 * |--------------|-------|
	 * | [0, 255)     | byte  |
	 * | [256, 65536)	| short	|
	 * | [65537, )	| int	|
	 * </pre>
	 */
	private static GlNumericType getSmallestIndexType(int indexCount) {
		indexCount = indexCount >>> 8;
		if (indexCount == 0) {
			return GlNumericType.UBYTE;
		}
		indexCount = indexCount >>> 8;
		if (indexCount == 0) {
			return GlNumericType.USHORT;
		}

		return GlNumericType.UINT;
	}

	// make sure this gets reset first so it has a chance to repopulate
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRendererReload(ReloadRenderersEvent event) {
		if (INSTANCE != null) INSTANCE.free();
	}
}
