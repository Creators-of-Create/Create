package com.simibubi.create.foundation.render.backend.instancing;

import static com.simibubi.create.foundation.render.Compartment.PARTIAL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderCallback;

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
    protected final Map<Compartment<?>, Cache<Object, MODEL>> models;
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
        this.models = new HashMap<>();
        this.factory = factory;
        this.programSpec = programSpec;
        this.layerPredicate = layerPredicate;
        registerCompartment(Compartment.PARTIAL);
        registerCompartment(Compartment.DIRECTIONAL_PARTIAL);
        registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
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
        teardown();
    }

    public void teardown() {}

    public void delete() {
        runOnAll(InstancedModel::delete);
        models.values().forEach(Cache::invalidateAll);
    }

    protected void makeRenderCalls() {
        for (Cache<Object, MODEL> cache : models.values()) {
            for (MODEL model : cache.asMap().values()) {
                if (!model.isEmpty()) {
                    model.render();
                }
            }
        }
    }

    public void runOnAll(Consumer<MODEL> f) {
        for (Cache<Object, MODEL> cache : models.values()) {
            for (MODEL model : cache.asMap().values()) {
                f.accept(model);
            }
        }
    }

    public void registerCompartment(Compartment<?> instance) {
        models.put(instance, CacheBuilder.newBuilder().build());
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState) {
        return get(PARTIAL, partial, () -> buildModel(partial.get(), referenceState));
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState, Direction dir) {
        return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                   () -> buildModel(partial.get(), referenceState));
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
        return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                   () -> buildModel(partial.get(), referenceState, modelTransform.get()));
    }

    public MODEL getModel(Compartment<BlockState> compartment, BlockState toRender) {
        return get(compartment, toRender, () -> buildModel(toRender));
    }

    public <T> MODEL get(Compartment<T> compartment, T key, Supplier<MODEL> supplier) {
        Cache<Object, MODEL> compartmentCache = models.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
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
