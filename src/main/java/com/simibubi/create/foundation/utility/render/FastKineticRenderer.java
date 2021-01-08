package com.simibubi.create.foundation.utility.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.render.instancing.*;
import com.simibubi.create.foundation.utility.render.shader.Shader;
import com.simibubi.create.foundation.utility.render.shader.ShaderCallback;
import com.simibubi.create.foundation.utility.render.shader.ShaderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.utility.render.SuperByteBufferCache.PARTIAL;

public class FastKineticRenderer {
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, RotatingBuffer>> rotating;
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, BeltBuffer>> belts;

    Queue<Runnable> runs;

    boolean rebuild;

    public FastKineticRenderer() {
        rotating = new HashMap<>();
        belts = new HashMap<>();
        runs = new ConcurrentLinkedQueue<>();
        registerCompartment(SuperByteBufferCache.GENERIC_TILE);
        registerCompartment(SuperByteBufferCache.PARTIAL);
        registerCompartment(SuperByteBufferCache.DIRECTIONAL_PARTIAL);
    }

    public void buildTileEntityBuffers(World world) {

        List<TileEntity> tileEntities = world.loadedTileEntityList;

        if (!tileEntities.isEmpty()) {
            for (TileEntity te : tileEntities) {
                if (te instanceof IInstanceRendered) {
                    TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);

                    if (renderer instanceof IInstancedTileEntityRenderer) {
                        addInstancedData(te, (IInstancedTileEntityRenderer<? super TileEntity>) renderer);
                    }
                }
            }
        }
    }

    private <T extends TileEntity> void addInstancedData(T te, IInstancedTileEntityRenderer<T> renderer) {
        renderer.addInstanceData(te);
    }

    public void tick() {
        // TODO: (later) detect changes in lighting with a mixin to ClientChunkProvider.markLightChanged()
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

//        rebuild = true;
    }

    public void enqueue(Runnable run) {
        runs.add(run);
    }

    public void renderInstances(RenderWorldLastEvent event) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
//
//        if (rebuild) {
//            buildTileEntityBuffers(Minecraft.getInstance().world);
//            rebuild = false;
//        }

        setup(gameRenderer);

        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(event);

        ShaderHelper.useShader(Shader.ROTATING_INSTANCED, callback);

        rotating.values()
                .stream()
                .flatMap(cache -> cache.asMap().values().stream())
                .filter(type -> !type.isEmpty())
                .forEach(InstanceBuffer::render);

        ShaderHelper.useShader(Shader.BELT_INSTANCED, callback);

        belts.values()
             .stream()
             .flatMap(cache -> cache.asMap().values().stream())
             .filter(type -> !type.isEmpty())
             .forEach(InstanceBuffer::render);

        ShaderHelper.releaseShader();

        teardown();

        while (!runs.isEmpty()) {
            runs.remove().run();
        }
    }

    public void setup(GameRenderer gameRenderer) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        GL11.glCullFace(GL11.GL_BACK);

        LightTexture lightManager = gameRenderer.getLightmapTextureManager();

        Texture blockAtlasTexture = Minecraft.getInstance().textureManager.getTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
        Texture lightTexture = Minecraft.getInstance().textureManager.getTexture(lightManager.resourceLocation);

        // bind the block atlas texture to 0
        GL13.glActiveTexture(GL40.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockAtlasTexture.getGlTextureId());
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // bind the light texture to 1 and setup the mysterious filtering options
        GL13.glActiveTexture(GL40.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightTexture.getGlTextureId());
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10241, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10240, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10242, 10496);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10243, 10496);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableTexture();
    }

    public void teardown() {

        GL13.glActiveTexture(GL40.GL_TEXTURE0 + 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL13.glActiveTexture(GL40.GL_TEXTURE0);
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
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
            cache.asMap().values().forEach(InstanceBuffer::invalidate);
            cache.invalidateAll();
        });

        belts.values().forEach(cache -> {
            cache.asMap().values().forEach(InstanceBuffer::invalidate);
            cache.invalidateAll();
        });
    }
}
