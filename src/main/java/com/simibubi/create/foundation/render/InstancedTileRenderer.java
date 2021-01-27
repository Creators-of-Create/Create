package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.shader.Shader;
import com.simibubi.create.foundation.render.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import com.simibubi.create.foundation.render.instancing.*;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class InstancedTileRenderer {
    protected Map<TileEntity, TileEntityInstance<?>> renderers = new HashMap<>();

    protected Map<MaterialType<?>, RenderMaterial<?>> materials = new HashMap<>();

    public InstancedTileRenderer() {
        registerMaterials();
    }

    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.ROTATING, RotatingModel::new));
    }

    @SuppressWarnings("unchecked")
    public <M extends InstancedModel<?>> RenderMaterial<M> get(MaterialType<M> materialType) {
        return (RenderMaterial<M>) materials.get(materialType);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> getRenderer(T tile) {
        if (renderers.containsKey(tile)) {
            return (TileEntityInstance<? super T>) renderers.get(tile);
        } else {
            TileEntityInstance<? super T> renderer = InstancedTileRenderRegistry.instance.create(this, tile);

            if (renderer != null) {
                FastRenderDispatcher.addedLastTick.get(tile.getWorld()).add(renderer);
                renderers.put(tile, renderer);
            }

            return renderer;
        }
    }

    public <T extends TileEntity> void onLightUpdate(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> renderer = getRenderer(tile);

            if (renderer != null)
                renderer.updateLight();
        }
    }

    public <T extends TileEntity> void update(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> renderer = getRenderer(tile);

            if (renderer != null)
                renderer.update();
        }
    }

    public <T extends TileEntity> void remove(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> renderer = getRenderer(tile);

            if (renderer != null) {
                renderer.remove();
                renderers.remove(tile);
            }
        }
    }

    public void invalidate() {
        for (RenderMaterial<?> material : materials.values()) {
            material.delete();
        }
        renderers.clear();
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view) {
        render(layer, projection, view, null);
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view, ShaderCallback callback) {
        for (RenderMaterial<?> material : materials.values()) {
            if (material.canRenderInLayer(layer))
                material.render(layer, projection, view, callback);
        }

        ShaderHelper.releaseShader();
    }
}
