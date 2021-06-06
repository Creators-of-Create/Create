package com.jozufozu.flywheel.backend.instancing;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.util.AttribUtil;

import net.minecraft.util.math.vector.Vector3i;

public class Instancer<D extends InstanceData> {

	public final Supplier<Vector3i> originCoordinate;

	protected final BufferedModel model;

	protected final VertexFormat instanceFormat;
	protected final IInstanceFactory<D> factory;
	protected GlVertexArray vao;
	protected GlBuffer instanceVBO;
	protected int glBufferSize = -1;
	protected int glInstanceCount = 0;
	private boolean deleted;

	protected final ArrayList<D> data = new ArrayList<>();

	boolean anyToRemove;
	boolean anyToUpdate;

	public Instancer(BufferedModel model, Supplier<Vector3i> originCoordinate, MaterialSpec<D> spec) {
		this.model = model;
		this.factory = spec.getInstanceFactory();
		this.instanceFormat = spec.getInstanceFormat();
		this.originCoordinate = originCoordinate;

		if (model.getVertexCount() <= 0)
			throw new IllegalArgumentException("Refusing to instance a model with no vertices.");

		vao = new GlVertexArray();
		instanceVBO = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		vao.bind();

		// bind the model's vbo to our vao
		model.setupState();

		AttribUtil.enableArrays(model.getAttributeCount() + instanceFormat.getAttributeCount());

		vao.unbind();

		model.clearState();
	}

	public void render() {
		if (deleted) return;

		vao.bind();
		renderSetup();

		if (glInstanceCount > 0)
			model.drawInstances(glInstanceCount);

		vao.unbind();
	}

	public D createInstance() {
		D instanceData = factory.create(this);
		instanceData.dirty = true;
		anyToUpdate = true;
		data.add(instanceData);

		return instanceData;
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		data.clear();
		anyToRemove = true;
	}

	/**
	 * Free acquired resources. Attempting to use this after calling delete is undefined behavior.
	 */
	public void delete() {
		if (deleted) return;

		deleted = true;

		model.delete();

		instanceVBO.delete();
		vao.delete();
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		instanceVBO.bind();
		if (!realloc()) {

			if (anyToRemove) {
				clearBufferTail();
			}

			if (anyToUpdate) {
				updateBuffer();
			}

			glInstanceCount = data.size();
		}

		instanceVBO.unbind();

		anyToRemove = anyToUpdate = false;
	}

	private void clearBufferTail() {
		int size = data.size();
		final int offset = size * instanceFormat.getStride();
		final int length = glBufferSize - offset;
		if (length > 0) {
			instanceVBO.getBuffer(offset, length)
					.putByteArray(new byte[length])
					.flush();
		}
	}

	private void updateBuffer() {
		final int size = data.size();

		if (size <= 0) return;

		final int stride = instanceFormat.getStride();
		final BitSet dirtySet = getDirtyBitSet();

		if (dirtySet.isEmpty()) return;

		final int firstDirty = dirtySet.nextSetBit(0);
		final int lastDirty = dirtySet.previousSetBit(size);

		final int offset = firstDirty * stride;
		final int length = (1 + lastDirty - firstDirty) * stride;

		if (length > 0) {
			MappedBuffer mapped = instanceVBO.getBuffer(offset, length);

			dirtySet.stream().forEach(i -> {
				final D d = data.get(i);

				mapped.position(i * stride);
				d.write(mapped);
			});
			mapped.flush();
		}
	}

	private BitSet getDirtyBitSet() {
		final int size = data.size();
		final BitSet dirtySet = new BitSet(size);

		for (int i = 0; i < size; i++) {
			D element = data.get(i);
			if (element.dirty) {
				dirtySet.set(i);

				element.dirty = false;
			}
		}
		return dirtySet;
	}

	private boolean realloc() {
		int size = this.data.size();
		int stride = instanceFormat.getStride();
		int requiredSize = size * stride;
		if (requiredSize > glBufferSize) {
			glBufferSize = requiredSize + stride * 16;
			instanceVBO.alloc(glBufferSize);

			MappedBuffer buffer = instanceVBO.getBuffer(0, glBufferSize);
			for (D datum : data) {
				datum.write(buffer);
			}
			buffer.flush();

			glInstanceCount = size;

			informAttribDivisors();

			return true;
		}
		return false;
	}

	private void removeDeletedInstances() {
		// Figure out which elements are to be removed.
		final int oldSize = this.data.size();
		int removeCount = 0;
		final BitSet removeSet = new BitSet(oldSize);
		for (int i = 0; i < oldSize; i++) {
			final D element = this.data.get(i);
			if (element.removed) {
				removeSet.set(i);
				removeCount++;
			}
		}

		final int newSize = oldSize - removeCount;

		// shift surviving elements left over the spaces left by removed elements
		for (int i = 0, j = 0; (i < oldSize) && (j < newSize); i++, j++) {
			i = removeSet.nextClearBit(i);

			if (i != j) {
				D element = data.get(i);
				data.set(j, element);
				element.dirty = true;
			}
		}

		anyToUpdate = true;

		data.subList(newSize, oldSize).clear();

	}

	private void informAttribDivisors() {
		int staticAttributes = model.getAttributeCount();
		instanceFormat.vertexAttribPointers(staticAttributes);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			Backend.getInstance().compat.instancedArrays.vertexAttribDivisor(i + staticAttributes, 1);
		}
	}

}
