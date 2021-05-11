package com.jozufozu.flywheel.backend.instancing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.core.PartialModel;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderCallback;
import com.jozufozu.flywheel.util.BakedQuadWrapper;
import com.jozufozu.flywheel.util.RenderUtil;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.model.data.IModelData;

public class RenderMaterial<P extends BasicProgram, MODEL extends InstancedModel<?>> {

	protected final InstancedTileRenderer<P> renderer;
	protected final Cache<Object, MODEL> models;
	protected final ModelFactory<MODEL> factory;
	protected final ProgramSpec programSpec;
	protected final Predicate<RenderType> layerPredicate;

	/**
	 * Creates a material that renders in the default layer (CUTOUT_MIPPED)
	 */
	public RenderMaterial(InstancedTileRenderer<P> renderer, ProgramSpec programSpec, ModelFactory<MODEL> factory) {
		this(renderer, programSpec, factory, type -> type == RenderType.getCutoutMipped());
	}

	public RenderMaterial(InstancedTileRenderer<P> renderer, ProgramSpec programSpec, ModelFactory<MODEL> factory, Predicate<RenderType> layerPredicate) {
		this.renderer = renderer;
		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> ((InstancedModel<?>) notification.getValue()).delete())
				.build();
		this.factory = factory;
		this.programSpec = programSpec;
		this.layerPredicate = layerPredicate;
	}

	public boolean canRenderInLayer(RenderType layer) {
		return layerPredicate.test(layer);
	}

	public void render(RenderType layer, Matrix4f projection, double camX, double camY, double camZ) {
		render(layer, projection, camX, camY, camZ, null);
	}

	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, ShaderCallback<P> setup) {
		P program = renderer.context.getProgram(programSpec);
		program.bind(viewProjection, camX, camY, camZ, Backend.getDebugMode());

		if (setup != null) setup.call(program);

		makeRenderCalls();
	}

	public void delete() {
		//runOnAll(InstancedModel::delete);
		models.invalidateAll();
	}

	protected void makeRenderCalls() {
		runOnAll(InstancedModel::render);
	}

	public void runOnAll(Consumer<MODEL> f) {
		for (MODEL model : models.asMap().values()) {
			f.accept(model);
		}
	}

	public MODEL getModel(PartialModel partial, BlockState referenceState) {
		return get(partial, () -> buildModel(partial.get(), referenceState));
	}

	public MODEL getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	public MODEL getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
		return get(Pair.of(dir, partial),
				() -> buildModel(partial.get(), referenceState, modelTransform.get()));
	}

	public MODEL getModel(BlockState toRender) {
		return get(toRender, () -> buildModel(toRender));
	}

	public MODEL get(Object key, Supplier<MODEL> supplier) {
		try {
			return models.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private MODEL buildModel(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		return buildModel(dispatcher.getModelForState(renderedState), renderedState);
	}

	private MODEL buildModel(IBakedModel model, BlockState renderedState) {
		return buildModel(model, renderedState, new MatrixStack());
	}

	private MODEL buildModel(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		BufferBuilder builder = getBufferBuilder(model, referenceState, ms);

		return factory.makeModel(renderer, builder);
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

		BakedQuadWrapper quadReader = new BakedQuadWrapper();

		IModelData modelData = model.getModelData(mc.world, BlockPos.ZERO.up(255), referenceState, VirtualEmptyModelData.INSTANCE);
		List<BakedQuad> quads = Arrays.stream(dirs)
				.flatMap(dir -> model.getQuads(referenceState, dir, mc.world.rand, modelData).stream())
				.collect(Collectors.toList());

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModel(mc.world, model, referenceState, BlockPos.ZERO.up(255), ms, builder, true,
				mc.world.rand, 42, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
		builder.finishDrawing();
		return builder;
	}

}
