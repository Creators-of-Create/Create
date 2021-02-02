package com.simibubi.create.foundation.render.instancing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.gl.shader.AllShaderPrograms;
import com.simibubi.create.foundation.render.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.render.Compartment.PARTIAL;

public class RenderMaterial<MODEL extends InstancedModel<?>> {

    protected final Map<Compartment<?>, Cache<Object, MODEL>> models;
    protected final ModelFactory<MODEL> factory;
    protected final AllShaderPrograms shader;
    protected final Predicate<RenderType> layerPredicate;

    /**
     * Creates a material that renders in the default layer (CUTOUT_MIPPED)
     */
    public RenderMaterial(AllShaderPrograms shader, ModelFactory<MODEL> factory) {
        this(shader, factory, type -> type == RenderType.getCutoutMipped());
    }

    public RenderMaterial(AllShaderPrograms shader, ModelFactory<MODEL> factory, Predicate<RenderType> layerPredicate) {
        this.models = new HashMap<>();
        this.factory = factory;
        this.shader = shader;
        this.layerPredicate = layerPredicate;
        registerCompartment(Compartment.PARTIAL);
        registerCompartment(Compartment.DIRECTIONAL_PARTIAL);
        registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
    }

    public boolean canRenderInLayer(RenderType layer) {
        return layerPredicate.test(layer);
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view) {
        render(layer, projection, view, null);
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view, ShaderCallback setup) {
        ShaderCallback cb = ShaderHelper.getViewProjectionCallback(projection, view);

        if (setup != null) cb = cb.andThen(setup);

        ShaderHelper.useShader(shader, cb);
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

        return factory.convert(builder);
    }

}
