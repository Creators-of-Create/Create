package com.simibubi.create.foundation.utility.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.render.shader.Shader;
import com.simibubi.create.foundation.utility.render.shader.ShaderCallback;
import com.simibubi.create.foundation.utility.render.shader.ShaderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.utility.render.SuperByteBufferCache.PARTIAL;

@Mod.EventBusSubscriber(modid = Create.ID, value = Dist.CLIENT)
public class FastKineticRenderer {
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, RotatingBuffer>> rotating;
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, BeltBuffer>> belts;

    Queue<Runnable> runs;

    public FastKineticRenderer() {
        rotating = new HashMap<>();
        belts = new HashMap<>();
        runs = new ConcurrentLinkedQueue<>();
        registerCompartment(SuperByteBufferCache.GENERIC_TILE);
        registerCompartment(SuperByteBufferCache.PARTIAL);
        registerCompartment(SuperByteBufferCache.DIRECTIONAL_PARTIAL);
    }

    public void tick() {
        for (Cache<Object, RotatingBuffer> cache : rotating.values()) {
            for (RotatingBuffer renderer : cache.asMap().values()) {
                renderer.clearInstanceData();
            }
        }

        for (Cache<Object, BeltBuffer> cache : belts.values()) {
            for (BeltBuffer renderer : cache.asMap().values()) {
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

        ShaderCallback callback = shader -> {
            ShaderHelper.MATRIX_BUFFER.position(0);
            event.getProjectionMatrix().write(ShaderHelper.MATRIX_BUFFER);

            int projection = GlStateManager.getUniformLocation(shader, "projection");
            GlStateManager.uniformMatrix4(projection, false, ShaderHelper.MATRIX_BUFFER);

            // view matrix
            Vec3d pos = gameRenderer.getActiveRenderInfo().getProjectedView();
            Matrix4f translate = Matrix4f.translate((float) -pos.x, (float) -pos.y, (float) -pos.z);
            translate.multiplyBackward(event.getMatrixStack().peek().getModel());

            ShaderHelper.MATRIX_BUFFER.position(0);
            translate.write(ShaderHelper.MATRIX_BUFFER);
            int view = GlStateManager.getUniformLocation(shader, "view");
            GlStateManager.uniformMatrix4(view, false, ShaderHelper.MATRIX_BUFFER);
        };
        ShaderHelper.useShader(Shader.ROTATING_INSTANCED, callback);

        rotating.values()
                .stream()
                .flatMap(cache -> cache.asMap().values().stream())
                .filter(type -> !type.isEmpty())
                .forEach(InstancedBuffer::render);

        ShaderHelper.useShader(Shader.BELT_INSTANCED, callback);

        belts.values()
                .stream()
                .flatMap(cache -> cache.asMap().values().stream())
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
        rotating.put(instance, CacheBuilder.newBuilder().build());
        belts.put(instance, CacheBuilder.newBuilder().build());
    }

    public void registerCompartment(SuperByteBufferCache.Compartment<?> instance, long ticksUntilExpired) {
        rotating.put(instance, CacheBuilder.newBuilder().expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS).build());
        belts.put(instance, CacheBuilder.newBuilder().expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS).build());
    }

    public RotatingBuffer renderPartialRotating(AllBlockPartials partial, BlockState referenceState) {
        return getRotating(PARTIAL, partial, () -> rotatingInstancedRenderer(partial.get(), referenceState));
    }

    public BeltBuffer renderPartialBelt(AllBlockPartials partial, BlockState referenceState) {
        return getBelt(PARTIAL, partial, () -> beltInstancedRenderer(partial.get(), referenceState));
    }

    public RotatingBuffer renderDirectionalPartialInstanced(AllBlockPartials partial, BlockState referenceState, Direction dir,
                                                             MatrixStack modelTransform) {
        return getRotating(SuperByteBufferCache.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                           () -> rotatingInstancedRenderer(partial.get(), referenceState, modelTransform));
    }

    public RotatingBuffer renderBlockInstanced(SuperByteBufferCache.Compartment<BlockState> compartment, BlockState toRender) {
        return getRotating(compartment, toRender, () -> rotatingInstancedRenderer(toRender));
    }

    public <T> RotatingBuffer getRotating(SuperByteBufferCache.Compartment<T> compartment, T key, Supplier<RotatingBuffer> supplier) {
        Cache<Object, RotatingBuffer> compartmentCache = this.rotating.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> BeltBuffer getBelt(SuperByteBufferCache.Compartment<T> compartment, T key, Supplier<BeltBuffer> supplier) {
        Cache<Object, BeltBuffer> compartmentCache = this.belts.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }


    private RotatingBuffer rotatingInstancedRenderer(BlockState renderedState) {
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        return rotatingInstancedRenderer(dispatcher.getModelForState(renderedState), renderedState);
    }

    private RotatingBuffer rotatingInstancedRenderer(IBakedModel model, BlockState renderedState) {
        return rotatingInstancedRenderer(model, renderedState, new MatrixStack());
    }

    private BeltBuffer beltInstancedRenderer(IBakedModel model, BlockState renderedState) {
        return beltInstancedRenderer(model, renderedState, new MatrixStack());
    }

    private RotatingBuffer rotatingInstancedRenderer(IBakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return new RotatingBuffer(builder);
    }

    private BeltBuffer beltInstancedRenderer(IBakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return new BeltBuffer(builder);
    }

    public void invalidate() {
        rotating.values().forEach(cache -> {
            cache.asMap().values().forEach(InstancedBuffer::invalidate);
            cache.invalidateAll();
        });

        belts.values().forEach(cache -> {
            cache.asMap().values().forEach(InstancedBuffer::invalidate);
            cache.invalidateAll();
        });
    }
}
