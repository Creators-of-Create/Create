package com.simibubi.create.foundation.render.instancing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL40;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.render.Compartment.PARTIAL;

public class RenderMaterial<MODEL extends InstanceBuffer<?>> {

    protected final Map<Compartment<?>, Cache<Object, MODEL>> models;
    protected final ModelFactory<MODEL> factory;
    protected final Shader shader;

    public RenderMaterial(Shader shader, ModelFactory<MODEL> factory) {
        this.models = new HashMap<>();
        this.factory = factory;
        this.shader = shader;
        registerCompartment(Compartment.PARTIAL);
        registerCompartment(Compartment.DIRECTIONAL_PARTIAL);
        registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        render(projection, view, null);
    }

    public void render(Matrix4f projection, Matrix4f view, ShaderCallback setup) {
        int handle = setupShader(projection, view);
        if (setup != null) setup.call(handle);
        makeRenderCalls();
        teardown();
    }

    protected int setupShader(Matrix4f projection, Matrix4f view) {
        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(projection, view);

        return ShaderHelper.useShader(shader, callback);
    }

    public void teardown() {}

    public void delete() {
        runOnAll(InstanceBuffer::delete);
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
