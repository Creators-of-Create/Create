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
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.BufferedModel;
import com.jozufozu.flywheel.core.model.IndexedModel;
import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.QuadConverter;
import com.jozufozu.flywheel.util.RenderUtil;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderMaterial<P extends WorldProgram, D extends InstanceData> {

	protected final InstancedTileRenderer<P> renderer;
	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;

	public RenderMaterial(InstancedTileRenderer<P> renderer, MaterialSpec<D> spec) {
		this.renderer = renderer;
		this.spec = spec;

		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					Instancer<?> model = (Instancer<?>) notification.getValue();
					RenderWork.enqueue(model::delete);
				})
				.build();
	}

	public void render(RenderType layer, Matrix4f projection, double camX, double camY, double camZ) {
		render(layer, projection, camX, camY, camZ, null);
	}

	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> setup) {
		if (!(layer == RenderType.getCutoutMipped())) return;

		P program = renderer.context.getProgram(this.spec.getProgramSpec());
		program.bind();
		program.uploadViewProjection(viewProjection);
		program.uploadCameraPos(camX, camY, camZ);

		if (setup != null) setup.call(program);

		makeRenderCalls();
	}

	public void delete() {
		//runOnAll(InstancedModel::delete);
		models.invalidateAll();
	}

	protected void makeRenderCalls() {
		runOnAll(Instancer::render);
	}

	public void runOnAll(Consumer<Instancer<D>> f) {
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

	public Instancer<D> get(Object key, Supplier<Instancer<D>> supplier) {
		try {
			return models.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Instancer<D> buildModel(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		return buildModel(dispatcher.getModelForState(renderedState), renderedState);
	}

	private Instancer<D> buildModel(IBakedModel model, BlockState renderedState) {
		return buildModel(model, renderedState, new MatrixStack());
	}

	private Instancer<D> buildModel(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		BufferBuilderReader reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));

		VertexFormat format = spec.getModelFormat();
		int vertexCount = reader.getVertexCount();

		ByteBuffer vertices = ByteBuffer.allocate(vertexCount * format.getStride());
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

		BufferedModel bufferedModel = new IndexedModel(GlPrimitive.TRIANGLES, format, vertices, vertexCount, QuadConverter.getInstance().getEboForNQuads(vertexCount / 4));
		//BufferedModel bufferedModel = new BufferedModel(GlPrimitive.QUADS, format, vertices, vertexCount);

		return new Instancer<>(bufferedModel, renderer, spec.getInstanceFormat(), spec.getInstanceFactory());
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
