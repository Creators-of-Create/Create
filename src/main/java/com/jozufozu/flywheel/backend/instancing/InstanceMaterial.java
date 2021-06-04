package com.jozufozu.flywheel.backend.instancing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.RenderUtil;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class InstanceMaterial<D extends InstanceData> {

	protected final Supplier<Vector3i> originCoordinate;
	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;
	private final VertexFormat modelFormat;

	public InstanceMaterial(Supplier<Vector3i> renderer, MaterialSpec<D> spec) {
		this.originCoordinate = renderer;
		this.spec = spec;

		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					Instancer<?> model = (Instancer<?>) notification.getValue();
					RenderWork.enqueue(model::delete);
				})
				.build();
		modelFormat = this.spec.getModelFormat();
	}

	public void delete() {
		models.invalidateAll();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.asMap().values().forEach(Instancer::clear);
	}

	public void forEachInstancer(Consumer<Instancer<D>> f) {
		for (Instancer<D> model : models.asMap().values()) {
			f.accept(model);
		}
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return get(partial, () -> buildModel(partial.get(), referenceState));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
		return get(Pair.of(dir, partial),
				() -> buildModel(partial.get(), referenceState, modelTransform.get()));
	}

	public Instancer<D> getModel(BlockState toRender) {
		return get(toRender, () -> buildModel(toRender));
	}

	public Instancer<D> get(Object key, Supplier<BufferedModel> supplier) {
		try {
			return models.get(key, () -> new Instancer<>(supplier.get(), originCoordinate, spec));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedModel buildModel(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		return buildModel(dispatcher.getModelForState(renderedState), renderedState);
	}

	private BufferedModel buildModel(IBakedModel model, BlockState renderedState) {
		return buildModel(model, renderedState, new MatrixStack());
	}

	private BufferedModel buildModel(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		BufferBuilderReader reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));

		int vertexCount = reader.getVertexCount();

		ByteBuffer vertices = ByteBuffer.allocate(vertexCount * modelFormat.getStride());
		vertices.order(ByteOrder.nativeOrder());

		for (int i = 0; i < vertexCount; i++) {
			vertices.putFloat(reader.getX(i));
			vertices.putFloat(reader.getY(i));
			vertices.putFloat(reader.getZ(i));

			vertices.put(reader.getNX(i));
			vertices.put(reader.getNY(i));
			vertices.put(reader.getNZ(i));

			vertices.putFloat(reader.getU(i));
			vertices.putFloat(reader.getV(i));
		}

		vertices.rewind();

		// return new BufferedModel(GlPrimitive.QUADS, format, vertices, vertexCount);

		return new IndexedModel(GlPrimitive.TRIANGLES, modelFormat, vertices, vertexCount, QuadConverter.getInstance().quads2Tris(vertexCount / 4));
	}

	private static final Direction[] dirs;

	static {
		Direction[] directions = Direction.values();

		dirs = Arrays.copyOf(directions, directions.length + 1);
	}

	public static BufferBuilder getBufferBuilder(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

//		BakedQuadWrapper quadReader = new BakedQuadWrapper();
//
//		IModelData modelData = model.getModelData(mc.world, BlockPos.ZERO.up(255), referenceState, VirtualEmptyModelData.INSTANCE);
//		List<BakedQuad> quads = Arrays.stream(dirs)
//				.flatMap(dir -> model.getQuads(referenceState, dir, mc.world.rand, modelData).stream())
//				.collect(Collectors.toList());

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModel(mc.world, model, referenceState, BlockPos.ZERO.up(255), ms, builder, true,
				mc.world.rand, 42, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
		builder.finishDrawing();
		return builder;
	}

}
