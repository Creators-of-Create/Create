package com.simibubi.create.foundation.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.instancing.*;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
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
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.render.SuperByteBufferCache.PARTIAL;

public class FastKineticRenderer {
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, InstanceBuffer<RotatingData>>> rotating;
    Map<SuperByteBufferCache.Compartment<?>, Cache<Object, InstanceBuffer<BeltData>>> belts;

    public boolean dirty = false;

    public FastKineticRenderer() {
        rotating = new HashMap<>();
        belts = new HashMap<>();
        registerCompartment(SuperByteBufferCache.PARTIAL);
        registerCompartment(SuperByteBufferCache.DIRECTIONAL_PARTIAL);
        registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
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

    <T extends TileEntity> void addInstancedData(T te, IInstancedTileEntityRenderer<T> renderer) {
        renderer.addInstanceData(new InstanceContext.World<>(te));
    }

    <T extends TileEntity> void addInstancedData(FastContraptionRenderer c, T te, IInstancedTileEntityRenderer<T> renderer) {
        renderer.addInstanceData(new InstanceContext.Contraption<>(te, c));
    }

    /**
     * This function should be called after building instances.
     * It must be called either on the render thread before committing to rendering, or in a place where there are
     * guaranteed to be no race conditions with the render thread, i.e. when constructing a FastContraptionRenderer.
     */
    public void markAllDirty() {
        for (Cache<Object, InstanceBuffer<RotatingData>> cache : rotating.values()) {
            for (InstanceBuffer<RotatingData> renderer : cache.asMap().values()) {
                renderer.markDirty();
            }
        }

        for (Cache<Object, InstanceBuffer<BeltData>> cache : belts.values()) {
            for (InstanceBuffer<BeltData> renderer : cache.asMap().values()) {
                renderer.markDirty();
            }
        }
    }

    void renderBelts() {
        for (Cache<Object, InstanceBuffer<BeltData>> cache : belts.values()) {
            for (InstanceBuffer<BeltData> type : cache.asMap().values()) {
                if (!type.isEmpty()) {
                    type.render();
                }
            }
        }
    }

    void renderRotating() {
        for (Cache<Object, InstanceBuffer<RotatingData>> cache : rotating.values()) {
            for (InstanceBuffer<RotatingData> rotatingDataInstanceBuffer : cache.asMap().values()) {
                if (!rotatingDataInstanceBuffer.isEmpty()) {
                    rotatingDataInstanceBuffer.render();
                }
            }
        }
    }

    public void renderInstancesAsWorld(RenderType layer, Matrix4f projection, Matrix4f view) {
        if (dirty) {
            buildTileEntityBuffers(Minecraft.getInstance().world);
            markAllDirty();

            dirty = false;
        }

        layer.startDrawing();

        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(projection, view);

        ShaderHelper.useShader(Shader.ROTATING, callback);
        renderRotating();

        ShaderHelper.useShader(Shader.BELT, callback);
        renderBelts();

        ShaderHelper.releaseShader();

        layer.endDrawing();
    }

    public static void setup(GameRenderer gameRenderer) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
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

    public static void teardown() {

        GL13.glActiveTexture(GL40.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL13.glActiveTexture(GL40.GL_TEXTURE0);
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GL40.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableLighting();
    }

    public void registerCompartment(SuperByteBufferCache.Compartment<?> instance) {
        rotating.put(instance, CacheBuilder.newBuilder().build());
        belts.put(instance, CacheBuilder.newBuilder().build());
    }

    public InstanceBuffer<RotatingData> renderPartialRotating(AllBlockPartials partial, BlockState referenceState) {
        return getRotating(PARTIAL, partial, () -> rotatingInstancedRenderer(partial.get(), referenceState));
    }

    public InstanceBuffer<BeltData> renderPartialBelt(AllBlockPartials partial, BlockState referenceState) {
        return getBelt(PARTIAL, partial, () -> beltInstancedRenderer(partial.get(), referenceState));
    }

    public InstanceBuffer<RotatingData> renderDirectionalPartialInstanced(AllBlockPartials partial, BlockState referenceState, Direction dir,
                                                             MatrixStack modelTransform) {
        return getRotating(SuperByteBufferCache.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                           () -> rotatingInstancedRenderer(partial.get(), referenceState, modelTransform));
    }

    public InstanceBuffer<RotatingData> renderBlockInstanced(SuperByteBufferCache.Compartment<BlockState> compartment, BlockState toRender) {
        return getRotating(compartment, toRender, () -> rotatingInstancedRenderer(toRender));
    }

    public <T> InstanceBuffer<RotatingData> getRotating(SuperByteBufferCache.Compartment<T> compartment, T key, Supplier<InstanceBuffer<RotatingData>> supplier) {
        Cache<Object, InstanceBuffer<RotatingData>> compartmentCache = this.rotating.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> InstanceBuffer<BeltData> getBelt(SuperByteBufferCache.Compartment<T> compartment, T key, Supplier<InstanceBuffer<BeltData>> supplier) {
        Cache<Object, InstanceBuffer<BeltData>> compartmentCache = this.belts.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private InstanceBuffer<RotatingData> rotatingInstancedRenderer(BlockState renderedState) {
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        return rotatingInstancedRenderer(dispatcher.getModelForState(renderedState), renderedState);
    }

    private InstanceBuffer<RotatingData> rotatingInstancedRenderer(IBakedModel model, BlockState renderedState) {
        return rotatingInstancedRenderer(model, renderedState, new MatrixStack());
    }

    private InstanceBuffer<BeltData> beltInstancedRenderer(IBakedModel model, BlockState renderedState) {
        return beltInstancedRenderer(model, renderedState, new MatrixStack());
    }

    private InstanceBuffer<RotatingData> rotatingInstancedRenderer(IBakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return new RotatingBuffer(builder);
    }

    private InstanceBuffer<BeltData> beltInstancedRenderer(IBakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return new BeltBuffer(builder);
    }

    public void invalidate() {
        for (Cache<Object, InstanceBuffer<RotatingData>> objectInstanceBufferCache : rotating.values()) {
            objectInstanceBufferCache.asMap().values().forEach(InstanceBuffer::delete);
            objectInstanceBufferCache.invalidateAll();
        }

        for (Cache<Object, InstanceBuffer<BeltData>> cache : belts.values()) {
            cache.asMap().values().forEach(InstanceBuffer::delete);
            cache.invalidateAll();
        }
    }

    public static RenderType getKineticRenderLayer() {
        return RenderType.getCutoutMipped();
    }
}
