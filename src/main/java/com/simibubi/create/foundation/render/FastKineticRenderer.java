package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import com.simibubi.create.foundation.render.instancing.*;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastKineticRenderer {
    protected Map<MaterialType<?>, RenderMaterial<?>> materials = new HashMap<>();

    public boolean dirty = false;

    public FastKineticRenderer() {
        registerMaterials();
    }

    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.BELT, BeltBuffer::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.ROTATING, RotatingBuffer::new));
    }

    @SuppressWarnings("unchecked")
    public <M extends InstanceBuffer<?>> RenderMaterial<M> get(MaterialType<M> materialType) {
        return (RenderMaterial<M>) materials.get(materialType);
    }

    @SuppressWarnings("unchecked")
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

    public <T extends TileEntity> void addInstancedData(T te, IInstancedTileEntityRenderer<T> renderer) {
        renderer.addInstanceData(new InstanceContext.World<>(te));
    }

    public <T extends TileEntity> void addInstancedData(RenderedContraption c, T te, IInstancedTileEntityRenderer<T> renderer) {
        renderer.addInstanceData(new InstanceContext.Contraption<>(te, c));
    }

    /**
     * This function should be called after building instances.
     * It must be called either on the render thread before committing to rendering, or in a place where there are
     * guaranteed to be no race conditions with the render thread, i.e. when constructing a FastContraptionRenderer.
     */
    public void markAllDirty() {
        for (RenderMaterial<?> material : materials.values()) {
            material.runOnAll(InstanceBuffer::markDirty);
        }
    }

    public void invalidate() {
        for (RenderMaterial<?> material : materials.values()) {
            material.delete();
        }
        dirty = true;
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view) {
        render(layer, projection, view, null);
    }

    protected void prepareFrame() {
        if (dirty) {
            buildTileEntityBuffers(Minecraft.getInstance().world);
            markAllDirty();

            dirty = false;
        }
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view, ShaderCallback callback) {
        prepareFrame();

        layer.startDrawing();

        for (RenderMaterial<?> material : materials.values()) {
            material.render(projection, view, callback);
        }

        ShaderHelper.releaseShader();

        layer.endDrawing();
    }

    public static RenderType getKineticRenderLayer() {
        return RenderType.getCutoutMipped();
    }
}
