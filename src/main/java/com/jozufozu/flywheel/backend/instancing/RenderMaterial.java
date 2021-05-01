package com.jozufozu.flywheel.backend.instancing;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.FastRenderDispatcher;
import com.jozufozu.flywheel.backend.RenderUtil;
import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.core.PartialModel;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderCallback;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.SuperByteBufferCache;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderMaterial<P extends BasicProgram, MODEL extends InstancedModel<?>> {

	protected final InstancedTileRenderer<?> renderer;
	protected final Cache<Object, MODEL> models;
	protected final ModelFactory<MODEL> factory;
	protected final ProgramSpec<P> programSpec;
	protected final Predicate<RenderType> layerPredicate;

	/**
	 * Creates a material that renders in the default layer (CUTOUT_MIPPED)
	 */
	public RenderMaterial(InstancedTileRenderer<?> renderer, ProgramSpec<P> programSpec, ModelFactory<MODEL> factory) {
		this(renderer, programSpec, factory, type -> type == RenderType.getCutoutMipped());
	}

	public RenderMaterial(InstancedTileRenderer<?> renderer, ProgramSpec<P> programSpec, ModelFactory<MODEL> factory, Predicate<RenderType> layerPredicate) {
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
		P program = Backend.getProgram(programSpec);
		program.bind(viewProjection, camX, camY, camZ, FastRenderDispatcher.getDebugMode());

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
		BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

		return factory.makeModel(renderer, builder);
	}

}
