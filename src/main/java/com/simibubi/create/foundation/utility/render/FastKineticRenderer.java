package com.simibubi.create.foundation.utility.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.render.shader.Shader;
import com.simibubi.create.foundation.utility.render.shader.ShaderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.utility.render.SuperByteBufferCache.PARTIAL;

@Mod.EventBusSubscriber(modid = Create.ID, value = Dist.CLIENT)
public class FastKineticRenderer {
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, InstancedBuffer>> cache;

    Queue<Runnable> runs;

    public FastKineticRenderer() {
        cache = new HashMap<>();
        runs = new ConcurrentLinkedQueue<>();
        registerCompartment(SuperByteBufferCache.GENERIC_TILE);
        registerCompartment(SuperByteBufferCache.PARTIAL);
        registerCompartment(SuperByteBufferCache.DIRECTIONAL_PARTIAL);
    }

    public void tick() {
        for (Cache<Object, InstancedBuffer> cache : cache.values()) {
            for (InstancedBuffer renderer : cache.asMap().values()) {
                renderer.clearInstanceData();
            }
        }
    }

    public void enqueue(Runnable run) {
        runs.add(run);
    }

    public void renderInstances(RenderWorldLastEvent event) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableCull();

        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        LightTexture lightManager = gameRenderer.getLightmapTextureManager();

        ShaderHelper.useShader(Shader.ROTATING_INSTANCED, shader -> {
            ShaderHelper.MATRIX_BUFFER.position(0);
            event.getProjectionMatrix().write(ShaderHelper.MATRIX_BUFFER);

            int projection = GlStateManager.getUniformLocation(shader, "projection");
            GlStateManager.uniformMatrix4(projection, false, ShaderHelper.MATRIX_BUFFER);

            // view matrix
            Vector3d pos = gameRenderer.getActiveRenderInfo().getProjectedView();
            Matrix4f translate = Matrix4f.translate((float) -pos.x, (float) -pos.y, (float) -pos.z);
            translate.multiplyBackward(event.getMatrixStack().peek().getModel());

            ShaderHelper.MATRIX_BUFFER.position(0);
            translate.write(ShaderHelper.MATRIX_BUFFER);
            int view = GlStateManager.getUniformLocation(shader, "view");
            GlStateManager.uniformMatrix4(view, false, ShaderHelper.MATRIX_BUFFER);

            Texture blockAtlasTexture = Minecraft.getInstance().textureManager.getTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
            Texture lightTexture = Minecraft.getInstance().textureManager.getTexture(lightManager.resourceLocation);

            GL40.glActiveTexture(GL40.GL_TEXTURE0);
            GL40.glBindTexture(GL11.GL_TEXTURE_2D, blockAtlasTexture.getGlTextureId());

            GL40.glActiveTexture(GL40.GL_TEXTURE0 + 1);
            GL40.glBindTexture(GL11.GL_TEXTURE_2D, lightTexture.getGlTextureId());
            RenderSystem.texParameter(3553, 10241, 9729);
            RenderSystem.texParameter(3553, 10240, 9729);
            RenderSystem.texParameter(3553, 10242, 10496);
            RenderSystem.texParameter(3553, 10243, 10496);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableTexture();
        });

        cache.values()
             .stream()
             .flatMap(cache -> {
                 ConcurrentMap<Object, InstancedBuffer> map = cache.asMap();

                 return map.values().stream();
             })
             .filter(type -> !type.isEmpty())
             .forEach(InstancedBuffer::render);

        ShaderHelper.releaseShader();

        GL40.glActiveTexture(GL40.GL_TEXTURE0 + 1);
        GL40.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL40.glActiveTexture(GL40.GL_TEXTURE0);
        GL40.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        while (!runs.isEmpty()) {
            runs.remove().run();
        }
    }

    public void registerCompartment(SuperByteBufferCache.Compartment<?> instance) {
        cache.put(instance, CacheBuilder.newBuilder().build());
    }

    public void registerCompartment(SuperByteBufferCache.Compartment<?> instance, long ticksUntilExpired) {
        cache.put(instance, CacheBuilder.newBuilder().expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS).build());
    }

    public InstancedBuffer renderPartialInstanced(AllBlockPartials partial, BlockState referenceState) {
        return getInstanced(PARTIAL, partial, () -> rotatingInstancedRenderer(partial.get(), referenceState));
    }

    public InstancedBuffer renderDirectionalPartialInstanced(AllBlockPartials partial, BlockState referenceState, Direction dir,
                                                             MatrixStack modelTransform) {
        return getInstanced(SuperByteBufferCache.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                            () -> rotatingInstancedRenderer(partial.get(), referenceState, modelTransform));
    }

    public InstancedBuffer renderBlockInstanced(SuperByteBufferCache.Compartment<BlockState> compartment, BlockState toRender) {
        return getInstanced(compartment, toRender, () -> rotatingInstancedRenderer(toRender));
    }

    public <T> InstancedBuffer getInstanced(SuperByteBufferCache.Compartment<T> compartment, T key, Supplier<InstancedBuffer> supplier) {
        Cache<Object, InstancedBuffer> compartmentCache = this.cache.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }


    private InstancedBuffer rotatingInstancedRenderer(BlockState renderedState) {
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        return rotatingInstancedRenderer(dispatcher.getModelForState(renderedState), renderedState);
    }

    private InstancedBuffer rotatingInstancedRenderer(IBakedModel model, BlockState renderedState) {
        return rotatingInstancedRenderer(model, renderedState, new MatrixStack());
    }

    private InstancedBuffer rotatingInstancedRenderer(IBakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return new InstancedBuffer(builder);
    }

    public void invalidate() {
        cache.values().forEach(cache -> {
            cache.asMap().values().forEach(InstancedBuffer::invalidate);
            cache.invalidateAll();
        });
    }
}
